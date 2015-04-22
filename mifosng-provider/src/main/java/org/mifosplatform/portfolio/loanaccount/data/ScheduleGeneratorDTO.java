/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;

public class ScheduleGeneratorDTO {

    final LoanScheduleGeneratorFactory loanScheduleFactory;
    final ApplicationCurrency applicationCurrency;
    final LocalDate calculatedRepaymentsStartingFromDate;
    final HolidayDetailDTO holidayDetailDTO;
    final CalendarInstance calendarInstanceForInterestRecalculation;
    final CalendarInstance compoundingCalendarInstance;
    LocalDate recalculateFrom;
    final Long overdurPenaltyWaitPeriod;

    public ScheduleGeneratorDTO(final LoanScheduleGeneratorFactory loanScheduleFactory, final ApplicationCurrency applicationCurrency,
            final LocalDate calculatedRepaymentsStartingFromDate, final HolidayDetailDTO holidayDetailDTO,
            final CalendarInstance calendarInstanceForInterestRecalculation, final CalendarInstance compoundingCalendarInstance) {

        this.loanScheduleFactory = loanScheduleFactory;
        this.applicationCurrency = applicationCurrency;
        this.calculatedRepaymentsStartingFromDate = calculatedRepaymentsStartingFromDate;
        this.calendarInstanceForInterestRecalculation = calendarInstanceForInterestRecalculation;
        this.compoundingCalendarInstance = compoundingCalendarInstance;
        this.recalculateFrom = null;
        this.overdurPenaltyWaitPeriod = null;
        this.holidayDetailDTO = holidayDetailDTO;

    }

    public ScheduleGeneratorDTO(final LoanScheduleGeneratorFactory loanScheduleFactory, final ApplicationCurrency applicationCurrency,
            final LocalDate calculatedRepaymentsStartingFromDate, final HolidayDetailDTO holidayDetailDTO,
            final CalendarInstance calendarInstanceForInterestRecalculation, final CalendarInstance compoundingCalendarInstance,
            final LocalDate recalculateFrom, final Long overdurPenaltyWaitPeriod) {

        this.loanScheduleFactory = loanScheduleFactory;
        this.applicationCurrency = applicationCurrency;
        this.calculatedRepaymentsStartingFromDate = calculatedRepaymentsStartingFromDate;
        this.calendarInstanceForInterestRecalculation = calendarInstanceForInterestRecalculation;
        this.compoundingCalendarInstance = compoundingCalendarInstance;
        this.recalculateFrom = recalculateFrom;
        this.overdurPenaltyWaitPeriod = overdurPenaltyWaitPeriod;
        this.holidayDetailDTO = holidayDetailDTO;

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

}
