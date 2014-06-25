/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.data;

import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.INCENTIVE_RESOURCE_NAME;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.INTERESTRATE_INCENTIVE_CREATE_REQUEST_DATA_PARAMETERS;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.INTERESTRATE_INCENTIVE_UPDATE_REQUEST_DATA_PARAMETERS;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.amountParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.attributeNameParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.attributeValueParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.conditionTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.entityTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.incentiveTypeparamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.common.domain.ConditionType;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveAttributeName;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveEntityType;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class InterestIncentiveDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

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