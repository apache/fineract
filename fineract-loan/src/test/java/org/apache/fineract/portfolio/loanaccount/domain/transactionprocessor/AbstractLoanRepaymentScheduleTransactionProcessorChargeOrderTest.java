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
package org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AbstractLoanRepaymentScheduleTransactionProcessorChargeOrderTest {

    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, null);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 3, 15);

    private static MockedStatic<MoneyHelper> moneyHelperMock;

    private final AbstractLoanRepaymentScheduleTransactionProcessor processor = mock(
            AbstractLoanRepaymentScheduleTransactionProcessor.class, CALLS_REAL_METHODS);

    @BeforeAll
    static void setUpStatics() {
        moneyHelperMock = mockStatic(MoneyHelper.class);
        moneyHelperMock.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelperMock.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    static void tearDownStatics() {
        moneyHelperMock.close();
    }

    @Test
    void sameDueDatePaysLowerIdFirstWhateverTheSetOrder() {
        LoanCharge first = unpaidCharge(1L, DUE_DATE);
        LoanCharge second = unpaidCharge(2L, DUE_DATE);

        assertThat(processor.findEarliestUnpaidChargeFromUnOrderedSet(orderedSet(second, first), CURRENCY)).isSameAs(first);
        assertThat(processor.findEarliestUnpaidChargeFromUnOrderedSet(orderedSet(first, second), CURRENCY)).isSameAs(first);
    }

    @Test
    void sameDueDatePaysUnsavedChargeAfterSavedOne() {
        LoanCharge saved = unpaidCharge(5L, DUE_DATE);
        LoanCharge unsaved = unpaidCharge(null, DUE_DATE);

        assertThat(processor.findEarliestUnpaidChargeFromUnOrderedSet(orderedSet(unsaved, saved), CURRENCY)).isSameAs(saved);
    }

    @Test
    void earlierDueDateStillWinsOverLowerId() {
        LoanCharge laterLowerId = unpaidCharge(1L, DUE_DATE.plusDays(1));
        LoanCharge earlierHigherId = unpaidCharge(2L, DUE_DATE);

        assertThat(processor.findEarliestUnpaidChargeFromUnOrderedSet(orderedSet(laterLowerId, earlierHigherId), CURRENCY))
                .isSameAs(earlierHigherId);
    }

    @Test
    void undoOnSameDueDateTakesBackHigherIdFirst() {
        LoanCharge first = paidCharge(1L, DUE_DATE);
        LoanCharge second = paidCharge(2L, DUE_DATE);
        Money amount = Money.of(CURRENCY, new BigDecimal("5.00"));
        when(second.undoPaidOrPartiallyAmountBy(any(), anyInt(), any())).thenReturn(amount);
        LoanTransaction transaction = mock(LoanTransaction.class);
        Set<LoanChargePaidBy> paidBy = new HashSet<>();
        when(transaction.getLoanChargesPaid()).thenReturn(paidBy);

        processor.undoChargesPaidAmountBy(transaction, amount, orderedSet(first, second), 1);

        verify(second).undoPaidOrPartiallyAmountBy(any(), anyInt(), any());
        verify(first, never()).undoPaidOrPartiallyAmountBy(any(), anyInt(), any());
        assertThat(paidBy).extracting(LoanChargePaidBy::getLoanCharge).containsExactly(second);
    }

    private static LoanCharge unpaidCharge(Long id, LocalDate dueDate) {
        LoanCharge charge = charge(id, dueDate);
        Money outstanding = Money.of(CURRENCY, BigDecimal.TEN);
        when(charge.getAmountOutstanding(any(MonetaryCurrency.class))).thenReturn(outstanding);
        return charge;
    }

    private static LoanCharge paidCharge(Long id, LocalDate dueDate) {
        LoanCharge charge = charge(id, dueDate);
        when(charge.isPaidOrPartiallyPaid(any(MonetaryCurrency.class))).thenReturn(true);
        return charge;
    }

    private static LoanCharge charge(Long id, LocalDate dueDate) {
        LoanCharge charge = mock(LoanCharge.class);
        when(charge.getId()).thenReturn(id);
        when(charge.getDueLocalDate()).thenReturn(dueDate);
        return charge;
    }

    private static Set<LoanCharge> orderedSet(LoanCharge... charges) {
        return new LinkedHashSet<>(List.of(charges));
    }
}
