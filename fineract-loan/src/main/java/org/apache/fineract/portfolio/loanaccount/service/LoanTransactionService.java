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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionComparator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanTransactionService {

    private final LoanTransactionRepository loanTransactionRepository;

    public static final List<LoanTransactionType> PAYMENT_LOAN_TRANSACTION_TYPES = List.of(LoanTransactionType.REPAYMENT, //
            LoanTransactionType.MERCHANT_ISSUED_REFUND, //
            LoanTransactionType.PAYOUT_REFUND, //
            LoanTransactionType.GOODWILL_CREDIT, //
            LoanTransactionType.CHARGE_REFUND, //
            LoanTransactionType.CHARGE_ADJUSTMENT, //
            LoanTransactionType.DOWN_PAYMENT, //
            LoanTransactionType.INTEREST_PAYMENT_WAIVER, //
            LoanTransactionType.INTEREST_REFUND, //
            LoanTransactionType.CAPITALIZED_INCOME_ADJUSTMENT);

    public List<LoanTransaction> retrieveListOfTransactionsForReprocessing(final Loan loan) {
        return loan.getLoanTransactions().stream().filter(loanTransactionForReprocessingPredicate())
                .sorted(LoanTransactionComparator.INSTANCE).collect(Collectors.toList());
    }

    public boolean isChronologicallyLatestRepaymentOrWaiver(final Loan loan, final LoanTransaction loanTransaction) {
        final Optional<LocalDate> lastTransactionDateForReprocessing = loanTransactionRepository
                .findLastTransactionDateForReprocessing(loan);

        return lastTransactionDateForReprocessing.isEmpty()
                || !DateUtils.isAfter(lastTransactionDateForReprocessing.get(), loanTransaction.getTransactionDate());
    }

    private Predicate<LoanTransaction> loanTransactionForReprocessingPredicate() {
        return transaction -> transaction.isNotReversed()
                && (transaction.isChargeOff() || transaction.isReAge() || transaction.isAccrualActivity() || transaction.isReAmortize()
                        || !transaction.isNonMonetaryTransaction() || transaction.isContractTermination());
    }

    private boolean hasChargeOffTransaction(final Loan loan) {
        return loanTransactionRepository.hasChargeOffTransaction(loan);
    }

    private boolean hasContractTerminationTransaction(final Loan loan) {
        return loanTransactionRepository.hasContractTerminationTransaction(loan);
    }

    public boolean shouldRegenerateRepaymentSchedule(final Loan loan) {
        final boolean hasChargeOffTransaction = hasChargeOffTransaction(loan);
        final boolean hasContractTerminationTransaction = hasContractTerminationTransaction(loan);
        return loan.isProgressiveSchedule()
                && ((hasChargeOffTransaction && loan.hasAccelerateChargeOffStrategy()) || hasContractTerminationTransaction);
    }

    public Money calculateTotalPaidInRepayments(final Loan loan) {
        return Money.of(loan.getCurrency(),
                loanTransactionRepository.sumTotalAmountByLoanAndTransactionTypes(loan, PAYMENT_LOAN_TRANSACTION_TYPES));
    }

    public List<LoanTransaction> fetchNonReversedPaymentLoanTransactions(final Loan loan) {
        return loanTransactionRepository.fetchNonReversedByLoanAndTransactionTypes(loan, PAYMENT_LOAN_TRANSACTION_TYPES);
    }

    public List<LoanTransaction> fetchNonReversedMonetaryTransactionsByLoan(final Loan loan) {
        return loanTransactionRepository.findNonReversedMonetaryTransactionsByLoan(loan);
    }

    public List<LoanTransaction> fetchNonReversedPaymentLoanTransactions(final Loan loan, LoanTransaction loanTransaction) {
        List<LoanTransaction> loanTransactions = loanTransactionRepository.findNonReversedMonetaryTransactionsByLoan(loan);
        if (loanTransaction.isNotZero() || loanTransaction.isReversed()) {
            loanTransactions.add(loanTransaction);
        }
        loanTransactions.sort(LoanTransactionComparator.INSTANCE);
        return loanTransactions;
    }
}
