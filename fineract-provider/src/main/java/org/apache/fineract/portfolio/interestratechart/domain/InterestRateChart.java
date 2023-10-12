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

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.deleteParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.idParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeFromParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeToParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.annualInterestRateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.fromPeriodParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.periodTypeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.toPeriodParamName;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants;
import org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;

@Entity
@Table(name = "m_interest_rate_chart")
public class InterestRateChart extends AbstractPersistableCustom {

    @Embedded
    private InterestRateChartFields chartFields;

    @OneToMany(mappedBy = "interestRateChart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<InterestRateChartSlab> chartSlabs = new HashSet<>();

    protected InterestRateChart() {
        //
    }

    public static InterestRateChart createNew(InterestRateChartFields chartFields,
            Collection<InterestRateChartSlab> interestRateChartSlabs) {

        return new InterestRateChart(chartFields, new HashSet<>(interestRateChartSlabs));
    }

    private InterestRateChart(InterestRateChartFields chartFields, Set<InterestRateChartSlab> interestRateChartSlabs) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_RESOURCE_NAME);
        this.chartFields = chartFields;
        // validate before setting the other fields
        this.validateChartSlabs(baseDataValidator);
        this.throwExceptionIfValidationWarningsExist(dataValidationErrors);

        this.addChartSlabs(interestRateChartSlabs);

    }

    public void validateChartSlabs(DataValidatorBuilder baseDataValidator) {
        Collection<InterestRateChartSlab> chartSlabs = this.setOfChartSlabs();

        Integer tmpPeriodType = null;
        List<InterestRateChartSlab> chartSlabsList = new ArrayList<>(chartSlabs);
        boolean isPrimaryGroupingByAmount = this.chartFields.isPrimaryGroupingByAmount();
        chartSlabsList.sort(new InterestRateChartSlabComparator<InterestRateChartSlab>(isPrimaryGroupingByAmount));
        boolean isPeriodChart = !isPrimaryGroupingByAmount;
        boolean isAmountChart = isPrimaryGroupingByAmount;

        for (int i = 0; i < chartSlabsList.size(); i++) {
            InterestRateChartSlab iSlabs = chartSlabsList.get(i);
            if (!iSlabs.slabFields().isValidChart(isPrimaryGroupingByAmount)) {
                if (isPrimaryGroupingByAmount) {
                    baseDataValidator.parameter(InterestRateChartSlabApiConstants.amountRangeFromParamName).failWithCode("cannot.be.blank");
                } else {
                    baseDataValidator.parameter(InterestRateChartSlabApiConstants.fromPeriodParamName).failWithCode("cannot.be.blank");
                }

            } else if (i > 0) {
                if (isPeriodChart ^ iSlabs.slabFields().fromPeriod() != null) {
                    baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.slabs.period.range.incomplete");
                    isPeriodChart = isPeriodChart || iSlabs.slabFields().fromPeriod() != null;
                }
                if (isAmountChart ^ iSlabs.slabFields().getAmountRangeFrom() != null) {
                    baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.slabs.amount.range.incomplete");
                    isAmountChart = isAmountChart || iSlabs.slabFields().getAmountRangeFrom() != null;
                }
            }

            if (i == 0) {
                tmpPeriodType = iSlabs.slabFields().periodType();
                if (iSlabs.slabFields().isNotProperChartStart()) {
                    baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.slabs.range.start.incorrect",
                            iSlabs.slabFields().fromPeriod(), iSlabs.slabFields().getAmountRangeFrom());
                }
                isAmountChart = isAmountChart || iSlabs.slabFields().getAmountRangeFrom() != null;
                isPeriodChart = isPeriodChart || iSlabs.slabFields().fromPeriod() != null;
            } else if (iSlabs.slabFields().periodType() != null && !iSlabs.slabFields().periodType().equals(tmpPeriodType)) {
                baseDataValidator.parameter(periodTypeParamName).value(iSlabs.slabFields().periodType())
                        .failWithCode("period.type.is.not.same", tmpPeriodType);
            }
            if (i + 1 < chartSlabsList.size()) {
                InterestRateChartSlab nextSlabs = chartSlabsList.get(i + 1);
                if (iSlabs.slabFields().isValidChart(isPrimaryGroupingByAmount)
                        && nextSlabs.slabFields().isValidChart(isPrimaryGroupingByAmount)) {
                    if (iSlabs.slabFields().isRateChartOverlapping(nextSlabs.slabFields(), isPrimaryGroupingByAmount)) {
                        baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.slabs.range.overlapping",
                                iSlabs.slabFields().fromPeriod(), iSlabs.slabFields().toPeriod(), nextSlabs.slabFields().fromPeriod(),
                                nextSlabs.slabFields().toPeriod(), iSlabs.slabFields().getAmountRangeFrom(),
                                iSlabs.slabFields().getAmountRangeTo(), nextSlabs.slabFields().getAmountRangeFrom(),
                                nextSlabs.slabFields().getAmountRangeTo());
                    } else if (iSlabs.slabFields().isRateChartHasGap(nextSlabs.slabFields(), isPrimaryGroupingByAmount)) {
                        baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.slabs.range.has.gap",
                                iSlabs.slabFields().fromPeriod(), iSlabs.slabFields().toPeriod(), nextSlabs.slabFields().fromPeriod(),
                                nextSlabs.slabFields().toPeriod(), iSlabs.slabFields().getAmountRangeFrom(),
                                iSlabs.slabFields().getAmountRangeTo(), nextSlabs.slabFields().getAmountRangeFrom(),
                                nextSlabs.slabFields().getAmountRangeTo());
                    }
                    if (isPrimaryGroupingByAmount) {
                        if (!iSlabs.slabFields().isAmountSame(nextSlabs.slabFields())) {
                            if (InterestRateChartSlabFields.isNotProperPeriodStart(nextSlabs.slabFields())) {
                                baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.slabs.period.range.start.incorrect",
                                        nextSlabs.slabFields().toPeriod());
                            }
                            if (iSlabs.slabFields().toPeriod() != null) {
                                baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.slabs.period.range.end.incorrect",
                                        iSlabs.slabFields().toPeriod());
                            }

                        }
                    } else if (!iSlabs.slabFields().isPeriodsSame(nextSlabs.slabFields())) {
                        if (InterestRateChartSlabFields.isNotProperAmountStart(nextSlabs.slabFields())) {
                            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.slabs.amount.range.start.incorrect",
                                    nextSlabs.slabFields().getAmountRangeFrom());
                        }
                        if (iSlabs.slabFields().getAmountRangeTo() != null) {
                            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.slabs.amount.range.end.incorrect",
                                    iSlabs.slabFields().getAmountRangeTo());
                        }

                    }
                }
            } else if (iSlabs.slabFields().isNotProperPriodEnd()) {
                baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.slabs.range.end.incorrect", iSlabs.slabFields().toPeriod(),
                        iSlabs.slabFields().getAmountRangeTo());
            }
        }
    }

    public void addChartSlabs(Collection<InterestRateChartSlab> interestRateChartSlabsSet) {
        Set<InterestRateChartSlab> existingChartSlabs = setOfChartSlabs();
        for (InterestRateChartSlab newChartSlabs : interestRateChartSlabsSet) {
            newChartSlabs.setInterestRateChart(this);
            existingChartSlabs.add(newChartSlabs);
        }
    }

    public void addChartSlab(InterestRateChartSlab newChartSlab) {
        newChartSlab.setInterestRateChart(this);
        setOfChartSlabs().add(newChartSlab);
    }

    public Set<InterestRateChartSlab> setOfChartSlabs() {
        if (this.chartSlabs == null) {
            this.chartSlabs = new HashSet<>();
        }
        return this.chartSlabs;
    }

    public void update(JsonCommand command, final Map<String, Object> actualChanges) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_RESOURCE_NAME);

        this.update(command, actualChanges, baseDataValidator, null, null);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void update(JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator,
            final Set<InterestRateChart> existingCharts, String currencyCode) {

        this.chartFields.update(command, actualChanges, baseDataValidator);

        // interestRateChartSlabs
        if (command.hasParameter(InterestRateChartApiConstants.chartSlabs)) {
            updateChartSlabs(command, actualChanges, baseDataValidator, currencyCode);
        }

        this.validateCharts(baseDataValidator, existingCharts);
    }

    private void validateCharts(final DataValidatorBuilder baseDataValidator, final Set<InterestRateChart> existingCharts) {

        for (InterestRateChart existingChart : existingCharts) {
            if (!existingChart.equals(this)) {
                if (this.chartFields.isOverlapping(existingChart.chartFields)) {
                    baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.overlapping.from.and.end.dates",
                            existingChart.getFromDate(), existingChart.getEndDate(), this.getFromDate(), this.getEndDate());
                }
            }
        }
    }

    public void updateChartSlabs(JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator,
            String currencyCode) {

        final Map<String, Object> deleteChartSlabs = new HashMap<>();
        final Map<String, Object> chartSlabsChanges = new HashMap<>();
        final Locale locale = command.extractLocale();
        if (command.hasParameter(InterestRateChartApiConstants.chartSlabs)) {
            final JsonArray array = command.arrayOfParameterNamed(InterestRateChartApiConstants.chartSlabs);
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject chartSlabsElement = array.get(i).getAsJsonObject();
                    JsonCommand chartSlabsCommand = JsonCommand.fromExistingCommand(command, chartSlabsElement);
                    if (chartSlabsCommand.parameterExists(idParamName)) {
                        final Long chartSlabId = chartSlabsCommand.longValueOfParameterNamed(idParamName);
                        final InterestRateChartSlab chartSlab = this.findChartSlab(chartSlabId);
                        if (chartSlab == null) {
                            baseDataValidator.parameter(idParamName).value(chartSlabId).failWithCode("no.chart.slab.associated.with.id");
                        } else if (chartSlabsCommand.parameterExists(deleteParamName)) {
                            if (this.removeChartSlab(chartSlab)) {
                                deleteChartSlabs.put(idParamName, chartSlabId);
                            }
                        } else {
                            chartSlab.update(chartSlabsCommand, chartSlabsChanges, baseDataValidator, locale);
                        }
                    } else {

                        /**
                         * TODO: AA: Move this code to InterestRateChartSlabAssembler
                         */
                        final String description = chartSlabsCommand.stringValueOfParameterNamed(descriptionParamName);
                        final Integer periodTypeId = chartSlabsCommand.integerValueOfParameterNamed(periodTypeParamName, locale);
                        final SavingsPeriodFrequencyType periodFrequencyType = SavingsPeriodFrequencyType.fromInt(periodTypeId);
                        final Integer fromPeriod = chartSlabsCommand.integerValueOfParameterNamed(fromPeriodParamName, locale);
                        final Integer toPeriod = chartSlabsCommand.integerValueOfParameterNamed(toPeriodParamName, locale);
                        final BigDecimal amountRangeFrom = chartSlabsCommand.bigDecimalValueOfParameterNamed(amountRangeFromParamName,
                                locale);
                        final BigDecimal amountRangeTo = chartSlabsCommand.bigDecimalValueOfParameterNamed(amountRangeToParamName, locale);
                        final BigDecimal annualInterestRate = chartSlabsCommand.bigDecimalValueOfParameterNamed(annualInterestRateParamName,
                                locale);

                        final InterestRateChartSlabFields slabFields = InterestRateChartSlabFields.createNew(description,
                                periodFrequencyType, fromPeriod, toPeriod, amountRangeFrom, amountRangeTo, annualInterestRate,
                                currencyCode);
                        final InterestRateChartSlab chartSlab = InterestRateChartSlab.createNew(slabFields, this);
                        chartSlab.slabFields().validateChartSlabPlatformRules(chartSlabsCommand, baseDataValidator, locale);
                        chartSlab.updateIncentives(chartSlabsCommand, actualChanges, baseDataValidator, chartSlab, locale);
                        this.addChartSlab(chartSlab);
                    }
                }
            }
        }

        // add chart slab changes to actual changes list.
        if (!chartSlabsChanges.isEmpty()) {
            actualChanges.put(InterestRateChartApiConstants.chartSlabs, chartSlabsChanges);
        }

        // add deleted chart Slabs to actual changes
        if (!deleteChartSlabs.isEmpty()) {
            actualChanges.put("deletedChartSlabs", deleteChartSlabs);
        }

        this.validateChartSlabs(baseDataValidator);
    }

    public InterestRateChartSlab findChartSlab(Long chartSlabId) {
        final Set<InterestRateChartSlab> chartSlabs = setOfChartSlabs();

        for (InterestRateChartSlab interestRateChartSlab : chartSlabs) {
            if (interestRateChartSlab.getId().equals(chartSlabId)) {
                return interestRateChartSlab;
            }
        }
        return null;
    }

    private boolean removeChartSlab(InterestRateChartSlab chartSlab) {
        final Set<InterestRateChartSlab> chartSlabs = setOfChartSlabs();
        return chartSlabs.remove(chartSlab);
    }

    public LocalDate getFromDate() {
        return this.chartFields.getFromDate();
    }

    public LocalDate getEndDate() {
        return this.chartFields.getEndDate();
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public InterestRateChartFields chartFields() {
        return this.chartFields;
    }

    public boolean isApplicableChartFor(final LocalDate target) {
        return this.chartFields.isApplicableChartFor(target);
    }
}
