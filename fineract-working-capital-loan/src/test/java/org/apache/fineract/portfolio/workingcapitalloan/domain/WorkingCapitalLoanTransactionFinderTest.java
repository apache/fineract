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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionDateAndAmountHolder;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class WorkingCapitalLoanTransactionFinderTest {

    private static final Long LOAN_ID = 42L;

    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;

    @InjectMocks
    private WorkingCapitalLoanTransactionFinder finder;

    @Captor
    private ArgumentCaptor<List<LoanTransactionType>> typesCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    public void findLastPayment_countsEveryPaymentLikeType_includingGoodwillCredit() {
        when(transactionRepository.findActiveByTypesOrderByDateDesc(eq(LOAN_ID), anyList(), any())).thenReturn(List.of());

        finder.findLastPayment(LOAN_ID);

        verifyQueriedTypes();
        assertThat(typesCaptor.getValue()).contains(LoanTransactionType.REPAYMENT, LoanTransactionType.GOODWILL_CREDIT,
                LoanTransactionType.PAYOUT_REFUND, LoanTransactionType.CHARGE_ADJUSTMENT);
    }

    @Test
    public void findLastRepayment_countsRepaymentOnly_soAGoodwillCreditDoesNotMoveIt() {
        when(transactionRepository.findActiveByTypesOrderByDateDesc(eq(LOAN_ID), anyList(), any())).thenReturn(List.of());

        finder.findLastRepayment(LOAN_ID);

        verifyQueriedTypes();
        assertThat(typesCaptor.getValue()).containsExactly(LoanTransactionType.REPAYMENT);
    }

    @Test
    public void lookups_askForASingleRowAndTakeIt() {
        final TransactionDateAndAmountHolder latest = new TransactionDateAndAmountHolder(LocalDate.of(2026, 1, 20),
                new BigDecimal("30.00"));
        final TransactionDateAndAmountHolder older = new TransactionDateAndAmountHolder(LocalDate.of(2026, 1, 20), new BigDecimal("20.00"));
        when(transactionRepository.findActiveByTypesOrderByDateDesc(eq(LOAN_ID), anyList(), any())).thenReturn(List.of(latest, older));

        assertThat(finder.findLastPayment(LOAN_ID)).contains(latest);

        verifyQueriedTypes();
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    public void lookups_areEmptyWhenTheLoanHasNoMatchingTransaction() {
        when(transactionRepository.findActiveByTypesOrderByDateDesc(eq(LOAN_ID), anyList(), any())).thenReturn(List.of());

        assertThat(finder.findLastPayment(LOAN_ID)).isEmpty();
        assertThat(finder.findLastRepayment(LOAN_ID)).isEqualTo(Optional.empty());
    }

    private void verifyQueriedTypes() {
        verify(transactionRepository).findActiveByTypesOrderByDateDesc(eq(LOAN_ID), typesCaptor.capture(), pageableCaptor.capture());
    }
}
