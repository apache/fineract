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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Undoing a payment on a recurring (weekly/monthly/annual) savings fee. A partial payment leaves the cycle open - pay()
 * only advances the due date once the outstanding reaches zero - so undoing it must give the amount back to that cycle
 * without resetting the outstanding to the full fee or rolling the due date back a cycle.
 */
class SavingsAccountChargeUndoPaymentTest {

    private static final BigDecimal FEE = new BigDecimal("45.00");
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 11, 20);

    private final MonetaryCurrency currency = new MonetaryCurrency("USD", 2, 0);

    private MockedStatic<MoneyHelper> moneyHelperStatic;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        moneyHelperStatic = Mockito.mockStatic(MoneyHelper.class, Mockito.CALLS_REAL_METHODS);
        moneyHelperStatic.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
        moneyHelperStatic.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterEach
    void tearDown() {
        moneyHelperStatic.close();
        ThreadLocalContextUtil.reset();
    }

    @Test
    void undoingOnePartialPaymentKeepsTheOtherAndTheDueDate() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("1.70"));
        charge.pay(currency, money("1.70"));

        charge.undoPayment(currency, money("1.70"));

        assertThat(amountPaid(charge)).isEqualByComparingTo("1.70");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo("43.30");
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
        assertThat(charge.isPaid()).isFalse();
    }

    @Test
    void undoingTheOnlyPartialPaymentRestoresTheFullFeeOnTheSameCycle() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("10.00"));

        charge.undoPayment(currency, money("10.00"));

        assertThat(amountPaid(charge)).isEqualByComparingTo("0");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo(FEE);
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
    }

    @Test
    void undoingAPartialPaymentAfterAnEarlierWaivedCycleStaysOnTheOpenCycle() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.waive(currency);
        charge.pay(currency, money("5.00"));

        charge.undoPayment(currency, money("5.00"));

        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo(FEE);
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE.plusWeeks(1));
    }

    @Test
    void undoingTheFullCyclePaymentStillRollsTheDueDateBack() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("45.00"));
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE.plusWeeks(1));

        charge.undoPayment(currency, money("45.00"));

        assertThat(amountPaid(charge)).isEqualByComparingTo("0");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo(FEE);
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
        assertThat(charge.isPaid()).isFalse();
    }

    private SavingsAccountCharge weeklyFee() {
        final Charge chargeDefinition = mock(Charge.class);
        when(chargeDefinition.isPenalty()).thenReturn(false);
        when(chargeDefinition.getChargeTimeType()).thenReturn(ChargeTimeType.WEEKLY_FEE.getValue());
        when(chargeDefinition.getChargeCalculation()).thenReturn(ChargeCalculationType.FLAT.getValue());
        when(chargeDefinition.feeInterval()).thenReturn(1);
        final SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getCurrency()).thenReturn(currency);
        final JsonCommand command = mock(JsonCommand.class);
        when(command.bigDecimalValueOfParameterNamed(SavingsApiConstants.amountParamName)).thenReturn(FEE);
        when(command.localDateValueOfParameterNamed(SavingsApiConstants.dueAsOfDateParamName)).thenReturn(DUE_DATE);
        when(command.integerValueOfParameterNamed(SavingsApiConstants.feeIntervalParamName)).thenReturn(1);
        return SavingsAccountCharge.createNewFromJson(savingsAccount, chargeDefinition, command);
    }

    private Money money(final String amount) {
        return Money.of(currency, new BigDecimal(amount));
    }

    // amountPaid has no public accessor.
    private static BigDecimal amountPaid(final SavingsAccountCharge charge) {
        try {
            final Field field = SavingsAccountCharge.class.getDeclaredField("amountPaid");
            field.setAccessible(true);
            final BigDecimal value = (BigDecimal) field.get(charge);
            return value == null ? BigDecimal.ZERO : value;
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
