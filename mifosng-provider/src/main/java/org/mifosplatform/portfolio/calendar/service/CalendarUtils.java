/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.service;

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
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.RRule;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.service.WorkingDaysUtil;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarFrequencyType;
import org.mifosplatform.portfolio.calendar.domain.CalendarWeekDaysType;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;

public class CalendarUtils {

    static {
        System.setProperty("net.fortuna.ical4j.timezone.date.floating", "true");
    }

    public static LocalDate getNextRecurringDate(final String recurringRule, final LocalDate seedDate, final LocalDate startDate) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return null; }
        return getNextRecurringDate(recur, seedDate, startDate);
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
        return getRecurringDates(recurringRule, seedDate, periodStartDate, periodEndDate, maxCount);
    }

    public static Collection<LocalDate> getRecurringDates(final String recurringRule, final LocalDate seedDate,
            final LocalDate periodStartDate, final LocalDate periodEndDate, final int maxCount) {

        final Recur recur = CalendarUtils.getICalRecur(recurringRule);

        return getRecurringDates(recur, seedDate, periodStartDate, periodEndDate, maxCount);
    }

    private static Collection<LocalDate> getRecurringDates(final Recur recur, final LocalDate seedDate, final LocalDate periodStartDate,
            final LocalDate periodEndDate, final int maxCount) {
        if (recur == null) { return null; }
        final Date seed = convertToiCal4JCompatibleDate(seedDate);
        final DateTime periodStart = new DateTime(periodStartDate.toDate());
        final DateTime periodEnd = new DateTime(periodEndDate.toDate());

        final Value value = new Value(Value.DATE.getValue());
        final DateList recurringDates = recur.getDates(seed, periodStart, periodEnd, value, maxCount);
        return convertToLocalDateList(recurringDates);
    }

    private static Collection<LocalDate> convertToLocalDateList(final DateList dates) {

        final Collection<LocalDate> recurringDates = new ArrayList<>();

        for (@SuppressWarnings("rawtypes")
        final Iterator iterator = dates.iterator(); iterator.hasNext();) {
            final Date date = (Date) iterator.next();
            recurringDates.add(new LocalDate(date));
        }

        return recurringDates;
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
            if (recur.getInterval() == 1) {
                humanReadable = "Monthly on day " + startDate.getDayOfMonth();
            } else {
                humanReadable = "Every " + recur.getInterval() + " months on day " + startDate.getDayOfMonth();
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

        return isValidRecurringDate(recur, seedDate, date);
    }

    public static boolean isValidRecurringDate(final Recur recur, final LocalDate seedDate, final LocalDate date) {

        final Collection<LocalDate> recurDate = getRecurringDates(recur, seedDate, date, date.plusDays(1), 1);
        return (recurDate == null || recurDate.isEmpty()) ? false : true;
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

    public static PeriodFrequencyType getMeetingPeriodFrequencyType(final String recurringRule) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
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

    public static LocalDate getFirstRepaymentMeetingDate(final Calendar calendar, final LocalDate disbursementDate,
            final Integer loanRepaymentInterval, final String frequency) {
        final Recur recur = CalendarUtils.getICalRecur(calendar.getRecurrence());
        if (recur == null) { return null; }
        LocalDate startDate = disbursementDate;
        final LocalDate seedDate = calendar.getStartDateLocalDate();
        if (isValidRedurringDate(calendar.getRecurrence(), seedDate, startDate)) {
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

        return firstRepaymentDate;
    }

    public static LocalDate getNewRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate,
            final LocalDate oldRepaymentDate, final Integer loanRepaymentInterval, final String frequency, final WorkingDays workingDays) {
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return null; }
        if (isValidRecurringDate(recur, seedDate, oldRepaymentDate)) { return oldRepaymentDate; }
        return getNextRepaymentMeetingDate(recurringRule, seedDate, oldRepaymentDate, loanRepaymentInterval, frequency, workingDays);
    }

    public static LocalDate getNextRepaymentMeetingDate(final String recurringRule, final LocalDate seedDate,
            final LocalDate repaymentDate, final Integer loanRepaymentInterval, final String frequency, final WorkingDays workingDays) {

        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return null; }
        LocalDate tmpDate = repaymentDate;
        if (isValidRecurringDate(recur, seedDate, repaymentDate)) {
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

    public static LocalDate getRecentEligibleMeetingDate(final String recurringRule, final LocalDate seedDate) {
        LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        final Recur recur = CalendarUtils.getICalRecur(recurringRule);
        if (recur == null) { return null; }

        if (isValidRecurringDate(recur, seedDate, currentDate)) { return currentDate; }

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
}
