package org.mifosplatform.portfolio.calendar.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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

public class CalendarHelper {

    public static LocalDate getNextRecurringDate(final String recurringRule, final LocalDate seedDate, final LocalDate startDate) {

        final Recur recur = CalendarHelper.getICalRecur(recurringRule);

        if (recur == null) { return null; }

        final Date nextRecDate = recur.getNextDate(new Date(seedDate.toDate()), new Date(startDate.toDate()));
        return nextRecDate == null ? null : new LocalDate(nextRecDate);
    }

    public static Collection<LocalDate> getRecurringDates(final String recurringRule, final LocalDate seedDate, final LocalDate endDate) {

        final LocalDate periodStartDate = DateUtils.getLocalDateOfTenant();
        final LocalDate periodEndDate = (endDate == null) ? DateUtils.getLocalDateOfTenant().plusYears(5) : endDate;
        return getRecurringDates(recurringRule, seedDate, periodStartDate, periodEndDate);
    }

    public static Collection<LocalDate> getRecurringDates(final String recurringRule, final LocalDate seedDate,
            final LocalDate periodStartDate, final LocalDate periodEndDate) {
        final int maxCount = 15;// Default number of recurring dates
        return getRecurringDates(recurringRule, seedDate, periodStartDate, periodEndDate, maxCount);
    }

    public static Collection<LocalDate> getRecurringDates(final String recurringRule, final LocalDate seedDate,
            final LocalDate periodStartDate, final LocalDate periodEndDate, final int maxCount) {

        final Recur recur = CalendarHelper.getICalRecur(recurringRule);

        if (recur == null) { return null; }
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String seedDateStr = df.format(seedDate.toDateTimeAtStartOfDay().toDate()); 
        Date seed = null;
        try {
            seed = new Date(seedDateStr, "yyyy-MM-dd");
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final DateTime periodStart = new DateTime(periodStartDate.toDate());
        final DateTime periodEnd = new DateTime(periodEndDate.toDate());

        final Value value = new Value(Value.DATE.getValue());
        final DateList recurringDates = recur.getDates(seed, periodStart, periodEnd, value, maxCount);
        return convertToLocalDateList(recurringDates);
    }

    private static Collection<LocalDate> convertToLocalDateList(final DateList dates) {

        final Collection<LocalDate> recurringDates = new ArrayList<LocalDate>();

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
                humanReadable += DayNameEnum.from(weekDay.getDay()).getValue();
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
    
    public static boolean isValidRedurringDate(final String recurringRule, final LocalDate seedDate, final LocalDate date){
        
        final Collection<LocalDate> recurDate = getRecurringDates(recurringRule, seedDate, date, date.plusDays(1), 1);
        return (recurDate == null || recurDate.isEmpty()) ? false : true;
    }
    
    public static enum DayNameEnum{
        MO("Monday"),TU("Tuesday"),WE("Wednesday"),TH("Thursday"),FR("Friday"),SA("Saturday"),SU("Sunday");
        private final String value;
        
        private DayNameEnum(String value){
            this.value = value;
        }
        
        public String getValue(){
            return this.value;
        }
        
        public static DayNameEnum from(String code){
            for (DayNameEnum dayName : DayNameEnum.values()){
                if(dayName.toString().equals(code)) return dayName;
            }
            return DayNameEnum.MO;//Default it to Monday
        }
    }
}
