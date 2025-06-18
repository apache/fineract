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
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.interestpauses.service.LoanAccountTransfersService;
import org.apache.fineract.portfolio.loanaccount.data.TransactionChangeData;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReprocessLoanTransactionsServiceImpl implements ReprocessLoanTransactionsService {

    private final LoanAccountService loanAccountService;
    private final LoanAccountTransfersService loanAccountTransfersService;
    private final ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService;
    private final LoanTransactionProcessingService loanTransactionProcessingService;
    private final InterestScheduleModelRepositoryWrapper interestScheduleModelRepositoryWrapper;
    private final LoanBalanceService loanBalanceService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanTransactionService loanTransactionService;

    @Override
    public void reprocessTransactions(final Loan loan) {
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = loanTransactionService
                .retrieveListOfTransactionsForReprocessing(loan);

        final ChangedTransactionDetail changedTransactionDetail = reprocessTransactionsAndFetchChangedTransactions(loan,
                allNonContraTransactionsPostDisbursement);
        handleChangedDetail(changedTransactionDetail);
    }

    @Override
    public void reprocessParticularTransactions(final Loan loan, final List<LoanTransaction> loanTransactions) {
        final ChangedTransactionDetail changedTransactionDetail = reprocessTransactionsAndFetchChangedTransactions(loan, loanTransactions);
        handleChangedDetail(changedTransactionDetail);
    }

    @Override
    public void reprocessTransactionsWithPostTransactionChecks(final Loan loan, final LocalDate transactionDate) {
        final List<LoanTransaction> transactions = loanTransactionRepository.findNonReversedTransactionsForReprocessingByLoan(loan);
        final ChangedTransactionDetail changedTransactionDetail = reprocessTransactionsAndFetchChangedTransactions(loan, transactions);
        handleChangedDetail(changedTransactionDetail);
    }

    @Override
    public void processPostDisbursementTransactions(final Loan loan) {
        loanTransactionProcessingService.processPostDisbursementTransactions(loan).ifPresent(this::handleChangedDetail);
    }

    @Override
    public void removeLoanCharge(final Loan loan, final LoanCharge loanCharge) {
        final boolean removed = loanCharge.isActive();
        if (removed) {
            loanCharge.setActive(false);
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(loan.getCurrency(), loan.getDisbursementDate(), loan.getRepaymentScheduleInstallments(),
                    loan.getActiveCharges());
            loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());
        }

        removeOrModifyTransactionAssociatedWithLoanChargeIfDueAtDisbursement(loan, loanCharge);

        if (!loanCharge.isDueAtDisbursement() && loanCharge.isPaidOrPartiallyPaid(loan.getCurrency())) {
            /*
             * TODO Vishwas Currently we do not allow removing a loan charge after a loan is approved (hence there is no
             * need to adjust any loan transactions).
             *
             * Consider removing this block of code or logically completing it for the future by getting the list of
             * affected Transactions
             */
            reprocessTransactions(loan);
            return;
        }
        loan.getLoanCharges().remove(loanCharge);
        loanBalanceService.updateLoanSummaryDerivedFields(loan);
    }

    private void removeOrModifyTransactionAssociatedWithLoanChargeIfDueAtDisbursement(final Loan loan, final LoanCharge loanCharge) {
        if (loanCharge.isDueAtDisbursement()) {
            LoanTransaction transactionToRemove = null;
            List<LoanTransaction> transactions = loan.getLoanTransactions();
            for (final LoanTransaction transaction : transactions) {
                if (transaction.isRepaymentAtDisbursement()
                        && doesLoanChargePaidByContainLoanCharge(transaction.getLoanChargesPaid(), loanCharge)) {
                    final MonetaryCurrency currency = loan.getCurrency();
                    final Money chargeAmount = Money.of(currency, loanCharge.amount());
                    if (transaction.isGreaterThan(chargeAmount)) {
                        final Money principalPortion = Money.zero(currency);
                        final Money interestPortion = Money.zero(currency);
                        final Money penaltyChargesPortion = Money.zero(currency);

                        transaction.updateComponentsAndTotal(principalPortion, interestPortion, chargeAmount, penaltyChargesPortion);

                    } else {
                        transactionToRemove = transaction;
                    }
                }
            }

            if (transactionToRemove != null) {
                loan.removeLoanTransaction(transactionToRemove);
            }
        }
    }

    private boolean doesLoanChargePaidByContainLoanCharge(Set<LoanChargePaidBy> loanChargePaidBys, LoanCharge loanCharge) {
        return loanChargePaidBys.stream() //
                .anyMatch(loanChargePaidBy -> loanChargePaidBy.getLoanCharge().equals(loanCharge));
    }

    @Override
    public void processLatestTransaction(final LoanTransaction loanTransaction, final Loan loan) {
        final ChangedTransactionDetail changedTransactionDetail = loanTransactionProcessingService.processLatestTransaction(
                loan.getTransactionProcessingStrategyCode(), loanTransaction,
                new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                        new MoneyHolder(loan.getTotalOverpaidAsMoney()), new ChangedTransactionDetail()));
        final List<LoanTransaction> newTransactions = changedTransactionDetail.getTransactionChanges().stream()
                .map(TransactionChangeData::getNewTransaction).peek(transaction -> transaction.updateLoan(loan)).toList();
        loan.getLoanTransactions().addAll(newTransactions);

        loanBalanceService.updateLoanSummaryDerivedFields(loan);
        handleChangedDetail(changedTransactionDetail);
    }

    @Override
    public void updateModel(Loan loan) {
        Optional<ProgressiveLoanInterestScheduleModel> savedModel = interestScheduleModelRepositoryWrapper.getSavedModel(loan,
                ThreadLocalContextUtil.getBusinessDate());
        if (savedModel.isEmpty()) {
            reprocessTransactions(loan);
        }
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

    private ChangedTransactionDetail reprocessTransactionsAndFetchChangedTransactions(final Loan loan,
            final List<LoanTransaction> loanTransactions) {
        final ChangedTransactionDetail changedTransactionDetail = loanTransactionProcessingService.reprocessLoanTransactions(
                loan.getTransactionProcessingStrategyCode(), loan.getDisbursementDate(), loanTransactions, loan.getCurrency(),
                loan.getRepaymentScheduleInstallments(), loan.getActiveCharges());
        for (TransactionChangeData change : changedTransactionDetail.getTransactionChanges()) {
            change.getNewTransaction().updateLoan(loan);
        }
        final List<LoanTransaction> newTransactions = changedTransactionDetail.getTransactionChanges().stream()
                .map(TransactionChangeData::getNewTransaction).toList();
        loan.getLoanTransactions().addAll(newTransactions);
        loanBalanceService.updateLoanSummaryDerivedFields(loan);
        return changedTransactionDetail;
    }
}
