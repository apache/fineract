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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SavingsAccountFreeWithdrawalResetTest {

    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, 0);
    private static final LocalDate ACTIVATED_ON = LocalDate.of(2026, 1, 1);
    private static final BigDecimal FEE = new BigDecimal("10");
    private static final BigDecimal WITHDRAWAL_AMOUNT = new BigDecimal("100");

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        MoneyHelper.initializeTenantRoundingMode("default", 6); // HALF_EVEN
        setBusinessDate(ACTIVATED_ON);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
        MoneyHelper.clearCache();
    }

    @Test
    void daysRestartPeriod_elapsed_restartsFreeWithdrawalCount() {
        final SavingsAccountCharge charge = freeWithdrawalFeeCharge(7, PeriodFrequencyType.DAYS);
        final SavingsAccount account = activeAccountWith(charge);

        useUpFreeWithdrawals(account, charge);

        // 14 days after activation, more than the 7-day restart period: the count must restart
        final LocalDate afterRestartPeriod = LocalDate.of(2026, 1, 15);
        assertThat(withdrawOn(account, afterRestartPeriod)).as("fee charged on %s", afterRestartPeriod).isEqualByComparingTo("0");
        assertThat(charge.getFreeWithdrawalCount()).isEqualTo(1);
        assertThat(charge.getResetChargeDate()).isEqualTo(afterRestartPeriod);
    }

    @Test
    void monthsRestartPeriod_elapsed_restartsFreeWithdrawalCount() {
        final SavingsAccountCharge charge = freeWithdrawalFeeCharge(1, PeriodFrequencyType.MONTHS);
        final SavingsAccount account = activeAccountWith(charge);

        useUpFreeWithdrawals(account, charge);

        // the next calendar month, past the 1-month restart period: the count must restart
        final LocalDate afterRestartPeriod = LocalDate.of(2026, 2, 15);
        assertThat(withdrawOn(account, afterRestartPeriod)).as("fee charged on %s", afterRestartPeriod).isEqualByComparingTo("0");
        assertThat(charge.getFreeWithdrawalCount()).isEqualTo(1);
        assertThat(charge.getResetChargeDate()).isEqualTo(afterRestartPeriod);
    }

    private void useUpFreeWithdrawals(final SavingsAccount account, final SavingsAccountCharge charge) {
        assertThat(withdrawOn(account, LocalDate.of(2026, 1, 2))).isEqualByComparingTo("0");
        assertThat(charge.getFreeWithdrawalCount()).isEqualTo(1);

        assertThat(withdrawOn(account, LocalDate.of(2026, 1, 3))).isEqualByComparingTo("0");
        assertThat(charge.getFreeWithdrawalCount()).isEqualTo(2);

        assertThat(withdrawOn(account, LocalDate.of(2026, 1, 4))).isEqualByComparingTo(FEE);
        assertThat(charge.getFreeWithdrawalCount()).isEqualTo(2);
    }

    /**
     * Runs the withdrawal-fee step of {@link SavingsAccount#withdraw} on the given business date and returns the
     * withdrawal fee it posted.
     */
    private static BigDecimal withdrawOn(final SavingsAccount account, final LocalDate date) {
        setBusinessDate(date);
        final BigDecimal feesBefore = postedWithdrawalFees(account);
        try {
            final Method payWithdrawalFee = SavingsAccount.class.getDeclaredMethod("payWithdrawalFee", BigDecimal.class, LocalDate.class,
                    PaymentDetail.class, boolean.class, String.class);
            payWithdrawalFee.setAccessible(true);
            payWithdrawalFee.invoke(account, WITHDRAWAL_AMOUNT, date, null, false, "ref-" + date);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("payWithdrawalFee failed on " + date, e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to call payWithdrawalFee", e);
        }
        return postedWithdrawalFees(account).subtract(feesBefore);
    }

    private static BigDecimal postedWithdrawalFees(final SavingsAccount account) {
        return account.getTransactions().stream().filter(SavingsAccountTransaction::isWithdrawalFee)
                .map(SavingsAccountTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static SavingsAccountCharge freeWithdrawalFeeCharge(final int restartCountFrequency,
            final PeriodFrequencyType countFrequencyType) {
        final Charge chargeDefinition = mock(Charge.class);
        when(chargeDefinition.getChargeTimeType()).thenReturn(ChargeTimeType.WITHDRAWAL_FEE.getValue());
        when(chargeDefinition.getChargeCalculation()).thenReturn(ChargeCalculationType.FLAT.getValue());
        when(chargeDefinition.isEnableFreeWithdrawal()).thenReturn(true);
        when(chargeDefinition.getFrequencyFreeWithdrawalCharge()).thenReturn(2);
        when(chargeDefinition.getRestartFrequency()).thenReturn(restartCountFrequency);
        when(chargeDefinition.getRestartFrequencyEnum()).thenReturn(countFrequencyType.getValue());

        return SavingsAccountCharge.createNewWithoutSavingsAccount(chargeDefinition, FEE, ChargeTimeType.WITHDRAWAL_FEE,
                ChargeCalculationType.FLAT, null, true, null, null);
    }

    private static SavingsAccount activeAccountWith(final SavingsAccountCharge charge) {
        final SavingsAccount account = new SavingsAccount();
        setField(account, "activatedOnDate", ACTIVATED_ON);
        setField(account, "currency", CURRENCY);
        final HashSet<SavingsAccountCharge> charges = new HashSet<>();
        charges.add(charge);
        setField(account, "charges", charges);
        return account;
    }

    private static void setBusinessDate(final LocalDate date) {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, date)));
    }

    private static void setField(final Object target, final String fieldName, final Object value) {
        try {
            final Field field = SavingsAccount.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set field " + fieldName, e);
        }
    }
}
