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
package org.apache.fineract.portfolio.account.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionReplacement;
import org.apache.fineract.portfolio.savings.service.SavingsAccountTransfersService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingsAccountTransfersServiceImpl implements SavingsAccountTransfersService {

    private final AccountTransferRepository accountTransferRepository;

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findTransferTransactionIds(final Collection<SavingsAccountTransactionReplacement> transactionReplacements) {
        if (transactionReplacements.isEmpty()) {
            return Set.of();
        }

        final var replacementsByTransactionId = mapReplacementsByTransactionId(transactionReplacements);
        final var transferTransactions = this.accountTransferRepository.findBySavingsTransactionIds(replacementsByTransactionId.keySet());
        final Set<Long> transferTransactionIds = new HashSet<>();
        transferTransactions.forEach(transferTransaction -> {
            final var fromSavingsTransaction = transferTransaction.getFromTransaction();
            if (fromSavingsTransaction != null) {
                addTransactionIds(fromSavingsTransaction, replacementsByTransactionId, transferTransactionIds);
            }

            final var toSavingsTransaction = transferTransaction.getToSavingsTransaction();
            if (toSavingsTransaction != null) {
                addTransactionIds(toSavingsTransaction, replacementsByTransactionId, transferTransactionIds);
            }
        });
        return transferTransactionIds;
    }

    @Override
    @Transactional
    public void updateSavingsTransactionReferences(final Collection<SavingsAccountTransactionReplacement> transactionReplacements) {
        if (transactionReplacements.isEmpty()) {
            return;
        }

        final var replacementsByTransactionId = mapReplacementsByTransactionId(transactionReplacements);
        final var transferTransactions = this.accountTransferRepository.findBySavingsTransactionIds(replacementsByTransactionId.keySet());
        transferTransactions.forEach(transferTransaction -> {
            final var fromSavingsTransaction = transferTransaction.getFromTransaction();
            if (fromSavingsTransaction != null) {
                final var replacementChain = resolveReplacementChain(fromSavingsTransaction, replacementsByTransactionId);
                if (!replacementChain.isEmpty()) {
                    transferTransaction.updateFromSavingsTransaction(replacementChain.getLast());
                }
            }

            final var toSavingsTransaction = transferTransaction.getToSavingsTransaction();
            if (toSavingsTransaction != null) {
                final var replacementChain = resolveReplacementChain(toSavingsTransaction, replacementsByTransactionId);
                if (!replacementChain.isEmpty()) {
                    transferTransaction.updateToSavingsTransaction(replacementChain.getLast());
                }
            }
        });
        this.accountTransferRepository.saveAll(transferTransactions);
    }

    private Map<Long, SavingsAccountTransactionReplacement> mapReplacementsByTransactionId(
            final Collection<SavingsAccountTransactionReplacement> transactionReplacements) {
        final Map<Long, SavingsAccountTransactionReplacement> replacementsByTransactionId = new HashMap<>();
        transactionReplacements
                .forEach(replacement -> replacementsByTransactionId.put(replacement.replacedTransaction().getId(), replacement));
        return replacementsByTransactionId;
    }

    private void addTransactionIds(final SavingsAccountTransaction replacedTransaction,
            final Map<Long, SavingsAccountTransactionReplacement> replacementsByTransactionId, final Set<Long> transferTransactionIds) {
        resolveReplacementChain(replacedTransaction, replacementsByTransactionId).stream().map(SavingsAccountTransaction::getId)
                .forEach(transferTransactionIds::add);
    }

    private List<SavingsAccountTransaction> resolveReplacementChain(final SavingsAccountTransaction replacedTransaction,
            final Map<Long, SavingsAccountTransactionReplacement> replacementsByTransactionId) {
        if (!replacementsByTransactionId.containsKey(replacedTransaction.getId())) {
            return List.of();
        }

        final List<SavingsAccountTransaction> replacementChain = new ArrayList<>();
        final Set<Long> visitedTransactionIds = new HashSet<>();
        var currentTransaction = replacedTransaction;
        while (currentTransaction != null) {
            final var currentTransactionId = currentTransaction.getId();
            if (!visitedTransactionIds.add(currentTransactionId)) {
                throw new IllegalStateException("Circular savings transaction replacement chain for transaction " + currentTransactionId);
            }

            replacementChain.add(currentTransaction);
            final var replacement = replacementsByTransactionId.get(currentTransactionId);
            if (replacement == null) {
                break;
            }
            currentTransaction = replacement.replacementTransaction();
        }
        return replacementChain;
    }
}
