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
package org.apache.fineract.portfolio.savings.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SavingsAccountAccountingBridgeTest {

    @Test
    void excludeAuditOnlyReversalTransactionsFromAccounting() {
        final var account = new SavingsAccount() {};
        final var product = mock(SavingsProduct.class);
        when(product.getId()).thenReturn(1L);
        when(product.isCashBasedAccountingEnabled()).thenReturn(true);
        when(product.isAccrualBasedAccountingEnabled()).thenReturn(false);
        account.product = product;

        final var reversedTransaction = mock(SavingsAccountTransaction.class);
        final var auditReversalTransaction = mock(SavingsAccountTransaction.class);
        final var replacementTransaction = mock(SavingsAccountTransaction.class);
        final Map<String, Object> reversedTransactionData = Map.of("id", 10L);
        final Map<String, Object> replacementTransactionData = Map.of("id", 12L);

        when(reversedTransaction.getId()).thenReturn(10L);
        when(reversedTransaction.isReversed()).thenReturn(true);
        when(reversedTransaction.toMapData("USD")).thenReturn(reversedTransactionData);
        when(auditReversalTransaction.getId()).thenReturn(11L);
        when(auditReversalTransaction.isReversalTransaction()).thenReturn(true);
        when(replacementTransaction.getId()).thenReturn(12L);
        when(replacementTransaction.toMapData("USD")).thenReturn(replacementTransactionData);
        account.transactions = List.of(reversedTransaction, auditReversalTransaction, replacementTransaction);

        final var accountingBridgeData = account.deriveAccountingBridgeData("USD", Set.of(10L), Set.of(), false, false);

        assertEquals(List.of(reversedTransactionData, replacementTransactionData), accountingBridgeData.get("newSavingsTransactions"));
        verify(auditReversalTransaction, never()).toMapData(anyString());
    }
}
