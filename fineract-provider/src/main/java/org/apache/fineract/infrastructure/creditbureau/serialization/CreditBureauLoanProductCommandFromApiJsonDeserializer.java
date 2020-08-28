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
package org.apache.fineract.infrastructure.creditbureau.serialization;

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
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreditBureauLoanProductCommandFromApiJsonDeserializer {

    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("loanProductId", "isCreditcheckMandatory",
            "skipCreditcheckInFailure", "stalePeriod", "isActive", "locale", "creditbureauLoanProductMappingId"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CreditBureauLoanProductCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json, final Long cb_id) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("CREDITBUREAU_LOANPRODUCT_MAPPING");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        baseDataValidator.reset().value(cb_id).notBlank().integerGreaterThanZero();

        final long loanProductId = this.fromApiJsonHelper.extractLongNamed("loanProductId", element);
        baseDataValidator.reset().parameter("loanProductId").value(loanProductId).notBlank().integerGreaterThanZero();

        if (this.fromApiJsonHelper.extractBooleanNamed("isCreditcheckMandatory", element) != null) {
            final boolean isCreditcheckMandatory = this.fromApiJsonHelper.extractBooleanNamed("isCreditcheckMandatory", element);
            baseDataValidator.reset().parameter("isCreditcheckMandatory").value(isCreditcheckMandatory).notBlank()
                    .trueOrFalseRequired(isCreditcheckMandatory);
        } else {
            baseDataValidator.reset().parameter("isCreditcheckMandatory")
                    .value(this.fromApiJsonHelper.extractBooleanNamed("isCreditcheckMandatory", element)).notBlank()
                    .trueOrFalseRequired(this.fromApiJsonHelper.extractBooleanNamed("isCreditcheckMandatory", element));
        }

        if (this.fromApiJsonHelper.extractBooleanNamed("skipCreditcheckInFailure", element) != null) {
            final boolean skipCreditcheckInFailure = this.fromApiJsonHelper.extractBooleanNamed("skipCreditcheckInFailure", element);
            baseDataValidator.reset().parameter("skipCreditcheckInFailure").value(skipCreditcheckInFailure).notBlank()
                    .trueOrFalseRequired(skipCreditcheckInFailure);

        } else {
            baseDataValidator.reset().parameter("skipCreditcheckInFailure")
                    .value(this.fromApiJsonHelper.extractBooleanNamed("skipCreditcheckInFailure", element)).notBlank()
                    .trueOrFalseRequired(this.fromApiJsonHelper.extractBooleanNamed("skipCreditcheckInFailure", element));
        }

        if (this.fromApiJsonHelper.extractLongNamed("stalePeriod", element) != null) {
            final long stalePeriod = this.fromApiJsonHelper.extractLongNamed("stalePeriod", element);
            baseDataValidator.reset().parameter("stalePeriod").value(stalePeriod).notBlank().integerGreaterThanZero();
        } else {
            baseDataValidator.reset().parameter("stalePeriod").value(this.fromApiJsonHelper.extractLongNamed("stalePeriod", element))
                    .notBlank().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractBooleanNamed("isActive", element) != null) {
            Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
            if (isActive == null) {
                isActive = false;
            } else {

                baseDataValidator.reset().parameter("isActive").value(isActive).notBlank().trueOrFalseRequired(isActive);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("CREDITBUREAU_LOANPRODUCT_MAPPING");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String creditbureauLoanProductMappingIdParameter = "creditbureauLoanProductMappingId";
        if (this.fromApiJsonHelper.parameterExists(creditbureauLoanProductMappingIdParameter, element)) {
            final Long creditbureauLoanProductMappingId = this.fromApiJsonHelper.extractLongNamed("creditbureauLoanProductMappingId",
                    element);
            baseDataValidator.reset().parameter("creditbureauLoanProductMappingId").value(creditbureauLoanProductMappingId).notNull()
                    .notBlank().longGreaterThanZero();
        }

        final String is_activeParameter = "isActive";
        if (this.fromApiJsonHelper.parameterExists(is_activeParameter, element)) {
            final boolean isActive = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
            baseDataValidator.reset().parameter("isActive").value(isActive).notNull().notBlank().trueOrFalseRequired(isActive);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
