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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.RRule;

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
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.JsonElement;

public class CalendarUtils {

    static {
        System.setProperty("net.fortuna.ical4j.timezone.date.floating", "true");
    }

    public static LocalDate getNextRecurringDate(final String recurringRule, final LocalDate seedDate, final LocalDate startDate) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return null; }
        LocalDate nextDate = getNextRecurringDate(recur, seedDate, startDate);
        nextDate = adjustDate(nextDate, seedDate, getMeetingPeriodFrequencyType(recurringRule));
        return nextDate;
    }

    public static LocalDate adjustDate(final LocalDate date, final LocalDate seedDate, final PeriodFrequencyType frequencyType) {
        LocalDate adjustedVal = date;
        if (frequencyType.isMonthly() && seedDate.getDayOfMonth() > 28) {
            switch (date.getMonthOfYear()) {
                case 2:
                    if (date.year().isLeap()) {
                        adjustedVal = date.dayOfMonth().setCopy(29);
                    }
                break;
                case 4:
                case 6:
                case 9:
                case 11:
                    if (seedDate.getDayOfMonth() > 30) {
                        adjustedVal = date.dayOfMonth().setCopy(30);
                    } else {
                        adjustedVal = date.dayOfMonth().setCopy(seedDate.getDayOfMonth());
                    }
                break;
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    adjustedVal = date.dayOfMonth().setCopy(seedDate.getDayOfMonth());
                break;
            }
        }
        return adjustedVal;
    }

    private static LocalDate getNextRecurringDate(final Recur recur, final LocalDate seedDate, final LocalDate startDate) {
        final DateTime periodStart = new DateTime(startDate.toDate());
        final Date seed = convertToiCal4JCompatibleDate(seedDate);
        final Date nextRecDate = recur.getNextDate(seed, periodStart);
        return nextRecDate == null ? null : new LocalDate(nextRecDate);
    }

    private static Date convertToiCal4JCompatibleDate(final LocalDate inputDate) {
        // Date format in iCal4J is hard coded
        Date formattedDate = null;
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String seedDateStr = df.format(inputDate.toDateTimeAtStartOfDay().toDate());
        try {
            formattedDate = new Date(seedDateStr, "yyyy-MM-dd");
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    public static Collection<LocalDate> getRecurringDates(final String recurringRule, final LocalDate seedDate, final LocalDate endDate) {

        final LocalDate periodStartDate = DateUtils.getLocalDateOfTenant();
        final LocalDate periodEndDate = (endDate == null) ? DateUtils.getLocalDateOfTenant().plusYears(5) : endDate;
        return getRecurringDates(recurringRule, seedDate, periodStartDate, periodEndDate);
    }

    public static Collection<LocalDate> getRecurringDatesFrom(final String recurringRule, final LocalDate seedDate,
            final LocalDate startDate) {
        final LocalDate periodStartDate = (startDate == null) ? DateUtils.getLocalDateOfTenant() : startDate;
        final LocalDate periodEndDate = DateUtils.getLocalDateOfTenant().plusYears(5);
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
        if (recur == null) { return null; }
        final Date seed = convertToiCal4JCompatibleDate(seedDate);
        final DateTime periodStart = new DateTime(periodStartDate.toDate());
        final DateTime periodEnd = new DateTime(periodEndDate.toDate());

        final Value value = new Value(Value.DATE.getValue());
        final DateList recurringDates = recur.getDates(seed, periodStart, periodEnd, value, maxCount);
        return convertToLocalDateList(recurringDates, seedDate, getMeetingPeriodFrequencyType(recur), isSkippMeetingOnFirstDay,
                numberOfDays);
    }

    private static Collection<LocalDate> convertToLocalDateList(final DateList dates, final LocalDate seedDate,
            final PeriodFrequencyType frequencyType, boolean isSkippMeetingOnFirstDay, final Integer numberOfDays) {

        final Collection<LocalDate> recurringDates = new ArrayList<>();

        for (@SuppressWarnings("rawtypes")
        final Iterator iterator = dates.iterator(); iterator.hasNext();) {
            final Date date = (Date) iterator.next();
            recurringDates.add(adjustDate(new LocalDate(date), seedDate, frequencyType));
        }

        if (isSkippMeetingOnFirstDay) { return skipMeetingOnFirstdayOfMonth(recurringDates, numberOfDays); }

        return recurringDates;
    }

    private static Collection<LocalDate> skipMeetingOnFirstdayOfMonth(final Collection<LocalDate> recurringDates, final Integer numberOfDays) {
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
            e.printStackTrace();
        } catch (final ValidationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            throw new PlatformDataIntegrityException("error.msg.invalid.recurring.rule", "The Recurring Rule value: " + recurringRule
                    + " is not valid.", "recurrence", recurringRule);
        } catch (final ParseException e) {
            throw new PlatformDataIntegrityException("error.msg.recurring.rule.parsing.error",
                    "Error in pasring the Recurring Rule value: " + recurringRule, "recurrence", recurringRule);
        }

        if (recur == null) { return humanReadable; }

        if (recur.getFrequency().equals(Recur.DAILY)) {
            if (recur.getInterval() == 1) {
                humanReadable = "Daily";
            } else {
                humanReadable = "Every " + recur.getInterval() + " days";
            }
        } else if (recur.getFrequency().equals(Recur.WEEKLY)) {
            if (recur.getInterval() == 1 || recur.getInterval() == -1) {
                humanReadable = "Weekly";
            } else {
                humanReadable = "Every " + recur.getInterval() + " weeks";
            }

            humanReadable += " on ";
            final WeekDayList weekDayList = recur.getDayList();

            for (@SuppressWarnings("rawtypes")
            final Iterator iterator = weekDayList.iterator(); iterator.hasNext();) {
                final WeekDay weekDay = (WeekDay) iterator.next();
                humanReadable += DayNameEnum.from(weekDay.getDay()).getCode();
            }

        } else if (recur.getFrequency().equals(Recur.MONTHLY)) {
            NumberList nthDays = recur.getSetPosList();
            Integer nthDay = null;
            if (!nthDays.isEmpty())
                nthDay = (Integer) nthDays.get(0);
            NumberList monthDays = recur.getMonthDayList();
            Integer monthDay = null;
            if (!monthDays.isEmpty())
                monthDay = (Integer) monthDays.get(0);
            WeekDayList weekdays = recur.getDayList();
            WeekDay weekDay = null;
            if (!weekdays.isEmpty()) 
                weekDay = (WeekDay) weekdays.get(0);
            if (nthDay != null && weekDay != null) {
                NthDayType nthDayType = NthDayType.fromInt(nthDay);
                NthDayNameEnum nthDayName = NthDayNameEnum.from(nthDayType.toString());
                DayNameEnum weekdayType = DayNameEnum.from(weekDay.getDay());
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
        } else if (recur.getFrequency().equals(Recur.YEARLY)) {
            if (recur.getInterval() == 1) {
                humanReadable = "Annually on " + startDate.toString("MMM") + " " + startDate.getDayOfMonth();
            } else {
                humanReadable = "Every " + recur.getInterval() + " years on " + startDate.toString("MMM") + " " + startDate.getDayOfMonth();
            }
        }

        if (recur.getCount() > 0) {
            if (recur.getCount() == 1) {
                humanReadable = "Once";
            }
            humanReadable += ", " + recur.getCount() + " times";
        }

        final Date endDate = recur.getUntil();
        final LocalDate date = new LocalDate(endDate);
        final DateTimeFormatter fmt = DateTimeFormat.forPattern("dd MMMM YY");
        final String formattedDate = date.toString(fmt);
        if (endDate != null) {
            humanReadable += ", until " + formattedDate;
        }

        return humanReadable;
    }

    public static boolean isValidRedurringDate(final String recurringRule, final LocalDate seedDate, final LocalDate date) {

        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return false; }
        final boolean isSkipRepaymentonFirstDayOfMonth = false;
        final int numberOfDays = 0;
        return isValidRecurringDate(recur, seedDate, date, isSkipRepaymentonFirstDayOfMonth, numberOfDays);
    }

    public static boolean isValidRedurringDate(final String recurringRule, final LocalDate seedDate, final LocalDate date,
            boolean isSkipRepaymentonFirstDayOfMonth, final Integer numberOfDays) {

        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return false; }

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

    public static enum DayNameEnum {
        MO(1, "Monday"), TU(2, "Tuesday"), WE(3, "Wednesday"), TH(4, "Thursday"), FR(5, "Friday"), SA(6, "Saturday"), SU(7, "Sunday");

        private final String code;
        private final Integer value;

        private DayNameEnum(final Integer value, final String code) {
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
                if (dayName.toString().equals(name)) { return dayName; }
            }
            return DayNameEnum.MO;// Default it to Monday
        }
    }
    public static enum NthDayNameEnum {
        ONE(1, "First"), TWO(2, "Second"), THREE(3, "Third"), FOUR(4, "Fourth"), FIVE(5, "Fifth"), LAST(-1, "Last"), INVALID(0, "Invalid");
        private final String code;
        private final Integer value;

        private NthDayNameEnum(final Integer value, final String code) {
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
                if (nthDayName.toString().equals(name)) { return nthDayName; }
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
        if (recur.getFrequency().equals(Recur.DAILY)) {
            meetingFrequencyType = PeriodFrequencyType.DAYS;
        } else if (recur.getFrequency().equals(Recur.WEEKLY)) {
            meetingFrequencyType = PeriodFrequencyType.WEEKS;
        } else if (recur.getFrequency().equals(Recur.MONTHLY)) {
            meetingFrequencyType = PeriodFrequencyType.MONTHS;
        } else if (recur.getFrequency().equals(Recur.YEARLY)) {
            meetingFrequencyType = PeriodFrequencyType.YEARS;
        }
        return meetingFrequencyType;
    }

    public static String getMeetingFrequencyFromPeriodFrequencyType(final PeriodFrequencyType periodFrequency) {
        String frequency = null;
        if (periodFrequency.equals(PeriodFrequencyType.DAYS)) {
            frequency = Recur.DAILY;
        } else if (periodFrequency.equals(PeriodFrequencyType.WEEKS)) {
            frequency = Recur.WEEKLY;
        } else if (periodFrequency.equals(PeriodFrequencyType.MONTHS)) {
            frequency = Recur.MONTHLY;
        } else if (periodFrequency.equals(PeriodFrequencyType.YEARS)) {
            frequency = Recur.YEARLY;
        }
        return frequency;
    }

    public static int getInterval(final String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        return recur.getInterval();
    }

    public static CalendarFrequencyType getFrequency(final String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        return CalendarFrequencyType.fromString(recur.getFrequency());
    }

    public static CalendarWeekDaysType getRepeatsOnDay(final String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        final WeekDayList weekDays = recur.getDayList();
        if (weekDays.isEmpty()) return CalendarWeekDaysType.INVALID;
        // supports only one day
        WeekDay weekDay = (WeekDay) weekDays.get(0);
        return CalendarWeekDaysType.fromString(weekDay.getDay());
    }
    public static NthDayType getRepeatsOnNthDayOfMonth(final String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        NumberList monthDays = null;
        if(recur.getDayList().isEmpty())
        	monthDays = recur.getMonthDayList();
        else
        	monthDays = recur.getSetPosList();
        if (monthDays.isEmpty()) return NthDayType.INVALID;
        if (!recur.getMonthDayList().isEmpty() && recur.getSetPosList().isEmpty()) return NthDayType.ONDAY;
        Integer monthDay = (Integer) monthDays.get(0);
        return NthDayType.fromInt(monthDay);
    }

    public static LocalDate getFirstRepaymentMeetingDate(final Calendar calendar, final LocalDate disbursementDate,
            final Integer loanRepaymentInterval, final String frequency, boolean isSkipRepaymentOnFirstDayOfMonth,
            final Integer numberOfDays) {
        final Recur recur = CalendarUtils.getICalRecur(calendar.getRecurrence());
        if (recur == null) { return null; }
        LocalDate startDate = disbursementDate;
        final LocalDate seedDate = calendar.getStartDateLocalDate();
        if (isValidRedurringDate(calendar.getRecurrence(), seedDate, startDate, isSkipRepaymentOnFirstDayOfMonth, numberOfDays) && !frequency.equals(Recur.DAILY)) {
            startDate = startDate.plusDays(1);
        }
        // Recurring dates should follow loanRepaymentInterval.
        // e.g.
        // for weekly meeting interval is 1
        // where as for loan product with fortnightly frequency interval is 2
        // to generate currect set of meeting dates reset interval same as loan
        // repayment interval.
        recur.setInterval(loanRepaymentInterval);

        // Recurring dates should follow loanRepayment frequency.
        // e.g.
        // daily meeting frequency should support all loan products with any
        // frequency type.
        // to generate currect set of meeting dates reset frequency same as loan
        // repayment frequency.
        if (recur.getFrequency().equals(Recur.DAILY)) {
            recur.setFrequency(frequency);
        }

        final LocalDate firstRepaymentDate = getNextRecurringDate(recur, seedDate, startDate);
        if (isSkipRepaymentOnFirstDayOfMonth && firstRepaymentDate.getDayOfMonth() == 1) { return adjustRecurringDate(firstRepaymentDate,
                numberOfDays); }

        return firstRepaymentDate;
    }

    public static LocalDate getNewRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate,
            final LocalDate oldRepaymentDate, final Integer loanRepaymentInterval, final String frequency, final WorkingDays workingDays,
            final boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return null; }
        if (isValidRecurringDate(recur, seedDate, oldRepaymentDate, isSkipRepaymentOnFirstDayOfMonth, numberOfDays)) { return oldRepaymentDate; }
        LocalDate nextRapaymentDate = getNextRepaymentMeetingDate(recurringRule, seedDate, oldRepaymentDate, loanRepaymentInterval,
                frequency, workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays);

        return nextRapaymentDate;
    }

    public static LocalDate getNextRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate,
            final LocalDate repaymentDate, final Integer loanRepaymentInterval, final String frequency, final WorkingDays workingDays,
            boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays) {

        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return null; }
        LocalDate tmpDate = repaymentDate;
        if (isValidRecurringDate(recur, seedDate, repaymentDate, isSkipRepaymentOnFirstDayOfMonth, numberOfDays)) {
            tmpDate = repaymentDate.plusDays(1);
        }
        /*
         * Recurring dates should follow loanRepaymentInterval.
         * 
         * e.g. The weekly meeting will have interval of 1, if the loan product
         * with fortnightly frequency will have interval of 2, to generate right
         * set of meeting dates reset interval same as loan repayment interval.
         */
        recur.setInterval(loanRepaymentInterval);

        /*
         * Recurring dates should follow loanRepayment frequency. //e.g. daily
         * meeting frequency should support all loan products with any type of
         * frequency. to generate right set of meeting dates reset frequency
         * same as loan repayment frequency.
         */
        if (recur.getFrequency().equals(Recur.DAILY)) {
            recur.setFrequency(frequency);
        }

        LocalDate newRepaymentDate = getNextRecurringDate(recur, seedDate, tmpDate);
        final LocalDate nextRepaymentDate = getNextRecurringDate(recur, seedDate, newRepaymentDate);

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

        if (oldRecur == null || oldRecur.getFrequency() == null || newRecur == null || newRecur.getFrequency() == null) { return false; }
        return oldRecur.getFrequency().equals(newRecur.getFrequency());
    }

    public static boolean isIntervalSame(final String oldRRule, final String newRRule) {
        final Recur oldRecur = getICalRecur(oldRRule);
        final Recur newRecur = getICalRecur(newRRule);

        if (oldRecur == null || oldRecur.getFrequency() == null || newRecur == null || newRecur.getFrequency() == null) { return false; }
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
     * function returns a comma separated list of calendar_type_enum values ex.
     * 1,2,3,4
     * 
     * @param calendarTypeOptions
     * @return
     */
    public static String getSqlCalendarTypeOptionsInString(final List<Integer> calendarTypeOptions) {
        String sqlCalendarTypeOptions = "";
        final int size = calendarTypeOptions.size();
        for (int i = 0; i < size - 1; i++) {
            sqlCalendarTypeOptions += calendarTypeOptions.get(i).toString() + ",";
        }
        sqlCalendarTypeOptions += calendarTypeOptions.get(size - 1).toString();
        return sqlCalendarTypeOptions;
    }

    public static LocalDate getRecentEligibleMeetingDate(final String recurringRule, final LocalDate seedDate,
            final boolean isSkipMeetingOnFirstDay, final Integer numberOfDays) {
        LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return null; }

        if (isValidRecurringDate(recur, seedDate, currentDate, isSkipMeetingOnFirstDay, numberOfDays)) { return currentDate; }

        if (recur.getFrequency().equals(Recur.DAILY)) {
            currentDate = currentDate.plusDays(recur.getInterval());
        } else if (recur.getFrequency().equals(Recur.WEEKLY)) {
            currentDate = currentDate.plusWeeks(recur.getInterval());
        } else if (recur.getFrequency().equals(Recur.MONTHLY)) {
            currentDate = currentDate.plusMonths(recur.getInterval());
        } else if (recur.getFrequency().equals(Recur.YEARLY)) {
            currentDate = currentDate.plusYears(recur.getInterval());
        }

        return getNextRecurringDate(recur, seedDate, currentDate);
    }

    public static LocalDate getNextScheduleDate(final Calendar calendar, final LocalDate startDate) {
        final Recur recur = CalendarUtils.getICalRecur(calendar.getRecurrence());
        if (recur == null) { return null; }
        LocalDate date = startDate;
        final LocalDate seedDate = calendar.getStartDateLocalDate();
        /**
         * if (isValidRedurringDate(calendar.getRecurrence(), seedDate, date)) {
         * date = date.plusDays(1); }
         **/

        final LocalDate scheduleDate = getNextRecurringDate(recur, seedDate, date);

        return scheduleDate;
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
                monthOnDay = (Integer) monthDayList.get(0);
            }
        }
        return monthOnDay;
    }
    
    public static LocalDate getNextRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate,
            final LocalDate repaymentDate, final Integer loanRepaymentInterval, final String frequency, final WorkingDays workingDays,
            boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays, boolean applyWorkingDays) {
        boolean isCalledFirstTime = true;
        return getNextRepaymentMeetingDate(recurringRule, seedDate, repaymentDate, loanRepaymentInterval, frequency,
                workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays, isCalledFirstTime, applyWorkingDays);
    }
    
    public static LocalDate getNextRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate,
            final LocalDate repaymentDate, final Integer loanRepaymentInterval, final String frequency, 
            boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays) {
        boolean isCalledFirstTime = true;
        final WorkingDays workingDays = null;
        boolean applyWorkingDays = false;
        return getNextRepaymentMeetingDate(recurringRule, seedDate, repaymentDate, loanRepaymentInterval, frequency,
                workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays, isCalledFirstTime, applyWorkingDays);
    }
    
    public static LocalDate getNextRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate,
            final LocalDate repaymentDate, final Integer loanRepaymentInterval, final String frequency, final WorkingDays workingDays,
            boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays, boolean isCalledFirstTime, boolean applyWorkingDays) {

        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return null; }
        LocalDate tmpDate = repaymentDate;
        
        final Integer repaymentInterval = getMeetingIntervalFromFrequency(loanRepaymentInterval, frequency, recur);
        /*
         * Recurring dates should follow loanRepaymentInterval.
         * 
         * e.g. The weekly meeting will have interval of 1, if the loan product
         * with fortnightly frequency will have interval of 2, to generate right
         * set of meeting dates reset interval same as loan repayment interval.
         */
        int meetingInterval = recur.getInterval();
        if(meetingInterval < 1){
                meetingInterval = 1;
        }
        int rep = repaymentInterval<meetingInterval ? 1: repaymentInterval / meetingInterval ;

        /*
         * Recurring dates should follow loanRepayment frequency. //e.g. daily
         * meeting frequency should support all loan products with any type of
         * frequency. to generate right set of meeting dates reset frequency
         * same as loan repayment frequency.
         */
        if (recur.getFrequency().equals(Recur.DAILY)) {
            recur.setFrequency(frequency);
        }
        
        /**
         * Below code modified as discussed with Pramod N 
         */
        LocalDate newRepaymentDate = tmpDate;
        int newRepayment = rep;
        while (newRepayment > 0) {
            newRepaymentDate = getNextRecurringDate(recur, seedDate, newRepaymentDate);
            newRepayment--;
        }

        LocalDate nextRepaymentDate = null;
        if (applyWorkingDays) {
            if (WorkingDaysUtil.isNonWorkingDay(workingDays, newRepaymentDate)
                    && WorkingDaysUtil.getRepaymentRescheduleType(workingDays, newRepaymentDate).isMoveToNextRepaymentDay()) {
                newRepaymentDate = getNextRepaymentMeetingDate(recurringRule, seedDate, newRepaymentDate.plusDays(1),
                        loanRepaymentInterval, frequency, workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays, isCalledFirstTime,
                        applyWorkingDays);
            } else {
                newRepaymentDate = WorkingDaysUtil.getOffSetDateIfNonWorkingDay(newRepaymentDate, nextRepaymentDate, workingDays);
            }
        }
        
        if(isCalledFirstTime && newRepaymentDate.equals(repaymentDate)){
            isCalledFirstTime = false;
            newRepaymentDate = getNextRepaymentMeetingDate(recurringRule, seedDate, repaymentDate.plusDays(1), loanRepaymentInterval,
                    frequency, workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays, isCalledFirstTime, applyWorkingDays);
        }
        
        if (isSkipRepaymentOnFirstDayOfMonth) {
            final LocalDate newRepaymentDateTemp = adjustRecurringDate(newRepaymentDate, numberOfDays);
            if (applyWorkingDays) {
                if (WorkingDaysUtil.isNonWorkingDay(workingDays, newRepaymentDateTemp)
                        && WorkingDaysUtil.getRepaymentRescheduleType(workingDays, newRepaymentDateTemp).isMoveToNextRepaymentDay()) {
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
         * check loanRepaymentInterval equal to 1, if repayments frequency is
         * monthly and meeting frequency is weekly, then generate repayments
         * schedule as every 4 weeks
         */
        if (frequency.equals(Recur.MONTHLY) && recur.getFrequency().equals(Recur.WEEKLY)) {
            repaymentInterval = loanRepaymentInterval*interval;
        }
        return repaymentInterval;
    }
}
