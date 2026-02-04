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
package org.apache.fineract.portfolio.loanorigination.serialization;

import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.CHANNEL_TYPE_CODE_NAME;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.CHANNEL_TYPE_ID_PARAM;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.EXTERNAL_ID_PARAM;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.ORIGINATOR_TYPE_CODE_NAME;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.ORIGINATOR_TYPE_ID_PARAM;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.exception.CodeValueNotFoundException;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.loanorigination.data.LoanApplicationOriginatorData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public class LoanApplicationOriginatorDataValidator {

    private static final String RESOURCE_NAME = "loan.originator";
    private static final String ID_PARAM = "id";
    private static final String NAME_PARAM = "name";

    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    public LoanApplicationOriginatorData validateAndExtract(JsonObject jsonObject) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(RESOURCE_NAME);

        final Long id = extractLong(jsonObject, ID_PARAM);
        final String externalId = extractString(jsonObject, EXTERNAL_ID_PARAM);

        if (id == null && (externalId == null || externalId.isBlank())) {
            baseDataValidator.reset().parameter(ID_PARAM).failWithCode("or.externalId.required",
                    "Either 'id' or 'externalId' must be provided for originator");
        }

        final String name = extractString(jsonObject, NAME_PARAM);
        baseDataValidator.reset().parameter(NAME_PARAM).value(name).ignoreIfNull().notExceedingLengthOf(255);

        final Long typeId = extractLong(jsonObject, ORIGINATOR_TYPE_ID_PARAM);
        if (typeId != null) {
            validateCodeValue(typeId, ORIGINATOR_TYPE_CODE_NAME, ORIGINATOR_TYPE_ID_PARAM, baseDataValidator);
        }

        final Long channelTypeId = extractLong(jsonObject, CHANNEL_TYPE_ID_PARAM);
        if (channelTypeId != null) {
            validateCodeValue(channelTypeId, CHANNEL_TYPE_CODE_NAME, CHANNEL_TYPE_ID_PARAM, baseDataValidator);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

        return new LoanApplicationOriginatorData(id, externalId, name, typeId, channelTypeId);
    }

    private Long extractLong(JsonObject jsonObject, String paramName) {
        if (jsonObject.has(paramName)) {
            JsonElement element = jsonObject.get(paramName);
            if (!element.isJsonNull()) {
                try {
                    return element.getAsLong();
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private String extractString(JsonObject jsonObject, String paramName) {
        if (jsonObject.has(paramName)) {
            JsonElement element = jsonObject.get(paramName);
            if (!element.isJsonNull()) {
                return element.getAsString();
            }
        }
        return null;
    }

    private void validateCodeValue(Long codeValueId, String codeName, String paramName, DataValidatorBuilder baseDataValidator) {
        try {
            this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(codeName, codeValueId);
        } catch (CodeValueNotFoundException e) {
            baseDataValidator.reset().parameter(paramName).value(codeValueId).failWithCode("invalid.code.value",
                    "Invalid code value id " + codeValueId + " for " + codeName);
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
