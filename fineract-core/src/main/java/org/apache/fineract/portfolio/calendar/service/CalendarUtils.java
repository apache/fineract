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
package org.apache.fineract.portfolio.calendar.service;

import com.google.gson.JsonElement;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.service.WorkingDaysUtil;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarWeekDaysType;
import org.apache.fineract.portfolio.common.domain.NthDayType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

@Slf4j
public final class CalendarUtils {

    public static final String FLOATING_TIMEZONE_PROPERTY_KEY = "net.fortuna.ical4j.timezone.date.floating";

    private CalendarUtils() {

    }

    static {
        System.setProperty(FLOATING_TIMEZONE_PROPERTY_KEY, "true");
    }

    public static LocalDateTime getNextRecurringDate(final String recurringRule, final LocalDateTime seedDate,
            final LocalDateTime startDate) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) {
            return null;
        }
        LocalDateTime nextDate = getNextRecurringDate(recur, seedDate, startDate);
        nextDate = (LocalDateTime) adjustDate(nextDate, seedDate, getMeetingPeriodFrequencyType(recurringRule));
        return nextDate;
    }

    public static LocalDate getNextRecurringDate(final String recurringRule, final LocalDate seedDate, final LocalDate startDate) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) {
            return null;
        }
        LocalDate nextDate = getNextRecurringDate(recur, seedDate, startDate);
        nextDate = (LocalDate) adjustDate(nextDate, seedDate, getMeetingPeriodFrequencyType(recurringRule));
        return nextDate;
    }

    public static Temporal adjustDate(final Temporal date, final Temporal seedDate, final PeriodFrequencyType frequencyType) {
        if (frequencyType.isMonthly() && seedDate.get(ChronoField.DAY_OF_MONTH) > 28 && date.get(ChronoField.DAY_OF_MONTH) >= 28) {
            int noOfDaysInCurrentMonth = YearMonth.from(date).lengthOfMonth();
            int seedDay = seedDate.get(ChronoField.DAY_OF_MONTH);
            int adjustedDay = Math.min(noOfDaysInCurrentMonth, seedDay);
            return date.with(ChronoField.DAY_OF_MONTH, adjustedDay);
        }
        return date;
    }

    private static LocalDateTime getNextRecurringDate(final Recur recur, final LocalDateTime seedDate, final LocalDateTime startDate) {
        final DateTime periodStart = new DateTime(java.util.Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant()));
        final Date seed = convertToiCal4JCompatibleDate(seedDate);
        final Date nextRecDate = recur.getNextDate(seed, periodStart);
        return nextRecDate == null ? null : LocalDateTime.ofInstant(nextRecDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
    }

    private static LocalDate getNextRecurringDate(final Recur recur, final LocalDate seedDate, final LocalDate startDate) {
        final DateTime periodStart = new DateTime(java.util.Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        final Date seed = convertToiCal4JCompatibleDate(seedDate.atStartOfDay());
        final Date nextRecDate = recur.getNextDate(seed, periodStart);
        return nextRecDate == null ? null : LocalDate.ofInstant(nextRecDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
    }

    private static Date convertToiCal4JCompatibleDate(final LocalDateTime inputDate) {
        Date formattedDate = null;
        final String seedDateStr = DateUtils.DEFAULT_DATETIME_FORMATTER.format(inputDate);
        try {
            formattedDate = new Date(seedDateStr, DateUtils.DEFAULT_DATETIME_FORMAT);
        } catch (final ParseException e) {
            log.error("Invalid date: {}", seedDateStr, e);
        }
        return formattedDate;
    }

    public static Collection<LocalDate> getRecurringDates(final String recurringRule, final LocalDate seedDate, final LocalDate endDate) {
        final LocalDate periodStartDate = DateUtils.getLocalDateOfTenant();
        final LocalDate periodEndDate = endDate == null ? periodStartDate.plusYears(5) : endDate;
        return getRecurringDates(recurringRule, seedDate, periodStartDate, periodEndDate);
    }

    public static Collection<LocalDate> getRecurringDatesFrom(final String recurringRule, final LocalDate seedDate,
            final LocalDate startDate) {
        LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        final LocalDate periodStartDate = startDate == null ? currentDate : startDate;
        final LocalDate periodEndDate = currentDate.plusYears(5);
        return getRecurringDates(recurringRule, seedDate, periodStartDate, periodEndDate);
    }

    public static Collection<LocalDate> getRecurringDates(final String recurringRule, final LocalDate seedDate,
            final LocalDate periodStartDate, final LocalDate periodEndDate) {
        final int maxCount = 10;// Default number of recurring dates
        boolean isSkipRepaymentOnFirstdayofMonth = false;
        final Integer numberofDays = 0;
        return getRecurringDates(recurringRule, seedDate, periodStartDate, periodEndDate, maxCount, isSkipRepaymentOnFirstdayofMonth,
                numberofDays);
    }

    public static Collection<LocalDate> getRecurringDates(final String recurringRule, final LocalDate seedDate,
            final LocalDate periodStartDate, final LocalDate periodEndDate, final int maxCount, boolean isSkippMeetingOnFirstDay,
            final Integer numberOfDays) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);

        return getRecurringDates(recur, seedDate, periodStartDate, periodEndDate, maxCount, isSkippMeetingOnFirstDay, numberOfDays);
    }

    private static Collection<LocalDate> getRecurringDates(final Recur recur, final LocalDate seedDate, final LocalDate periodStartDate,
            final LocalDate periodEndDate, final int maxCount, boolean isSkippMeetingOnFirstDay, final Integer numberOfDays) {
        if (recur == null) {
            return null;
        }
        final Date seed = convertToiCal4JCompatibleDate(seedDate.atStartOfDay());
        final DateTime periodStart = new DateTime(java.util.Date.from(periodStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        final DateTime periodEnd = new DateTime(java.util.Date.from(periodEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        final Value value = new Value(Value.DATE.getValue());
        final DateList recurringDates = recur.getDates(seed, periodStart, periodEnd, value, maxCount);
        return convertToLocalDateList(recurringDates, seedDate, getMeetingPeriodFrequencyType(recur), isSkippMeetingOnFirstDay,
                numberOfDays);
    }

    static Collection<LocalDate> convertToLocalDateList(final DateList dates, final LocalDate seedDate,
            final PeriodFrequencyType frequencyType, boolean isSkippMeetingOnFirstDay, final Integer numberOfDays) {
        final Collection<LocalDate> recurringDates = new ArrayList<>();

        for (final Date date : dates) {
            LocalDateTime dateTimeInProperTz = getLocalDateTimeFromICal4JDate(date);
            ZoneId tenantZoneId = DateUtils.getDateTimeZoneOfTenant();

            recurringDates.add((LocalDate) adjustDate(dateTimeInProperTz.atZone(tenantZoneId).toLocalDate(), seedDate, frequencyType));
        }

        if (isSkippMeetingOnFirstDay) {
            return skipMeetingOnFirstdayOfMonth(recurringDates, numberOfDays);
        }

        return recurringDates;
    }

    private static LocalDateTime getLocalDateTimeFromICal4JDate(Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        return LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId());
    }

    private static Collection<LocalDate> skipMeetingOnFirstdayOfMonth(final Collection<LocalDate> recurringDates,
            final Integer numberOfDays) {
        final Collection<LocalDate> adjustedRecurringDates = new ArrayList<>();

        for (@SuppressWarnings("rawtypes")
        final Iterator iterator = recurringDates.iterator(); iterator.hasNext();) {
            LocalDate recuringDate = (LocalDate) iterator.next();
            adjustedRecurringDates.add(adjustRecurringDate(recuringDate, numberOfDays));
        }
        return adjustedRecurringDates;
    }

    public static LocalDate adjustRecurringDate(final LocalDate recuringDate, final Integer numberOfDays) {
        if (recuringDate.getDayOfMonth() == 1) {
            LocalDate adjustedRecurringDate = recuringDate.plusDays(numberOfDays);
            return adjustedRecurringDate;
        }
        return recuringDate;
    }

    public static Recur getICalRecur(final String recurringRule) {

        // Construct RRule
        try {
            final RRule rrule = new RRule(recurringRule);
            rrule.validate();

            final Recur recur = rrule.getRecur();

            return recur;
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            log.error("Problem occurred in getICalRecur function", e);
        } catch (final ValidationException e) {
            // TODO Auto-generated catch block
            log.error("Problem occurred in getICalRecur function", e);
        }

        return null;
    }

    public static String getRRuleReadable(final LocalDate startDate, final String recurringRule) {

        String humanReadable = "";

        RRule rrule;
        Recur recur = null;
        try {
            rrule = new RRule(recurringRule);
            rrule.validate();
            recur = rrule.getRecur();
        } catch (final ValidationException e) {
            throw new PlatformDataIntegrityException("error.msg.invalid.recurring.rule",
                    "The Recurring Rule value: " + recurringRule + " is not valid.", "recurrence", recurringRule, e);
        } catch (final ParseException e) {
            throw new PlatformDataIntegrityException("error.msg.recurring.rule.parsing.error",
                    "Error in pasring the Recurring Rule value: " + recurringRule, "recurrence", recurringRule, e);
        }

        if (recur == null) {
            return humanReadable;
        }

        if (recur.getFrequency().equals(Recur.Frequency.DAILY)) {
            if (recur.getInterval() == 1) {
                humanReadable = "Daily";
            } else {
                humanReadable = "Every " + recur.getInterval() + " days";
            }
        } else if (recur.getFrequency().equals(Recur.Frequency.WEEKLY)) {
            if (recur.getInterval() == 1 || recur.getInterval() == -1) {
                humanReadable = "Weekly";
            } else {
                humanReadable = "Every " + recur.getInterval() + " weeks";
            }

            humanReadable += " on ";
            final WeekDayList weekDayList = recur.getDayList();
            StringBuilder sb = new StringBuilder();

            for (@SuppressWarnings("rawtypes")
            final Iterator iterator = weekDayList.iterator(); iterator.hasNext();) {
                final WeekDay weekDay = (WeekDay) iterator.next();
                sb.append(DayNameEnum.from(weekDay.getDay().name()).getCode());
            }
            humanReadable += sb.toString();

        } else if (recur.getFrequency().equals(Recur.Frequency.MONTHLY)) {
            NumberList nthDays = recur.getSetPosList();
            Integer nthDay = null;
            if (!nthDays.isEmpty()) {
                nthDay = nthDays.get(0);
            }
            NumberList monthDays = recur.getMonthDayList();
            Integer monthDay = null;
            if (!monthDays.isEmpty()) {
                monthDay = monthDays.get(0);
            }
            WeekDayList weekdays = recur.getDayList();
            WeekDay weekDay = null;
            if (!weekdays.isEmpty()) {
                weekDay = weekdays.get(0);
            }
            if (nthDay != null && weekDay != null) {
                NthDayType nthDayType = NthDayType.fromInt(nthDay);
                NthDayNameEnum nthDayName = NthDayNameEnum.from(nthDayType.toString());
                DayNameEnum weekdayType = DayNameEnum.from(weekDay.getDay().name());
                if (recur.getInterval() == 1 || recur.getInterval() == -1) {
                    humanReadable = "Monthly on " + nthDayName.getCode().toLowerCase() + " " + weekdayType.getCode().toLowerCase();
                } else {
                    humanReadable = "Every " + recur.getInterval() + " months on " + nthDayName.getCode().toLowerCase() + " "
                            + weekdayType.getCode().toLowerCase();
                }
            } else if (monthDay != null) {
                if (monthDay == -1) {
                    if (recur.getInterval() == 1 || recur.getInterval() == -1) {
                        humanReadable = "Monthly on last day";
                    } else {
                        humanReadable = "Every " + recur.getInterval() + " months on last day";
                    }
                } else {
                    if (recur.getInterval() == 1 || recur.getInterval() == -1) {
                        humanReadable = "Monthly on day " + monthDay;
                    } else {
                        humanReadable = "Every " + recur.getInterval() + " months on day " + monthDay;
                    }
                }
            } else {
                if (recur.getInterval() == 1 || recur.getInterval() == -1) {
                    humanReadable = "Monthly on day " + startDate.getDayOfMonth();
                } else {
                    humanReadable = "Every " + recur.getInterval() + " months on day " + startDate.getDayOfMonth();
                }
            }
        } else if (recur.getFrequency().equals(Recur.Frequency.YEARLY)) {
            if (recur.getInterval() == 1) {
                humanReadable = "Annually on " + startDate.format(DateTimeFormatter.ofPattern("MMM")) + " " + startDate.getDayOfMonth();
            } else {
                humanReadable = "Every " + recur.getInterval() + " years on " + startDate.format(DateTimeFormatter.ofPattern("MMM")) + " "
                        + startDate.getDayOfMonth();
            }
        }

        if (recur.getCount() > 0) {
            if (recur.getCount() == 1) {
                humanReadable = "Once";
            }
            humanReadable += ", " + recur.getCount() + " times";
        }

        final Date endDate = recur.getUntil();
        if (endDate != null) {
            final LocalDate date = LocalDate.ofInstant(endDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yy");
            final String formattedDate = date.format(fmt);
            humanReadable += ", until " + formattedDate;
        }

        return humanReadable;
    }

    public static boolean isValidRecurringDate(final String recurringRule, final LocalDate seedDate, final LocalDate date) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) {
            return false;
        }
        final boolean isSkipRepaymentonFirstDayOfMonth = false;
        final int numberOfDays = 0;
        return isValidRecurringDate(recur, seedDate, date, isSkipRepaymentonFirstDayOfMonth, numberOfDays);
    }

    public static boolean isValidRecurringDate(final String recurringRule, final LocalDate seedDate, final LocalDate date,
            boolean isSkipRepaymentonFirstDayOfMonth, final Integer numberOfDays) {

        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) {
            return false;
        }

        return isValidRecurringDate(recur, seedDate, date, isSkipRepaymentonFirstDayOfMonth, numberOfDays);
    }

    public static boolean isValidRecurringDate(final Recur recur, final LocalDate seedDate, final LocalDate date,
            boolean isSkipRepaymentonFirstDayOfMonth, final int numberOfDays) {
        LocalDate startDate = date;
        if (isSkipRepaymentonFirstDayOfMonth && date.getDayOfMonth() == (numberOfDays + 1)) {
            startDate = startDate.minusDays(numberOfDays);
        }
        final Collection<LocalDate> recurDate = getRecurringDates(recur, seedDate, startDate, date.plusDays(1), 1,
                isSkipRepaymentonFirstDayOfMonth, numberOfDays);

        return (recurDate == null || recurDate.isEmpty()) ? false : recurDate.contains(date);
    }

    public enum DayNameEnum {

        MO(1, "Monday"), TU(2, "Tuesday"), WE(3, "Wednesday"), TH(4, "Thursday"), FR(5, "Friday"), SA(6, "Saturday"), SU(7, "Sunday");

        private final String code;
        private final Integer value;

        DayNameEnum(final Integer value, final String code) {
            this.value = value;
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

        public int getValue() {
            return this.value;
        }

        public static DayNameEnum from(final String name) {
            for (final DayNameEnum dayName : DayNameEnum.values()) {
                if (dayName.toString().equals(name)) {
                    return dayName;
                }
            }
            return DayNameEnum.MO;// Default it to Monday
        }
    }

    public enum NthDayNameEnum {

        ONE(1, "First"), TWO(2, "Second"), THREE(3, "Third"), FOUR(4, "Fourth"), FIVE(5, "Fifth"), LAST(-1, "Last"), INVALID(0, "Invalid");

        private final String code;
        private final Integer value;

        NthDayNameEnum(final Integer value, final String code) {
            this.value = value;
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

        public int getValue() {
            return this.value;
        }

        public static NthDayNameEnum from(final String name) {
            for (final NthDayNameEnum nthDayName : NthDayNameEnum.values()) {
                if (nthDayName.toString().equals(name)) {
                    return nthDayName;
                }
            }
            return NthDayNameEnum.INVALID;
        }
    }

    public static PeriodFrequencyType getMeetingPeriodFrequencyType(final String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        return getMeetingPeriodFrequencyType(recur);
    }

    private static PeriodFrequencyType getMeetingPeriodFrequencyType(final Recur recur) {
        PeriodFrequencyType meetingFrequencyType = PeriodFrequencyType.INVALID;
        if (recur.getFrequency().equals(Recur.Frequency.DAILY)) {
            meetingFrequencyType = PeriodFrequencyType.DAYS;
        } else if (recur.getFrequency().equals(Recur.Frequency.WEEKLY)) {
            meetingFrequencyType = PeriodFrequencyType.WEEKS;
        } else if (recur.getFrequency().equals(Recur.Frequency.MONTHLY)) {
            meetingFrequencyType = PeriodFrequencyType.MONTHS;
        } else if (recur.getFrequency().equals(Recur.Frequency.YEARLY)) {
            meetingFrequencyType = PeriodFrequencyType.YEARS;
        }
        return meetingFrequencyType;
    }

    public static String getMeetingFrequencyFromPeriodFrequencyType(final PeriodFrequencyType periodFrequency) {
        String frequency = null;
        if (periodFrequency.equals(PeriodFrequencyType.DAYS)) {
            frequency = Recur.Frequency.DAILY.name();
        } else if (periodFrequency.equals(PeriodFrequencyType.WEEKS)) {
            frequency = Recur.Frequency.WEEKLY.name();
        } else if (periodFrequency.equals(PeriodFrequencyType.MONTHS)) {
            frequency = Recur.Frequency.MONTHLY.name();
        } else if (periodFrequency.equals(PeriodFrequencyType.YEARS)) {
            frequency = Recur.Frequency.YEARLY.name();
        }
        return frequency;
    }

    public static int getInterval(final String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        return recur.getInterval();
    }

    public static CalendarFrequencyType getFrequency(final String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        return CalendarFrequencyType.fromString(recur.getFrequency().name());
    }

    public static CalendarWeekDaysType getRepeatsOnDay(final String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        final WeekDayList weekDays = recur.getDayList();
        if (weekDays.isEmpty()) {
            return CalendarWeekDaysType.INVALID;
        }
        // supports only one day
        WeekDay weekDay = weekDays.get(0);
        return CalendarWeekDaysType.fromString(weekDay.getDay().name());
    }

    public static NthDayType getRepeatsOnNthDayOfMonth(final String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        NumberList monthDays = null;
        if (recur.getDayList().isEmpty()) {
            monthDays = recur.getMonthDayList();
        } else {
            monthDays = recur.getSetPosList();
        }
        if (monthDays.isEmpty()) {
            return NthDayType.INVALID;
        }
        if (!recur.getMonthDayList().isEmpty() && recur.getSetPosList().isEmpty()) {
            return NthDayType.ONDAY;
        }
        Integer monthDay = monthDays.get(0);
        return NthDayType.fromInt(monthDay);
    }

    public static LocalDate getFirstRepaymentMeetingDate(final Calendar calendar, final LocalDate disbursementDate,
            final Integer loanRepaymentInterval, final String frequency, boolean isSkipRepaymentOnFirstDayOfMonth,
            final Integer numberOfDays) {
        final Recur recur = CalendarUtils.getICalRecur(calendar.getRecurrence());
        if (recur == null) {
            return null;
        }
        LocalDate startDate = disbursementDate;
        final LocalDate seedDate = calendar.getStartDateLocalDate();
        if (isValidRecurringDate(calendar.getRecurrence(), seedDate, startDate, isSkipRepaymentOnFirstDayOfMonth, numberOfDays)
                && !frequency.equals(Recur.Frequency.DAILY.name())) {
            startDate = startDate.plusDays(1);
        }
        // Recurring dates should follow loanRepaymentInterval.
        // e.g.
        // for weekly meeting interval is 1
        // where as for loan product with fortnightly frequency interval is 2
        // to generate currect set of meeting dates reset interval same as loan
        // repayment interval.

        Recur.Builder recurBuilder = getRecurBuilder(recur);
        recurBuilder = recurBuilder.interval(loanRepaymentInterval);

        // Recurring dates should follow loanRepayment frequency.
        // e.g.
        // daily meeting frequency should support all loan products with any
        // frequency type.
        // to generate currect set of meeting dates reset frequency same as loan
        // repayment frequency.

        if (recur.getFrequency().equals(Recur.Frequency.DAILY)) {
            recurBuilder = recurBuilder.frequency(Recur.Frequency.valueOf(frequency));
        }

        Recur modifiedRecur = recurBuilder.build();
        final LocalDate firstRepaymentDate = getNextRecurringDate(modifiedRecur, seedDate, startDate);
        if (isSkipRepaymentOnFirstDayOfMonth && firstRepaymentDate.getDayOfMonth() == 1) {
            return adjustRecurringDate(firstRepaymentDate, numberOfDays);
        }

        return firstRepaymentDate;
    }

    public static LocalDate getNewRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate,
            final LocalDate oldRepaymentDate, final Integer loanRepaymentInterval, final String frequency, final WorkingDays workingDays,
            final boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) {
            return null;
        }
        if (isValidRecurringDate(recur, seedDate, oldRepaymentDate, isSkipRepaymentOnFirstDayOfMonth, numberOfDays)) {
            return oldRepaymentDate;
        }
        LocalDate nextRepaymentDate = getNextRepaymentMeetingDate(recurringRule, seedDate, oldRepaymentDate, loanRepaymentInterval,
                frequency, workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays);

        return nextRepaymentDate;
    }

    public static LocalDate getNextRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate, final LocalDate repaymentDate,
            final Integer loanRepaymentInterval, final String frequency, final WorkingDays workingDays,
            boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays) {

        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) {
            return null;
        }
        LocalDate tmpDate = repaymentDate;
        if (isValidRecurringDate(recur, seedDate, repaymentDate, isSkipRepaymentOnFirstDayOfMonth, numberOfDays)) {
            tmpDate = repaymentDate.plusDays(1);
        }
        /*
         * Recurring dates should follow loanRepaymentInterval.
         *
         * e.g. The weekly meeting will have interval of 1, if the loan product with fortnightly frequency will have
         * interval of 2, to generate right set of meeting dates reset interval same as loan repayment interval.
         */

        Recur.Builder recurBuilder = getRecurBuilder(recur);

        recurBuilder = recurBuilder.interval(loanRepaymentInterval);

        /*
         * Recurring dates should follow loanRepayment frequency. //e.g. daily meeting frequency should support all loan
         * products with any type of frequency. to generate right set of meeting dates reset frequency same as loan
         * repayment frequency.
         */

        if (recur.getFrequency().equals(Recur.Frequency.DAILY)) {
            recurBuilder = recurBuilder.frequency(Recur.Frequency.valueOf(frequency));
        }

        Recur modifiedRecur = recurBuilder.build();

        LocalDate newRepaymentDate = getNextRecurringDate(modifiedRecur, seedDate, tmpDate);
        final LocalDate nextRepaymentDate = getNextRecurringDate(modifiedRecur, seedDate, newRepaymentDate);

        newRepaymentDate = WorkingDaysUtil.getOffSetDateIfNonWorkingDay(newRepaymentDate, nextRepaymentDate, workingDays);
        if (isSkipRepaymentOnFirstDayOfMonth) {
            LocalDate newRepaymentDateTemp = adjustRecurringDate(newRepaymentDate, numberOfDays);
            return WorkingDaysUtil.getOffSetDateIfNonWorkingDay(newRepaymentDateTemp, nextRepaymentDate, workingDays);
        }

        return newRepaymentDate;
    }

    public static boolean isFrequencySame(final String oldRRule, final String newRRule) {
        final Recur oldRecur = getICalRecur(oldRRule);
        final Recur newRecur = getICalRecur(newRRule);

        if (oldRecur == null || oldRecur.getFrequency() == null || newRecur == null || newRecur.getFrequency() == null) {
            return false;
        }
        return oldRecur.getFrequency().equals(newRecur.getFrequency());
    }

    public static boolean isIntervalSame(final String oldRRule, final String newRRule) {
        final Recur oldRecur = getICalRecur(oldRRule);
        final Recur newRecur = getICalRecur(newRRule);

        if (oldRecur == null || oldRecur.getFrequency() == null || newRecur == null || newRecur.getFrequency() == null) {
            return false;
        }
        return (oldRecur.getInterval() == newRecur.getInterval());
    }

    public static List<Integer> createIntegerListFromQueryParameter(final String calendarTypeQuery) {
        final List<Integer> calendarTypeOptions = new ArrayList<>();
        // adding all calendar Types if query parameter is "all"
        if (calendarTypeQuery.equalsIgnoreCase("all")) {
            calendarTypeOptions.add(1);
            calendarTypeOptions.add(2);
            calendarTypeOptions.add(3);
            calendarTypeOptions.add(4);
            return calendarTypeOptions;
        }
        // creating a list of calendar type options from the comma separated
        // query parameter.
        final List<String> calendarTypeOptionsInQuery = new ArrayList<>();
        final StringTokenizer st = new StringTokenizer(calendarTypeQuery, ",");
        while (st.hasMoreElements()) {
            calendarTypeOptionsInQuery.add(st.nextElement().toString());
        }

        for (final String calType : calendarTypeOptionsInQuery) {
            if (calType.equalsIgnoreCase("collection")) {
                calendarTypeOptions.add(1);
            } else if (calType.equalsIgnoreCase("training")) {
                calendarTypeOptions.add(2);
            } else if (calType.equalsIgnoreCase("audit")) {
                calendarTypeOptions.add(3);
            } else if (calType.equalsIgnoreCase("general")) {
                calendarTypeOptions.add(4);
            }
        }

        return calendarTypeOptions;
    }

    /**
     * function returns a comma separated list of calendar_type_enum values ex. 1,2,3,4
     *
     * @param calendarTypeOptions
     * @return
     */
    public static String getSqlCalendarTypeOptionsInString(final List<Integer> calendarTypeOptions) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < calendarTypeOptions.size() - 1; i++) {
            sb.append(calendarTypeOptions.get(i).toString() + ",");
        }

        sb.append(calendarTypeOptions.get(calendarTypeOptions.size() - 1).toString());

        return sb.toString();
    }

    public static LocalDate getRecentEligibleMeetingDate(final String recurringRule, final LocalDate seedDate,
            final boolean isSkipMeetingOnFirstDay, final Integer numberOfDays) {
        LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) {
            return null;
        }

        if (isValidRecurringDate(recur, seedDate, currentDate, isSkipMeetingOnFirstDay, numberOfDays)) {
            return currentDate;
        }

        if (recur.getFrequency().equals(Recur.Frequency.DAILY)) {
            currentDate = currentDate.plusDays(recur.getInterval());
        } else if (recur.getFrequency().equals(Recur.Frequency.WEEKLY)) {
            currentDate = currentDate.plusWeeks(recur.getInterval());
        } else if (recur.getFrequency().equals(Recur.Frequency.MONTHLY)) {
            currentDate = currentDate.plusMonths(recur.getInterval());
        } else if (recur.getFrequency().equals(Recur.Frequency.YEARLY)) {
            currentDate = currentDate.plusYears(recur.getInterval());
        }

        return getNextRecurringDate(recur, seedDate, currentDate);
    }

    public static LocalDate getNextScheduleDate(final Calendar calendar, final LocalDate startDate) {
        final Recur recur = CalendarUtils.getICalRecur(calendar.getRecurrence());
        if (recur == null) {
            return null;
        }
        final LocalDate seedDate = calendar.getStartDateLocalDate();
        return getNextRecurringDate(recur, seedDate, startDate);
    }

    public static void validateNthDayOfMonthFrequency(DataValidatorBuilder baseDataValidator, final String repeatsOnNthDayOfMonthParamName,
            final String repeatsOnDayParamName, final JsonElement element, final FromJsonHelper fromApiJsonHelper) {
        final Integer repeatsOnNthDayOfMonth = fromApiJsonHelper.extractIntegerSansLocaleNamed(repeatsOnNthDayOfMonthParamName, element);
        baseDataValidator.reset().parameter(repeatsOnNthDayOfMonthParamName).value(repeatsOnNthDayOfMonth).ignoreIfNull()
                .isOneOfTheseValues(NthDayType.ONE.getValue(), NthDayType.TWO.getValue(), NthDayType.THREE.getValue(),
                        NthDayType.FOUR.getValue(), NthDayType.LAST.getValue(), NthDayType.ONDAY.getValue());
        final Integer repeatsOnDay = fromApiJsonHelper.extractIntegerSansLocaleNamed(repeatsOnDayParamName, element);
        baseDataValidator.reset().parameter(repeatsOnDayParamName).value(repeatsOnDay).ignoreIfNull()
                .inMinMaxRange(CalendarWeekDaysType.getMinValue(), CalendarWeekDaysType.getMaxValue());
        NthDayType nthDayType = null;
        if (repeatsOnNthDayOfMonth != null) {
            nthDayType = NthDayType.fromInt(repeatsOnNthDayOfMonth);
        }
        if (nthDayType != null && nthDayType != NthDayType.INVALID) {
            if (nthDayType == NthDayType.ONE || nthDayType == NthDayType.TWO || nthDayType == NthDayType.THREE
                    || nthDayType == NthDayType.FOUR) {
                baseDataValidator.reset().parameter(repeatsOnDayParamName).value(repeatsOnDay).cantBeBlankWhenParameterProvidedIs(
                        repeatsOnNthDayOfMonthParamName, NthDayNameEnum.from(nthDayType.toString()).getCode().toLowerCase());
            }
        }
    }

    public static Integer getMonthOnDay(String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        NumberList monthDayList = null;
        Integer monthOnDay = null;
        if (getMeetingPeriodFrequencyType(recur).isMonthly()) {
            monthDayList = recur.getMonthDayList();
            if (!monthDayList.isEmpty()) {
                monthOnDay = monthDayList.get(0);
            }
        }
        return monthOnDay;
    }

    public static LocalDate getNextRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate, final LocalDate repaymentDate,
            final Integer loanRepaymentInterval, final String frequency, final WorkingDays workingDays,
            boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays, boolean applyWorkingDays) {
        boolean isCalledFirstTime = true;
        return getNextRepaymentMeetingDate(recurringRule, seedDate, repaymentDate, loanRepaymentInterval, frequency, workingDays,
                isSkipRepaymentOnFirstDayOfMonth, numberOfDays, isCalledFirstTime, applyWorkingDays);
    }

    public static LocalDate getNextRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate, final LocalDate repaymentDate,
            final Integer loanRepaymentInterval, final String frequency, boolean isSkipRepaymentOnFirstDayOfMonth,
            final Integer numberOfDays) {
        boolean isCalledFirstTime = true;
        final WorkingDays workingDays = null;
        boolean applyWorkingDays = false;
        return getNextRepaymentMeetingDate(recurringRule, seedDate, repaymentDate, loanRepaymentInterval, frequency, workingDays,
                isSkipRepaymentOnFirstDayOfMonth, numberOfDays, isCalledFirstTime, applyWorkingDays);
    }

    public static LocalDate getNextRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate, final LocalDate repaymentDate,
            final Integer loanRepaymentInterval, final String frequency, final WorkingDays workingDays,
            boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays, boolean isCalledFirstTime, boolean applyWorkingDays) {

        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) {
            return null;
        }
        LocalDate tmpDate = repaymentDate;

        final Integer repaymentInterval = getMeetingIntervalFromFrequency(loanRepaymentInterval, frequency, recur);
        /*
         * Recurring dates should follow loanRepaymentInterval.
         *
         * e.g. The weekly meeting will have interval of 1, if the loan product with fortnightly frequency will have
         * interval of 2, to generate right set of meeting dates reset interval same as loan repayment interval.
         */
        int meetingInterval = recur.getInterval();
        if (meetingInterval < 1) {
            meetingInterval = 1;
        }
        int rep = repaymentInterval < meetingInterval ? 1 : repaymentInterval / meetingInterval;

        /*
         * Recurring dates should follow loanRepayment frequency. //e.g. daily meeting frequency should support all loan
         * products with any type of frequency. to generate right set of meeting dates reset frequency same as loan
         * repayment frequency.
         */

        Recur.Builder recurBuilder = getRecurBuilder(recur);

        if (recur.getFrequency().equals(Recur.Frequency.DAILY)) {
            recurBuilder = recurBuilder.frequency(Recur.Frequency.valueOf(frequency));
        }

        Recur modifiedRecur = recurBuilder.build();

        /**
         * Below code modified as discussed with Pramod N
         */
        LocalDate newRepaymentDate = tmpDate;
        int newRepayment = rep;
        while (newRepayment > 0) {
            newRepaymentDate = getNextRecurringDate(modifiedRecur, seedDate, newRepaymentDate);
            newRepayment--;
        }

        LocalDate nextRepaymentDate = null;
        if (applyWorkingDays) {
            if (WorkingDaysUtil.isNonWorkingDay(workingDays, newRepaymentDate)
                    && WorkingDaysUtil.getRepaymentRescheduleType(workingDays).isMoveToNextRepaymentDay()) {
                newRepaymentDate = getNextRepaymentMeetingDate(recurringRule, seedDate, newRepaymentDate.plusDays(1), loanRepaymentInterval,
                        frequency, workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays, isCalledFirstTime, applyWorkingDays);
            } else {
                newRepaymentDate = WorkingDaysUtil.getOffSetDateIfNonWorkingDay(newRepaymentDate, nextRepaymentDate, workingDays);
            }
        }

        if (isCalledFirstTime && newRepaymentDate.equals(repaymentDate)) {
            isCalledFirstTime = false;
            newRepaymentDate = getNextRepaymentMeetingDate(recurringRule, seedDate, repaymentDate.plusDays(1), loanRepaymentInterval,
                    frequency, workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays, isCalledFirstTime, applyWorkingDays);
        }

        if (isSkipRepaymentOnFirstDayOfMonth) {
            final LocalDate newRepaymentDateTemp = adjustRecurringDate(newRepaymentDate, numberOfDays);
            if (applyWorkingDays) {
                if (WorkingDaysUtil.isNonWorkingDay(workingDays, newRepaymentDateTemp)
                        && WorkingDaysUtil.getRepaymentRescheduleType(workingDays).isMoveToNextRepaymentDay()) {
                    newRepaymentDate = getNextRepaymentMeetingDate(recurringRule, seedDate, newRepaymentDate.plusDays(1),
                            loanRepaymentInterval, frequency, workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays,
                            isCalledFirstTime, applyWorkingDays);
                } else {
                    newRepaymentDate = WorkingDaysUtil.getOffSetDateIfNonWorkingDay(newRepaymentDateTemp, nextRepaymentDate, workingDays);
                }
            }
        }
        return newRepaymentDate;
    }

    public static Integer getMeetingIntervalFromFrequency(final Integer loanRepaymentInterval, final String frequency, final Recur recur) {
        final Integer interval = 4;
        Integer repaymentInterval = loanRepaymentInterval;
        /*
         * check loanRepaymentInterval equal to 1, if repayments frequency is monthly and meeting frequency is weekly,
         * then generate repayments schedule as every 4 weeks
         */
        if (frequency.equals(Recur.Frequency.MONTHLY.name()) && recur.getFrequency().equals(Recur.Frequency.WEEKLY)) {
            repaymentInterval = loanRepaymentInterval * interval;
        }
        return repaymentInterval;
    }

    private static Recur.Builder getRecurBuilder(Recur recur) {
        Recur.Builder recurBuilder = new Recur.Builder();
        recurBuilder = recurBuilder.frequency(recur.getFrequency()).until(recur.getUntil()).count(recur.getCount())
                .interval(recur.getInterval()).secondList(recur.getSecondList()).minuteList(recur.getMinuteList())
                .hourList(recur.getHourList()).dayList(recur.getDayList()).monthDayList(recur.getMonthDayList())
                .yearDayList(recur.getYearDayList()).weekNoList(recur.getWeekNoList()).monthList(recur.getMonthList())
                .setPosList(recur.getSetPosList()).weekStartDay(recur.getWeekStartDay());
        return recurBuilder;
    }
}
