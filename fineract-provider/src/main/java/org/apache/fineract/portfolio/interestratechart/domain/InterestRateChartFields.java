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
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.localeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.nameParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.isPrimaryGroupingByAmountParamName;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.joda.time.LocalDate;

@Embeddable
public class InterestRateChartFields {

    @Column(name = "name", length = 100, unique = false, nullable = false)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;

    @Temporal(TemporalType.DATE)
    @Column(name = "from_date", nullable = false)
    private Date fromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date", nullable = true)
    private Date endDate;

    @Column(name = "is_primary_grouping_by_amount", nullable = false)
    private boolean isPrimaryGroupingByAmount;

    protected InterestRateChartFields() {
        //
    }

    public static InterestRateChartFields createNew(String name, String description, LocalDate fromDate, LocalDate toDate,
            boolean isPrimaryGroupingByAmount) {
        return new InterestRateChartFields(name, description, fromDate, toDate, isPrimaryGroupingByAmount);
    }

    private InterestRateChartFields(String name, String description, LocalDate fromDate, LocalDate toDate, boolean isPrimaryGroupingByAmount) {
        this.name = name;
        this.description = description;
        this.fromDate = fromDate.toDate();
        this.endDate = (toDate == null) ? null : toDate.toDate();
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

        if (command.isChangeInLocalDateParameterNamed(fromDateParamName, getFromDateAsLocalDate())) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(fromDateParamName);
            final String newValueAsString = command.stringValueOfParameterNamed(fromDateParamName);
            actualChanges.put(fromDateParamName, newValueAsString);
            actualChanges.put(localeParamName, localeAsInput);
            actualChanges.put(dateFormatParamName, dateFormat);
            this.fromDate = newValue.toDate();
        }

        if (command.isChangeInLocalDateParameterNamed(endDateParamName, getEndDateAsLocalDate())) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(endDateParamName);
            final String newValueAsString = command.stringValueOfParameterNamed(endDateParamName);
            actualChanges.put(endDateParamName, newValueAsString);
            actualChanges.put(localeParamName, localeAsInput);
            actualChanges.put(dateFormatParamName, dateFormat);
            this.endDate = newValue.toDate();
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
        return isFromDateAfter(getEndDateAsLocalDate());
    }

    public boolean isFromDateAfter(LocalDate compare) {
        final LocalDate fromDate = getFromDateAsLocalDate();
        if (fromDate != null && compare != null) { return fromDate.isAfter(compare); }
        return false;
    }

    public LocalDate getFromDateAsLocalDate() {
        LocalDate fromDate = null;
        if (this.fromDate != null) {
            fromDate = new LocalDate(this.fromDate);
        }
        return fromDate;
    }

    public LocalDate getEndDateAsLocalDate() {
        LocalDate endDate = null;
        if (this.endDate != null) {
            endDate = new LocalDate(this.endDate);
        }
        return endDate;
    }

    public boolean isOverlapping(InterestRateChartFields that) {
        final LocalDate thisFromDate = this.getFromDateAsLocalDate();
        LocalDate thisEndDate = this.getEndDateAsLocalDate();
        thisEndDate = thisEndDate == null ? DateUtils.getLocalDateOfTenant() : thisEndDate;
        final LocalDate thatFromDate = that.getFromDateAsLocalDate();
        LocalDate thatEndDate = that.getEndDateAsLocalDate();
        thatEndDate = thatEndDate == null ? DateUtils.getLocalDateOfTenant() : thatEndDate;

        final LocalDateInterval thisInterval = LocalDateInterval.create(thisFromDate, thisEndDate);
        final LocalDateInterval thatInterval = LocalDateInterval.create(thatFromDate, thatEndDate);

        if (thisInterval.containsPortionOf(thatInterval) || thatInterval.containsPortionOf(thisInterval)) { return true; }
        return false;// no overlapping
    }

    public boolean isApplicableChartFor(final LocalDate target) {
        final LocalDate endDate = this.endDate == null ? DateUtils.getLocalDateOfTenant() : this.getEndDateAsLocalDate();
        final LocalDateInterval interval = LocalDateInterval.create(getFromDateAsLocalDate(), endDate);
        return interval.contains(target);
    }

    public boolean isPrimaryGroupingByAmount() {
        return this.isPrimaryGroupingByAmount;
    }

}