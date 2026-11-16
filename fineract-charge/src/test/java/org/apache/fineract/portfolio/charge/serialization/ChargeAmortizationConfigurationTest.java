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
package org.apache.fineract.portfolio.charge.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationIncomeType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationPeriodType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Amortization configuration on the charge definition: the defaults kept by {@link Charge} and the rules checked by
 * {@link ChargeDefinitionCommandFromApiJsonDeserializer}, run in the order the charge write service runs them.
 */
class ChargeAmortizationConfigurationTest {

    private static final int LOAN = 1;
    private static final int SAVINGS = 2;

    private final ChargeDefinitionCommandFromApiJsonDeserializer validator = new ChargeDefinitionCommandFromApiJsonDeserializer(
            new FromJsonHelper());

    @Test
    void createWithoutAmortizationFields_defaultsToNone() {
        final Charge charge = create(loanFee(ChargeTimeType.DISBURSEMENT, ""));

        assertEquals(ChargeAmortizationPeriodType.NONE, charge.getAmortizationPeriodType());
        assertFalse(charge.isAmortized());
        assertNull(charge.getAmortizationDays());
        assertNull(charge.getAmortizationCalculationType());
        assertNull(charge.getAmortizationIncomeType());
    }

    @Test
    void createFullLoanTerm_defaultsToFlatFee() {
        final Charge charge = create(loanFee(ChargeTimeType.DISBURSEMENT, "\"amortizationPeriodType\": \"FULL_LOAN_TERM\""));

        assertEquals(ChargeAmortizationPeriodType.FULL_LOAN_TERM, charge.getAmortizationPeriodType());
        assertNull(charge.getAmortizationDays());
        assertEquals(ChargeAmortizationCalculationType.FLAT, charge.getAmortizationCalculationType());
        assertEquals(ChargeAmortizationIncomeType.FEE, charge.getAmortizationIncomeType());
    }

    @Test
    void createNumberOfDays_keepsTheGivenConfiguration() {
        final Charge charge = create(loanFee(ChargeTimeType.SPECIFIED_DUE_DATE, """
                "amortizationPeriodType": "NUMBER_OF_DAYS", "amortizationDays": 30, "amortizationCalculationType": "FLAT",
                "amortizationIncomeType": "INTEREST"
                """));

        assertEquals(ChargeAmortizationPeriodType.NUMBER_OF_DAYS, charge.getAmortizationPeriodType());
        assertEquals(30, charge.getAmortizationDays());
        assertEquals(ChargeAmortizationCalculationType.FLAT, charge.getAmortizationCalculationType());
        assertEquals(ChargeAmortizationIncomeType.INTEREST, charge.getAmortizationIncomeType());
    }

    @Test
    void createFullLoanTermEffectiveRate_isAccepted() {
        final Charge charge = create(loanFee(ChargeTimeType.TRANCHE_DISBURSEMENT,
                "\"amortizationPeriodType\": \"FULL_LOAN_TERM\", \"amortizationCalculationType\": \"EFFECTIVE_RATE\""));

        assertEquals(ChargeAmortizationCalculationType.EFFECTIVE_RATE, charge.getAmortizationCalculationType());
    }

    @Test
    void createEnumValues_areReadIgnoringCase() {
        final Charge charge = create(loanFee(ChargeTimeType.DISBURSEMENT,
                "\"amortizationPeriodType\": \"full_loan_term\", \"amortizationIncomeType\": \"interest\""));

        assertEquals(ChargeAmortizationPeriodType.FULL_LOAN_TERM, charge.getAmortizationPeriodType());
        assertEquals(ChargeAmortizationIncomeType.INTEREST, charge.getAmortizationIncomeType());
    }

    @Test
    void createNumberOfDaysWithoutDays_isRejected() {
        assertRejected(List.of("validation.msg.charge.amortizationDays.cannot.be.blank"),
                () -> create(loanFee(ChargeTimeType.DISBURSEMENT, "\"amortizationPeriodType\": \"NUMBER_OF_DAYS\"")));
    }

    @Test
    void createNumberOfDaysWithZeroDays_isRejected() {
        assertRejected(List.of("validation.msg.charge.amortizationDays.not.greater.than.zero"), () -> create(
                loanFee(ChargeTimeType.DISBURSEMENT, "\"amortizationPeriodType\": \"NUMBER_OF_DAYS\", \"amortizationDays\": 0")));
    }

    @Test
    void createWithUnknownPeriodType_isRejected() {
        assertRejected(List.of("validation.msg.charge.amortizationPeriodType.is.not.one.of.expected.enumerations"),
                () -> create(loanFee(ChargeTimeType.DISBURSEMENT, "\"amortizationPeriodType\": \"WEEKLY\"")));
    }

