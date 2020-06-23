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

package org.apache.fineract.portfolio.self.pockets.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.self.pockets.api.PocketApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PocketDataValidator {

    private final Set<String> linkingAccountsSupportedParameters = new HashSet<>(Arrays.asList(PocketApiConstants.accountIdParamName,
            PocketApiConstants.accountTypeParamName, PocketApiConstants.accountsDetail));

    private final Set<String> delinkingAccountsSupportedParameters = new HashSet<>(
            Arrays.asList(PocketApiConstants.pocketAccountMappingList));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public PocketDataValidator(FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForLinkingAccounts(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.linkingAccountsSupportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(PocketApiConstants.pocketsResourceName);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        JsonArray accountsDetail = this.fromApiJsonHelper.extractJsonArrayNamed(PocketApiConstants.accountsDetail, element);
        baseDataValidator.reset().parameter(PocketApiConstants.accountsDetail).value(accountsDetail).notNull().jsonArrayNotEmpty();

        final List<String> valueList = Arrays.asList(EntityAccountType.LOAN.name().toLowerCase(),
                EntityAccountType.SAVINGS.name().toLowerCase(), EntityAccountType.SHARES.name().toLowerCase());

        for (JsonElement accountDetails : accountsDetail) {

            final Long accountId = this.fromApiJsonHelper.extractLongNamed(PocketApiConstants.accountIdParamName, accountDetails);
            baseDataValidator.reset().parameter(PocketApiConstants.accountIdParamName).value(accountId).notBlank();

            final String accountType = this.fromApiJsonHelper.extractStringNamed(PocketApiConstants.accountTypeParamName, accountDetails);
            baseDataValidator.reset().parameter(PocketApiConstants.accountTypeParamName).value(accountType).notBlank()
                    .isOneOfTheseStringValues(valueList);

        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForDeLinkingAccounts(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.delinkingAccountsSupportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(PocketApiConstants.pocketsResourceName);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        JsonArray pocketAccountMappingList = this.fromApiJsonHelper.extractJsonArrayNamed(PocketApiConstants.pocketAccountMappingList,
                element);
        baseDataValidator.reset().parameter(PocketApiConstants.pocketAccountMappingList).value(pocketAccountMappingList).notNull()
                .jsonArrayNotEmpty();

        for (JsonElement pocketAccountMapping : pocketAccountMappingList) {

            final Long mappingId = pocketAccountMapping.getAsLong();
            baseDataValidator.reset().parameter(PocketApiConstants.pocketAccountMappingId).value(mappingId).notBlank();

        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(PocketApiConstants.dataValidationMessage,
                    PocketApiConstants.validationErrorMessage, dataValidationErrors);
        }
    }

}
