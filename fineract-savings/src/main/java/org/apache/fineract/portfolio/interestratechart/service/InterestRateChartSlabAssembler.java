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
package org.apache.fineract.portfolio.interestratechart.service;

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.chartSlabs;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_RESOURCE_NAME;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeFromParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeToParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.annualInterestRateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.currencyCodeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.fromPeriodParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.periodTypeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.toPeriodParamName;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChart;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartRepositoryWrapper;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlabFields;
import org.apache.fineract.portfolio.interestratechart.exception.InterestRateChartSlabNotFoundException;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;

@RequiredArgsConstructor
public class InterestRateChartSlabAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper;
    private final InterestIncentiveAssembler incentiveAssembler;

    /**
     * Assembles a new {@link InterestRateChartSlab} from JSON Slabs passed in request
     */
    public InterestRateChartSlab assembleFrom(final JsonCommand command) {

        final JsonElement element = command.parsedJson();
        final JsonObject elementObject = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(elementObject);
        final String currencyCode = command.stringValueOfParameterNamed(currencyCodeParamName);

        final Long chartId = command.subentityId();// returns chart id

        final InterestRateChart chart = this.interestRateChartRepositoryWrapper.findOneWithNotFoundDetection(chartId);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);

        final InterestRateChartSlab newChartSlab = assembleChartSlabs(chart, elementObject, currencyCode, locale);
        // validate chart Slabs
        newChartSlab.slabFields().validateChartSlabPlatformRules(command, baseDataValidator, locale);
        chart.validateChartSlabs(baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        return newChartSlab;
    }

    private InterestRateChartSlab assembleChartSlabs(final InterestRateChart interestRateChart, final JsonElement element,
            final String currencyCode, final Locale locale) {
        final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
        final Integer periodTypeId = this.fromApiJsonHelper.extractIntegerNamed(periodTypeParamName, element, locale);
        final SavingsPeriodFrequencyType periodType = SavingsPeriodFrequencyType.fromInt(periodTypeId);
        final Integer fromPeriod = this.fromApiJsonHelper.extractIntegerNamed(fromPeriodParamName, element, locale);
        final Integer toPeriod = this.fromApiJsonHelper.extractIntegerNamed(toPeriodParamName, element, locale);
        final BigDecimal amountRangeFrom = this.fromApiJsonHelper.extractBigDecimalNamed(amountRangeFromParamName, element, locale);
        final BigDecimal amountRangeTo = this.fromApiJsonHelper.extractBigDecimalNamed(amountRangeToParamName, element, locale);
        final BigDecimal annualInterestRate = this.fromApiJsonHelper.extractBigDecimalNamed(annualInterestRateParamName, element, locale);

        final InterestRateChartSlabFields slabFields = InterestRateChartSlabFields.createNew(description, periodType, fromPeriod, toPeriod,
                amountRangeFrom, amountRangeTo, annualInterestRate, currencyCode);
        InterestRateChartSlab interestRateChartSlab = InterestRateChartSlab.createNew(slabFields, interestRateChart);
        this.incentiveAssembler.assembleIncentivesFrom(element, interestRateChartSlab, locale);
        return interestRateChartSlab;

    }

    public InterestRateChartSlab assembleFrom(final Long chartSlabId, final Long chartId) {
        final InterestRateChart chart = this.interestRateChartRepositoryWrapper.findOneWithNotFoundDetection(chartId);
        final InterestRateChartSlab interestRateChartSlab = chart.findChartSlab(chartSlabId);

        if (interestRateChartSlab == null) {
            throw new InterestRateChartSlabNotFoundException(chartSlabId, chartId);
        }

        return interestRateChartSlab;
    }

    public Collection<InterestRateChartSlab> assembleChartSlabsFrom(final JsonElement element, String currencyCode) {
        final Collection<InterestRateChartSlab> interestRateChartSlabsSet = new HashSet<>();

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.has(chartSlabs) && topLevelJsonElement.get(chartSlabs).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(chartSlabs).getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject interstRateChartElement = array.get(i).getAsJsonObject();
                    final InterestRateChartSlab chartSlab = this.assembleChartSlabs(null, interstRateChartElement, currencyCode, locale);
                    interestRateChartSlabsSet.add(chartSlab);
                }
            }
        }

        return interestRateChartSlabsSet;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
