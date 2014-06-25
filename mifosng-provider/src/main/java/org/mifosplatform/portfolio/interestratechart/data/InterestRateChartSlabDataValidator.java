/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.data;

import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_CREATE_REQUEST_DATA_PARAMETERS;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_RESOURCE_NAME;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_UPDATE_REQUEST_DATA_PARAMETERS;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeFromParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeToParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.annualInterestRateParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.fromPeriodParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.incentivesParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.periodTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.toPeriodParamName;

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
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants;
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

        validateChartSlabsCreate(element, baseDataValidator, locale);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChartSlabsCreate(final JsonElement element, final DataValidatorBuilder baseDataValidator, final Locale locale) {

        if (this.fromApiJsonHelper.parameterExists(descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
            baseDataValidator.reset().parameter(descriptionParamName).value(description).notNull();
        }

        final Integer periodType = this.fromApiJsonHelper.extractIntegerNamed(periodTypeParamName, element, locale);
        baseDataValidator.reset().parameter(periodTypeParamName).value(periodType).notNull()
                .isOneOfTheseValues(PeriodFrequencyType.integerValues());

        Integer toPeriod = null;

        final Integer fromPeriod = this.fromApiJsonHelper.extractIntegerNamed(fromPeriodParamName, element, locale);
        baseDataValidator.reset().parameter(fromPeriodParamName).value(fromPeriod).notNull().integerZeroOrGreater();

        if (this.fromApiJsonHelper.parameterExists(toPeriodParamName, element)) {
            toPeriod = this.fromApiJsonHelper.extractIntegerNamed(toPeriodParamName, element, locale);
            baseDataValidator.reset().parameter(toPeriodParamName).value(toPeriod).notNull().integerZeroOrGreater();
        }

        if (fromPeriod != null && toPeriod != null) {
            if (fromPeriod > toPeriod) {
                baseDataValidator.parameter(fromPeriodParamName).value(fromPeriod).failWithCode("fromperiod.greater.than.to.period");
            }
        }
        BigDecimal amountRangeFrom = null;
        BigDecimal amountRangeTo = null;
        if (this.fromApiJsonHelper.parameterExists(amountRangeFromParamName, element)) {
            amountRangeFrom = this.fromApiJsonHelper.extractBigDecimalNamed(amountRangeFromParamName, element, locale);
            baseDataValidator.reset().parameter(amountRangeFromParamName).value(amountRangeFrom).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(amountRangeToParamName, element)) {
            amountRangeTo = this.fromApiJsonHelper.extractBigDecimalNamed(amountRangeToParamName, element, locale);
            baseDataValidator.reset().parameter(amountRangeToParamName).value(amountRangeTo).notNull().positiveAmount();
        }

        if (amountRangeFrom != null && amountRangeTo != null) {
            if (amountRangeFrom.compareTo(amountRangeTo) > 1) {
                baseDataValidator.parameter(fromPeriodParamName).value(fromPeriod).failWithCode("fromperiod.greater.than.toperiod");
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
        validateChartSlabsUpdate(element, baseDataValidator, locale);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChartSlabsUpdate(final JsonElement element, final DataValidatorBuilder baseDataValidator, final Locale locale) {

        if (this.fromApiJsonHelper.parameterExists(descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
            baseDataValidator.reset().parameter(descriptionParamName).value(description).ignoreIfNull();
        }

        if (this.fromApiJsonHelper.parameterExists(periodTypeParamName, element)) {
            final Integer periodType = this.fromApiJsonHelper.extractIntegerNamed(periodTypeParamName, element, locale);
            baseDataValidator.reset().parameter(periodTypeParamName).value(periodType).notNull()
                    .isOneOfTheseValues(PeriodFrequencyType.integerValues());
        }

        Integer fromPeriod = null;
        Integer toPeriod = null;

        if (this.fromApiJsonHelper.parameterExists(fromPeriodParamName, element)) {
            fromPeriod = this.fromApiJsonHelper.extractIntegerNamed(fromPeriodParamName, element, locale);
            baseDataValidator.reset().parameter(fromPeriodParamName).value(fromPeriod).notNull().integerGreaterThanNumber(-1);
        }

        if (this.fromApiJsonHelper.parameterExists(toPeriodParamName, element)) {
            toPeriod = this.fromApiJsonHelper.extractIntegerNamed(toPeriodParamName, element, locale);
            baseDataValidator.reset().parameter(toPeriodParamName).value(toPeriod).notNull().integerGreaterThanNumber(-1);
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
            baseDataValidator.reset().parameter(amountRangeFromParamName).value(amountRangeFrom).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(amountRangeToParamName, element)) {
            amountRangeTo = this.fromApiJsonHelper.extractBigDecimalNamed(amountRangeToParamName, element, locale);
            baseDataValidator.reset().parameter(amountRangeToParamName).value(amountRangeTo).notNull().positiveAmount();
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