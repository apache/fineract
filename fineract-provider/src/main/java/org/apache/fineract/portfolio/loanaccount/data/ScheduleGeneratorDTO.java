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
package org.apache.fineract.portfolio.loanaccount.data;

import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.portfolio.calendar.data.CalendarHistoryDataWrapper;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.joda.time.LocalDate;

public class ScheduleGeneratorDTO {

    final LoanScheduleGeneratorFactory loanScheduleFactory;
    final ApplicationCurrency applicationCurrency;
    final LocalDate calculatedRepaymentsStartingFromDate;
    final HolidayDetailDTO holidayDetailDTO;
    final CalendarInstance calendarInstanceForInterestRecalculation;
    final CalendarInstance compoundingCalendarInstance;
    LocalDate recalculateFrom;
    final Long overdurPenaltyWaitPeriod;
    final FloatingRateDTO floatingRateDTO;
    final Calendar calendar;
    final CalendarHistoryDataWrapper calendarHistoryDataWrapper;
    final Boolean isInterestChargedFromDateAsDisbursementDateEnabled;
    final Integer numberOfdays;
    final boolean isSkipRepaymentOnFirstDayofMonth;
    final Boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled;


    public ScheduleGeneratorDTO(final LoanScheduleGeneratorFactory loanScheduleFactory, final ApplicationCurrency applicationCurrency,
            final LocalDate calculatedRepaymentsStartingFromDate, final HolidayDetailDTO holidayDetailDTO,
            final CalendarInstance calendarInstanceForInterestRecalculation, final CalendarInstance compoundingCalendarInstance,
            final LocalDate recalculateFrom, final Long overdurPenaltyWaitPeriod, final FloatingRateDTO floatingRateDTO,
            final Calendar calendar, final CalendarHistoryDataWrapper calendarHistoryDataWrapper, 
            final Boolean isInterestChargedFromDateAsDisbursementDateEnabled, final Integer numberOfdays, final boolean isSkipRepaymentOnFirstDayofMonth,
            final Boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled) {
    	
        this.loanScheduleFactory = loanScheduleFactory;
        this.applicationCurrency = applicationCurrency;
        this.calculatedRepaymentsStartingFromDate = calculatedRepaymentsStartingFromDate;
        this.calendarInstanceForInterestRecalculation = calendarInstanceForInterestRecalculation;
        this.compoundingCalendarInstance = compoundingCalendarInstance;
        this.recalculateFrom = recalculateFrom;
        this.overdurPenaltyWaitPeriod = overdurPenaltyWaitPeriod;
        this.holidayDetailDTO = holidayDetailDTO;
        this.floatingRateDTO = floatingRateDTO;
        this.calendar = calendar;
        this.calendarHistoryDataWrapper  = calendarHistoryDataWrapper;
        this.isInterestChargedFromDateAsDisbursementDateEnabled = isInterestChargedFromDateAsDisbursementDateEnabled;
        this.numberOfdays = numberOfdays;
        this.isSkipRepaymentOnFirstDayofMonth = isSkipRepaymentOnFirstDayofMonth;
        this.isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled = isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled;
    }

    public LoanScheduleGeneratorFactory getLoanScheduleFactory() {
        return this.loanScheduleFactory;
    }

    public ApplicationCurrency getApplicationCurrency() {
        return this.applicationCurrency;
    }

    public LocalDate getCalculatedRepaymentsStartingFromDate() {
        return this.calculatedRepaymentsStartingFromDate;
    }

    public CalendarInstance getCalendarInstanceForInterestRecalculation() {
        return this.calendarInstanceForInterestRecalculation;
    }

    public LocalDate getRecalculateFrom() {
        return this.recalculateFrom;
    }

    public Long getOverdurPenaltyWaitPeriod() {
        return this.overdurPenaltyWaitPeriod;
    }

    public int getPenaltyWaitPeriod() {
        int penaltyWaitPeriod = 0;
        if (this.overdurPenaltyWaitPeriod != null) {
            penaltyWaitPeriod = this.overdurPenaltyWaitPeriod.intValue();
        }
        return penaltyWaitPeriod;
    }

    public HolidayDetailDTO getHolidayDetailDTO() {
        return this.holidayDetailDTO;
    }

    public void setRecalculateFrom(LocalDate recalculateFrom) {
        this.recalculateFrom = recalculateFrom;
    }

    public CalendarInstance getCompoundingCalendarInstance() {
        return this.compoundingCalendarInstance;
    }

    public FloatingRateDTO getFloatingRateDTO() {
        return this.floatingRateDTO;
    }
    
    public Calendar getCalendar(){
    	return this.calendar;
    }
    
    public CalendarHistoryDataWrapper getCalendarHistoryDataWrapper(){
        return this.calendarHistoryDataWrapper;
    }
    
    public Boolean isInterestChargedFromDateAsDisbursementDateEnabled(){
        return this.isInterestChargedFromDateAsDisbursementDateEnabled;
    }

    public Integer getNumberOfdays() {
        return numberOfdays;
    }

    public boolean isSkipRepaymentOnFirstDayofMonth() {
        return isSkipRepaymentOnFirstDayofMonth;
    }
    
    public Boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled() {
        return this.isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled;
    }

}
