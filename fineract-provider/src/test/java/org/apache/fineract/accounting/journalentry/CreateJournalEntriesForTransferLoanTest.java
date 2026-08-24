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
import static org.mockito.ArgumentMatchers.anyString;
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

@ExtendWith(MockitoExtension.class)
class CreateJournalEntriesForTransferLoanTest {

    private static final Long LOAN_ID = 1L;
    private static final Long LOAN_PRODUCT_ID = 1L;
    private static final Long LOAN_OFFICE_ID = 1L;
    private static final Long TRANSACTION_OFFICE_ID = 2L;
    private static final String CURRENCY_CODE = "USD";
    private static final String TRANSACTION_ID = "txn-transfer";
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2021, 2, 11);
    private static final BigDecimal TRANSACTION_AMOUNT = new BigDecimal("600.00");
    private static final BigDecimal PRINCIPAL_AMOUNT = new BigDecimal("500.00");

    @Mock
    private AccountingProcessorHelperImpl helper;
    @InjectMocks
    private AccrualBasedAccountingProcessorForLoan processor;
    private Office office;

    @BeforeEach
    void setUp() {
        office = Office.headOffice("Transaction Office", TRANSACTION_DATE, null);
        when(helper.getOfficeById(TRANSACTION_OFFICE_ID)).thenReturn(office);

        GLClosure mockClosure = mock(GLClosure.class);
        when(helper.getLatestClosureByBranch(TRANSACTION_OFFICE_ID)).thenReturn(mockClosure);
    }

    @Test
    void shouldCreateJournalEntriesForTransferInitiation() {
        LoanTransactionEnumData transactionType = mock(LoanTransactionEnumData.class);
        when(transactionType.isInitiateTransfer()).thenReturn(true);

        processor.createJournalEntriesForLoan(createLoanDTO(transactionType));

        verify(helper).createJournalEntriesForLoan(office, CURRENCY_CODE, AccrualAccountsForLoan.TRANSFERS_SUSPENSE.getValue(),
                AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), LOAN_PRODUCT_ID, null, LOAN_ID, TRANSACTION_ID, TRANSACTION_DATE,
                PRINCIPAL_AMOUNT);
    }

    @Test
    void shouldCreateJournalEntriesForTransferApproval() {
        LoanTransactionEnumData transactionType = mock(LoanTransactionEnumData.class);
        when(transactionType.isApproveTransfer()).thenReturn(true);

        processor.createJournalEntriesForLoan(createLoanDTO(transactionType));

        verify(helper).createJournalEntriesForLoan(office, CURRENCY_CODE, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                AccrualAccountsForLoan.TRANSFERS_SUSPENSE.getValue(), LOAN_PRODUCT_ID, null, LOAN_ID, TRANSACTION_ID, TRANSACTION_DATE,
                PRINCIPAL_AMOUNT);
    }

    @Test
    void shouldCreateJournalEntriesForTransferWithdrawal() {
        LoanTransactionEnumData transactionType = mock(LoanTransactionEnumData.class);
        when(transactionType.isWithdrawTransfer()).thenReturn(true);

        processor.createJournalEntriesForLoan(createLoanDTO(transactionType));

        verify(helper).createJournalEntriesForLoan(office, CURRENCY_CODE, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                AccrualAccountsForLoan.TRANSFERS_SUSPENSE.getValue(), LOAN_PRODUCT_ID, null, LOAN_ID, TRANSACTION_ID, TRANSACTION_DATE,
                PRINCIPAL_AMOUNT);
    }

    @Test
    void shouldNotCreateJournalEntriesForTransferWithoutPrincipalAmount() {
        LoanTransactionEnumData transactionType = mock(LoanTransactionEnumData.class);
        when(transactionType.isInitiateTransfer()).thenReturn(true);

        processor.createJournalEntriesForLoan(createLoanDTO(transactionType, null));

        verify(helper, never()).createJournalEntriesForLoan(any(Office.class), anyString(), any(Integer.class), any(Integer.class),
                any(Long.class), isNull(), any(Long.class), anyString(), any(LocalDate.class), any(BigDecimal.class));
    }

    private LoanDTO createLoanDTO(final LoanTransactionEnumData transactionType) {
        return createLoanDTO(transactionType, PRINCIPAL_AMOUNT);
    }

    private LoanDTO createLoanDTO(final LoanTransactionEnumData transactionType, final BigDecimal principalAmount) {
        LoanTransactionDTO loanTransactionDTO = new LoanTransactionDTO(TRANSACTION_OFFICE_ID, null, TRANSACTION_ID, TRANSACTION_DATE,
                transactionType, TRANSACTION_AMOUNT, principalAmount, null, null, null, null, false, Collections.emptyList(),
                Collections.emptyList(), false, "", null, null, null, null);

        return new LoanDTO(LOAN_ID, LOAN_PRODUCT_ID, LOAN_OFFICE_ID, CURRENCY_CODE, false, true, true, List.of(loanTransactionDTO), false,
                false, null, false, false, null, null, null);
    }
}
