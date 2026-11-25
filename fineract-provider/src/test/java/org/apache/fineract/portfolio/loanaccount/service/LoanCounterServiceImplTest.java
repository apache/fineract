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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanCounterServiceImplTest {

    private static final Long CLIENT_ID = 1L;
    private static final Long GROUP_ID = 2L;
    private static final Long PRODUCT_ID = 3L;
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2025, 5, 20);

    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;

    @InjectMocks
    private LoanCounterServiceImpl loanCounterService;

    private Loan loan;
    private LoanProduct loanProduct;

    @BeforeEach
    public void setUp() {
        loan = mock(Loan.class);
        loanProduct = mock(LoanProduct.class);
        when(loan.loanProduct()).thenReturn(loanProduct);
    }

    @Test
    public void updateLoanCounters_clientLoanWithoutLaterLoans_shouldSetNextCounters() {
        when(loan.isGroupLoan()).thenReturn(false);
        when(loan.getClientId()).thenReturn(CLIENT_ID);
        when(loanProduct.getId()).thenReturn(PRODUCT_ID);
        when(loanProduct.isIncludeInBorrowerCycle()).thenReturn(true);
        when(loanRepositoryWrapper.getClientOrJLGLoansDisbursedAfter(DISBURSEMENT_DATE, CLIENT_ID)).thenReturn(List.of());
        when(loanRepositoryWrapper.getMaxClientOrJLGLoanCounter(CLIENT_ID)).thenReturn(3);
        when(loanRepositoryWrapper.getMaxClientOrJLGLoanProductCounter(PRODUCT_ID, CLIENT_ID)).thenReturn(null);

        loanCounterService.updateLoanCounters(loan, DISBURSEMENT_DATE);

        verify(loan).updateClientLoanCounter(4);
        verify(loan).updateLoanProductLoanCounter(1);
        verify(loanRepositoryWrapper).save(List.of());
    }

    @Test
    public void updateLoanCounters_groupLoanNotInBorrowerCycle_shouldClearClientCounter() {
        when(loan.isGroupLoan()).thenReturn(true);
        when(loan.getGroupId()).thenReturn(GROUP_ID);
        when(loanProduct.getId()).thenReturn(PRODUCT_ID);
        when(loanProduct.isIncludeInBorrowerCycle()).thenReturn(false);
        when(loanRepositoryWrapper.getGroupLoansDisbursedAfter(DISBURSEMENT_DATE, GROUP_ID, AccountType.GROUP)).thenReturn(List.of());
        when(loanRepositoryWrapper.getMaxGroupLoanCounter(GROUP_ID, AccountType.GROUP)).thenReturn(null);
        when(loanRepositoryWrapper.getMaxGroupLoanProductCounter(PRODUCT_ID, GROUP_ID, AccountType.GROUP)).thenReturn(5);

        loanCounterService.updateLoanCounters(loan, DISBURSEMENT_DATE);

        verify(loan).updateClientLoanCounter(null);
        verify(loan).updateLoanProductLoanCounter(6);
        verify(loanRepositoryWrapper).save(List.of());
    }

    @Test
    public void removeLoanCycle_clientLoan_shouldShiftLaterLoansDown() {
        final Loan laterLoan = mock(Loan.class);
        when(laterLoan.loanProduct()).thenReturn(loanProduct);
        when(laterLoan.getCurrentLoanCounter()).thenReturn(3);
        when(laterLoan.getLoanProductLoanCounter()).thenReturn(2);
        when(loan.isGroupLoan()).thenReturn(false);
        when(loan.getClientId()).thenReturn(CLIENT_ID);
        when(loan.getCurrentLoanCounter()).thenReturn(2);
        when(loan.getLoanProductLoanCounter()).thenReturn(1);
        when(loanProduct.getId()).thenReturn(PRODUCT_ID);
        when(loanProduct.isIncludeInBorrowerCycle()).thenReturn(true);
        when(loanRepositoryWrapper.getClientOrJLGLoansToUpdateLoanCounter(2, CLIENT_ID)).thenReturn(List.of(laterLoan));

        loanCounterService.removeLoanCycle(loan);

        verify(laterLoan).updateClientLoanCounter(2);
        verify(laterLoan).updateLoanProductLoanCounter(1);
        verify(loanRepositoryWrapper).save(List.of(laterLoan));
        verify(loan).updateClientLoanCounter(null);
        verify(loan).updateLoanProductLoanCounter(null);
    }
}
