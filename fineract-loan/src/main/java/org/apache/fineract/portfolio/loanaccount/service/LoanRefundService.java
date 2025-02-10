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

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanRefundValidator;

@RequiredArgsConstructor
public class LoanRefundService {

    private final LoanRefundValidator loanRefundValidator;

    public void makeRefund(final Loan loan, final LoanTransaction loanTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());

        loanRefundValidator.validateTransferRefund(loan, loanTransaction);

        loanTransaction.updateLoan(loan);

        if (loanTransaction.isNotZero()) {
            loan.addLoanTransaction(loanTransaction);
        }
        loan.updateLoanSummaryDerivedFields();
        loan.doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);
    }

    public LocalDate extractTransactionDate(final Loan loan, final LoanTransaction loanTransaction) {
        final LocalDate loanTransactionDate = loanTransaction.getTransactionDate();
        loanRefundValidator.validateTransactionDateAfterDisbursement(loan, loanTransactionDate);
        return loanTransactionDate;
    }

    public void makeRefundForActiveLoan(final Loan loan, final LoanTransaction loanTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());

        handleRefundTransaction(loan, loanTransaction, loanLifecycleStateMachine);
    }

    public void creditBalanceRefund(final Loan loan, final LoanTransaction newCreditBalanceRefundTransaction,
            final LoanLifecycleStateMachine defaultLoanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        loanRefundValidator.validateCreditBalanceRefund(loan, newCreditBalanceRefundTransaction);

        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());

        loan.getLoanTransactions().add(newCreditBalanceRefundTransaction);

        loan.updateLoanSummaryDerivedFields();

        if (MathUtil.isEmpty(loan.getTotalOverpaid())) {
            loan.setOverpaidOnDate(null);
            loan.setClosedOnDate(newCreditBalanceRefundTransaction.getTransactionDate());
            defaultLoanLifecycleStateMachine.transition(LoanEvent.LOAN_CREDIT_BALANCE_REFUND, loan);
        }
    }

    private void handleRefundTransaction(final Loan loan, final LoanTransaction loanTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine) {
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_REFUND, loan);

        loanTransaction.updateLoan(loan);

        loanRefundValidator.validateRefundEligibility(loan, loanTransaction);

        if (loanTransaction.isNotZero()) {
            loan.addLoanTransaction(loanTransaction);
        }

        loanRefundValidator.validateRefundTransactionType(loanTransaction);

        final LocalDate loanTransactionDate = extractTransactionDate(loan, loanTransaction);

        loanRefundValidator.validateTransactionDateNotInFuture(loanTransactionDate);
        loanRefundValidator.validateTransactionAmountThreshold(loan, null);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loan.getTransactionProcessor();

        loanRepaymentScheduleTransactionProcessor.processLatestTransaction(loanTransaction, new TransactionCtx(loan.getCurrency(),
                loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(), new MoneyHolder(loan.getTotalOverpaidAsMoney()), null));

        loan.updateLoanSummaryDerivedFields();
        loan.doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);
    }
}
