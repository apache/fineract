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

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_RESOURCE_NAME;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeFromParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeToParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.annualInterestRateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.currencyCodeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.fromPeriodParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.incentivesParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.periodTypeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.toPeriodParamName;

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
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants;
import org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class InterestRateChartSlabDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final InterestIncentiveDataValidator interestIncentiveDataValidator;
	private static final Set<String> INTERESTRATE_CHART_SLAB_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(InterestRateChartSlabApiConstants.localeParamName,
					InterestRateChartSlabApiConstants.idParamName, descriptionParamName, periodTypeParamName,
					fromPeriodParamName, toPeriodParamName, amountRangeFromParamName, amountRangeToParamName,
					annualInterestRateParamName, currencyCodeParamName, incentivesParamName));

	private static final Set<String> INTERESTRATE_CHART_SLAB_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(InterestRateChartSlabApiConstants.localeParamName,
					InterestRateChartSlabApiConstants.idParamName, descriptionParamName, periodTypeParamName,
					fromPeriodParamName, toPeriodParamName, amountRangeFromParamName, amountRangeToParamName,
					annualInterestRateParamName, currencyCodeParamName, incentivesParamName));

    @Autowired
    public InterestRateChartSlabDataValidator(final FromJsonHelper fromApiJsonHelper,
            final InterestIncentiveDataValidator interestIncentiveDataValidator) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.interestIncentiveDataValidator = interestIncentiveDataValidator;
    }

    public void validateCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, INTERESTRATE_CHART_SLAB_CREATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final JsonObject objectElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(objectElement);

        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(currencyCodeParamName, element);
        baseDataValidator.reset().parameter(currencyCodeParamName).value(currencyCode).notBlank().notExceedingLengthOf(3);

        final Boolean isPrimaryGroupingByAmount = null;
        validateChartSlabsCreate(element, baseDataValidator, locale, isPrimaryGroupingByAmount);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChartSlabsCreate(final JsonElement element, final DataValidatorBuilder baseDataValidator, final Locale locale,
            final Boolean isPrimaryGroupingByAmount) {

        if (this.fromApiJsonHelper.parameterExists(descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
            baseDataValidator.reset().parameter(descriptionParamName).value(description).notNull();
        }

        final Integer periodType = this.fromApiJsonHelper.extractIntegerNamed(periodTypeParamName, element, locale);
        if (this.fromApiJsonHelper.parameterExists(periodTypeParamName, element)) {
            baseDataValidator.reset().parameter(periodTypeParamName).value(periodType)
                    .isOneOfTheseValues(PeriodFrequencyType.integerValues());
        }
        Integer toPeriod = null;

        final Integer fromPeriod = this.fromApiJsonHelper.extractIntegerNamed(fromPeriodParamName, element, locale);
        if (this.fromApiJsonHelper.parameterExists(fromPeriodParamName, element)) {
            baseDataValidator.reset().parameter(fromPeriodParamName).value(fromPeriod).integerZeroOrGreater();
        }

        if ((isPrimaryGroupingByAmount != null && !isPrimaryGroupingByAmount) || (periodType != null || fromPeriod != null)) {
            baseDataValidator.reset().parameter(periodTypeParamName).value(periodType).notNull();
            baseDataValidator.reset().parameter(fromPeriodParamName).value(fromPeriod).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(toPeriodParamName, element)) {
            toPeriod = this.fromApiJsonHelper.extractIntegerNamed(toPeriodParamName, element, locale);
            baseDataValidator.reset().parameter(toPeriodParamName).value(toPeriod).integerGreaterThanZero();
        }

        if (fromPeriod != null && toPeriod != null) {
            if (fromPeriod > toPeriod) {
                baseDataValidator.parameter(fromPeriodParamName).value(fromPeriod).failWithCode("fromperiod.greater.than.to.period");
            }
        }
        final BigDecimal amountRangeFrom = this.fromApiJsonHelper.extractBigDecimalNamed(amountRangeFromParamName, element, locale);
        final BigDecimal amountRangeTo = this.fromApiJsonHelper.extractBigDecimalNamed(amountRangeToParamName, element, locale);
        if (this.fromApiJsonHelper.parameterExists(amountRangeFromParamName, element)) {
            baseDataValidator.reset().parameter(amountRangeFromParamName).value(amountRangeFrom).zeroOrPositiveAmount();
            if (isPrimaryGroupingByAmount != null && isPrimaryGroupingByAmount) {
                baseDataValidator.reset().parameter(amountRangeFromParamName).value(amountRangeFrom).notNull();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(amountRangeToParamName, element)) {
            baseDataValidator.reset().parameter(amountRangeToParamName).value(amountRangeTo).positiveAmount();
        }

        if (amountRangeFrom == null && fromPeriod == null) {
            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("fromperiod.or.amountRangeFrom.required");
        }

        if (amountRangeFrom != null && amountRangeTo != null) {
            if (amountRangeFrom.compareTo(amountRangeTo) > 1) {
                baseDataValidator.parameter(amountRangeFromParamName).value(fromPeriod).failWithCode("from.amount.greater.than.to.amount");
            }
        }

        final BigDecimal annualInterestRate = this.fromApiJsonHelper.extractBigDecimalNamed(annualInterestRateParamName, element, locale);
        baseDataValidator.reset().parameter(annualInterestRateParamName).value(annualInterestRate).notNull().zeroOrPositiveAmount();

        validateIncentives(element, baseDataValidator, locale);
    }

    public void validateUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, INTERESTRATE_CHART_SLAB_UPDATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final JsonObject objectElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(objectElement);
        final Boolean isPrimaryGroupingByAmount = null;
        validateChartSlabsUpdate(element, baseDataValidator, locale, isPrimaryGroupingByAmount);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChartSlabsUpdate(final JsonElement element, final DataValidatorBuilder baseDataValidator, final Locale locale,
            final Boolean isPrimaryGroupingByAmount) {

        if (this.fromApiJsonHelper.parameterExists(descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
            baseDataValidator.reset().parameter(descriptionParamName).value(description).ignoreIfNull();
        }

        if (this.fromApiJsonHelper.parameterExists(periodTypeParamName, element)) {
            final Integer periodType = this.fromApiJsonHelper.extractIntegerNamed(periodTypeParamName, element, locale);
            baseDataValidator.reset().parameter(periodTypeParamName).value(periodType)
                    .isOneOfTheseValues(PeriodFrequencyType.integerValues());
            if (isPrimaryGroupingByAmount != null && !isPrimaryGroupingByAmount) {
                baseDataValidator.reset().parameter(periodTypeParamName).value(periodType).notNull();
            }

        }

        Integer fromPeriod = null;
        Integer toPeriod = null;

        if (this.fromApiJsonHelper.parameterExists(fromPeriodParamName, element)) {
            fromPeriod = this.fromApiJsonHelper.extractIntegerNamed(fromPeriodParamName, element, locale);
            baseDataValidator.reset().parameter(fromPeriodParamName).value(fromPeriod).integerZeroOrGreater();
            if (isPrimaryGroupingByAmount != null && !isPrimaryGroupingByAmount) {
                baseDataValidator.reset().parameter(fromPeriodParamName).value(fromPeriod).notNull();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(toPeriodParamName, element)) {
            toPeriod = this.fromApiJsonHelper.extractIntegerNamed(toPeriodParamName, element, locale);
            baseDataValidator.reset().parameter(toPeriodParamName).value(toPeriod).integerGreaterThanZero();
        }

        if (fromPeriod != null && toPeriod != null) {
            if (fromPeriod > toPeriod) {
                baseDataValidator.parameter(fromPeriodParamName).value(fromPeriod).failWithCode("fromperiod.greater.than.toperiod");
            }
        }
        BigDecimal amountRangeFrom = null;
        BigDecimal amountRangeTo = null;
        if (this.fromApiJsonHelper.parameterExists(amountRangeFromParamName, element)) {
            amountRangeFrom = this.fromApiJsonHelper.extractBigDecimalNamed(amountRangeFromParamName, element, locale);
            baseDataValidator.reset().parameter(amountRangeFromParamName).value(amountRangeFrom).zeroOrPositiveAmount();
            if (isPrimaryGroupingByAmount != null && isPrimaryGroupingByAmount) {
                baseDataValidator.reset().parameter(amountRangeFromParamName).value(amountRangeFrom).notNull();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(amountRangeToParamName, element)) {
            amountRangeTo = this.fromApiJsonHelper.extractBigDecimalNamed(amountRangeToParamName, element, locale);
            baseDataValidator.reset().parameter(amountRangeToParamName).value(amountRangeTo).positiveAmount();
        }

        if (amountRangeFrom != null && amountRangeTo != null) {
            if (amountRangeFrom.compareTo(amountRangeTo) > 1) {
                baseDataValidator.parameter(fromPeriodParamName).value(fromPeriod).failWithCode("fromperiod.greater.than.toperiod");
            }
        }

        if (this.fromApiJsonHelper.parameterExists(annualInterestRateParamName, element)) {
            final BigDecimal annualInterestRate = this.fromApiJsonHelper.extractBigDecimalNamed(annualInterestRateParamName, element,
                    locale);
            baseDataValidator.reset().parameter(annualInterestRateParamName).value(annualInterestRate).notNull().zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(currencyCodeParamName, element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed(currencyCodeParamName, element);
            baseDataValidator.reset().parameter(currencyCodeParamName).value(currencyCode).notBlank().notExceedingLengthOf(3);
        }
        validateIncentives(element, baseDataValidator, locale);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validateIncentives(JsonElement element, DataValidatorBuilder baseDataValidator, final Locale locale) {

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            if (topLevelJsonElement.has(incentivesParamName) && topLevelJsonElement.get(incentivesParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(incentivesParamName).getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject incentiveElement = array.get(i).getAsJsonObject();
                    if (this.fromApiJsonHelper.parameterExists(InterestIncentiveApiConstants.idParamName, incentiveElement)) {
                        final Long id = this.fromApiJsonHelper
                                .extractLongNamed(InterestIncentiveApiConstants.idParamName, incentiveElement);
                        baseDataValidator.reset().parameter(InterestIncentiveApiConstants.idParamName).value(id).notNull()
                                .integerGreaterThanZero();
                        this.interestIncentiveDataValidator.validateIncentiveUpdate(incentiveElement, baseDataValidator, locale);
                    } else {
                        this.interestIncentiveDataValidator.validateIncentiveCreate(incentiveElement, baseDataValidator, locale);
                    }
                }
            }
        }
    }

}