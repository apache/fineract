package org.mifosplatform.portfolio.calendar.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.springframework.stereotype.Service;

@Service
public class CalendarWrapperService {

    public CalendarWrapperService() {

    }

    public LocalDate getNextRecurrence(final CalendarData calendar) {

        final Recur recur = getRecur(calendar);

        if (recur == null) { return null; }

        final Date nextRecDate = recur.getNextDate(new Date(calendar.getStartDate().toDate()), new Date(calendar.getStartDate().toDate()));
        final LocalDate lNextRecDate = new LocalDate(nextRecDate);
        return lNextRecDate;
    }

    public Collection<LocalDate> getNextRecurrencesList(final CalendarData calendarData) {
        int maxCount = 10;//Default number of recurring dates
        return getNextRecurrencesList(calendarData, maxCount);
    }
    
    public Collection<LocalDate> getNextRecurrencesList(final CalendarData calendarData, int maxCount) {
        final Recur recur = getRecur(calendarData);

        if (recur == null) { return null; }

        final Date seed = new Date(calendarData.getStartDate().toDate());
        final DateTime periodStart = new DateTime(calendarData.getStartDate().toDate());
        
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(calendarData.getStartDate().toDate());
        
        if(calendarData.getEndDate() != null){
            endDate.setTime(calendarData.getEndDate().toDate());
        }else{
            endDate.add(Calendar.YEAR, 5);
        }

        final DateTime periodEnd = new DateTime(endDate.getTime());

        final Value value = new Value(Value.DATE.getValue());

        return getRecurringDates(recur.getDates(seed, periodStart, periodEnd, value, maxCount));
    }

    private Collection<LocalDate> getRecurringDates(final DateList dates) {

        Collection<LocalDate> recurringDates = new ArrayList<LocalDate>();

        for (@SuppressWarnings("rawtypes")
        final Iterator iterator = dates.iterator(); iterator.hasNext();) {
            final Date date = (Date) iterator.next();
            recurringDates.add(new LocalDate(date));
        }

        return recurringDates;
    }

    private Recur getRecur(final CalendarData calendar) {
        if (!calendar.isRepeating()) {
            // throw exception or return null
            // throw new CalendarNotReccuring();
            return null;
        }

        // Construct RRule
        try {
            final RRule rrule = new RRule(calendar.getRecurrence());
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

    public static String getRRuleReadable(final LocalDate startDate, final String recurringRule){
        
        String humanReadable = "";
        
        RRule rrule;
        Recur recur = null;
        try {
            rrule = new RRule(recurringRule);
            rrule.validate();
            recur = rrule.getRecur();
        } catch (ValidationException e) {
            throw new PlatformDataIntegrityException("error.msg.invalid.recurring.rule",
                    "The Recurring Rule value: " + recurringRule + " is not valid.", "recurrence", recurringRule);
        } catch (ParseException e) {
            throw new PlatformDataIntegrityException("error.msg.recurring.rule.parsing.error",
                    "Error in pasring the Recurring Rule value: " + recurringRule , "recurrence", recurringRule);
        }       
        
        if(recur == null) return humanReadable;
        
        
        if(recur.getFrequency().equals(Recur.DAILY)){
            if(recur.getInterval() == 1){
                humanReadable = "Daily";
            }else{
                humanReadable = "Every " + recur.getInterval() + " days";
            }
        }else if(recur.getFrequency().equals(Recur.WEEKLY)){
            if(recur.getInterval() == 1 || recur.getInterval() == -1){
                humanReadable = "Weekly";
            }else{
                humanReadable = "Every " + recur.getInterval() + " weeks";
            }
            
            humanReadable += " on ";
            WeekDayList weekDayList = recur.getDayList();
            
            for (@SuppressWarnings("rawtypes")
            Iterator iterator = weekDayList.iterator(); iterator.hasNext();) {
                WeekDay weekDay = (WeekDay) iterator.next();
                humanReadable += weekDay.getDay();
            }
                        
        }else if(recur.getFrequency().equals(Recur.MONTHLY)){
            if(recur.getInterval() == 1){
                humanReadable = "Monthly on day " + startDate.getDayOfMonth();
            }else{
                humanReadable = "Every " + recur.getInterval() + " months on day " + startDate.getDayOfMonth();
            }
        }else if(recur.getFrequency().equals(Recur.YEARLY)){
            if(recur.getInterval() == 1){
                humanReadable = "Annually on " + startDate.toString("MMM") + " " +  startDate.getDayOfMonth();
            }else{
                humanReadable = "Every " + recur.getInterval() + " years on " + startDate.toString("MMM") + " " + startDate.getDayOfMonth();
            }
        } 
        
        if(recur.getCount() > 0){
            if(recur.getCount() == 1){
                humanReadable = "Once";
            }
            humanReadable += ", " + recur.getCount() + " times";
        }
        
        Date endDate = recur.getUntil();
        LocalDate date = new LocalDate(endDate);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd MMMM YY");
        String formattedDate = date.toString( fmt );
        if(endDate != null){
            humanReadable += ", until " + formattedDate;
        }
        
        return humanReadable;
    }
}
