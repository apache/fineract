/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * This Source Code Form is subject to the terms of the Mozilla Public
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.domain;

import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.deleteParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.idParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeFromParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeToParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.annualInterestRateParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.fromPeriodParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.periodTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.toPeriodParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Entity
@Table(name = "m_interest_rate_chart")
public class InterestRateChart extends AbstractPersistable<Long> {

    @Embedded
    private InterestRateChartFields chartFields;

    @OneToMany(mappedBy = "interestRateChart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<InterestRateChartSlab> chartSlabs = new HashSet<>();

    protected InterestRateChart() {
        //
    }

    public static InterestRateChart createNew(InterestRateChartFields chartFields, Collection<InterestRateChartSlab> interestRateChartSlabs) {

        return new InterestRateChart(chartFields, new HashSet<>(interestRateChartSlabs));
    }

    private InterestRateChart(InterestRateChartFields chartFields, Set<InterestRateChartSlab> interestRateChartSlabs) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_RESOURCE_NAME);

        // validate before setting the other fields
        this.validateChartSlabs(baseDataValidator);
        this.throwExceptionIfValidationWarningsExist(dataValidationErrors);

        this.chartFields = chartFields;
        this.addChartSlabs(interestRateChartSlabs);

    }

    public void validateChartSlabs(DataValidatorBuilder baseDataValidator) {
        Collection<InterestRateChartSlab> chartSlabs = this.setOfChartSlabs();

        Integer tmpPeriodType = null;
        List<InterestRateChartSlab> chartSlabsList = new ArrayList<>(chartSlabs);

        for (int i = 0; i < chartSlabsList.size(); i++) {
            InterestRateChartSlab iSlabs = chartSlabsList.get(i);
            if (tmpPeriodType == null) {
                tmpPeriodType = iSlabs.slabFields().periodType();
            } else if (!iSlabs.slabFields().periodType().equals(tmpPeriodType)) {
                baseDataValidator.parameter(periodTypeParamName).value(iSlabs.slabFields().periodType())
                        .failWithCode("period.type.is.not.same", tmpPeriodType);
            }
            for (int j = i + 1; j < chartSlabsList.size(); j++) {
                InterestRateChartSlab jSlabs = chartSlabsList.get(j);
                if (iSlabs.slabFields().isPeriodOverlapping(jSlabs.slabFields())) {
                    baseDataValidator
                            .failWithCodeNoParameterAddedToErrorCode("chart.slabs.period.overlapping", iSlabs.slabFields().fromPeriod(),
                                    iSlabs.slabFields().toPeriod(), jSlabs.slabFields().fromPeriod(), jSlabs.slabFields().toPeriod());
                }
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
                            existingChart.getFromDateAsLocalDate(), existingChart.getEndDateAsLocalDate(), this.getFromDateAsLocalDate(),
                            this.getEndDateAsLocalDate());
                }
            }
        }
    }

    public void updateChartSlabs(JsonCommand command, final Map<String, Object> actualChanges,
            final DataValidatorBuilder baseDataValidator, String currencyCode) {

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
                         * TODO: AA: Move this code to
                         * InterestRateChartSlabAssembler
                         */
                        final String description = chartSlabsCommand.stringValueOfParameterNamed(descriptionParamName);
                        final Integer periodTypeId = chartSlabsCommand.integerValueOfParameterNamed(periodTypeParamName, locale);
                        final SavingsPeriodFrequencyType periodFrequencyType = SavingsPeriodFrequencyType.fromInt(periodTypeId);
                        final Integer fromPeriod = chartSlabsCommand.integerValueOfParameterNamed(fromPeriodParamName, locale);
                        final Integer toPeriod = chartSlabsCommand.integerValueOfParameterNamed(toPeriodParamName, locale);
                        final BigDecimal amountRangeFrom = chartSlabsCommand.bigDecimalValueOfParameterNamed(amountRangeFromParamName,
                                locale);
                        final BigDecimal amountRangeTo = chartSlabsCommand.bigDecimalValueOfParameterNamed(amountRangeToParamName, locale);
                        final BigDecimal annualInterestRate = chartSlabsCommand.bigDecimalValueOfParameterNamed(
                                annualInterestRateParamName, locale);

                        final InterestRateChartSlabFields slabFields = InterestRateChartSlabFields
                                .createNew(description, periodFrequencyType, fromPeriod, toPeriod, amountRangeFrom, amountRangeTo,
                                        annualInterestRate, currencyCode);
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
            if (interestRateChartSlab.getId().equals(chartSlabId)) { return interestRateChartSlab; }
        }
        return null;
    }

    private boolean removeChartSlab(InterestRateChartSlab chartSlab) {
        final Set<InterestRateChartSlab> chartSlabs = setOfChartSlabs();
        return chartSlabs.remove(chartSlab);
    }

    public LocalDate getFromDateAsLocalDate() {
        return this.chartFields.getFromDateAsLocalDate();
    }

    public LocalDate getEndDateAsLocalDate() {
        return this.chartFields.getEndDateAsLocalDate();
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public InterestRateChartFields chartFields() {
        return this.chartFields;
    }

    public boolean isApplicableChartFor(final LocalDate target) {
        return this.chartFields.isApplicableChartFor(target);
    }
}