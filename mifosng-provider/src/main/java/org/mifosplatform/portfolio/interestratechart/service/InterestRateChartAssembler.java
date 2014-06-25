/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.endDateParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.fromDateParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.nameParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.currencyCodeParamName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartRepositoryWrapper;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartFields;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class InterestRateChartAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper;
    private final InterestRateChartSlabAssembler chartSlabAssembler;

    @Autowired
    public InterestRateChartAssembler(final FromJsonHelper fromApiJsonHelper,
            final InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper,
            final InterestRateChartSlabAssembler chartSlabAssembler) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.interestRateChartRepositoryWrapper = interestRateChartRepositoryWrapper;
        this.chartSlabAssembler = chartSlabAssembler;
    }

    /**
     * Assembles a new {@link InterestRateChart} from JSON Slabs passed in
     * request
     */
    public InterestRateChart assembleFrom(final JsonCommand command) {

        final JsonElement element = command.parsedJson();
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        @SuppressWarnings("unused")
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_RESOURCE_NAME);
        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(currencyCodeParamName, element);
        final InterestRateChart newChart = this.assembleFrom(element, currencyCode);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        return newChart;
    }

    public InterestRateChart assembleFrom(final JsonElement element, final String currencyCode) {

        final String name = this.fromApiJsonHelper.extractStringNamed(nameParamName, element);
        final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
        final LocalDate fromDate = this.fromApiJsonHelper.extractLocalDateNamed(fromDateParamName, element);
        final LocalDate toDate = this.fromApiJsonHelper.extractLocalDateNamed(endDateParamName, element);
        

        // assemble chart Slabs
        final Collection<InterestRateChartSlab> newChartSlabs = this.chartSlabAssembler.assembleChartSlabsFrom(element,
                currencyCode);

        final InterestRateChartFields fields = InterestRateChartFields.createNew(name, description, fromDate, toDate);
        final InterestRateChart newChart = InterestRateChart.createNew(fields, newChartSlabs);
        return newChart;
    }

    public InterestRateChart assembleFrom(final Long interestRateChartId) {
        final InterestRateChart interestRateChart = this.interestRateChartRepositoryWrapper
                .findOneWithNotFoundDetection(interestRateChartId);
        return interestRateChart;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}