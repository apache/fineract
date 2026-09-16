/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanAccrualTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanChargeAccrualService {

    private static final String SUBMITTED_DATE = "submitted-date";
    private static final String DUE_DATE = "due-date";
    private static final String DEFAULT_ACCRUAL_DATE_CONFIG = DUE_DATE;

    private static final String REAL_TIME = "real-time";
    private static final String EOD = "eod";
    private static final String DEFAULT_WC_CHARGE_ACCRUAL_TIME_CONFIG = EOD;

    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanTransactionRelationRepository relationRepository;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;
    private final WorkingCapitalLoanTransactionFinder transactionFinder;
    private final BusinessEventNotifierService businessEventNotifierService;

    /**
     * When {@code wcl-charge-accrual-time} is real-time, posts the charge accrual immediately on add. The COB sweep
     * always remains responsible for catching charges that were not accrued yet (for example after switching from EOD
     * to real-time), gated by {@code charge-accrual-date}; {@link #isAlreadyAccrued} keeps both paths idempotent.
     */
    public void processOnChargeAdded(final WorkingCapitalLoan loan, final WorkingCapitalLoanCharge charge) {
        if (isAccrualPostingDisabled(loan)) {
            return;
        }
        if (isRealTimeChargeAccrual()) {
            createChargeAccrualIfMissing(loan, charge, charge.getSubmittedOnDate());
        }
    }

    /**
     * Posts pending charge accruals during COB according to {@code charge-accrual-date}, regardless of
     * {@code wcl-charge-accrual-time}. Real-time only adds an earlier post-on-add path; it must not disable this sweep,
     * otherwise charges added under EOD and left unaccrued when the config flips to real-time would only be recognized
     * at loan closure.
     */
    public void processChargeAccrualsOnCOB(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        if (isAccrualPostingDisabled(loan)) {
            return;
        }
        final String accrualDateMode = retrieveChargeAccrualDateConfig();
        final List<WorkingCapitalLoanCharge> charges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId());
        if (SUBMITTED_DATE.equalsIgnoreCase(accrualDateMode)) {
            charges.stream().filter(charge -> charge.getSubmittedOnDate() != null && !charge.getSubmittedOnDate().isAfter(businessDate))
                    .forEach(charge -> createChargeAccrualIfMissing(loan, charge, charge.getSubmittedOnDate()));
        } else if (DUE_DATE.equalsIgnoreCase(accrualDateMode)) {
            charges.stream().filter(charge -> charge.getDueDate() != null && !charge.getDueDate().isAfter(businessDate))
                    .forEach(charge -> createChargeAccrualIfMissing(loan, charge, charge.getDueDate()));
        }
    }

    /**
     * Accrues any pending charge income once the loan has reached a closed or overpaid state, no matter which operation
     * closed it (repayment, goodwill credit, charge adjustment, credit balance refund, discount-fee adjustment). It is
     * a no-op while the loan is still active, so callers can invoke it unconditionally after any balance-changing
     * operation. Mirrors how the term/progressive loan reacts to loan-closure events.
     */
    public void accrueOnClosure(final WorkingCapitalLoan loan, final LocalDate closingDate) {
        if (!loan.isClosedObligationsMet() && !loan.isClosedWrittenOff() && !loan.isOverpaid()) {
            return;
        }
        processClosureAccruals(loan, closingDate);
    }

    /**
     * Posts, on early closure, any pending charge accrual that has not been recognized yet. Once the loan is closed it
     * is no longer picked up by the end-of-day job, so the accrual is accelerated to the closing date to make sure the
     * income is recognized. This runs regardless of the {@code charge-accrual-date} / {@code wcl-charge-accrual-time}
     * modes: the idempotency guard skips charges already accrued (the common case in real-time mode, and in
     * submitted-date EOD mode after COB), while charges that slipped through every accrual step are caught here.
     */
    public void processClosureAccruals(final WorkingCapitalLoan loan, final LocalDate closingDate) {
        if (isAccrualPostingDisabled(loan)) {
            return;
        }
        final List<WorkingCapitalLoanCharge> activeCharges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId());
        activeCharges.forEach(charge -> createChargeAccrualIfMissing(loan, charge, closingDate));
    }

    private boolean isAccrualPostingDisabled(final WorkingCapitalLoan loan) {
        return !loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization();
    }

    private void createChargeAccrualIfMissing(final WorkingCapitalLoan loan, final WorkingCapitalLoanCharge charge,
            final LocalDate accrualDate) {
        // The accrual recognizes the full charge income regardless of whether the charge was already paid or adjusted;
        // gating on the outstanding amount would skip fully-settled charges and leave income unrecognized (and the
        // receivable/income pair un-netted). Waived charges are excluded upstream via the active-charge filter.
        if (accrualDate == null || isAlreadyAccrued(charge) || !MathUtil.isGreaterThanZero(charge.getAmount())) {
            return;
        }
        final WorkingCapitalLoanTransaction accrualTransaction = WorkingCapitalLoanTransaction.accrual(loan, ExternalId.empty(),
                charge.getAmount(), accrualDate);
        final WorkingCapitalLoanTransactionRelation relation = WorkingCapitalLoanTransactionRelation.linkToCharge(accrualTransaction,
                charge, LoanTransactionRelationTypeEnum.RELATED);
        accrualTransaction.getLoanTransactionRelations().add(relation);

        transactionRepository.saveAndFlush(accrualTransaction);

        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation
                .forChargeAccrual(accrualTransaction, charge.getAmount(), charge.isPenaltyCharge());
        allocationRepository.saveAndFlush(allocation);
        accountingProcessor.postJournalEntries(loan, accrualTransaction, allocation,
                transactionFinder.isAfterActiveChargeOffForAccountingRouting(loan, accrualTransaction));

        businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanAccrualTransactionBusinessEvent(accrualTransaction, loan.getId()));
    }

    private boolean isAlreadyAccrued(final WorkingCapitalLoanCharge charge) {
        return !relationRepository
                .findAllByToChargeAndFromTransactionReversedAndFromTransactionTransactionType(charge, false, LoanTransactionType.ACCRUAL)
                .isEmpty();
    }

    private boolean isRealTimeChargeAccrual() {
        return REAL_TIME.equals(resolveWCChargeAccrualTimeConfig());
    }

    /**
     * Resolves {@code wcl-charge-accrual-time} to a known value. Unknown / mistyped values fall back to EOD so charge
     * income is still recognized through COB instead of silently stopping until loan closure.
     */
    private String resolveWCChargeAccrualTimeConfig() {
        final String configuredValue = globalConfigurationRepository
                .findOneByNameWithNotFoundDetection(GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME).getStringValue();
        if (configuredValue == null || configuredValue.isBlank()) {
            return DEFAULT_WC_CHARGE_ACCRUAL_TIME_CONFIG;
        }
        final String normalized = configuredValue.trim();
        if (REAL_TIME.equalsIgnoreCase(normalized)) {
            return REAL_TIME;
        } else {
            return EOD;
        }
    }

    private String retrieveChargeAccrualDateConfig() {
        final String configuredValue = globalConfigurationRepository
                .findOneByNameWithNotFoundDetection(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE).getStringValue();
        return configuredValue == null || configuredValue.isBlank() ? DEFAULT_ACCRUAL_DATE_CONFIG : configuredValue;
    }
}
