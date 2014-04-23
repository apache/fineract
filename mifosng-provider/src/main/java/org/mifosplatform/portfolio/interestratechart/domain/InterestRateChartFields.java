/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.domain;

import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.dateFormatParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.endDateParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.fromDateParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.localeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.nameParamName;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.infrastructure.core.service.DateUtils;

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
    @Column(name = "end_date", nullable = false)
    private Date endDate;

    protected InterestRateChartFields() {
        //
    }

    public static InterestRateChartFields createNew(String name, String description, LocalDate fromDate, LocalDate toDate) {
        return new InterestRateChartFields(name, description, fromDate, toDate);
    }

    private InterestRateChartFields(String name, String description, LocalDate fromDate, LocalDate toDate) {
        this.name = name;
        this.description = description;
        this.fromDate = fromDate.toDate();
        this.endDate = (toDate == null) ? null : toDate.toDate();
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
        
        if(thisInterval.containsPortionOf(thatInterval) || thatInterval.containsPortionOf(thisInterval)){
            return true;
        }
        return false;// no overlapping
    }
    
    public boolean isApplicableChartFor(final LocalDate target){
        final LocalDate endDate = this.endDate == null ? DateUtils.getLocalDateOfTenant() : this.getEndDateAsLocalDate();
        final LocalDateInterval interval = LocalDateInterval.create(getFromDateAsLocalDate(), endDate);
        return interval.contains(target);
    }

}