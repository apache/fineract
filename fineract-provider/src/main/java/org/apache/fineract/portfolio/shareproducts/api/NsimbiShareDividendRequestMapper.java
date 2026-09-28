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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Maps Nsimbi's dividend form onto Fineract's existing product-scoped, fixed-amount dividend command. The password is
 * checked for presence only and is never copied into the audited command JSON.
 */
final class NsimbiShareDividendRequestMapper {

    private static final Set<String> SUPPORTED_FIELDS = Set.of("amount", "usePercentages", "date", "sharePeriodMonths", "method",
            "savingProductId", "referenceNumber", "comment", "password");

    String toFineractCommand(String requestJson) {
        final JsonObject request;
        try {
            JsonElement parsed = JsonParser.parseString(requestJson);
            if (!parsed.isJsonObject()) {
                throw new InvalidJsonException();
            }
            request = parsed.getAsJsonObject();
        } catch (JsonParseException | NullPointerException e) {
            throw new InvalidJsonException();
        }

        for (String field : request.keySet()) {
            if (!SUPPORTED_FIELDS.contains(field)) {
                throw invalid(field, "Unsupported Share Dividends field.");
            }
        }

        String password = stringValue(request, "password");
        if (password == null || password.isBlank()) {
            throw invalid("password", "Password is required To Perform This Action.");
        }

        JsonElement percentages = request.get("usePercentages");
        if (percentages != null && !percentages.isJsonNull()) {
            if (!percentages.isJsonPrimitive() || !percentages.getAsJsonPrimitive().isBoolean()) {
                throw invalid("usePercentages", "Use Percentages must be true or false.");
            }
            if (percentages.getAsBoolean()) {
                throw invalid("usePercentages", "Percentage-based share dividends are not supported yet.");
            }
        }

        String method = stringValue(request, "method");
        if (method != null && !method.isBlank() && !"Savings".equalsIgnoreCase(method)) {
            throw invalid("method", "Only the existing Savings dividend payout method is supported.");
        }
        rejectNonBlank(request, "savingProductId", "Selecting a payout savings product is not supported yet.");
        rejectNonBlank(request, "referenceNumber", "Dividend reference numbers are not stored yet.");
        rejectNonBlank(request, "comment", "Dividend comments are not stored yet.");

        final String amountText = stringValue(request, "amount");
        final BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
        } catch (NumberFormatException | NullPointerException e) {
            throw invalid("amount", "Amount must be a positive number.");
        }
        if (amount.signum() <= 0) {
            throw invalid("amount", "Amount must be a positive number.");
        }

        final LocalDate endDate;
        final LocalDate startDate;
        try {
            endDate = LocalDate.parse(stringValue(request, "date"), DateTimeFormatter.ISO_LOCAL_DATE);
            String monthsText = stringValue(request, "sharePeriodMonths");
            if (monthsText == null || !monthsText.matches("[0-9]+")) {
                throw invalid("sharePeriodMonths", "Share Period must be a positive whole number of months.");
            }
            int months = Integer.parseInt(monthsText);
            if (months <= 0) {
                throw invalid("sharePeriodMonths", "Share Period must be a positive whole number of months.");
            }
            startDate = endDate.minusMonths(months);
        } catch (PlatformApiDataValidationException e) {
            throw e;
        } catch (DateTimeException | NullPointerException e) {
            throw invalid("date", "Date must be a valid ISO date (yyyy-MM-dd).");
        } catch (NumberFormatException e) {
            throw invalid("sharePeriodMonths", "Share Period must be a positive whole number of months.");
        }

        JsonObject command = new JsonObject();
        command.addProperty("dividendPeriodStartDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        command.addProperty("dividendPeriodEndDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        command.addProperty("dividendAmount", amount);
        command.addProperty("dateFormat", "yyyy-MM-dd");
        command.addProperty("locale", "en");
        return command.toString();
    }

    private static void rejectNonBlank(JsonObject request, String name, String message) {
        String value = stringValue(request, name);
        if (value != null && !value.isBlank()) {
            throw invalid(name, message);
        }
    }

    private static String stringValue(JsonObject request, String name) {
        JsonElement value = request.get(name);
        if (value == null || value.isJsonNull()) {
            return null;
        }
        if (!value.isJsonPrimitive()) {
            throw invalid(name, "A single value is required.");
        }
        return value.getAsString();
    }

    private static PlatformApiDataValidationException invalid(String field, String message) {
        return new PlatformApiDataValidationException("validation.msg.nsimbi.share.dividend." + field, message, field);
    }
}
