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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SavingsAccountChargeTest {

    @Test
    void update_percentOfAmount_doesNotThrowAndRecalculatesDerivedFields() {
        final SavingsAccountCharge charge = withdrawalFeeCharge();

        final Map<String, Object> changes = charge.update(command("""
                {"amount": 7.5, "locale": "en"}
                """));

        assertThat(changes).containsEntry("amount", new BigDecimal("7.5"));
        assertThat(percentage(charge)).isEqualByComparingTo("7.5");
        assertThat(amountPercentageAppliedTo(charge)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(amount(charge)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(amountOutstanding(charge)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void update_flat_appliesNewAmountAndRecalculatesOutstanding() {
        final SavingsAccountCharge charge = withdrawalFeeCharge();
        setField(charge, "chargeCalculation", ChargeCalculationType.FLAT.getValue());
        setField(charge, "amount", new BigDecimal("100"));
        setField(charge, "amountOutstanding", new BigDecimal("100"));

        final Map<String, Object> changes = charge.update(command("""
                {"amount": 200, "locale": "en"}
                """));

        assertThat(changes).containsEntry("amount", new BigDecimal("200"));
        assertThat(amount(charge)).isEqualByComparingTo("200");
        assertThat(amountOutstanding(charge)).isEqualByComparingTo("200");
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 3, 4, 5 })
    void update_unsupportedSavingsCalculationType_isRejectedInsteadOfSilentlyIgnored(final int legacyCalculationType) {
        final SavingsAccountCharge charge = withdrawalFeeCharge();
        setField(charge, "chargeCalculation", legacyCalculationType);
        final BigDecimal percentageBefore = percentage(charge);
        final BigDecimal amountBefore = amount(charge);

        assertThatThrownBy(() -> charge.update(command("""
                {"amount": 7.5, "locale": "en"}
                """))).isInstanceOf(PlatformApiDataValidationException.class).satisfies(exception -> {
            final PlatformApiDataValidationException validationException = (PlatformApiDataValidationException) exception;
            assertThat(validationException.getErrors()).extracting(ApiParameterError::getUserMessageGlobalisationCode)
                    .containsExactly("validation.msg.charges.not.allowed.charge.calculation.type.for.savings");
        });

        assertThat(percentage(charge)).isEqualByComparingTo(percentageBefore);
        assertThat(amount(charge)).isEqualByComparingTo(amountBefore);
    }

    @Test
    void update_unsupportedSavingsCalculationType_viaFourArgOverload_isRejected() {
        final SavingsAccountCharge charge = withdrawalFeeCharge();
        setField(charge, "chargeCalculation", ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue());

        assertThatThrownBy(() -> charge.update(new BigDecimal("7.5"), null, null, null))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void update_dueDateOnlyOnLegacyUnsupportedCalculationType_doesNotTriggerCalculationTypeGuard() {
        final SavingsAccountCharge charge = withdrawalFeeCharge();
        setField(charge, "chargeCalculation", ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue());

        assertThatCode(() -> charge.update(command("""
                {"dueDate": "01 January 2025", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """))).doesNotThrowAnyException();

        assertThat(charge.getDueDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    private static SavingsAccountCharge withdrawalFeeCharge() {
        final Charge chargeDefinition = mock(Charge.class);
        when(chargeDefinition.getChargeTimeType()).thenReturn(ChargeTimeType.WITHDRAWAL_FEE.getValue());
        when(chargeDefinition.getChargeCalculation()).thenReturn(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue());

        return SavingsAccountCharge.createNewWithoutSavingsAccount(chargeDefinition, new BigDecimal("5"), ChargeTimeType.WITHDRAWAL_FEE,
                ChargeCalculationType.PERCENT_OF_AMOUNT, null, true, null, null);
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), "savingsaccountcharges", 1L, null, null, null,
                null, null, null, null, null, null, null, null, null);
    }

    private static BigDecimal percentage(final SavingsAccountCharge charge) {
        return getField(charge, "percentage");
    }

    private static BigDecimal amountPercentageAppliedTo(final SavingsAccountCharge charge) {
        return getField(charge, "amountPercentageAppliedTo");
    }

    private static BigDecimal amount(final SavingsAccountCharge charge) {
        return getField(charge, "amount");
    }

    private static BigDecimal amountOutstanding(final SavingsAccountCharge charge) {
        return getField(charge, "amountOutstanding");
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(final Object target, final String fieldName) {
        try {
            final Field field = SavingsAccountCharge.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read field " + fieldName, e);
        }
    }

    private static void setField(final Object target, final String fieldName, final Object value) {
        try {
            final Field field = SavingsAccountCharge.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set field " + fieldName, e);
        }
    }
}
