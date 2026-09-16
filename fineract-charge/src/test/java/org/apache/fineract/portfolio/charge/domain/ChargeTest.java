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
package org.apache.fineract.portfolio.charge.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.charge.exception.ChargeDueAtDisbursementCannotBePenaltyException;
import org.junit.jupiter.api.Test;

class ChargeTest {

    private static final String PAYMENT_MODE = "chargePaymentMode";

    @Test
    void updateOnWorkingCapitalCharge_explicitNullPaymentMode_resolvesToRegular() {
        final Charge charge = workingCapitalDisbursementCharge();

        final Map<String, Object> changes = charge.update(command("{\"chargePaymentMode\": null, \"locale\": \"en\"}"));

        assertFalse(changes.containsKey(PAYMENT_MODE), "Regular is already stored, so a null resolving to Regular is no change");
        assertEquals(ChargePaymentMode.REGULAR.getValue(), charge.getChargePaymentMode());
    }

    @Test
    void updateOnWorkingCapitalCharge_explicitRegularPaymentMode_isNoChange() {
        final Charge charge = workingCapitalDisbursementCharge();

        final Map<String, Object> changes = charge.update(command("{\"chargePaymentMode\": 0, \"locale\": \"en\"}"));

        assertFalse(changes.containsKey(PAYMENT_MODE));
        assertEquals(ChargePaymentMode.REGULAR.getValue(), charge.getChargePaymentMode());
    }

    @Test
    void updateOnWorkingCapitalCharge_accountTransferPaymentMode_isRejected() {
        final Charge charge = workingCapitalDisbursementCharge();

        final PlatformApiDataValidationException rejection = assertThrows(PlatformApiDataValidationException.class,
                () -> charge.update(command("{\"chargePaymentMode\": 1, \"locale\": \"en\"}")));

        assertEquals(List.of("validation.msg.charges.chargePaymentMode.is.not.one.of.expected.enumerations"), codesOf(rejection));
        assertEquals(ChargePaymentMode.REGULAR.getValue(), charge.getChargePaymentMode(), "a rejected value is not assigned");
    }

    @Test
    void updateOnSavingsCharge_explicitNullPaymentMode_isIgnored() {
        final Charge charge = savingsSpecifiedDueDateCharge();

        final Map<String, Object> changes = charge.update(command("{\"chargePaymentMode\": null, \"locale\": \"en\"}"));

        assertFalse(changes.containsKey(PAYMENT_MODE), "a savings charge carries no payment mode, so the parameter is ignored");
    }

    @Test
    void updateOnWorkingCapitalDisbursementPenalty_amountOnly_succeeds() {
        final Charge charge = Charge.fromJson(command("""
                {"name": "wc-disbursement-penalty", "amount": 20, "currencyCode": "USD", "chargeAppliesTo": 5, "chargeTimeType": 1,
                 "chargeCalculationType": 1, "penalty": true, "active": true, "locale": "en"}
                """), null, null, null);

        final Map<String, Object> changes = charge.update(command("{\"amount\": 30, \"locale\": \"en\"}"));

        assertEquals(new BigDecimal("30"), changes.get("amount"));
    }

    @Test
    void updateOnClientPenaltyCharge_toDisbursementTimeType_isStillRejected() {
        final Charge charge = Charge.fromJson(command("""
                {"name": "client-penalty", "amount": 20, "currencyCode": "USD", "chargeAppliesTo": 3, "chargeTimeType": 2,
                 "chargeCalculationType": 1, "penalty": true, "active": true, "locale": "en"}
                """), null, null, null);

        assertThrows(ChargeDueAtDisbursementCannotBePenaltyException.class,
                () -> charge.update(command("{\"chargeTimeType\": 1, \"locale\": \"en\"}")));
    }

    @Test
    void updateOnLoanCharge_explicitNullPaymentMode_isRejected() {
        final Charge charge = loanDisbursementCharge();

        final PlatformApiDataValidationException rejection = assertThrows(PlatformApiDataValidationException.class,
                () -> charge.update(command("{\"chargePaymentMode\": null, \"locale\": \"en\"}")));

        assertEquals(List.of("validation.msg.charges.chargePaymentMode.cannot.be.blank"), codesOf(rejection));
        assertEquals(ChargePaymentMode.REGULAR.getValue(), charge.getChargePaymentMode(), "a rejected update leaves the value alone");
    }

    @Test
    void updateOnLoanCharge_paymentModeChange_isPersistedAndReported() {
        final Charge charge = loanDisbursementCharge();

        final Map<String, Object> changes = charge.update(command("{\"chargePaymentMode\": 1, \"locale\": \"en\"}"));

        assertEquals(ChargePaymentMode.ACCOUNT_TRANSFER.getValue(), changes.get(PAYMENT_MODE));
        assertEquals(ChargePaymentMode.ACCOUNT_TRANSFER.getValue(), charge.getChargePaymentMode());
    }

    private static Charge workingCapitalDisbursementCharge() {
        return Charge.fromJson(command("""
                {"name": "wc-disbursement", "amount": 20, "currencyCode": "USD", "chargeAppliesTo": 5, "chargeTimeType": 1,
                 "chargeCalculationType": 1, "penalty": false, "active": true, "locale": "en"}
                """), null, null, null);
    }

    private static Charge savingsSpecifiedDueDateCharge() {
        return Charge.fromJson(command("""
                {"name": "savings-fee", "amount": 20, "currencyCode": "USD", "chargeAppliesTo": 2, "chargeTimeType": 2,
                 "chargeCalculationType": 1, "penalty": false, "active": true, "locale": "en"}
                """), null, null, null);
    }

    private static Charge loanDisbursementCharge() {
        return Charge.fromJson(command("""
                {"name": "loan-disbursement", "amount": 20, "currencyCode": "USD", "chargeAppliesTo": 1, "chargeTimeType": 1,
                 "chargeCalculationType": 1, "chargePaymentMode": 0, "penalty": false, "active": true, "locale": "en"}
                """), null, null, null);
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), "charges", 1L, null, null, null, null, null, null,
                null, null, null, null, null, null);
    }

    private static List<String> codesOf(final PlatformApiDataValidationException rejection) {
        return rejection.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList();
    }
}
