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
package org.apache.fineract.accounting.journalentry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForLoan;
import org.apache.fineract.accounting.journalentry.data.LoanDTO;
import org.apache.fineract.accounting.journalentry.data.LoanTransactionDTO;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorHelperImpl;
import org.apache.fineract.accounting.journalentry.service.AccrualBasedAccountingProcessorForLoan;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * A chargeback gives back the principal, interest, fee and penalty portions of a repayment, so each portion must be
 * debited to its own account, and the journal entries must balance the full amount credited to the fund source.
 */
@ExtendWith(MockitoExtension.class)
class CreateJournalEntriesForChargebackTest {

    private static final Long LOAN_ID = 1L;
    private static final Long LOAN_PRODUCT_ID = 2L;
    private static final Long OFFICE_ID = 3L;
    private static final String CURRENCY_CODE = "EUR";
    private static final String TRANSACTION_ID = "123";
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2026, 10, 20);

    @Mock
    private AccountingProcessorHelperImpl helper;
    @InjectMocks
    private AccrualBasedAccountingProcessorForLoan processor;

    private Office office;

    @BeforeEach
    void setUp() {
        office = Office.headOffice("Head Office", TRANSACTION_DATE, null);
        when(helper.getOfficeById(OFFICE_ID)).thenReturn(office);
        when(helper.getLatestClosureByBranch(OFFICE_ID)).thenReturn(mock(GLClosure.class));
    }

    @Test
    void interestPortionIsDebitedToInterestReceivable() {
        processor.createJournalEntriesForLoan(loan(false, chargeback("50", "45", "5", null, null, null)));

        verifyFundSourceCredited("50");
        verifyDebited(AccrualAccountsForLoan.LOAN_PORTFOLIO, "45");
        verifyDebited(AccrualAccountsForLoan.INTEREST_RECEIVABLE, "5");
    }

    @Test
    void interestPortionIsDebitedToIncomeFromChargeOffInterestWhenLoanIsChargedOff() {
        processor.createJournalEntriesForLoan(loan(true, chargeback("50", "45", "5", null, null, null)));

        verifyFundSourceCredited("50");
        verifyDebited(AccrualAccountsForLoan.CHARGE_OFF_EXPENSE, "45");
        verifyDebited(AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST, "5");
        verifyNotBooked(AccrualAccountsForLoan.INTEREST_RECEIVABLE);
    }

    @Test
    void interestPortionCoveredByOverpaymentIsNotDebited() {
        processor.createJournalEntriesForLoan(loan(false, chargeback("50", "45", "5", "50", "45", "5")));

        verifyFundSourceCredited("50");
        verifyDebited(AccrualAccountsForLoan.OVERPAYMENT, "50");
        verifyNotBooked(AccrualAccountsForLoan.LOAN_PORTFOLIO);
        verifyNotBooked(AccrualAccountsForLoan.INTEREST_RECEIVABLE);
    }

    @Test
    void onlyThePartOfTheInterestPortionNotCoveredByOverpaymentIsDebited() {
        processor.createJournalEntriesForLoan(loan(false, chargeback("50", "45", "5", "20", "18", "2")));

        verifyFundSourceCredited("50");
        verifyDebited(AccrualAccountsForLoan.OVERPAYMENT, "20");
        verifyDebited(AccrualAccountsForLoan.LOAN_PORTFOLIO, "27");
        verifyDebited(AccrualAccountsForLoan.INTEREST_RECEIVABLE, "3");
    }

    private LoanTransactionDTO chargeback(final String amount, final String principal, final String interest, final String overpayment,
            final String principalPaid, final String interestPaid) {
        final LoanTransactionEnumData transactionType = mock(LoanTransactionEnumData.class);
        when(transactionType.isChargeback()).thenReturn(true);
        return new LoanTransactionDTO(OFFICE_ID, null, TRANSACTION_ID, TRANSACTION_DATE, transactionType, new BigDecimal(amount),
                new BigDecimal(principal), new BigDecimal(interest), null, null, decimal(overpayment), false, Collections.emptyList(),
                Collections.emptyList(), false, null, null, decimal(principalPaid), decimal(interestPaid), null, null);
    }

    private LoanDTO loan(final boolean chargedOff, final LoanTransactionDTO transaction) {
        return new LoanDTO(LOAN_ID, LOAN_PRODUCT_ID, OFFICE_ID, CURRENCY_CODE, false, false, true, List.of(transaction), chargedOff, false,
                null, false, false, null, null, null);
    }

    private static BigDecimal decimal(final String value) {
        return value == null ? null : new BigDecimal(value);
    }

    private void verifyFundSourceCredited(final String amount) {
        verify(helper).createCreditJournalEntryForLoan(office, CURRENCY_CODE, AccrualAccountsForLoan.FUND_SOURCE, LOAN_PRODUCT_ID, null,
                LOAN_ID, TRANSACTION_ID, TRANSACTION_DATE, new BigDecimal(amount));
    }

    private void verifyDebited(final AccrualAccountsForLoan account, final String amount) {
        verify(helper).createDebitJournalEntryForLoan(office, CURRENCY_CODE, account.getValue(), LOAN_PRODUCT_ID, null, LOAN_ID,
                TRANSACTION_ID, TRANSACTION_DATE, new BigDecimal(amount));
    }

    private void verifyNotBooked(final AccrualAccountsForLoan account) {
        verify(helper, never()).createDebitJournalEntryForLoan(any(Office.class), anyString(), eq(account.getValue()), anyLong(), isNull(),
                anyLong(), anyString(), any(LocalDate.class), any(BigDecimal.class));
        verify(helper, never()).createCreditJournalEntryForLoan(any(Office.class), anyString(), eq(account.getValue()), anyLong(), isNull(),
                anyLong(), anyString(), any(LocalDate.class), any(BigDecimal.class));
    }
}