    @Test
    void createAmortizedPenalty_isRejected() {
        assertRejected(List.of("validation.msg.charge.amortizationPeriodType.not.supported.for.penalty"),
                () -> create(loanCharge(ChargeTimeType.SPECIFIED_DUE_DATE, true, "\"amortizationPeriodType\": \"FULL_LOAN_TERM\"")));
    }

    @ParameterizedTest
    @EnumSource(value = ChargeTimeType.class, names = { "INSTALMENT_FEE", "DOWN_PAYMENT" })
    void createAmortizedFeeWithNotAllowedChargeTime_isRejected(final ChargeTimeType chargeTimeType) {
        assertRejected(List.of("validation.msg.charge.amortizationPeriodType.not.supported.for.charge.time.type"),
                () -> create(loanFee(chargeTimeType, "\"amortizationPeriodType\": \"FULL_LOAN_TERM\"")));
    }

    @ParameterizedTest
    @EnumSource(value = ChargeTimeType.class, names = { "DISBURSEMENT", "TRANCHE_DISBURSEMENT", "SPECIFIED_DUE_DATE" })
    void createAmortizedFeeWithAllowedChargeTime_isAccepted(final ChargeTimeType chargeTimeType) {
        final Charge charge = create(loanFee(chargeTimeType, "\"amortizationPeriodType\": \"FULL_LOAN_TERM\""));

        assertEquals(ChargeAmortizationPeriodType.FULL_LOAN_TERM, charge.getAmortizationPeriodType());
    }

    @Test
    void createAmortizedOverduePenalty_isRejectedForPenaltyAndChargeTime() {
        assertRejected(
                List.of("validation.msg.charge.amortizationPeriodType.not.supported.for.penalty",
                        "validation.msg.charge.amortizationPeriodType.not.supported.for.charge.time.type"),
                () -> create(loanCharge(ChargeTimeType.OVERDUE_INSTALLMENT, true, "\"amortizationPeriodType\": \"FULL_LOAN_TERM\"")));
    }

    @Test
    void createAmortizedSavingsCharge_isRejected() {
        assertRejected(List.of("validation.msg.charge.amortizationPeriodType.supported.only.for.loan.charge"),
                () -> create("{\"name\": \"savings-fee\", \"amount\": 20, \"currencyCode\": \"USD\", \"chargeAppliesTo\": " + SAVINGS
                        + ", \"chargeTimeType\": 2, \"chargeCalculationType\": 1, \"penalty\": false, \"active\": true, \"locale\": \"en\","
                        + " \"amortizationPeriodType\": \"FULL_LOAN_TERM\"}"));
    }

    @Test
    void createEffectiveRateWithNumberOfDays_isRejected() {
        assertRejected(List.of("validation.msg.charge.amortizationCalculationType.effective.rate.supported.only.for.full.loan.term"),
                () -> create(loanFee(ChargeTimeType.DISBURSEMENT, """
                        "amortizationPeriodType": "NUMBER_OF_DAYS", "amortizationDays": 30,
                        "amortizationCalculationType": "EFFECTIVE_RATE"
                        """)));
    }

    @Test
    void createNoneWithCalculationAndIncomeType_isRejected() {
        assertRejected(
                List.of("validation.msg.charge.amortizationCalculationType.supported.only.for.amortized.charge",
                        "validation.msg.charge.amortizationIncomeType.supported.only.for.amortized.charge"),
                () -> create(loanFee(ChargeTimeType.DISBURSEMENT, """
                        "amortizationPeriodType": "NONE", "amortizationCalculationType": "FLAT", "amortizationIncomeType": "FEE"
                        """)));
    }

    @Test
    void createFullLoanTermWithDays_isRejected() {
        assertRejected(List.of("validation.msg.charge.amortizationDays.supported.only.for.number.of.days.period.type"), () -> create(
                loanFee(ChargeTimeType.DISBURSEMENT, "\"amortizationPeriodType\": \"FULL_LOAN_TERM\", \"amortizationDays\": 30")));
    }

    @Test
    void updateNoneToFullLoanTerm_appliesTheDefaultsAndReportsThem() {
        final Charge charge = create(loanFee(ChargeTimeType.DISBURSEMENT, ""));

        final Map<String, Object> changes = update(charge, "{\"amortizationPeriodType\": \"FULL_LOAN_TERM\"}");

        assertEquals(
                Map.of("amortizationPeriodType", "FULL_LOAN_TERM", "amortizationCalculationType", "FLAT", "amortizationIncomeType", "FEE"),
                changes);
        assertEquals(ChargeAmortizationCalculationType.FLAT, charge.getAmortizationCalculationType());
        assertEquals(ChargeAmortizationIncomeType.FEE, charge.getAmortizationIncomeType());
    }

