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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.apache.fineract.portfolio.loanaccount.data.TransactionChangeData;

/**
 * Stores details of {@link LoanTransaction}'s that were reversed or newly created
 */
@Getter
public class ChangedTransactionDetail {

    private final List<TransactionChangeData> transactionChanges = new ArrayList<>();

    public void addTransactionChange(final TransactionChangeData transactionChangeData) {
        for (TransactionChangeData change : transactionChanges) {
            if (transactionChangeData.getOldTransaction() != null && change.getOldTransaction() != null
                    && Objects.equals(change.getOldTransaction().getId(), transactionChangeData.getOldTransaction().getId())) {
                change.setOldTransaction(transactionChangeData.getOldTransaction());
                change.setNewTransaction(transactionChangeData.getNewTransaction());
                return;
            } else if (transactionChangeData.getOldTransaction() == null && change.getOldTransaction() == null
                    && change.getNewTransaction() != null && transactionChangeData.getNewTransaction() != null
                    && Objects.equals(change.getNewTransaction().getId(), transactionChangeData.getNewTransaction().getId())) {
                change.setNewTransaction(transactionChangeData.getNewTransaction());
                return;
            }
        }
        transactionChanges.add(transactionChangeData);
    }

    public void addNewTransactionChangeBeforeExistingOne(final TransactionChangeData newTransactionChange,
            final LoanTransaction existingLoanTransaction) {
        if (existingLoanTransaction != null) {
            final Optional<TransactionChangeData> existingChange = transactionChanges.stream().filter(
                    change -> change.getNewTransaction() != null && Objects.equals(change.getNewTransaction(), existingLoanTransaction))
                    .findFirst();

            if (existingChange.isPresent()) {
                transactionChanges.add(transactionChanges.indexOf(existingChange.get()), newTransactionChange);
                return;
            }
        }
        transactionChanges.add(newTransactionChange);
    }

    public void removeTransactionChange(final LoanTransaction newTransaction) {
        transactionChanges.removeIf(change -> change.getNewTransaction().equals(newTransaction));
    }
}
