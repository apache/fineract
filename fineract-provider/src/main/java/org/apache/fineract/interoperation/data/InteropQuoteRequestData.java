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

import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_ACCOUNT_ID;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_AMOUNT;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_AMOUNT_TYPE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_DATE_FORMAT;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_EXPIRATION;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_EXTENSION_LIST;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_FEES;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_GEO_CODE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_LOCALE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_NOTE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_QUOTE_CODE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_REQUEST_CODE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_TRANSACTION_CODE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_TRANSACTION_ROLE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_TRANSACTION_TYPE;

import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.interoperation.domain.InteropAmountType;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;

public class InteropQuoteRequestData extends InteropRequestData {

    static final String[] PARAMS = { PARAM_TRANSACTION_CODE, PARAM_REQUEST_CODE, PARAM_ACCOUNT_ID, PARAM_AMOUNT, PARAM_TRANSACTION_TYPE,
            PARAM_TRANSACTION_ROLE, PARAM_NOTE, PARAM_GEO_CODE, PARAM_EXPIRATION, PARAM_EXTENSION_LIST, PARAM_QUOTE_CODE, PARAM_AMOUNT_TYPE,
            PARAM_FEES, PARAM_LOCALE, PARAM_DATE_FORMAT };
    @NotNull
    private final String quoteCode;
    @NotNull
    private final InteropAmountType amountType;

    private final MoneyData fees; // only for disclosed Payer fees on the Payee
                                  // side

    public InteropQuoteRequestData(@NotNull String transactionCode, String requestCode, @NotNull String accountId,
            @NotNull MoneyData amount, @NotNull InteropTransactionRole transactionRole, @NotNull InteropTransactionTypeData transactionType,
            String note, GeoCodeData geoCode, LocalDateTime expiration, List<ExtensionData> extensionList, @NotNull String quoteCode,
            @NotNull InteropAmountType amountType, MoneyData fees) {
        super(transactionCode, requestCode, accountId, amount, transactionRole, transactionType, note, geoCode, expiration, extensionList);
        this.quoteCode = quoteCode;
        this.amountType = amountType;
        this.fees = fees;
    }

    public InteropQuoteRequestData(@NotNull String transactionCode, @NotNull String accountId, @NotNull InteropAmountType amountType,
            @NotNull MoneyData amount, @NotNull InteropTransactionRole transactionRole, @NotNull InteropTransactionTypeData transactionType,
            @NotNull String quoteCode) {
        this(transactionCode, null, accountId, amount, transactionRole, transactionType, null, null, null, null, quoteCode, amountType,
                null);
    }

    private InteropQuoteRequestData(@NotNull InteropRequestData other, @NotNull String quoteCode, @NotNull InteropAmountType amountType,
            MoneyData fees) {
        this(other.getTransactionCode(), other.getRequestCode(), other.getAccountId(), other.getAmount(), other.getTransactionRole(),
                other.getTransactionType(), other.getNote(), other.getGeoCode(), other.getExpiration(), other.getExtensionList(), quoteCode,
                amountType, fees);
    }

    public String getQuoteCode() {
        return quoteCode;
    }

    public InteropAmountType getAmountType() {
        return amountType;
    }

    public MoneyData getFees() {
        return fees;
    }

    @Override
    public void normalizeAmounts(@NotNull MonetaryCurrency currency) {
        super.normalizeAmounts(currency);
        if (fees != null) {
            fees.normalizeAmount(currency);
        }
    }

    public static InteropQuoteRequestData validateAndParse(final DataValidatorBuilder dataValidator, JsonObject element,
            FromJsonHelper jsonHelper) {
        if (element == null) {
            return null;
        }

        jsonHelper.checkForUnsupportedParameters(element, Arrays.asList(PARAMS));

        InteropRequestData interopRequestData = InteropRequestData.validateAndParse(dataValidator, element, jsonHelper);

        String quoteCode = jsonHelper.extractStringNamed(PARAM_QUOTE_CODE, element);
        DataValidatorBuilder dataValidatorCopy = dataValidator.reset().parameter(PARAM_QUOTE_CODE).value(quoteCode).notBlank();

        String amountTypeString = jsonHelper.extractStringNamed(PARAM_AMOUNT_TYPE, element);
        dataValidatorCopy = dataValidatorCopy.reset().parameter(PARAM_AMOUNT_TYPE).value(amountTypeString).notBlank();
        InteropAmountType amountType = InteropAmountType.valueOf(amountTypeString);

        JsonObject feesElement = jsonHelper.extractJsonObjectNamed(PARAM_FEES, element);
        dataValidator.merge(dataValidatorCopy);
        MoneyData fees = MoneyData.validateAndParse(dataValidator, feesElement, jsonHelper);

        String transactionRoleString = jsonHelper.extractStringNamed(PARAM_TRANSACTION_ROLE, element);
        dataValidatorCopy = dataValidator.reset().parameter(PARAM_TRANSACTION_ROLE).value(transactionRoleString).notNull();

        JsonObject transactionTypeElement = jsonHelper.extractJsonObjectNamed(PARAM_TRANSACTION_TYPE, element);
        dataValidatorCopy = dataValidatorCopy.reset().parameter(PARAM_TRANSACTION_TYPE).value(transactionTypeElement).notNull();

        dataValidator.merge(dataValidatorCopy);
        return dataValidator.hasError() ? null : new InteropQuoteRequestData(interopRequestData, quoteCode, amountType, fees);
    }
}
