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
package org.apache.fineract.accounting.journalentry.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.accounting.journalentry.data.ClientTransactionDTO;
import org.apache.fineract.organisation.office.domain.Office;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CashBasedAccountingProcessorForClientTransactionsTest {

    @ParameterizedTest
    @CsvSource({ "false,false", "true,false", "true,true", "false,true" })
    void preservesFundingAndReversalContext(boolean savingsFunded, boolean reversed) {
        AccountingProcessorHelper helper = mock(AccountingProcessorHelper.class);
        ClientTransactionDTO transaction = mock(ClientTransactionDTO.class);
        Office office = mock(Office.class);
        LocalDate date = LocalDate.of(2026, 11, 5);
        when(transaction.isAccountingEnabled()).thenReturn(true);
        when(transaction.isChargePayment()).thenReturn(true);
        when(transaction.getOfficeId()).thenReturn(1L);
        when(transaction.getClientId()).thenReturn(2L);
        when(transaction.getTransactionId()).thenReturn(3L);
        when(transaction.getCurrencyCode()).thenReturn("USD");
        when(transaction.getTransactionDate()).thenReturn(date);
        when(transaction.getAmount()).thenReturn(BigDecimal.TEN);
        when(transaction.isReversed()).thenReturn(reversed);
        when(transaction.isSavingsFunded()).thenReturn(savingsFunded);
        when(helper.getOfficeById(1L)).thenReturn(office);
        when(helper.createCreditJournalEntryOrReversalForClientPayments(eq(office), eq("USD"), eq(2L), eq(3L), eq(date), eq(reversed),
                any())).thenReturn(BigDecimal.TEN);

        new CashBasedAccountingProcessorForClientTransactions(helper).createJournalEntriesForClientTransaction(transaction);

        verify(helper).createDebitJournalEntryOrReversalForClientChargePayments(office, "USD", 2L, 3L, date, BigDecimal.TEN, reversed,
                savingsFunded);
    }
}
