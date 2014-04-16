/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.domain;

import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_RESOURCE_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_interest_rate_slab")
public class InterestRateChartSlab extends AbstractPersistable<Long> {

    @Embedded
    private InterestRateChartSlabFields slabFields;

    @ManyToOne(optional = false)
    @JoinColumn(name = "interest_rate_chart_id", referencedColumnName = "id", nullable = false)
    private InterestRateChart interestRateChart;

    protected InterestRateChartSlab() {
        //
    }

    public static InterestRateChartSlab createNew(InterestRateChartSlabFields slabFields, InterestRateChart interestRateChart) {
        return new InterestRateChartSlab(slabFields, interestRateChart);
    }

    public void update(JsonCommand command, final Map<String, Object> actualChanges) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);
        this.update(command, actualChanges, baseDataValidator);
        this.interestRateChart.validateChartSlabs(baseDataValidator);
        this.throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void update(JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator) {
        this.slabFields.update(command, actualChanges, baseDataValidator);
    }

    private InterestRateChartSlab(InterestRateChartSlabFields slabFields, InterestRateChart interestRateChart) {

        this.slabFields = slabFields;
        this.interestRateChart = interestRateChart;

        if (this.interestRateChart != null) {
            interestRateChart.addChartSlab(this);
        }
    }

    public void setInterestRateChart(InterestRateChart interestRateChart) {
        this.interestRateChart = interestRateChart;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
    
    public InterestRateChartSlabFields slabFields(){
        return this.slabFields;
    }
}