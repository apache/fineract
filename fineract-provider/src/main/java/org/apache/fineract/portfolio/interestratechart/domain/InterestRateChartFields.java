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

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.dateFormatParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.endDateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.fromDateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.isPrimaryGroupingByAmountParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.localeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.nameParamName;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Embeddable
public class InterestRateChartFields {

    @Column(name = "name", length = 100, unique = false, nullable = false)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    @Column(name = "is_primary_grouping_by_amount", nullable = false)
    private boolean isPrimaryGroupingByAmount;

    protected InterestRateChartFields() {
        //
    }

    public static InterestRateChartFields createNew(String name, String description, LocalDate fromDate, LocalDate toDate,
            boolean isPrimaryGroupingByAmount) {
        return new InterestRateChartFields(name, description, fromDate, toDate, isPrimaryGroupingByAmount);
    }

    private InterestRateChartFields(String name, String description, LocalDate fromDate, LocalDate toDate,
            boolean isPrimaryGroupingByAmount) {
        this.name = name;
        this.description = description;
        this.fromDate = fromDate;
        this.endDate = toDate;
        this.isPrimaryGroupingByAmount = isPrimaryGroupingByAmount;
    }

    public void update(JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator) {

        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        final String localeAsInput = command.locale();
        final String dateFormat = command.dateFormat();

        if (command.isChangeInLocalDateParameterNamed(fromDateParamName, getFromDate())) {
            final String newValueAsString = command.stringValueOfParameterNamed(fromDateParamName);
            actualChanges.put(fromDateParamName, newValueAsString);
            actualChanges.put(localeParamName, localeAsInput);
            actualChanges.put(dateFormatParamName, dateFormat);
            this.fromDate = command.localDateValueOfParameterNamed(fromDateParamName);
        }

        if (command.isChangeInLocalDateParameterNamed(endDateParamName, getEndDate())) {
            final String newValueAsString = command.stringValueOfParameterNamed(endDateParamName);
            actualChanges.put(endDateParamName, newValueAsString);
            actualChanges.put(localeParamName, localeAsInput);
            actualChanges.put(dateFormatParamName, dateFormat);
            this.endDate = command.localDateValueOfParameterNamed(endDateParamName);
        }

        if (command.isChangeInBooleanParameterNamed(isPrimaryGroupingByAmountParamName, this.isPrimaryGroupingByAmount)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isPrimaryGroupingByAmountParamName);
            actualChanges.put(isPrimaryGroupingByAmountParamName, newValue);
            this.isPrimaryGroupingByAmount = newValue;
        }

        if (isFromDateAfterToDate()) {
            baseDataValidator.parameter(fromDateParamName).value(fromDate).failWithCode("from.date.is.after.to.date");
        }
    }

    public boolean isFromDateAfterToDate() {
        return isFromDateAfter(getEndDate());
    }

    public boolean isFromDateAfter(LocalDate compare) {
        return compare != null && DateUtils.isAfter(getFromDate(), compare);
    }

    public LocalDate getFromDate() {
        return this.fromDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public boolean isOverlapping(InterestRateChartFields that) {
        final LocalDate thisFromDate = this.getFromDate();
        LocalDate thisEndDate = this.getEndDate();
        thisEndDate = thisEndDate == null ? DateUtils.getBusinessLocalDate() : thisEndDate;
        final LocalDate thatFromDate = that.getFromDate();
        LocalDate thatEndDate = that.getEndDate();
        thatEndDate = thatEndDate == null ? DateUtils.getBusinessLocalDate() : thatEndDate;

        final LocalDateInterval thisInterval = LocalDateInterval.create(thisFromDate, thisEndDate);
        final LocalDateInterval thatInterval = LocalDateInterval.create(thatFromDate, thatEndDate);

        if (thisInterval.containsPortionOf(thatInterval) || thatInterval.containsPortionOf(thisInterval)) {
            return true;
        }
        return false;// no overlapping
    }

    public boolean isApplicableChartFor(final LocalDate target) {
        final LocalDate endDate = this.endDate == null ? DateUtils.getBusinessLocalDate() : this.getEndDate();
        final LocalDateInterval interval = LocalDateInterval.create(getFromDate(), endDate);
        return interval.contains(target);
    }

    public boolean isPrimaryGroupingByAmount() {
        return this.isPrimaryGroupingByAmount;
    }

}
