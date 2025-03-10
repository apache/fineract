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
import org.apache.fineract.portfolio.interestpauses.service.LoanAccountTransfersService;
import org.apache.fineract.portfolio.loanaccount.data.TransactionChangeData;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReprocessLoanTransactionsServiceImpl implements ReprocessLoanTransactionsService {

    private final LoanAccountService loanAccountService;
    private final LoanAccountTransfersService loanAccountTransfersService;
    private final ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService;

    @Override
    public void reprocessTransactions(final Loan loan) {
        final ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();
        if (changedTransactionDetail != null) {
            handleChangedDetail(changedTransactionDetail);
        }
    }

    @Override
    public void reprocessTransactions(final Loan loan, final List<LoanTransaction> loanTransactions) {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loan.getTransactionProcessor();
        final ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(
                loan.getDisbursementDate(), loanTransactions, loan.getCurrency(), loan.getRepaymentScheduleInstallments(),
                loan.getActiveCharges());
        for (TransactionChangeData change : changedTransactionDetail.getTransactionChanges()) {
            change.getNewTransaction().updateLoan(loan);
        }
        final List<LoanTransaction> newTransactions = changedTransactionDetail.getTransactionChanges().stream()
                .map(TransactionChangeData::getNewTransaction).toList();
        loan.getLoanTransactions().addAll(newTransactions);
        loan.updateLoanSummaryDerivedFields();
        handleChangedDetail(changedTransactionDetail);
    }

    @Override
    public void reprocessTransactionsWithPostTransactionChecks(final Loan loan, final LocalDate transactionDate) {
        final ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactionsWithPostTransactionChecks(transactionDate);
        if (changedTransactionDetail != null) {
            handleChangedDetail(changedTransactionDetail);
        }
    }

    @Override
    public void processPostDisbursementTransactions(final Loan loan) {
        loan.processPostDisbursementTransactions().ifPresent(this::handleChangedDetail);
    }

    @Override
    public void removeLoanCharge(final Loan loan, final LoanCharge loanCharge) {
        loan.removeLoanCharge(loanCharge).ifPresent(this::handleChangedDetail);
    }

    @Override
    public void processLatestTransaction(final LoanTransaction loanTransaction, final Loan loan) {
        final ChangedTransactionDetail changedTransactionDetail = loan.getTransactionProcessor().processLatestTransaction(loanTransaction,
                new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                        new MoneyHolder(loan.getTotalOverpaidAsMoney()), new ChangedTransactionDetail()));
        final List<LoanTransaction> newTransactions = changedTransactionDetail.getTransactionChanges().stream()
                .map(TransactionChangeData::getNewTransaction).peek(transaction -> transaction.updateLoan(loan)).toList();
        loan.getLoanTransactions().addAll(newTransactions);

        loan.updateLoanSummaryDerivedFields();
        handleChangedDetail(changedTransactionDetail);
    }

    private void handleChangedDetail(final ChangedTransactionDetail changedTransactionDetail) {
        for (TransactionChangeData change : changedTransactionDetail.getTransactionChanges()) {
            final LoanTransaction newTransaction = change.getNewTransaction();
            final LoanTransaction oldTransaction = change.getOldTransaction();

            loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(newTransaction);

            if (oldTransaction != null) {
                loanAccountTransfersService.updateLoanTransaction(oldTransaction.getId(), newTransaction);
            }
        }
        replayedTransactionBusinessEventService.raiseTransactionReplayedEvents(changedTransactionDetail);
    }
}
