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

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.chartSlabs;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.endDateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.fromDateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.idParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.isPrimaryGroupingByAmountParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.nameParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.productIdParamName;

import java.lang.reflect.Type;
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
import org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class InterestRateChartDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final InterestRateChartSlabDataValidator chartSlabDataValidator;
	private static final Set<String> INTERESTRATE_CHART_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			InterestRateChartApiConstants.localeParamName, InterestRateChartApiConstants.dateFormatParamName,
			nameParamName, descriptionParamName, fromDateParamName, endDateParamName, productIdParamName, chartSlabs,
			isPrimaryGroupingByAmountParamName));

	private static final Set<String> INTERESTRATE_CHART_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			InterestRateChartApiConstants.localeParamName, InterestRateChartApiConstants.dateFormatParamName,
			idParamName, nameParamName, descriptionParamName, fromDateParamName, endDateParamName, chartSlabs,
			InterestRateChartApiConstants.deleteParamName, isPrimaryGroupingByAmountParamName));

    @Autowired
    public InterestRateChartDataValidator(final FromJsonHelper fromApiJsonHelper,
            final InterestRateChartSlabDataValidator chartSlabDataValidator) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.chartSlabDataValidator = chartSlabDataValidator;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_RESOURCE_NAME);

        validateForCreate(json, baseDataValidator);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForCreate(final String json, final DataValidatorBuilder baseDataValidator) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, INTERESTRATE_CHART_CREATE_REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(nameParamName, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(nameParamName, element);
            baseDataValidator.reset().parameter(nameParamName).value(name).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
            baseDataValidator.reset().parameter(descriptionParamName).value(description).notNull();
        }

        final LocalDate fromDate = this.fromApiJsonHelper.extractLocalDateNamed(fromDateParamName, element);
        baseDataValidator.reset().parameter(fromDateParamName).value(fromDate).notNull();

        LocalDate toDate = null;
        if (this.fromApiJsonHelper.parameterExists(endDateParamName, element)) {
            toDate = this.fromApiJsonHelper.extractLocalDateNamed(endDateParamName, element);
            baseDataValidator.reset().parameter(endDateParamName).value(toDate).notNull();
        }
        
        Boolean isPrimaryGroupingByAmount = this.fromApiJsonHelper.extractBooleanNamed(isPrimaryGroupingByAmountParamName, element);
        if (isPrimaryGroupingByAmount == null) {
            isPrimaryGroupingByAmount = false;
        }

        if (fromDate != null && toDate != null) {
            if (fromDate.isAfter(toDate)) {
                baseDataValidator.parameter(fromDateParamName).value(fromDate).failWithCode("from.date.is.after.to.date");
            }
        }

        // validate chart Slabs
        validateChartSlabs(element, baseDataValidator, isPrimaryGroupingByAmount);
    }

    public void validateUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_RESOURCE_NAME);
        this.validateForUpdate(json, baseDataValidator);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json, final DataValidatorBuilder baseDataValidator) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, INTERESTRATE_CHART_UPDATE_REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(productIdParamName, element)) {
            final Long savingsProductId = this.fromApiJsonHelper.extractLongNamed(productIdParamName, element);
            baseDataValidator.reset().parameter(productIdParamName).value(savingsProductId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(nameParamName, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(nameParamName, element);
            baseDataValidator.reset().parameter(nameParamName).value(name).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
            baseDataValidator.reset().parameter(descriptionParamName).value(description).notNull();
        }

        LocalDate fromDate = null;
        if (this.fromApiJsonHelper.parameterExists(fromDateParamName, element)) {
            fromDate = this.fromApiJsonHelper.extractLocalDateNamed(fromDateParamName, element);
            baseDataValidator.reset().parameter(fromDateParamName).value(fromDate).notNull();
        }

        LocalDate toDate = null;
        if (this.fromApiJsonHelper.parameterExists(endDateParamName, element)) {
            toDate = this.fromApiJsonHelper.extractLocalDateNamed(endDateParamName, element);
            baseDataValidator.reset().parameter(endDateParamName).value(toDate).notNull();
        }
        
        Boolean isPrimaryGroupingByAmount = this.fromApiJsonHelper.extractBooleanNamed(isPrimaryGroupingByAmountParamName, element);
        if (isPrimaryGroupingByAmount == null) {
            isPrimaryGroupingByAmount = false;
        }

        if (fromDate != null && toDate != null) {
            if (fromDate.isAfter(toDate)) {
                baseDataValidator.parameter(fromDateParamName).value(fromDate).failWithCode("from.date.is.after.to.date");
            }
        }

        validateChartSlabs(element, baseDataValidator, isPrimaryGroupingByAmount);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validateChartSlabs(JsonElement element, DataValidatorBuilder baseDataValidator,final boolean isPrimaryGroupingByAmount) {

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.has(chartSlabs) && topLevelJsonElement.get(chartSlabs).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(chartSlabs).getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject interstRateChartElement = array.get(i).getAsJsonObject();
                    if (this.fromApiJsonHelper.parameterExists(idParamName, interstRateChartElement)) {
                        final Long id = this.fromApiJsonHelper.extractLongNamed(idParamName, interstRateChartElement);
                        baseDataValidator.reset().parameter(idParamName).value(id).notNull().integerGreaterThanZero();
                        this.chartSlabDataValidator.validateChartSlabsUpdate(interstRateChartElement, baseDataValidator, locale, isPrimaryGroupingByAmount);
                    } else {
                        this.chartSlabDataValidator.validateChartSlabsCreate(interstRateChartElement, baseDataValidator, locale, isPrimaryGroupingByAmount);
                    }
                }
            }
        }
    }
}