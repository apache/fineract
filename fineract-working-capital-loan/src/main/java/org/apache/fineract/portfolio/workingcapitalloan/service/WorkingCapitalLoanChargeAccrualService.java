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

import java.math.BigDecimal;
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

    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanTransactionRelationRepository relationRepository;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;
    private final WorkingCapitalLoanTransactionFinder transactionFinder;
    private final BusinessEventNotifierService businessEventNotifierService;

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
        if (!loan.isClosedObligationsMet() && !loan.isClosedWrittenOff() && !loan.isOverpaid()) {
            return;
        }
        processClosureAccruals(loan, closingDate);
    }

    /**
     * A closed loan is no longer picked up by the end-of-day job, so any accrual it still owes is accelerated to the
     * closing date. Not gated on the {@code charge-accrual-date} mode: under due-date a charge the loan closed before
     * reaching was accrued by nothing, and a charge that already reached its target is left alone anyway.
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
        // Not gated on the outstanding amount: that would skip fully-settled charges and leave their income
        // unrecognized, with the receivable/income pair un-netted. Only the waived part is left out of the target:
        // nothing further is recognized on a debt the borrower no longer owes. Income recognized before the waiver
        // is not unwound here.
        //
        // Chasing a target rather than gating on "already accrued": undoing a waiver raises the target again, so the
        // next run tops the recognized income back up instead of leaving it unrecognized forever.
        final BigDecimal target = MathUtil.subtractToZero(charge.getAmount(), charge.getAmountWaived());
        final BigDecimal toAccrue = MathUtil.subtractToZero(target, getAccruedAmount(charge));
        if (accrualDate == null || !MathUtil.isGreaterThanZero(toAccrue)) {
            return;
        }
        final WorkingCapitalLoanTransaction accrualTransaction = WorkingCapitalLoanTransaction.accrual(loan, ExternalId.empty(), toAccrue,
                accrualDate);
        final WorkingCapitalLoanTransactionRelation relation = WorkingCapitalLoanTransactionRelation.linkToCharge(accrualTransaction,
                charge, LoanTransactionRelationTypeEnum.RELATED);
        accrualTransaction.getLoanTransactionRelations().add(relation);

        transactionRepository.saveAndFlush(accrualTransaction);

        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation
                .forChargeAccrual(accrualTransaction, toAccrue, charge.isPenaltyCharge());
        allocationRepository.saveAndFlush(allocation);
        accountingProcessor.postJournalEntries(loan, accrualTransaction, allocation,
                transactionFinder.isAfterActiveChargeOffForAccountingRouting(loan, accrualTransaction));

        businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanAccrualTransactionBusinessEvent(accrualTransaction, loan.getId()));
    }

    public BigDecimal getAccruedAmount(final WorkingCapitalLoanCharge charge) {
        return MathUtil.nullToZero(relationRepository.fetchTransactionAmountForCharge(charge, LoanTransactionType.ACCRUAL));
    }

    private String retrieveChargeAccrualDateConfig() {
        final String configuredValue = globalConfigurationRepository
                .findOneByNameWithNotFoundDetection(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE).getStringValue();
        return configuredValue == null || configuredValue.isBlank() ? DEFAULT_ACCRUAL_DATE_CONFIG : configuredValue;
    }
}
