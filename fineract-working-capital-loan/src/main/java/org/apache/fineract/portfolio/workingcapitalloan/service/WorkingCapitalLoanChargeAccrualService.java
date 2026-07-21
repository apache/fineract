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
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
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

    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanTransactionRelationRepository relationRepository;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;

    public void processOnChargeAdded(final WorkingCapitalLoan loan, final WorkingCapitalLoanCharge charge) {
        if (isAccrualPostingDisabled(loan)) {
            return;
        }
        if (!SUBMITTED_DATE.equalsIgnoreCase(retrieveChargeAccrualDateConfig())) {
            return;
        }
        createChargeAccrualIfMissing(loan, charge, charge.getSubmittedOnDate());
    }

    public void processDueDateAccruals(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        if (isAccrualPostingDisabled(loan)) {
            return;
        }
        if (!DUE_DATE.equalsIgnoreCase(retrieveChargeAccrualDateConfig())) {
            return;
        }
        final List<WorkingCapitalLoanCharge> activeCharges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId());
        activeCharges.stream().filter(charge -> charge.getDueDate() != null && !charge.getDueDate().isAfter(businessDate))
                .forEach(charge -> createChargeAccrualIfMissing(loan, charge, charge.getDueDate()));
    }

    /**
     * Accrues any pending charge income once the loan has reached a closed or overpaid state, no matter which operation
     * closed it (repayment, goodwill credit, charge adjustment, credit balance refund, discount-fee adjustment). It is
     * a no-op while the loan is still active, so callers can invoke it unconditionally after any balance-changing
     * operation. Mirrors how the term/progressive loan reacts to loan-closure events.
     */
    public void accrueOnClosure(final WorkingCapitalLoan loan, final LocalDate closingDate) {
        if (!loan.getLoanStatus().isClosed() && !loan.getLoanStatus().isOverpaid()) {
            return;
        }
        processClosureAccruals(loan, closingDate);
    }

    /**
     * Posts, on early closure, any pending charge accrual that has not been recognized yet. Once the loan is closed it
     * is no longer picked up by the end-of-day job, so the accrual is accelerated to the closing date to make sure the
     * income is recognized. This runs regardless of the {@code charge-accrual-date} mode: the idempotency guard skips
     * charges already accrued (the common case in submitted-date mode, where charges are accrued when added), while
     * charges that slipped through every accrual step are caught here. That gap is real when the mode changes between
     * the charge being added and the loan closing: a charge added under due-date with a future due date is never
     * accrued on add, never reached by the due-date COB step, and would be missed at closure if this were gated on the
     * mode.
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
        accountingProcessor.postJournalEntries(loan, accrualTransaction, allocation, false);
    }

    private boolean isAlreadyAccrued(final WorkingCapitalLoanCharge charge) {
        return !relationRepository
                .findAllByToChargeAndFromTransactionReversedAndFromTransactionTransactionType(charge, false, LoanTransactionType.ACCRUAL)
                .isEmpty();
    }

    private String retrieveChargeAccrualDateConfig() {
        final String configuredValue = globalConfigurationRepository
                .findOneByNameWithNotFoundDetection(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE).getStringValue();
        return configuredValue == null || configuredValue.isBlank() ? DEFAULT_ACCRUAL_DATE_CONFIG : configuredValue;
    }
}
