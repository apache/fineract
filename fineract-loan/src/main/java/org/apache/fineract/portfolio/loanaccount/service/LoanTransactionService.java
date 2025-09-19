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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionComparator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public List<LoanTransaction> retrieveListOfTransactionsForReprocessing(final Loan loan, LoanTransaction... inFlightTransactions) {
        return retrieveListOfTransactionsForReprocessing(loan, null, inFlightTransactions);
    }

    public List<LoanTransaction> retrieveListOfTransactionsForReprocessing(final Loan loan, LoanTransaction originalTransaction,
            LoanTransaction... inFlightTransactions) {
        Predicate<LoanTransaction> predicate = loanTransactionForReprocessingPredicate();
        List<LoanTransaction> transactions = loanTransactionRepository.findNonReversedTransactionsForReprocessingByLoan(loan).stream()
                .filter(predicate).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        Set<Long> existingIds = new HashSet<>();
        for (LoanTransaction transaction : transactions) {
            Long id = transaction.getId();
            if (id != null) {
                existingIds.add(id);
            }
        }

        if (inFlightTransactions != null) {
            for (LoanTransaction candidate : inFlightTransactions) {
                if (candidate == null || (!predicate.test(candidate))) {
                    continue;
                }
                Long candidateId = candidate.getId();
                if (candidateId != null && existingIds.contains(candidateId)) {
                    continue;
                }
                transactions.add(candidate);
                if (candidateId != null) {
                    existingIds.add(candidateId);
                }
            }
        }

        // Include chargeback-related original transactions to ensure visibility
        includeChargebackRelatedTransactions(loan, transactions, existingIds, originalTransaction);

        transactions.sort(LoanTransactionComparator.INSTANCE);
        return transactions;
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

    /**
     * Ensures that chargeback transactions have their related original transactions included in the processing set.
     * This prevents the "Chargeback transaction must have an original transaction" error during reprocessing.
     *
     * @param loan
     *            the loan being processed
     * @param transactions
     *            the list of transactions to be reprocessed
     * @param existingIds
     *            set of transaction IDs already included
     * @param knownOriginalTransaction
     *            the original transaction being charged back (if known)
     */
    private void includeChargebackRelatedTransactions(final Loan loan, List<LoanTransaction> transactions, Set<Long> existingIds,
            LoanTransaction knownOriginalTransaction) {
        // Find all chargeback transactions in the current transaction set
        List<LoanTransaction> chargebackTransactions = transactions.stream().filter(t -> t.isChargeback()).toList();

        log.info("Checking chargeback-related transactions. Total transactions: {}, Chargeback transactions found: {}", transactions.size(),
                chargebackTransactions.size());

        // If we have a known original transaction for chargeback creation, include it immediately
        if (knownOriginalTransaction != null && knownOriginalTransaction.getId() != null
                && !existingIds.contains(knownOriginalTransaction.getId())) {
            log.info("Including known original transaction {} for chargeback processing", knownOriginalTransaction.getId());
            transactions.add(knownOriginalTransaction);
            existingIds.add(knownOriginalTransaction.getId());
        }

        if (chargebackTransactions.isEmpty()) {
            return;
        }

        log.info("Found {} chargeback transactions, checking for related original transactions", chargebackTransactions.size());

        // For each chargeback, find its related original transaction
        for (LoanTransaction chargebackTransaction : chargebackTransactions) {
            Long chargebackId = chargebackTransaction.getId();

            log.info("Processing chargeback transaction {}, has ID: {}", chargebackTransaction, chargebackId != null);

            // If the chargeback is an in-flight transaction without ID, we can't find relations yet
            if (chargebackId == null) {
                log.info("Chargeback transaction has no ID, skipping relation lookup");
                continue;
            }

            // Look for the original transaction that this chargeback references
            Optional<LoanTransaction> originalTransaction = loan.getLoanTransactions().stream().filter(t -> t.isNotReversed()).filter(t -> {
                // Get stable reference to collection and ensure it's initialized to avoid EclipseLink change tracking
                // issues
                Set<LoanTransactionRelation> transactionRelations = t.getLoanTransactionRelations();
                transactionRelations.size(); // Force initialization
                boolean hasChargebackRelation = transactionRelations.stream().anyMatch(
                        rel -> rel.getRelationType() == LoanTransactionRelationTypeEnum.CHARGEBACK && rel.getToTransaction() != null
                                && chargebackId != null && chargebackId.equals(rel.getToTransaction().getId()));
                log.info("Checking transaction {} for chargeback relation to {}: {}", t.getId(), chargebackId, hasChargebackRelation);
                return hasChargebackRelation;
            }).findFirst();

            if (originalTransaction.isPresent()) {
                LoanTransaction original = originalTransaction.get();
                Long originalId = original.getId();

                // Only add if not already included
                if (originalId != null && !existingIds.contains(originalId)) {
                    log.info("Including original transaction {} for chargeback {}", originalId, chargebackId);
                    transactions.add(original);
                    existingIds.add(originalId);
                } else {
                    log.info("Original transaction {} for chargeback {} already included", originalId, chargebackId);
                }
            } else {
                log.info("No original transaction found for chargeback {}", chargebackId);
            }
        }
    }

    public Money calculateTotalPaidInRepayments(final Loan loan) {
        return Money.of(loan.getCurrency(),
                loanTransactionRepository.sumTotalAmountByLoanAndTransactionTypes(loan, PAYMENT_LOAN_TRANSACTION_TYPES));
    }
}
