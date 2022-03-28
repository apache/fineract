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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.service.HolidayUtil;
import org.apache.fineract.organisation.workingdays.data.AdjustedDateDetailsDTO;
import org.apache.fineract.organisation.workingdays.service.WorkingDaysUtil;
import org.apache.fineract.portfolio.calendar.data.CalendarHistoryDataWrapper;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarHistory;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultScheduledDateGenerator implements ScheduledDateGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultScheduledDateGenerator.class);

    @Override
    public LocalDate getLastRepaymentDate(final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO) {

        final int numberOfRepayments = loanApplicationTerms.getNumberOfRepayments();

        LocalDate lastRepaymentDate = loanApplicationTerms.getExpectedDisbursementDate();
        boolean isFirstRepayment = true;
        for (int repaymentPeriod = 1; repaymentPeriod <= numberOfRepayments; repaymentPeriod++) {
            lastRepaymentDate = generateNextRepaymentDate(lastRepaymentDate, loanApplicationTerms, isFirstRepayment);
            isFirstRepayment = false;
        }
        lastRepaymentDate = adjustRepaymentDate(lastRepaymentDate, loanApplicationTerms, holidayDetailDTO).getChangedScheduleDate();
        return lastRepaymentDate;
    }

    @Override
    public LocalDate generateNextRepaymentDate(final LocalDate lastRepaymentDate, final LoanApplicationTerms loanApplicationTerms,
            boolean isFirstRepayment) {
        final LocalDate firstRepaymentPeriodDate = loanApplicationTerms.getCalculatedRepaymentsStartingFromLocalDate();
        LocalDate dueRepaymentPeriodDate = null;
        if (isFirstRepayment && firstRepaymentPeriodDate != null) {
            dueRepaymentPeriodDate = firstRepaymentPeriodDate;

        } else {
            LocalDate seedDate = null;
            String reccuringString = null;
            Calendar currentCalendar = loanApplicationTerms.getLoanCalendar();
            dueRepaymentPeriodDate = getRepaymentPeriodDate(loanApplicationTerms.getRepaymentPeriodFrequencyType(),
                    loanApplicationTerms.getRepaymentEvery(), lastRepaymentDate);
            dueRepaymentPeriodDate = CalendarUtils.adjustDate(dueRepaymentPeriodDate, loanApplicationTerms.getSeedDate(),
                    loanApplicationTerms.getRepaymentPeriodFrequencyType());
            if (currentCalendar != null) {
                // If we have currentCalendar object, this means there is a
                // calendar associated with
                // the loan, and we should use it in order to calculate next
                // repayment

                CalendarHistory calendarHistory = null;
                CalendarHistoryDataWrapper calendarHistoryDataWrapper = loanApplicationTerms.getCalendarHistoryDataWrapper();
                if (calendarHistoryDataWrapper != null) {
                    calendarHistory = loanApplicationTerms.getCalendarHistoryDataWrapper().getCalendarHistory(dueRepaymentPeriodDate);
                }

                // get the start date from the calendar history
                if (calendarHistory == null) {
                    seedDate = currentCalendar.getStartDateLocalDate();
                    reccuringString = currentCalendar.getRecurrence();
                } else {
                    seedDate = calendarHistory.getStartDateLocalDate();
                    reccuringString = calendarHistory.getRecurrence();
                }

                dueRepaymentPeriodDate = CalendarUtils.getNextRepaymentMeetingDate(reccuringString, seedDate, lastRepaymentDate,
                        loanApplicationTerms.getRepaymentEvery(),
                        CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(loanApplicationTerms.getLoanTermPeriodFrequencyType()),
                        loanApplicationTerms.isSkipRepaymentOnFirstDayofMonth(), loanApplicationTerms.getNumberOfdays());
            }
        }

        return dueRepaymentPeriodDate;
    }

    @Override
    public AdjustedDateDetailsDTO adjustRepaymentDate(final LocalDate dueRepaymentPeriodDate,
            final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO) {
        final LocalDate adjustedDate = dueRepaymentPeriodDate;
        return getAdjustedDateDetailsDTO(dueRepaymentPeriodDate, loanApplicationTerms, holidayDetailDTO, adjustedDate);
    }

    private AdjustedDateDetailsDTO getAdjustedDateDetailsDTO(final LocalDate dueRepaymentPeriodDate,
            final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO, final LocalDate adjustedDate) {
        final boolean isFirstRepayment = false;
        final LocalDate nextRepaymentPeriodDueDate = generateNextRepaymentDate(adjustedDate, loanApplicationTerms, isFirstRepayment);
        final AdjustedDateDetailsDTO newAdjustedDateDetailsDTO = new AdjustedDateDetailsDTO(adjustedDate, dueRepaymentPeriodDate,
                nextRepaymentPeriodDueDate);
        return recursivelyCheckNonWorkingDaysAndHolidaysAndWorkingDaysExemptionToGenerateNextRepaymentPeriodDate(newAdjustedDateDetailsDTO,
                loanApplicationTerms, holidayDetailDTO, isFirstRepayment);
    }

    /**
     * Recursively checking non working days and holidays and working days exemption to generate next repayment period
     * date Base on the configuration
     *
     * @param adjustedDateDetailsDTO
     * @param loanApplicationTerms
     * @param holidayDetailDTO
     * @param nextRepaymentPeriodDueDate
     * @param rescheduleType
     * @param isFirstRepayment
     * @return
     */
    private AdjustedDateDetailsDTO recursivelyCheckNonWorkingDaysAndHolidaysAndWorkingDaysExemptionToGenerateNextRepaymentPeriodDate(
            final AdjustedDateDetailsDTO adjustedDateDetailsDTO, final LoanApplicationTerms loanApplicationTerms,
            final HolidayDetailDTO holidayDetailDTO, final boolean isFirstRepayment) {

        checkAndUpdateWorkingDayIfRepaymentDateIsNonWorkingDay(adjustedDateDetailsDTO, holidayDetailDTO, loanApplicationTerms,
                isFirstRepayment);

        checkAndUpdateWorkingDayIfRepaymentDateIsHolidayDay(adjustedDateDetailsDTO, holidayDetailDTO, loanApplicationTerms,
                isFirstRepayment);

        /**
         * Check Changed Schedule Date is holiday or is not a working day Then re-call this method to get the non
         * holiday and working day
         */
        if ((holidayDetailDTO.isHolidayEnabled() && HolidayUtil.getApplicableHoliday(adjustedDateDetailsDTO.getChangedScheduleDate(),
                holidayDetailDTO.getHolidays()) != null)
                || WorkingDaysUtil.isNonWorkingDay(holidayDetailDTO.getWorkingDays(), adjustedDateDetailsDTO.getChangedScheduleDate())) {
            recursivelyCheckNonWorkingDaysAndHolidaysAndWorkingDaysExemptionToGenerateNextRepaymentPeriodDate(adjustedDateDetailsDTO,
                    loanApplicationTerms, holidayDetailDTO, isFirstRepayment);
        }
        return adjustedDateDetailsDTO;
    }

    /**
     * This method to check and update the working day if repayment date is holiday
     *
     * @param adjustedDateDetailsDTO
     * @param holidayDetailDTO
     * @param loanApplicationTerms
     * @param isFirstRepayment
     */
    private void checkAndUpdateWorkingDayIfRepaymentDateIsHolidayDay(final AdjustedDateDetailsDTO adjustedDateDetailsDTO,
            final HolidayDetailDTO holidayDetailDTO, final LoanApplicationTerms loanApplicationTerms, final boolean isFirstRepayment) {
        if (holidayDetailDTO.isHolidayEnabled()) {
            Holiday applicableHolidayForNewAdjustedDate = null;
            while ((applicableHolidayForNewAdjustedDate = HolidayUtil.getApplicableHoliday(adjustedDateDetailsDTO.getChangedScheduleDate(),
                    holidayDetailDTO.getHolidays())) != null) {
                if (applicableHolidayForNewAdjustedDate.getReScheduleType().isResheduleToNextRepaymentDate()) {
                    LocalDate nextRepaymentPeriodDueDate = adjustedDateDetailsDTO.getChangedActualRepaymentDate();
                    while (!nextRepaymentPeriodDueDate.isAfter(adjustedDateDetailsDTO.getChangedScheduleDate())) {
                        nextRepaymentPeriodDueDate = generateNextRepaymentDate(nextRepaymentPeriodDueDate, loanApplicationTerms,
                                isFirstRepayment);
                    }
                    adjustedDateDetailsDTO.setChangedScheduleDate(nextRepaymentPeriodDueDate);
                    adjustedDateDetailsDTO.setNextRepaymentPeriodDueDate(nextRepaymentPeriodDueDate);
                    adjustedDateDetailsDTO.setChangedActualRepaymentDate(adjustedDateDetailsDTO.getChangedScheduleDate());
                } else {
                    HolidayUtil.updateRepaymentRescheduleDateToWorkingDayIfItIsHoliday(adjustedDateDetailsDTO,
                            applicableHolidayForNewAdjustedDate);
                }
            }
        }
    }

    /**
     * This method to check and update the working day if repayment date is non working day
     *
     * @param adjustedDateDetailsDTO
     * @param holidayDetailDTO
     * @param isFirstRepayment
     * @param loanApplicationTerms
     */
    private void checkAndUpdateWorkingDayIfRepaymentDateIsNonWorkingDay(final AdjustedDateDetailsDTO adjustedDateDetailsDTO,
            final HolidayDetailDTO holidayDetailDTO, final LoanApplicationTerms loanApplicationTerms, final boolean isFirstRepayment) {
        while (WorkingDaysUtil.isNonWorkingDay(holidayDetailDTO.getWorkingDays(), adjustedDateDetailsDTO.getChangedScheduleDate())) {
            if (WorkingDaysUtil
                    .getRepaymentRescheduleType(holidayDetailDTO.getWorkingDays(), adjustedDateDetailsDTO.getChangedScheduleDate())
                    .isMoveToNextRepaymentDay()) {
                while (WorkingDaysUtil.isNonWorkingDay(holidayDetailDTO.getWorkingDays(),
                        adjustedDateDetailsDTO.getNextRepaymentPeriodDueDate())
                        || adjustedDateDetailsDTO.getChangedScheduleDate()
                                .isAfter(adjustedDateDetailsDTO.getNextRepaymentPeriodDueDate())) {
                    final LocalDate nextRepaymentPeriodDueDate = generateNextRepaymentDate(
                            adjustedDateDetailsDTO.getNextRepaymentPeriodDueDate(), loanApplicationTerms, isFirstRepayment);
                    adjustedDateDetailsDTO.setNextRepaymentPeriodDueDate(nextRepaymentPeriodDueDate);
                }
            }
            WorkingDaysUtil.updateWorkingDayIfRepaymentDateIsNonWorkingDay(adjustedDateDetailsDTO, holidayDetailDTO.getWorkingDays());
        }
    }

    @Override
    public LocalDate getRepaymentPeriodDate(final PeriodFrequencyType frequency, final int repaidEvery, final LocalDate startDate) {
        LocalDate dueRepaymentPeriodDate = startDate;
        switch (frequency) {
            case DAYS:
                dueRepaymentPeriodDate = startDate.plusDays(repaidEvery);
            break;
            case WEEKS:
                dueRepaymentPeriodDate = startDate.plusWeeks(repaidEvery);
            break;
            case MONTHS:
                dueRepaymentPeriodDate = startDate.plusMonths(repaidEvery);
            break;
            case YEARS:
                dueRepaymentPeriodDate = startDate.plusYears(repaidEvery);
            break;
            case INVALID:
            break;
            case WHOLE_TERM:
                LOG.error("TODO Implement getRepaymentPeriodDate for WHOLE_TERM");
            break;
        }
        return dueRepaymentPeriodDate;
    }

    @Override
    public Boolean isDateFallsInSchedule(final PeriodFrequencyType frequency, final int repaidEvery, final LocalDate startDate,
            final LocalDate date) {
        boolean isScheduledDate = false;
        switch (frequency) {
            case DAYS:
                int diff = Math.toIntExact(ChronoUnit.DAYS.between(startDate, date));
                isScheduledDate = (diff % repaidEvery) == 0;
            break;
            case WEEKS:
                int weekDiff = Math.toIntExact(ChronoUnit.WEEKS.between(startDate, date));
                isScheduledDate = (weekDiff % repaidEvery) == 0;
                if (isScheduledDate) {
                    LocalDate modifiedDate = startDate.plusWeeks(weekDiff);
                    isScheduledDate = modifiedDate.isEqual(date);
                }
            break;
            case MONTHS:
                int monthDiff = Math.toIntExact(ChronoUnit.MONTHS.between(startDate, date));
                isScheduledDate = (monthDiff % repaidEvery) == 0;
                if (isScheduledDate) {
                    LocalDate modifiedDate = startDate.plusMonths(monthDiff);
                    isScheduledDate = modifiedDate.isEqual(date);
                }
            break;
            case YEARS:
                int yearDiff = Math.toIntExact(ChronoUnit.YEARS.between(startDate, date));
                isScheduledDate = (yearDiff % repaidEvery) == 0;
                if (isScheduledDate) {
                    LocalDate modifiedDate = startDate.plusYears(yearDiff);
                    isScheduledDate = modifiedDate.isEqual(date);
                }
            break;
            case INVALID:
            break;
            case WHOLE_TERM:
                LOG.error("TODO Implement isDateFallsInSchedule for WHOLE_TERM");
            break;
        }
        return isScheduledDate;
    }

    @Override
    public LocalDate idealDisbursementDateBasedOnFirstRepaymentDate(final PeriodFrequencyType repaymentPeriodFrequencyType,
            final int repaidEvery, final LocalDate firstRepaymentDate, final Calendar loanCalendar, final HolidayDetailDTO holidayDetailDTO,
            final LoanApplicationTerms loanApplicationTerms) {

        LocalDate idealDisbursementDate = null;

        switch (repaymentPeriodFrequencyType) {
            case DAYS:
                idealDisbursementDate = firstRepaymentDate.minusDays(repaidEvery);
            break;
            case WEEKS:
                idealDisbursementDate = firstRepaymentDate.minusWeeks(repaidEvery);
            break;
            case MONTHS:
                if (loanCalendar == null) {
                    idealDisbursementDate = firstRepaymentDate.minusMonths(repaidEvery);
                } else {
                    idealDisbursementDate = CalendarUtils.getNewRepaymentMeetingDate(loanCalendar.getRecurrence(),
                            firstRepaymentDate.minusMonths(repaidEvery), firstRepaymentDate.minusMonths(repaidEvery), repaidEvery,
                            CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType),
                            holidayDetailDTO.getWorkingDays(), loanApplicationTerms.isSkipRepaymentOnFirstDayofMonth(),
                            loanApplicationTerms.getNumberOfdays());
                }
            break;
            case YEARS:
                idealDisbursementDate = firstRepaymentDate.minusYears(repaidEvery);
            break;
            case INVALID:
            break;
            case WHOLE_TERM:
                LOG.error("TODO Implement repaymentPeriodFrequencyType for WHOLE_TERM");
            break;
        }

        return idealDisbursementDate;
    }

    @Override
    public LocalDate generateNextScheduleDateStartingFromDisburseDate(LocalDate lastRepaymentDate,
            LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO) {

        LocalDate generatedDate = loanApplicationTerms.getExpectedDisbursementDate();
        boolean isFirstRepayment = true;
        while (!generatedDate.isAfter(lastRepaymentDate)) {
            generatedDate = generateNextRepaymentDate(generatedDate, loanApplicationTerms, isFirstRepayment);
            isFirstRepayment = false;
        }
        generatedDate = adjustRepaymentDate(generatedDate, loanApplicationTerms, holidayDetailDTO).getChangedScheduleDate();
        return generatedDate;
    }

    @Override
    public LocalDate generateNextScheduleDateStartingFromDisburseDateOrRescheduleDate(LocalDate lastRepaymentDate,
            LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO) {

        LocalDate generatedDate = loanApplicationTerms.getExpectedDisbursementDate();
        boolean isFirstRepayment = true;
        if (loanApplicationTerms.getNewScheduledDueDateStart() != null) {
            generatedDate = loanApplicationTerms.getNewScheduledDueDateStart();
            isFirstRepayment = false;
        }
        LocalDate adjustedDate = generatedDate;
        while (!adjustedDate.isAfter(lastRepaymentDate)) {
            generatedDate = generateNextRepaymentDate(generatedDate, loanApplicationTerms, isFirstRepayment);
            adjustedDate = adjustRepaymentDate(generatedDate, loanApplicationTerms, holidayDetailDTO).getChangedScheduleDate();
            isFirstRepayment = false;
        }
        return adjustedDate;
    }
}
