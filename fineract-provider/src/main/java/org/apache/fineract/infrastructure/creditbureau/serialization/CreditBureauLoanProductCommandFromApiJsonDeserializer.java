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

    public static final String LOAN_PRODUCT_ID = "loanProductId";
    public static final String IS_CREDITCHECK_MANDATORY = "isCreditcheckMandatory";
    public static final String SKIP_CREDITCHECK_IN_FAILURE = "skipCreditcheckInFailure";
    public static final String STALE_PERIOD = "stalePeriod";
    public static final String IS_ACTIVE = "isActive";
    public static final String LOCALE = "locale";
    public static final String CREDITBUREAU_LOAN_PRODUCT_MAPPING_ID = "creditbureauLoanProductMappingId";
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList(LOAN_PRODUCT_ID, IS_CREDITCHECK_MANDATORY,
            SKIP_CREDITCHECK_IN_FAILURE, STALE_PERIOD, IS_ACTIVE, LOCALE, CREDITBUREAU_LOAN_PRODUCT_MAPPING_ID));

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
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("CREDITBUREAU_LOANPRODUCT_MAPPING");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        baseDataValidator.reset().value(cb_id).notBlank().integerGreaterThanZero();

        final long loanProductId = this.fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ID, element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ID).value(loanProductId).notBlank().integerGreaterThanZero();

        if (this.fromApiJsonHelper.extractBooleanNamed(IS_CREDITCHECK_MANDATORY, element) != null) {
            final boolean isCreditcheckMandatory = this.fromApiJsonHelper.extractBooleanNamed(IS_CREDITCHECK_MANDATORY, element);
            baseDataValidator.reset().parameter(IS_CREDITCHECK_MANDATORY).value(isCreditcheckMandatory).notBlank()
                    .trueOrFalseRequired(isCreditcheckMandatory);
        } else {
            baseDataValidator.reset().parameter(IS_CREDITCHECK_MANDATORY)
                    .value(this.fromApiJsonHelper.extractBooleanNamed(IS_CREDITCHECK_MANDATORY, element)).notBlank()
                    .trueOrFalseRequired(this.fromApiJsonHelper.extractBooleanNamed(IS_CREDITCHECK_MANDATORY, element));
        }

        if (this.fromApiJsonHelper.extractBooleanNamed(SKIP_CREDITCHECK_IN_FAILURE, element) != null) {
            final boolean skipCreditcheckInFailure = this.fromApiJsonHelper.extractBooleanNamed(SKIP_CREDITCHECK_IN_FAILURE, element);
            baseDataValidator.reset().parameter(SKIP_CREDITCHECK_IN_FAILURE).value(skipCreditcheckInFailure).notBlank()
                    .trueOrFalseRequired(skipCreditcheckInFailure);

        } else {
            baseDataValidator.reset().parameter(SKIP_CREDITCHECK_IN_FAILURE)
                    .value(this.fromApiJsonHelper.extractBooleanNamed(SKIP_CREDITCHECK_IN_FAILURE, element)).notBlank()
                    .trueOrFalseRequired(this.fromApiJsonHelper.extractBooleanNamed(SKIP_CREDITCHECK_IN_FAILURE, element));
        }

        if (this.fromApiJsonHelper.extractLongNamed(STALE_PERIOD, element) != null) {
            final long stalePeriod = this.fromApiJsonHelper.extractLongNamed(STALE_PERIOD, element);
            baseDataValidator.reset().parameter(STALE_PERIOD).value(stalePeriod).notBlank().integerGreaterThanZero();
        } else {
            baseDataValidator.reset().parameter(STALE_PERIOD).value(this.fromApiJsonHelper.extractLongNamed(STALE_PERIOD, element))
                    .notBlank().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractBooleanNamed(IS_ACTIVE, element) != null) {
            Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed(IS_ACTIVE, element);
            if (isActive == null) {
                isActive = false;
            } else {

                baseDataValidator.reset().parameter(IS_ACTIVE).value(isActive).notBlank().trueOrFalseRequired(isActive);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("CREDITBUREAU_LOANPRODUCT_MAPPING");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String creditbureauLoanProductMappingIdParameter = CREDITBUREAU_LOAN_PRODUCT_MAPPING_ID;
        if (this.fromApiJsonHelper.parameterExists(creditbureauLoanProductMappingIdParameter, element)) {
            final Long creditbureauLoanProductMappingId = this.fromApiJsonHelper.extractLongNamed(CREDITBUREAU_LOAN_PRODUCT_MAPPING_ID,
                    element);
            baseDataValidator.reset().parameter(CREDITBUREAU_LOAN_PRODUCT_MAPPING_ID).value(creditbureauLoanProductMappingId).notNull()
                    .notBlank().longGreaterThanZero();
        }

        final String is_activeParameter = IS_ACTIVE;
        if (this.fromApiJsonHelper.parameterExists(is_activeParameter, element)) {
            final boolean isActive = this.fromApiJsonHelper.extractBooleanNamed(IS_ACTIVE, element);
            baseDataValidator.reset().parameter(IS_ACTIVE).value(isActive).notNull().notBlank().trueOrFalseRequired(isActive);
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
