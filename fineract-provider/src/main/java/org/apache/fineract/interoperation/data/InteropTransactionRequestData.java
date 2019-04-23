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

import com.google.gson.JsonObject;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.joda.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

import static org.apache.fineract.interoperation.util.InteropUtil.*;

public class InteropTransactionRequestData extends InteropRequestData {

    static final String[] PARAMS = {PARAM_TRANSACTION_CODE, PARAM_REQUEST_CODE, PARAM_ACCOUNT_ID, PARAM_AMOUNT, PARAM_TRANSACTION_ROLE,
            PARAM_TRANSACTION_TYPE, PARAM_NOTE, PARAM_GEO_CODE, PARAM_EXPIRATION, PARAM_EXTENSION_LIST, PARAM_LOCALE, PARAM_DATE_FORMAT};


    public InteropTransactionRequestData(@NotNull String transactionCode, @NotNull String requestCode, @NotNull String accountId,
                                         @NotNull MoneyData amount, @NotNull InteropTransactionTypeData transactionType, String note,
                                         GeoCodeData geoCode, LocalDateTime expiration, List<ExtensionData> extensionList) {
        super(transactionCode, requestCode, accountId, amount, InteropTransactionRole.PAYER, transactionType, note, geoCode, expiration, extensionList);
    }

    public InteropTransactionRequestData(@NotNull String transactionCode, @NotNull String requestCode, @NotNull String accountId,
                                         @NotNull MoneyData amount, @NotNull InteropTransactionTypeData transactionType) {
        this(transactionCode, requestCode, accountId, amount, transactionType, null, null, null, null);
    }

    private InteropTransactionRequestData(InteropRequestData other) {
        this(other.getTransactionCode(), other.getRequestCode(), other.getAccountId(), other.getAmount(), other.getTransactionType(),
                other.getNote(), other.getGeoCode(), other.getExpiration(), other.getExtensionList());
    }

    public static InteropTransactionRequestData validateAndParse(final DataValidatorBuilder dataValidator, JsonObject element, FromJsonHelper jsonHelper) {
        if (element == null)
            return null;

        jsonHelper.checkForUnsupportedParameters(element, Arrays.asList(PARAMS));

        InteropRequestData interopRequestData = InteropRequestData.validateAndParse(dataValidator, element, jsonHelper);

        DataValidatorBuilder dataValidatorCopy = dataValidator.reset().parameter(PARAM_REQUEST_CODE).value(interopRequestData.getRequestCode()).notNull();
        dataValidatorCopy = dataValidatorCopy.reset().parameter(PARAM_TRANSACTION_TYPE).value(interopRequestData.getTransactionType()).notNull();
        dataValidatorCopy = dataValidatorCopy.reset().parameter(PARAM_TRANSACTION_ROLE).value(interopRequestData.getTransactionRole()).ignoreIfNull()
                .isOneOfTheseValues(InteropTransactionRole.PAYER);

        dataValidator.merge(dataValidatorCopy);
        return dataValidator.hasError() ? null : new InteropTransactionRequestData(interopRequestData);
    }
}
