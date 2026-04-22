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

import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.CREATE_REQUEST_PARAMS;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.EXTERNAL_ID_PARAM;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.NAME_PARAM;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.RESOURCE_NAME;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.STATUS_PARAM;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.UPDATE_REQUEST_PARAMS;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorStatus;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorInvalidStatusException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public class LoanOriginatorDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_REQUEST_PARAMS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String externalId = this.fromApiJsonHelper.extractStringNamed(EXTERNAL_ID_PARAM, element);
        baseDataValidator.reset().parameter(EXTERNAL_ID_PARAM).value(externalId).notBlank().notExceedingLengthOf(100);

        final String name = this.fromApiJsonHelper.extractStringNamed(NAME_PARAM, element);
        baseDataValidator.reset().parameter(NAME_PARAM).value(name).ignoreIfNull().notExceedingLengthOf(255);

        if (this.fromApiJsonHelper.parameterExists(STATUS_PARAM, element)) {
            final String status = this.fromApiJsonHelper.extractStringNamed(STATUS_PARAM, element);
            baseDataValidator.reset().parameter(STATUS_PARAM).value(status).notBlank();
            validateStatus(status);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_REQUEST_PARAMS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(NAME_PARAM, element);
        baseDataValidator.reset().parameter(NAME_PARAM).value(name).ignoreIfNull().notExceedingLengthOf(255);

        if (this.fromApiJsonHelper.parameterExists(STATUS_PARAM, element)) {
            final String status = this.fromApiJsonHelper.extractStringNamed(STATUS_PARAM, element);
            baseDataValidator.reset().parameter(STATUS_PARAM).value(status).notBlank();
            validateStatus(status);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateStatus(final String status) {
        if (status != null) {
            try {
                LoanOriginatorStatus.fromString(status);
            } catch (IllegalArgumentException e) {
                throw new LoanOriginatorInvalidStatusException(status, e);
            }
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
