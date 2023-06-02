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
package org.apache.fineract.portfolio.interestratechart.domain;

import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.amountParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.attributeNameParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.attributeValueParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.conditionTypeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.entityTypeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants.incentiveTypeparamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.deleteParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.idParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_RESOURCE_NAME;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.interestratechart.InterestIncentiveApiConstants;
import org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants;

@Entity
@Table(name = "m_interest_rate_slab")
public class InterestRateChartSlab extends AbstractPersistableCustom {

    @Embedded
    private InterestRateChartSlabFields slabFields;

    @ManyToOne(optional = false)
    @JoinColumn(name = "interest_rate_chart_id", referencedColumnName = "id", nullable = false)
    private InterestRateChart interestRateChart;

    @OneToMany(mappedBy = "interestRateChartSlab", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
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
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
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
            if (interestIncentive.getId().equals(interestIncentiveId)) {
                return interestIncentive;
            }
        }
        return null;
    }

    public boolean removeInterestIncentive(InterestIncentives incentive) {
        final Set<InterestIncentives> incentives = setOfInterestIncentives();
        return incentives.remove(incentive);
    }

    public void updateIncentives(JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator,
            final InterestRateChartSlab chartSlab, final Locale locale) {
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
