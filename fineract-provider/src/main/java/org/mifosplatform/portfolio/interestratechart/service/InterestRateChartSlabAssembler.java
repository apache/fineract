/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.chartSlabs;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_RESOURCE_NAME;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeFromParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeToParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.annualInterestRateParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.fromPeriodParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.periodTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.toPeriodParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartRepositoryWrapper;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartSlabFields;
import org.mifosplatform.portfolio.interestratechart.exception.InterestRateChartSlabNotFoundException;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class InterestRateChartSlabAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper;
    private final InterestIncentiveAssembler incentiveAssembler;

    @Autowired
    public InterestRateChartSlabAssembler(final FromJsonHelper fromApiJsonHelper,
            final InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper, final InterestIncentiveAssembler incentiveAssembler) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.interestRateChartRepositoryWrapper = interestRateChartRepositoryWrapper;
        this.incentiveAssembler = incentiveAssembler;
    }

    /**
     * Assembles a new {@link InterestRateChartSlab} from JSON Slabs passed in
     * request
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

        if (interestRateChartSlab == null) throw new InterestRateChartSlabNotFoundException(chartSlabId, chartId);

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
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}