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

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.periodTypeParamName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChart;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlabComparator;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlabFields;

/**
 * Checks the rules that span several slabs of a chart, or several charts of a product. Errors are added to the given
 * builder, so that callers can report them together with their own.
 */
public final class InterestRateChartValidator {

    private InterestRateChartValidator() {}

    public static void validateChartSlabs(final InterestRateChart chart, final DataValidatorBuilder baseDataValidator) {
        Collection<InterestRateChartSlab> chartSlabs = chart.setOfChartSlabs();

        Integer tmpPeriodType = null;
        List<InterestRateChartSlab> chartSlabsList = new ArrayList<>(chartSlabs);
        boolean isPrimaryGroupingByAmount = chart.chartFields().isPrimaryGroupingByAmount();
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
                        // Use DataValidatorBuilder with resource/parameter context for clean error codes
                        DataValidatorBuilder v = new DataValidatorBuilder(baseDataValidator.getDataValidationErrors())
                                .resource("savings.interestRateChart").parameter("slabs");
                        v.failWithCode("overlap", iSlabs.slabFields().fromPeriod(), iSlabs.slabFields().toPeriod(),
                                nextSlabs.slabFields().fromPeriod(), nextSlabs.slabFields().toPeriod(),
                                iSlabs.slabFields().getAmountRangeFrom(), iSlabs.slabFields().getAmountRangeTo(),
                                nextSlabs.slabFields().getAmountRangeFrom(), nextSlabs.slabFields().getAmountRangeTo());
                    } else if (iSlabs.slabFields().isRateChartHasGap(nextSlabs.slabFields(), isPrimaryGroupingByAmount)) {
                        // Use DataValidatorBuilder with resource/parameter context for clean error codes
                        DataValidatorBuilder v = new DataValidatorBuilder(baseDataValidator.getDataValidationErrors())
                                .resource("savings.interestRateChart").parameter("slabs");
                        v.failWithCode("gap", iSlabs.slabFields().fromPeriod(), iSlabs.slabFields().toPeriod(),
                                nextSlabs.slabFields().fromPeriod(), nextSlabs.slabFields().toPeriod(),
                                iSlabs.slabFields().getAmountRangeFrom(), iSlabs.slabFields().getAmountRangeTo(),
                                nextSlabs.slabFields().getAmountRangeFrom(), nextSlabs.slabFields().getAmountRangeTo());
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
            }
        }
    }

    public static void validateNoOverlapWithOtherCharts(final InterestRateChart chart, final Set<InterestRateChart> existingCharts,
            final DataValidatorBuilder baseDataValidator) {
        for (InterestRateChart existingChart : existingCharts) {
            if (!existingChart.equals(chart)) {
                if (chart.chartFields().isOverlapping(existingChart.chartFields())) {
                    baseDataValidator.failWithCodeNoParameterAddedToErrorCode("chart.overlapping.from.and.end.dates",
                            existingChart.getFromDate(), existingChart.getEndDate(), chart.getFromDate(), chart.getEndDate());
                }
            }
        }
    }
}