    @Test
    void updateNumberOfDaysToNone_clearsTheConfiguration() {
        final Charge charge = numberOfDaysInterestFee();

        final Map<String, Object> changes = update(charge, "{\"amortizationPeriodType\": \"NONE\"}");

        assertEquals("NONE", changes.get("amortizationPeriodType"));
        assertEquals(ChargeAmortizationPeriodType.NONE, charge.getAmortizationPeriodType());
        assertNull(charge.getAmortizationDays());
        assertNull(charge.getAmortizationCalculationType());
        assertNull(charge.getAmortizationIncomeType());
    }

    @Test
    void updateNumberOfDaysToFullLoanTerm_clearsTheDaysAndKeepsTheRest() {
        final Charge charge = numberOfDaysInterestFee();

        final Map<String, Object> changes = update(charge, "{\"amortizationPeriodType\": \"FULL_LOAN_TERM\"}");

        assertEquals("FULL_LOAN_TERM", changes.get("amortizationPeriodType"));
        assertNull(charge.getAmortizationDays());
        assertEquals(ChargeAmortizationCalculationType.FLAT, charge.getAmortizationCalculationType());
        assertEquals(ChargeAmortizationIncomeType.INTEREST, charge.getAmortizationIncomeType());
    }

    @Test
    void updateDaysOnly_isReported() {
        final Charge charge = numberOfDaysInterestFee();

        final Map<String, Object> changes = update(charge, "{\"amortizationDays\": 60}");

        assertEquals(Map.of("amortizationDays", 60), changes);
        assertEquals(60, charge.getAmortizationDays());
    }

    @Test
    void updateDaysOnFullLoanTermCharge_isRejected() {
        final Charge charge = create(loanFee(ChargeTimeType.DISBURSEMENT, "\"amortizationPeriodType\": \"FULL_LOAN_TERM\""));

        assertRejected(List.of("validation.msg.charge.amortizationDays.supported.only.for.number.of.days.period.type"),
                () -> update(charge, "{\"amortizationDays\": 60}"));
    }

    @Test
    void updateAmortizedFeeToPenalty_isRejected() {
        final Charge charge = numberOfDaysInterestFee();

        assertRejected(List.of("validation.msg.charge.amortizationPeriodType.not.supported.for.penalty"),
                () -> update(charge, "{\"penalty\": true}"));
    }

    @Test
    void updateAmortizedFeeToInstalmentFee_isRejected() {
        final Charge charge = numberOfDaysInterestFee();

        assertRejected(List.of("validation.msg.charge.amortizationPeriodType.not.supported.for.charge.time.type"),
                () -> update(charge, "{\"chargeTimeType\": %d, \"locale\": \"en\"}".formatted(ChargeTimeType.INSTALMENT_FEE.getValue())));
    }

    @Test
    void updateOtherFieldsOfAnAmortizedCharge_leavesTheConfigurationAlone() {
        final Charge charge = numberOfDaysInterestFee();

        final Map<String, Object> changes = update(charge, "{\"name\": \"renamed\"}");

        assertEquals(Map.of("name", "renamed"), changes);
        assertEquals(ChargeAmortizationPeriodType.NUMBER_OF_DAYS, charge.getAmortizationPeriodType());
        assertEquals(30, charge.getAmortizationDays());
    }

    private Charge numberOfDaysInterestFee() {
        return create(loanFee(ChargeTimeType.SPECIFIED_DUE_DATE,
                "\"amortizationPeriodType\": \"NUMBER_OF_DAYS\", \"amortizationDays\": 30, \"amortizationIncomeType\": \"INTEREST\""));
    }

    private Charge create(final String json) {
        validator.validateForCreate(json);
        final Charge charge = Charge.fromJson(command(json), null, null, null);
        validator.validateAmortizationConfiguration(charge);
        return charge;
    }

    private Map<String, Object> update(final Charge charge, final String json) {
        validator.validateForUpdate(json);
        final Map<String, Object> changes = charge.update(command(json));
        validator.validateAmortizationConfiguration(charge);
        return changes;
    }

    private static String loanFee(final ChargeTimeType chargeTimeType, final String amortizationFields) {
        return loanCharge(chargeTimeType, false, amortizationFields);
    }

    private static String loanCharge(final ChargeTimeType chargeTimeType, final boolean penalty, final String amortizationFields) {
        return "{\"name\": \"loan-charge\", \"amount\": 20, \"currencyCode\": \"USD\", \"chargeAppliesTo\": " + LOAN
                + ", \"chargeTimeType\": " + chargeTimeType.getValue() + ", \"chargeCalculationType\": 1, \"chargePaymentMode\": 0,"
                + " \"penalty\": " + penalty + ", \"active\": true, \"locale\": \"en\""
                + (amortizationFields.isBlank() ? "" : ", " + amortizationFields) + "}";
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), "charges", 1L, null, null, null, null, null, null,
                null, null, null, null, null, null);
    }

    private static void assertRejected(final List<String> expectedCodes, final Runnable action) {
        final PlatformApiDataValidationException rejection = assertThrows(PlatformApiDataValidationException.class, action::run);
        assertEquals(expectedCodes, rejection.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList());
    }
}
