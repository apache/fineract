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
package org.apache.fineract.interoperation.data;

import static org.apache.fineract.interoperation.util.InteropUtil.DEFAULT_LOCALE;
import static org.apache.fineract.interoperation.util.InteropUtil.ISO8601_DATE_TIME_FORMAT;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_ACCOUNT_ID;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_AMOUNT;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_EXPIRATION;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_EXTENSION_LIST;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_GEO_CODE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_LOCALE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_NOTE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_REQUEST_CODE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_TRANSACTION_CODE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_TRANSACTION_ROLE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_TRANSACTION_TYPE;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;

public class InteropRequestData {

    @NotNull
    private final String transactionCode;

    private final String requestCode;
    @NotNull
    private final String accountId;
    @NotNull
    private final MoneyData amount;
    @NotNull
    private final InteropTransactionRole transactionRole;

    private final InteropTransactionTypeData transactionType;

    private String note;

    private GeoCodeData geoCode;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime expiration;

    private List<ExtensionData> extensionList;

    protected InteropRequestData(@NotNull String transactionCode, String requestCode, @NotNull String accountId, @NotNull MoneyData amount,
            @NotNull InteropTransactionRole transactionRole, InteropTransactionTypeData transactionType, String note, GeoCodeData geoCode,
            LocalDateTime expiration, List<ExtensionData> extensionList) {
        this.transactionCode = transactionCode;
        this.requestCode = requestCode;
        this.accountId = accountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionRole = transactionRole;
        this.note = note;
        this.geoCode = geoCode;
        this.expiration = expiration;
        this.extensionList = extensionList;
    }

    protected InteropRequestData(@NotNull String transactionCode, @NotNull String accountId, @NotNull MoneyData amount,
            @NotNull InteropTransactionRole transactionRole) {
        this(transactionCode, null, accountId, amount, transactionRole, null, null, null, null, null);
    }

    @NotNull
    public String getTransactionCode() {
        return transactionCode;
    }

    public String getRequestCode() {
        return requestCode;
    }

    @NotNull
    public String getAccountId() {
        return accountId;
    }

    @NotNull
    public MoneyData getAmount() {
        return amount;
    }

    public InteropTransactionTypeData getTransactionType() {
        return transactionType;
    }

    @NotNull
    public InteropTransactionRole getTransactionRole() {
        return transactionRole;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public GeoCodeData getGeoCode() {
        return geoCode;
    }

    public void setGeoCode(GeoCodeData geoCode) {
        this.geoCode = geoCode;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public LocalDate getExpirationLocalDate() {
        return expiration == null ? null : expiration.toLocalDate();
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public List<ExtensionData> getExtensionList() {
        return extensionList;
    }

    public void setExtensionList(List<ExtensionData> extensionList) {
        this.extensionList = extensionList;
    }

    public void normalizeAmounts(@NotNull MonetaryCurrency currency) {
        amount.normalizeAmount(currency);
    }

    public static InteropRequestData validateAndParse(final DataValidatorBuilder dataValidator, JsonObject element,
            FromJsonHelper jsonHelper) {
        if (element == null) {
            return null;
        }

        String transactionCode = jsonHelper.extractStringNamed(PARAM_TRANSACTION_CODE, element);
        DataValidatorBuilder dataValidatorCopy = dataValidator.reset().parameter(PARAM_TRANSACTION_CODE).value(transactionCode).notBlank();

        String requestCode = jsonHelper.extractStringNamed(PARAM_REQUEST_CODE, element);

        String accountId = jsonHelper.extractStringNamed(PARAM_ACCOUNT_ID, element);
        dataValidatorCopy = dataValidatorCopy.reset().parameter(PARAM_ACCOUNT_ID).value(accountId).notBlank();

        JsonObject moneyElement = jsonHelper.extractJsonObjectNamed(PARAM_AMOUNT, element);
        dataValidatorCopy = dataValidatorCopy.reset().parameter(PARAM_AMOUNT).value(moneyElement).notNull();
        dataValidator.merge(dataValidatorCopy);
        MoneyData amount = MoneyData.validateAndParse(dataValidator, moneyElement, jsonHelper);

        JsonObject transactionTypeElement = jsonHelper.extractJsonObjectNamed(PARAM_TRANSACTION_TYPE, element);
        InteropTransactionTypeData transactionType = InteropTransactionTypeData.validateAndParse(dataValidator, transactionTypeElement,
                jsonHelper);

        String transactionRoleString = jsonHelper.extractStringNamed(PARAM_TRANSACTION_ROLE, element);
        InteropTransactionRole transactionRole = transactionRoleString == null ? InteropTransactionRole.PAYER
                : InteropTransactionRole.valueOf(transactionRoleString);

        String note = jsonHelper.extractStringNamed(PARAM_NOTE, element);

        JsonObject geoCodeElement = jsonHelper.extractJsonObjectNamed(PARAM_GEO_CODE, element);
        GeoCodeData geoCode = GeoCodeData.validateAndParse(dataValidator, geoCodeElement, jsonHelper);

        String locale = jsonHelper.extractStringNamed(PARAM_LOCALE, element);
        LocalDateTime expiration = locale == null
                ? jsonHelper.extractLocalDateTimeNamed(PARAM_EXPIRATION, element, ISO8601_DATE_TIME_FORMAT, DEFAULT_LOCALE)
                : jsonHelper.extractLocalDateTimeNamed(PARAM_EXPIRATION, element); // PARAM_DATE_FORMAT
        // also
        // must
        // be
        // set

        JsonArray extensionArray = jsonHelper.extractJsonArrayNamed(PARAM_EXTENSION_LIST, element);
        ArrayList<ExtensionData> extensionList = null;
        if (extensionArray != null) {
            extensionList = new ArrayList<>(extensionArray.size());
            for (JsonElement jsonElement : extensionArray) {
                if (jsonElement.isJsonObject()) {
                    extensionList.add(ExtensionData.validateAndParse(dataValidator, jsonElement.getAsJsonObject(), jsonHelper));
                }
            }
        }

        return dataValidator.hasError() ? null
                : new InteropRequestData(transactionCode, requestCode, accountId, amount, transactionRole, transactionType, note, geoCode,
                        expiration, extensionList);
    }
}
