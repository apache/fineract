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

import jakarta.persistence.FlushModeType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.annotation.WithFlushMode;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualAdjustmentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.interestpauses.service.LoanAccountTransfersService;
import org.apache.fineract.portfolio.loanaccount.data.TransactionChangeData;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.ProgressiveTransactionCtx;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@WithFlushMode(FlushModeType.COMMIT)
public class ReprocessLoanTransactionsServiceImpl implements ReprocessLoanTransactionsService {

    private final LoanAccountService loanAccountService;
    private final LoanAccountTransfersService loanAccountTransfersService;
    private final ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService;
    private final LoanTransactionProcessingService loanTransactionProcessingService;
    private final InterestScheduleModelRepositoryWrapper interestScheduleModelRepositoryWrapper;
    private final LoanBalanceService loanBalanceService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanTransactionService loanTransactionService;
    private final LoanJournalEntryPoster loanJournalEntryPoster;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanAccrualActivityProcessingService loanAccrualActivityProcessingService;

    @Override
    public void reprocessTransactions(final Loan loan) {
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = loanTransactionService
                .retrieveListOfTransactionsForReprocessing(loan);

        final ChangedTransactionDetail changedTransactionDetail = reprocessTransactionsAndFetchChangedTransactions(loan,
                allNonContraTransactionsPostDisbursement);
        handleChangedDetail(changedTransactionDetail);
    }

    @Override
    public void reprocessTransactions(final Loan loan, final List<LoanTransaction> newTransactions) {
        final List<LoanTransaction> transactions = loanTransactionRepository.findNonReversedTransactionsForReprocessingByLoan(loan);
        transactions.addAll(newTransactions);
        final ChangedTransactionDetail changedTransactionDetail = reprocessTransactionsAndFetchChangedTransactions(loan, transactions);
        handleChangedDetail(changedTransactionDetail);
    }

    @Override
    public void reprocessTransactionsWithoutChecks(final Loan loan, final List<LoanTransaction> newTransactions) {
        final List<LoanTransaction> transactions = loanTransactionRepository.findNonReversedTransactionsForReprocessingByLoan(loan);
        transactions.addAll(newTransactions);
        reprocessTransactionsAndFetchChangedTransactions(loan, transactions);
    }

    @Override
    public void processLatestTransaction(final LoanTransaction loanTransaction, final Loan loan) {
        LoanRepaymentScheduleTransactionProcessor transactionProcessor = loanTransactionProcessingService
                .getTransactionProcessor(loan.getTransactionProcessingStrategyCode());

        TransactionCtx transactionCtx;
        if (transactionProcessor instanceof AdvancedPaymentScheduleTransactionProcessor) {
            Optional<ProgressiveLoanInterestScheduleModel> savedModel = interestScheduleModelRepositoryWrapper.getSavedModel(loan,
                    loanTransaction.getTransactionDate());
            if (savedModel.isEmpty()) {
                throw new IllegalArgumentException("No saved model found for loan transaction " + loanTransaction);
            } else {

                final ProgressiveTransactionCtx progressiveTransactionCtx = new ProgressiveTransactionCtx(loan.getCurrency(),
                        loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(), new MoneyHolder(loan.getTotalOverpaidAsMoney()),
                        new ChangedTransactionDetail(), savedModel.get());
                progressiveTransactionCtx.setChargedOff(loan.isChargedOff());
                progressiveTransactionCtx.setWrittenOff(loan.isClosedWrittenOff());
                progressiveTransactionCtx.setContractTerminated(loan.isContractTermination());
                transactionCtx = progressiveTransactionCtx;
            }
        } else {
            transactionCtx = new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                    new MoneyHolder(loan.getTotalOverpaidAsMoney()), new ChangedTransactionDetail());
        }

        final ChangedTransactionDetail changedTransactionDetail = loanTransactionProcessingService
                .processLatestTransaction(loan.getTransactionProcessingStrategyCode(), loanTransaction, transactionCtx);
        final List<LoanTransaction> newTransactions = changedTransactionDetail.getTransactionChanges().stream()
                .map(TransactionChangeData::getNewTransaction).toList().stream().filter(LoanTransaction::isNotReversed)
                .peek(transaction -> transaction.updateLoan(loan)).toList();
        loan.getLoanTransactions().addAll(newTransactions);

        loanBalanceService.updateLoanSummaryDerivedFields(loan);
        handleChangedDetail(changedTransactionDetail);
    }

    @Override
    public void updateModel(Loan loan) {
        interestScheduleModelRepositoryWrapper.getSavedModel(loan, ThreadLocalContextUtil.getBusinessDate());
    }

    private void handleChangedDetail(final ChangedTransactionDetail changedTransactionDetail) {
        for (TransactionChangeData change : changedTransactionDetail.getTransactionChanges()) {
            final LoanTransaction newTransaction = change.getNewTransaction();
            final LoanTransaction oldTransaction = change.getOldTransaction();
            if (newTransaction.isNotReversed()) {
                loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(newTransaction);

                // Create journal entries for new transaction
                loanJournalEntryPoster.postJournalEntriesForLoanTransaction(newTransaction, false, false);
                if (oldTransaction == null && (newTransaction.isAccrual() || newTransaction.isAccrualAdjustment())) {
                    final LoanTransactionBusinessEvent businessEvent = newTransaction.isAccrual()
                            ? new LoanAccrualTransactionCreatedBusinessEvent(newTransaction)
                            : new LoanAccrualAdjustmentTransactionBusinessEvent(newTransaction);
                    businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
                }
                if (oldTransaction != null) {
                    loanAccountTransfersService.updateLoanTransaction(oldTransaction.getId(), newTransaction);
                }
            }

            if (oldTransaction != null) {
                // Create reversal journal entries for old transaction if it exists (reverse-replay scenario)
                loanJournalEntryPoster.postJournalEntriesForLoanTransaction(oldTransaction, false, false);
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
                .map(TransactionChangeData::getNewTransaction).toList().stream().filter(LoanTransaction::isNotReversed).toList();
        loan.getLoanTransactions().addAll(newTransactions);
        loanBalanceService.updateLoanSummaryDerivedFields(loan);
        loanAccrualActivityProcessingService.recalculateAccrualActivityTransaction(loan, changedTransactionDetail);
        return changedTransactionDetail;
    }
}
