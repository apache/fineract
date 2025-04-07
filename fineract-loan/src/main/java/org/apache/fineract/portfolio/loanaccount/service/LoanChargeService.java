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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.SingleLoanChargeRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;

@RequiredArgsConstructor
public class LoanChargeService {

    private final LoanChargeValidator loanChargeValidator;
    private final LoanTransactionProcessingService loanTransactionProcessingService;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;

    public void recalculateAllCharges(final Loan loan) {
        Set<LoanCharge> charges = loan.getActiveCharges();
        int penaltyWaitPeriod = 0;
        for (final LoanCharge loanCharge : charges) {
            recalculateLoanCharge(loan, loanCharge, penaltyWaitPeriod);
        }
        loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());
    }

    public void recalculateLoanCharge(final Loan loan, final LoanCharge loanCharge, final int penaltyWaitPeriod) {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal chargeAmt;
        BigDecimal totalChargeAmt = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            if (loanCharge.isOverdueInstallmentCharge()) {
                amount = loan.calculateOverdueAmountPercentageAppliedTo(loanCharge, penaltyWaitPeriod);
            } else {
                amount = loan.calculateAmountPercentageAppliedTo(loanCharge);
            }
            chargeAmt = loanCharge.getPercentage();
            if (loanCharge.isInstalmentFee()) {
                totalChargeAmt = loan.calculatePerInstallmentChargeAmount(loanCharge);
            }
        } else {
            chargeAmt = loanCharge.amountOrPercentage();
        }
        if (loanCharge.isActive()) {
            loan.clearLoanInstallmentChargesBeforeRegeneration(loanCharge);
            loanCharge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, loan.fetchNumberOfInstallmensAfterExceptions(),
                    totalChargeAmt);
            loanChargeValidator.validateChargeHasValidSpecifiedDateIfApplicable(loan, loanCharge, loan.getDisbursementDate());
        }
    }

    public void makeChargePayment(final Loan loan, final Long chargeId, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, final LoanTransaction paymentTransaction, final Integer installmentNumber) {
        loanChargeValidator.validateChargePaymentNotInFuture(paymentTransaction);
        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
        LoanCharge charge = null;
        for (final LoanCharge loanCharge : loan.getCharges()) {
            if (loanCharge.isActive() && chargeId.equals(loanCharge.getId())) {
                charge = loanCharge;
            }
        }
        handleChargePaidTransaction(loan, charge, paymentTransaction, installmentNumber);
    }

    public void handleChargePaidTransaction(final Loan loan, final LoanCharge charge, final LoanTransaction chargesPayment,
            final Integer installmentNumber) {
        chargesPayment.updateLoan(loan);
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge,
                chargesPayment.getAmount(loan.getCurrency()).getAmount(), installmentNumber);
        chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
        loan.addLoanTransaction(chargesPayment);
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_PAYMENT, loan);

        final List<LoanRepaymentScheduleInstallment> chargePaymentInstallments = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                .fetchFirstNormalInstallmentNumber(loan.getRepaymentScheduleInstallments());
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            boolean isFirstInstallment = installment.getInstallmentNumber().equals(firstNormalInstallmentNumber);
            if (installment.getInstallmentNumber().equals(installmentNumber) || (installmentNumber == null
                    && charge.isDueInPeriod(installment.getFromDate(), installment.getDueDate(), isFirstInstallment))) {
                chargePaymentInstallments.add(installment);
                break;
            }
        }
        final Set<LoanCharge> loanCharges = new HashSet<>(1);
        loanCharges.add(charge);
        loanTransactionProcessingService.processLatestTransaction(loan.getTransactionProcessingStrategyCode(), chargesPayment,
                new TransactionCtx(loan.getCurrency(), chargePaymentInstallments, loanCharges,
                        new MoneyHolder(loan.getTotalOverpaidAsMoney()), null));

        loanLifecycleStateMachine.determineAndTransition(loan, chargesPayment.getTransactionDate());
    }

    public void addLoanCharge(final Loan loan, final LoanCharge loanCharge) {
        loanCharge.update(loan);

        final BigDecimal amount = loan.calculateAmountPercentageAppliedTo(loanCharge);
        BigDecimal chargeAmt;
        BigDecimal totalChargeAmt = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            chargeAmt = loanCharge.getPercentage();
            if (loanCharge.isInstalmentFee()) {
                totalChargeAmt = loan.calculatePerInstallmentChargeAmount(loanCharge);
            } else if (loanCharge.isOverdueInstallmentCharge()) {
                totalChargeAmt = loanCharge.amountOutstanding();
            }
        } else {
            chargeAmt = loanCharge.amountOrPercentage();
        }
        loanCharge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, loan.fetchNumberOfInstallmensAfterExceptions(), totalChargeAmt);

        // NOTE: must add new loan charge to set of loan charges before
        // reprocessing the repayment schedule.
        if (loan.getLoanCharges() == null) {
            loan.setCharges(new HashSet<>());
        }
        loan.getLoanCharges().add(loanCharge);
        loan.setSummary(loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement()));

        // store Id's of existing loan transactions and existing reversed loan transactions
        final SingleLoanChargeRepaymentScheduleProcessingWrapper wrapper = new SingleLoanChargeRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(loan.getCurrency(), loan.getDisbursementDate(), loan.getRepaymentScheduleInstallments(), loanCharge);
        loan.updateLoanSummaryDerivedFields();

        loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_ADDED, loan);
    }

}
