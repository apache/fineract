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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanAddChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanDeleteChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanUpdateChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.service.LoanDownPaymentFeeEventNotifier.FeeState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class LoanDownPaymentFeeEventNotifierTest {

    private static final LocalDate JAN_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate JAN_20 = LocalDate.of(2026, 1, 20);

    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);

    private final MonetaryCurrency currency = MonetaryCurrency.fromCurrencyData(new CurrencyData("EUR"));
    private final BusinessEventNotifierService businessEventNotifierService = mock(BusinessEventNotifierService.class);
    private final LoanDownPaymentFeeEventNotifier underTest = new LoanDownPaymentFeeEventNotifier(businessEventNotifierService);
    private final Set<LoanCharge> charges = new HashSet<>();
    private final Loan loan = mock(Loan.class);

    @BeforeAll
    public static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    public static void destruct() {
        MONEY_HELPER.close();
    }

    @Test
    public void testEachChangedFeeRaisesOneEventOfItsKind() {
        when(loan.getLoanCharges()).thenReturn(charges);
        when(loan.getCurrency()).thenReturn(currency);
        mockFee(1L, "10", JAN_1, false, true); // unchanged
        final LoanCharge paid = mockFee(2L, "10", JAN_1, false, true);
        final LoanCharge recalculated = mockFee(3L, "10", JAN_1, false, true);
        final LoanCharge moved = mockFee(4L, "10", JAN_20, false, true);
        final LoanCharge deactivated = mockFee(5L, "10", JAN_20, false, true);
        final LoanCharge reactivated = mockFee(6L, "10", JAN_1, false, false);
        mockFee(7L, "10", JAN_1, false, false); // stays inactive
        final LoanCharge notDownPaymentFee = mockFee(8L, "10", JAN_1, false, true);
        when(notDownPaymentFee.isDueAtDownPayment()).thenReturn(false);

        final LoanCharge deleted = mockFee(11L, "10", JAN_20, false, true);
        final LoanCharge deletedInactive = mockFee(12L, "10", JAN_20, false, false);

        final Map<Long, FeeState> before = underTest.captureFees(loan);
        charges.remove(deleted);
        charges.remove(deletedInactive);
        when(paid.isPaid()).thenReturn(true);
        stubAmount(recalculated, "25");
        when(moved.getDueLocalDate()).thenReturn(JAN_1);
        when(deactivated.isActive()).thenReturn(false);
        when(reactivated.isActive()).thenReturn(true);
        stubAmount(notDownPaymentFee, "30");
        mockFee(9L, "10", JAN_20, false, true); // created
        mockFee(10L, "10", JAN_20, false, false); // created and deactivated in the same command
        underTest.notifyFeeChanges(loan, before);

        final ArgumentCaptor<BusinessEvent<?>> events = ArgumentCaptor.captor();
        verify(businessEventNotifierService, times(7)).notifyPostBusinessEvent(events.capture());
        final List<String> raised = events.getAllValues().stream()
                .map(event -> event.getClass().getSimpleName() + ":" + ((LoanCharge) event.get()).getId()).sorted().toList();
        assertEquals(List.of(LoanAddChargeBusinessEvent.class.getSimpleName() + ":9",
                LoanDeleteChargeBusinessEvent.class.getSimpleName() + ":11", LoanDeleteChargeBusinessEvent.class.getSimpleName() + ":5",
                LoanUpdateChargeBusinessEvent.class.getSimpleName() + ":2",
                LoanUpdateChargeBusinessEvent.class.getSimpleName() + ":3", LoanUpdateChargeBusinessEvent.class.getSimpleName() + ":4",
                LoanUpdateChargeBusinessEvent.class.getSimpleName() + ":6"), raised);
    }

    @Test
    public void testAmountWithAnotherScaleIsNotAChange() {
        when(loan.getLoanCharges()).thenReturn(charges);
        when(loan.getCurrency()).thenReturn(currency);
        final LoanCharge fee = mockFee(1L, "10", JAN_1, false, true);

        final Map<Long, FeeState> before = underTest.captureFees(loan);
        stubAmount(fee, "10.000000");
        underTest.notifyFeeChanges(loan, before);

        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    private LoanCharge mockFee(final Long id, final String amount, final LocalDate dueDate, final boolean paid, final boolean active) {
        final LoanCharge fee = mock(LoanCharge.class);
        when(fee.getId()).thenReturn(id);
        when(fee.getLoan()).thenReturn(loan);
        when(fee.isDueAtDownPayment()).thenReturn(true);
        when(fee.getDueLocalDate()).thenReturn(dueDate);
        when(fee.isPaid()).thenReturn(paid);
        when(fee.isActive()).thenReturn(active);
        stubAmount(fee, amount);
        charges.add(fee);
        return fee;
    }

    private void stubAmount(final LoanCharge fee, final String amount) {
        final Money money = Money.of(currency, new BigDecimal(amount));
        when(fee.getAmount(currency)).thenReturn(money);
    }
}
