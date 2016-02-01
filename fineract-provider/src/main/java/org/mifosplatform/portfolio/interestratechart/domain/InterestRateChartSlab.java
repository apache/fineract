/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.domain;

import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.amountParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.attributeNameParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.attributeValueParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.conditionTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.entityTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.incentiveTypeparamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.deleteParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.idParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_RESOURCE_NAME;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants;
import org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Entity
@Table(name = "m_interest_rate_slab")
public class InterestRateChartSlab extends AbstractPersistable<Long> {

    @Embedded
    private InterestRateChartSlabFields slabFields;

    @ManyToOne(optional = false)
    @JoinColumn(name = "interest_rate_chart_id", referencedColumnName = "id", nullable = false)
    private InterestRateChart interestRateChart;

    @OneToMany(mappedBy = "interestRateChartSlab", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<InterestIncentives> interestIncentives = new HashSet<>();

    protected InterestRateChartSlab() {
        //
    }

    public static InterestRateChartSlab createNew(InterestRateChartSlabFields slabFields, InterestRateChart interestRateChart) {
        return new InterestRateChartSlab(slabFields, interestRateChart);
    }

    public void update(JsonCommand command, final Map<String, Object> actualChanges, final Locale locale) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);
        this.update(command, actualChanges, baseDataValidator, locale);
        this.interestRateChart.validateChartSlabs(baseDataValidator);
        this.throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void update(JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator,
            final Locale locale) {
        this.slabFields.update(command, actualChanges, baseDataValidator, locale);
        updateIncentives(command, actualChanges, baseDataValidator, this, locale);
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

    public InterestRateChartSlabFields slabFields() {
        return this.slabFields;
    }

    public Set<InterestIncentives> setOfInterestIncentives() {
        if (this.interestIncentives == null) {
            this.interestIncentives = new HashSet<>();
        }
        return this.interestIncentives;
    }

    public void addInterestIncentive(InterestIncentives interestIncentives) {
        interestIncentives.updateInterestRateChartSlab(this);
        setOfInterestIncentives().add(interestIncentives);
    }

    public InterestIncentives findInterestIncentive(Long interestIncentiveId) {
        final Set<InterestIncentives> interestIncentives = setOfInterestIncentives();

        for (InterestIncentives interestIncentive : interestIncentives) {
            if (interestIncentive.getId().equals(interestIncentiveId)) { return interestIncentive; }
        }
        return null;
    }

    public boolean removeInterestIncentive(InterestIncentives incentive) {
        final Set<InterestIncentives> incentives = setOfInterestIncentives();
        return incentives.remove(incentive);
    }

    public void updateIncentives(JsonCommand command, final Map<String, Object> actualChanges,
            final DataValidatorBuilder baseDataValidator, final InterestRateChartSlab chartSlab, final Locale locale) {
        final Map<String, Object> deleteIncentives = new HashMap<>();
        final Map<String, Object> IncentiveChanges = new HashMap<>();
        if (command.hasParameter(InterestRateChartSlabApiConstants.incentivesParamName)) {
            final JsonArray array = command.arrayOfParameterNamed(InterestRateChartSlabApiConstants.incentivesParamName);
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject incentiveElement = array.get(i).getAsJsonObject();
                    JsonCommand incentivesCommand = JsonCommand.fromExistingCommand(command, incentiveElement);
                    if (incentivesCommand.parameterExists(InterestIncentiveApiConstants.idParamName)) {
                        final Long interestIncentiveId = incentivesCommand
                                .longValueOfParameterNamed(InterestIncentiveApiConstants.idParamName);
                        final InterestIncentives interestIncentives = chartSlab.findInterestIncentive(interestIncentiveId);
                        if (interestIncentives == null) {
                            baseDataValidator.parameter(InterestIncentiveApiConstants.idParamName).value(interestIncentiveId)
                                    .failWithCode("no.interest.incentive.associated.with.id");
                        } else if (incentivesCommand.parameterExists(deleteParamName)) {
                            if (chartSlab.removeInterestIncentive(interestIncentives)) {
                                deleteIncentives.put(idParamName, interestIncentiveId);
                            }
                        } else {
                            interestIncentives.update(incentivesCommand, IncentiveChanges, baseDataValidator, locale);
                        }
                    } else {
                        Integer entityType = incentivesCommand.integerValueOfParameterNamed(entityTypeParamName, locale);
                        Integer conditionType = incentivesCommand.integerValueOfParameterNamed(conditionTypeParamName, locale);
                        Integer attributeName = incentivesCommand.integerValueOfParameterNamed(attributeNameParamName, locale);
                        String attributeValue = incentivesCommand.stringValueOfParameterNamed(attributeValueParamName);
                        Integer incentiveType = incentivesCommand.integerValueOfParameterNamed(incentiveTypeparamName, locale);
                        BigDecimal amount = incentivesCommand.bigDecimalValueOfParameterNamed(amountParamName, locale);
                        InterestIncentivesFields incentivesFields = InterestIncentivesFields.createNew(entityType, attributeName,
                                conditionType, attributeValue, incentiveType, amount, baseDataValidator);
                        InterestIncentives incentives = new InterestIncentives(chartSlab, incentivesFields);
                        chartSlab.addInterestIncentive(incentives);
                    }
                }
            }
            // add chart slab changes to actual changes list.
            if (!IncentiveChanges.isEmpty()) {
                actualChanges.put(InterestRateChartSlabApiConstants.incentivesParamName, IncentiveChanges);
            }

            // add deleted chart Slabs to actual changes
            if (!deleteIncentives.isEmpty()) {
                actualChanges.put("deletedIncentives", deleteIncentives);
            }

        }
    }

}