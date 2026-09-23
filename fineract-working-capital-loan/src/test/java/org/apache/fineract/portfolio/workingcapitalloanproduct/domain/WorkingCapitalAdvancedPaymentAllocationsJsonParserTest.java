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
package org.apache.fineract.portfolio.workingcapitalloanproduct.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Arrays;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class WorkingCapitalAdvancedPaymentAllocationsJsonParserTest {

    private final WorkingCapitalAdvancedPaymentAllocationsJsonParser parser = new WorkingCapitalAdvancedPaymentAllocationsJsonParser(
            new WorkingCapitalAdvancedPaymentAllocationsValidator());

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "default", " REPAYMENT ", "repayment", "UNKNOWN", "DOWN_PAYMENT", "INTEREST_REFUND", "MERCHANT_ISSUED_REFUND",
            "CHARGE_REFUND", "WAIVE_INTEREST", "CHARGE_PAYMENT", "REFUND_FOR_ACTIVE_LOAN", "INTEREST_PAYMENT_WAIVER",
            "CAPITALIZED_INCOME_ADJUSTMENT" })
    void rejectsInvalidTransactionTypes(final String transactionType) {
        JsonObject rule = new JsonObject();
        rule.addProperty("transactionType", transactionType);
        assertInvalidTransactionType("{\"paymentAllocation\":[{\"transactionType\":\"DEFAULT\"}," + rule + "]}",
                String.valueOf(transactionType));
    }

    @Test
    void rejectsMissingTransactionType() {
        assertInvalidTransactionType("{\"paymentAllocation\":[{\"transactionType\":\"DEFAULT\"},{}]}", "null");
    }

    @Test
    void reportsInvalidTypeInsteadOfMissingDefault() {
        assertInvalidTransactionType("{\"paymentAllocation\":[{\"transactionType\":\"default\"}]}", "default");
    }

    @Test
    void reportsInvalidTypeInsteadOfDuplicateNullTypes() {
        assertInvalidTransactionType(
                "{\"paymentAllocation\":[{\"transactionType\":\"DEFAULT\"},{\"transactionType\":\"repayment\"},{\"transactionType\":\"payout_refund\"}]}",
                "repayment");
    }

    @Test
    void rejectsRulesWithoutDefault() {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> parser.assembleWCPaymentAllocationRules(command(
                        "{\"paymentAllocation\":[{\"transactionType\":\"REPAYMENT\"},{\"transactionType\":\"GOODWILL_CREDIT\"}]}")));
        assertThat(exception.getErrors()).singleElement()
                .satisfies(error -> assertThat(error.getUserMessageGlobalisationCode()).isEqualTo("wc-payment-allocation-without-default"));
    }

    @Test
    void mapsAllSupportedTransactionTypes() {
        JsonArray allocations = new JsonArray();
        for (WorkingCapitalPaymentAllocationTransactionType type : WorkingCapitalPaymentAllocationTransactionType.values()) {
            JsonObject rule = new JsonObject();
            rule.addProperty("transactionType", type.name());
            allocations.add(rule);
        }
        JsonObject json = new JsonObject();
        json.add("paymentAllocation", allocations);
        assertThat(parser.assembleWCPaymentAllocationRules(command(json.toString())))
                .extracting(WorkingCapitalLoanProductPaymentAllocationRule::getTransactionType)
                .containsExactlyElementsOf(Arrays.stream(WorkingCapitalPaymentAllocationTransactionType.values())
                        .map(WorkingCapitalPaymentAllocationTransactionType::toPaymentAllocationTransactionType).toList());
    }

    private void assertInvalidTransactionType(final String json, final String rejectedValue) {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> parser.assembleWCPaymentAllocationRules(command(json)));
        assertThat(exception.getErrors()).singleElement().satisfies(error -> {
            assertThat(error.getUserMessageGlobalisationCode()).isEqualTo("wc-payment-allocation.with.not.valid.transaction.type");
            assertThat(error.getDefaultUserMessage()).contains("paymentAllocation.transactionType", "'" + rejectedValue + "'",
                    "Supported values: [DEFAULT, REPAYMENT, PAYOUT_REFUND, GOODWILL_CREDIT, CHARGE_ADJUSTMENT]");
        });
    }

    private JsonCommand command(final String json) {
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), null, 1L, 2L, 3L, 4L, null, null, null, null,
                null, null, null, null, null);
    }
}
