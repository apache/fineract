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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForSavings;
import org.apache.fineract.accounting.journalentry.data.SavingsDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsTransactionDTO;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CashBasedAccountingProcessorForSavingsTest {

    private static final Long OFFICE_ID = 1L;
    private static final Long SAVINGS_PRODUCT_ID = 10L;
    private static final Long SAVINGS_ID = 100L;
    private static final String TRANSACTION_ID = "200";
    private static final String CURRENCY_CODE = "USD";
    private static final LocalDate POSTING_DATE = LocalDate.of(2021, 8, 31);

    @Mock
    private AccountingProcessorHelper helper;
    @Mock
    private Office office;

    @InjectMocks
    private CashBasedAccountingProcessorForSavings processor;

    @Test
    void interestPostingThatPaysDownAnOverdraftIsExpensedToInterestOnSavings() {
        when(helper.getOfficeById(OFFICE_ID)).thenReturn(office);

        // 19.18 posted to an account overdrawn by 10: 10 pays down the overdraft and 9.18 goes to the balance
        processor.createJournalEntriesForSavings(savingsWith(interestPosting(new BigDecimal("19.18"), BigDecimal.TEN, false)));

        verify(helper).createCashBasedJournalEntriesAndReversalsForSavings(office, CURRENCY_CODE,
                CashAccountsForSavings.INTEREST_ON_SAVINGS.getValue(), CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                SAVINGS_PRODUCT_ID, null, SAVINGS_ID, TRANSACTION_ID, POSTING_DATE, BigDecimal.TEN, false);
        verify(helper).createCashBasedJournalEntriesAndReversalsForSavings(office, CURRENCY_CODE,
                CashAccountsForSavings.INTEREST_ON_SAVINGS.getValue(), CashAccountsForSavings.SAVINGS_CONTROL.getValue(), SAVINGS_PRODUCT_ID,
                null, SAVINGS_ID, TRANSACTION_ID, POSTING_DATE, new BigDecimal("9.18"), false);
    }

    @Test
    void reversalMirrorsTheStoredEntriesInsteadOfRebuildingThem() {
        when(helper.getOfficeById(OFFICE_ID)).thenReturn(office);

        processor.createJournalEntriesForSavings(savingsWith(interestPosting(new BigDecimal("19.18"), BigDecimal.TEN, true)));

        verify(helper).createJournalEntriesForReversedSavingsTransaction(TRANSACTION_ID, POSTING_DATE);
        verify(helper, never()).createCashBasedJournalEntriesAndReversalsForSavings(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any());
    }

    private static SavingsDTO savingsWith(final SavingsTransactionDTO transaction) {
        return new SavingsDTO(SAVINGS_ID, SAVINGS_PRODUCT_ID, OFFICE_ID, CURRENCY_CODE, true, false, List.of(transaction));
    }

    private static SavingsTransactionDTO interestPosting(final BigDecimal amount, final BigDecimal overdraftAmount,
            final boolean reversed) {
        return new SavingsTransactionDTO(OFFICE_ID, null, TRANSACTION_ID, POSTING_DATE,
                SavingsEnumerations.transactionType(SavingsAccountTransactionType.INTEREST_POSTING), amount, reversed, List.of(), List.of(),
                overdraftAmount, false, List.of());
    }
}
