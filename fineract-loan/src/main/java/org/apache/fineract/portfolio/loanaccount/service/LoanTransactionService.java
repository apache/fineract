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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionComparator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanTransactionService {

    private final LoanTransactionRepository loanTransactionRepository;

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

}
