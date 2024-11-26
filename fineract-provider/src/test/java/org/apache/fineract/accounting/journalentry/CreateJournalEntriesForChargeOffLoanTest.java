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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForLoan;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.data.LoanDTO;
import org.apache.fineract.accounting.journalentry.data.LoanTransactionDTO;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorHelper;
import org.apache.fineract.accounting.journalentry.service.AccrualBasedAccountingProcessorForLoan;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateJournalEntriesForChargeOffLoanTest {

    private static final Integer chargeOffReasons = 15;

    @Mock
    private AccountingProcessorHelper helper;
    @InjectMocks
    private AccrualBasedAccountingProcessorForLoan processor;
    private LoanDTO loanDTO;

    @BeforeEach
    void setUp() {
        Office office = Office.headOffice("Main Office", LocalDate.now(ZoneId.systemDefault()), null);
        when(helper.getOfficeById(1L)).thenReturn(office);

        GLClosure mockClosure = mock(GLClosure.class);
        when(helper.getLatestClosureByBranch(1L)).thenReturn(mockClosure);

        LoanTransactionEnumData transactionType = mock(LoanTransactionEnumData.class);
        when(transactionType.isChargeoff()).thenReturn(true);

        LoanTransactionDTO loanTransactionDTO = new LoanTransactionDTO(1L, 1L, "txn-123", LocalDate.now(ZoneId.systemDefault()),
                transactionType, new BigDecimal("500.00"), new BigDecimal("500.00"), null, null, null, null, false, Collections.emptyList(),
                Collections.emptyList(), false, "", null, null, null, null);

        loanDTO = new LoanDTO(1L, 1L, 1L, "USD", false, true, true, List.of(loanTransactionDTO), false, false, chargeOffReasons);
    }

    @Test
    void shouldCreateJournalEntriesForChargeOff() {
        GLAccount chargeOffGLAccount = new GLAccount();
        chargeOffGLAccount.setId(15L);
        chargeOffGLAccount.setName("Charge-Off Account");
        chargeOffGLAccount.setGlCode("12345");

        ProductToGLAccountMapping chargeToGLAccountMapper = new ProductToGLAccountMapping();
        chargeToGLAccountMapper.setGlAccount(chargeOffGLAccount);

        when(helper.getChargeOffMappingByCodeValue(chargeOffReasons)).thenReturn(chargeToGLAccountMapper);

        GLAccount loanPortfolioGLAccount = new GLAccount();
        loanPortfolioGLAccount.setId(20L);
        loanPortfolioGLAccount.setName("Loan Portfolio Account");
        loanPortfolioGLAccount.setGlCode("54321");

        when(helper.getLinkedGLAccountForLoanProduct(1L, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), 1L))
                .thenReturn(loanPortfolioGLAccount);

        processor.createJournalEntriesForLoan(loanDTO);

        verify(helper, times(1)).getChargeOffMappingByCodeValue(chargeOffReasons);
        verify(helper, times(1)).getLinkedGLAccountForLoanProduct(1L, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), 1L);
        verify(helper, times(1)).createCreditJournalEntryOrReversalForLoan(helper.getOfficeById(1L), "USD",
                AccrualAccountsForLoan.LOAN_PORTFOLIO, 1L, null, 1L, "txn-123", LocalDate.now(ZoneId.systemDefault()),
                new BigDecimal("500.00"), false);
        verify(helper, times(1)).createDebitJournalEntryOrReversalForLoan(helper.getOfficeById(1L), "USD",
                AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), 1L, null, 1L, "txn-123", LocalDate.now(ZoneId.systemDefault()),
                new BigDecimal("500.00"), false);
    }

    @Test
    void shouldCreateJournalEntriesForChargeOffWithFraud() {
        loanDTO.setMarkedAsFraud(true);

        when(helper.getChargeOffMappingByCodeValue(chargeOffReasons)).thenReturn(null);

        GLAccount loanPortfolioGLAccount = new GLAccount();
        loanPortfolioGLAccount.setId(20L);
        loanPortfolioGLAccount.setName("Loan Portfolio Account");
        loanPortfolioGLAccount.setGlCode("54321");

        GLAccount fraudExpenseGLAccount = new GLAccount();
        fraudExpenseGLAccount.setId(30L);
        fraudExpenseGLAccount.setName("Fraud Expense Account");
        fraudExpenseGLAccount.setGlCode("98765");

        when(helper.getLinkedGLAccountForLoanProduct(1L, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), 1L))
                .thenReturn(loanPortfolioGLAccount);

        when(helper.getLinkedGLAccountForLoanProduct(1L, AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), 1L))
                .thenReturn(fraudExpenseGLAccount);

        processor.createJournalEntriesForLoan(loanDTO);

        verify(helper, times(1)).getLinkedGLAccountForLoanProduct(1L, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), 1L);
        verify(helper, times(1)).getLinkedGLAccountForLoanProduct(1L, AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), 1L);
    }

    @Test
    void shouldCreateJournalEntriesForChargeOffWithoutFraud() {
        loanDTO.setMarkedAsFraud(false);

        when(helper.getChargeOffMappingByCodeValue(chargeOffReasons)).thenReturn(null);

        GLAccount loanPortfolioGLAccount = new GLAccount();
        loanPortfolioGLAccount.setId(20L);
        loanPortfolioGLAccount.setName("Loan Portfolio Account");
        loanPortfolioGLAccount.setGlCode("54321");

        GLAccount expenseGLAccount = new GLAccount();
        expenseGLAccount.setId(40L);
        expenseGLAccount.setName("Expense Account");
        expenseGLAccount.setGlCode("67890");

        when(helper.getLinkedGLAccountForLoanProduct(1L, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), 1L))
                .thenReturn(loanPortfolioGLAccount);

        when(helper.getLinkedGLAccountForLoanProduct(1L, AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), 1L))
                .thenReturn(expenseGLAccount);

        processor.createJournalEntriesForLoan(loanDTO);

        verify(helper, times(1)).getLinkedGLAccountForLoanProduct(1L, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), 1L);
        verify(helper, times(1)).getLinkedGLAccountForLoanProduct(1L, AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), 1L);
    }
}
