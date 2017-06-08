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
package org.apache.fineract.portfolio.interestratechart.data;

import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.INCENTIVE_RESOURCE_NAME;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.amountParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.attributeNameParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.attributeValueParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.conditionTypeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.entityTypeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.incentiveTypeparamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.common.domain.ConditionType;
import org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants;
import org.apache.fineract.portfolio.interestratechart.incentive.InterestIncentiveAttributeName;
import org.apache.fineract.portfolio.interestratechart.incentive.InterestIncentiveEntityType;
import org.apache.fineract.portfolio.interestratechart.incentive.InterestIncentiveType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class InterestIncentiveDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

	private static final Set<String> INTERESTRATE_INCENTIVE_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(InterestIncentiveApiConstants.idParamName, entityTypeParamName, attributeNameParamName,
					conditionTypeParamName, attributeValueParamName, incentiveTypeparamName, amountParamName));

	private static final Set<String> INTERESTRATE_INCENTIVE_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(InterestIncentiveApiConstants.idParamName, entityTypeParamName, attributeNameParamName,
					conditionTypeParamName, attributeValueParamName, incentiveTypeparamName, amountParamName));

    @Autowired
    public InterestIncentiveDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, INTERESTRATE_INCENTIVE_CREATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(INCENTIVE_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final JsonObject objectElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(objectElement);
        validateIncentiveCreate(element, baseDataValidator, locale);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateIncentiveCreate(final JsonElement element, final DataValidatorBuilder baseDataValidator, final Locale locale) {

        Integer entityType = this.fromApiJsonHelper.extractIntegerNamed(entityTypeParamName, element, locale);
        baseDataValidator.reset().parameter(entityTypeParamName).value(entityType).notNull()
                .isOneOfTheseValues(InterestIncentiveEntityType.integerValues());

        Integer attributeName = this.fromApiJsonHelper.extractIntegerNamed(attributeNameParamName, element, locale);
        baseDataValidator.reset().parameter(attributeNameParamName).value(attributeName).notNull()
                .isOneOfTheseValues(InterestIncentiveAttributeName.integerValues());

        Integer conditionType = this.fromApiJsonHelper.extractIntegerNamed(conditionTypeParamName, element, locale);
        baseDataValidator.reset().parameter(conditionTypeParamName).value(conditionType).notNull()
                .isOneOfTheseValues(ConditionType.integerValues());

        final String attributeValue = this.fromApiJsonHelper.extractStringNamed(attributeValueParamName, element);
        baseDataValidator.reset().parameter(attributeValueParamName).value(attributeValue).notNull();

        Integer incentiveType = this.fromApiJsonHelper.extractIntegerNamed(incentiveTypeparamName, element, locale);
        baseDataValidator.reset().parameter(incentiveTypeparamName).value(incentiveType).notNull()
                .isOneOfTheseValues(InterestIncentiveType.integerValues());

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(amountParamName, element, locale);
        baseDataValidator.reset().parameter(amountParamName).value(amount).notNull();

    }

    public void validateUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, INTERESTRATE_INCENTIVE_UPDATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(INCENTIVE_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final JsonObject objectElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(objectElement);
        validateIncentiveUpdate(element, baseDataValidator, locale);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateIncentiveUpdate(final JsonElement element, final DataValidatorBuilder baseDataValidator, final Locale locale) {

        if (this.fromApiJsonHelper.parameterExists(descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
            baseDataValidator.reset().parameter(descriptionParamName).value(description).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(entityTypeParamName, element)) {
            Integer entityType = this.fromApiJsonHelper.extractIntegerNamed(entityTypeParamName, element, locale);
            baseDataValidator.reset().parameter(entityTypeParamName).value(entityType).notNull()
                    .isOneOfTheseValues(InterestIncentiveEntityType.integerValues());
        }

        if (this.fromApiJsonHelper.parameterExists(attributeNameParamName, element)) {
            Integer attributeName = this.fromApiJsonHelper.extractIntegerNamed(attributeNameParamName, element, locale);
            baseDataValidator.reset().parameter(attributeNameParamName).value(attributeName).notNull()
                    .isOneOfTheseValues(InterestIncentiveAttributeName.integerValues());
        }

        if (this.fromApiJsonHelper.parameterExists(conditionTypeParamName, element)) {
            Integer conditionType = this.fromApiJsonHelper.extractIntegerNamed(conditionTypeParamName, element, locale);
            baseDataValidator.reset().parameter(conditionTypeParamName).value(conditionType).notNull()
                    .isOneOfTheseValues(ConditionType.integerValues());
        }

        if (this.fromApiJsonHelper.parameterExists(attributeValueParamName, element)) {
            final String attributeValue = this.fromApiJsonHelper.extractStringNamed(attributeValueParamName, element);
            baseDataValidator.reset().parameter(attributeValueParamName).value(attributeValue).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(incentiveTypeparamName, element)) {
            Integer incentiveType = this.fromApiJsonHelper.extractIntegerNamed(incentiveTypeparamName, element, locale);
            baseDataValidator.reset().parameter(incentiveTypeparamName).value(incentiveType).notNull()
                    .isOneOfTheseValues(InterestIncentiveType.integerValues());
        }

        if (this.fromApiJsonHelper.parameterExists(amountParamName, element)) {
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(amountParamName, element, locale);
            baseDataValidator.reset().parameter(amountParamName).value(amount).notNull();
        }

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

}