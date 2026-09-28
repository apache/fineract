/*
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fineract.portfolio.shareproducts.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NsimbiShareDividendRequestMapperTest {

    private final NsimbiShareDividendRequestMapper mapper = new NsimbiShareDividendRequestMapper();

    @Test
    void mapsFixedAmountToTheExistingDividendCommandWithoutThePassword() {
        String request = """
                {"amount":50000,"date":"2026-09-20","sharePeriodMonths":1,"method":"Savings",
                 "usePercentages":false,"password":"do-not-audit-this","referenceNumber":"","comment":""}
                """;

        JsonObject command = JsonParser.parseString(mapper.toFineractCommand(request)).getAsJsonObject();

        assertThat(command.get("dividendAmount").getAsBigDecimal()).isEqualByComparingTo("50000");
        assertThat(command.get("dividendPeriodStartDate").getAsString()).isEqualTo("2026-08-20");
        assertThat(command.get("dividendPeriodEndDate").getAsString()).isEqualTo("2026-09-20");
        assertThat(command.get("dateFormat").getAsString()).isEqualTo("yyyy-MM-dd");
        assertThat(command.get("locale").getAsString()).isEqualTo("en");
        assertThat(command.has("password")).isFalse();
        assertThat(command.toString()).doesNotContain("do-not-audit-this");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " " })
    void rejectsBlankPassword(String password) {
        String request = baseRequest().replace("\"password\":\"valid\"", "\"password\":\"" + password + "\"");
        assertError(request, "password", "Password is required To Perform This Action.");
    }

    @Test
    void rejectsMissingPassword() {
        assertError(baseRequest().replace(",\"password\":\"valid\"", ""), "password",
                "Password is required To Perform This Action.");
    }

    @Test
    void rejectsPercentageMode() {
        assertError(baseRequest().replace("\"password\":\"valid\"", "\"password\":\"valid\",\"usePercentages\":true"),
                "usePercentages", "Percentage-based share dividends are not supported yet.");
    }

    @Test
    void rejectsSelectedSavingsProductUntilDestinationPolicyIsDefined() {
        assertError(baseRequest().replace("\"password\":\"valid\"", "\"password\":\"valid\",\"savingProductId\":4"),
                "savingProductId", "Selecting a payout savings product is not supported yet.");
    }

    @Test
    void rejectsNonBlankReferenceRatherThanSilentlyDroppingIt() {
        assertError(baseRequest().replace("\"password\":\"valid\"", "\"password\":\"valid\",\"referenceNumber\":\"REF-1\""),
                "referenceNumber", "Dividend reference numbers are not stored yet.");
    }

    @Test
    void rejectsNonBlankCommentRatherThanSilentlyDroppingIt() {
        assertError(baseRequest().replace("\"password\":\"valid\"", "\"password\":\"valid\",\"comment\":\"note\""),
                "comment", "Dividend comments are not stored yet.");
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertError(baseRequest().replace("\"amount\":50000", "\"amount\":0"), "amount", "Amount must be a positive number.");
    }

    @Test
    void rejectsInvalidDate() {
        assertError(baseRequest().replace("2026-09-20", "2026-02-30"), "date", "Date must be a valid ISO date (yyyy-MM-dd).");
    }

    @Test
    void rejectsFractionalMonthPeriod() {
        assertError(baseRequest().replace("\"sharePeriodMonths\":1", "\"sharePeriodMonths\":1.5"), "sharePeriodMonths",
                "Share Period must be a positive whole number of months.");
    }

    private void assertError(String request, String parameter, String message) {
        PlatformApiDataValidationException error = assertThrows(PlatformApiDataValidationException.class,
                () -> mapper.toFineractCommand(request));
        assertThat(error.getErrors()).singleElement().satisfies(detail -> {
            assertThat(detail.getParameterName()).isEqualTo(parameter);
            assertThat(detail.getDefaultUserMessage()).isEqualTo(message);
        });
    }

    private static String baseRequest() {
        return "{\"amount\":50000,\"date\":\"2026-09-20\",\"sharePeriodMonths\":1,\"password\":\"valid\"}";
    }
}
