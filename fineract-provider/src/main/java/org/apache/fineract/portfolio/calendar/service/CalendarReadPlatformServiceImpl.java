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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.exception.CalendarNotFoundException;
import org.apache.fineract.portfolio.meeting.data.MeetingData;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class CalendarReadPlatformServiceImpl implements CalendarReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public CalendarReadPlatformServiceImpl(final RoutingDataSource dataSource, final ConfigurationDomainService configurationDomainService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.configurationDomainService = configurationDomainService;
    }

    private static final class CalendarDataMapper implements RowMapper<CalendarData> {

        public String schema() {
            return " select c.id as id, ci.id as calendarInstanceId, ci.entity_id as entityId, ci.entity_type_enum as entityTypeId, c.title as title, "
                    + " c.description as description, c.location as location, c.start_date as startDate, c.end_date as endDate, "
                    + " c.duration as duration, c.calendar_type_enum as typeId, c.repeating as repeating, "
                    + " c.recurrence as recurrence, c.remind_by_enum as remindById, c.first_reminder as firstReminder, c.second_reminder as secondReminder, "
                    + " c.created_date as createdDate, c.lastmodified_date as updatedDate, creatingUser.id as creatingUserId, creatingUser.username as creatingUserName, "
                    + " updatingUser.id as updatingUserId, updatingUser.username as updatingUserName,c.meeting_time as meetingTime "
                    + " from m_calendar c join m_calendar_instance ci on ci.calendar_id=c.id, m_appuser as creatingUser, m_appuser as updatingUser"
                    + " where c.createdby_id=creatingUser.id and c.lastmodifiedby_id=updatingUser.id ";
        }

        @Override
        public CalendarData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long calendarInstanceId = rs.getLong("calendarInstanceId");
            final Long entityId = rs.getLong("entityId");
            final Integer entityTypeId = rs.getInt("entityTypeId");
            final EnumOptionData entityType = CalendarEnumerations.calendarEntityType(entityTypeId);
            final String title = rs.getString("title");
            final String description = rs.getString("description");
            final String location = rs.getString("location");
            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");
            final Integer duration = rs.getInt("duration");
            final Integer typeId = rs.getInt("typeId");
            final EnumOptionData type = CalendarEnumerations.calendarType(typeId);
            final boolean repeating = rs.getBoolean("repeating");
            final String recurrence = rs.getString("recurrence");
            final EnumOptionData frequency = CalendarEnumerations.calendarFrequencyType(CalendarUtils.getFrequency(recurrence));
            final Integer interval = new Integer(CalendarUtils.getInterval(recurrence));
            final EnumOptionData repeatsOnDay = CalendarEnumerations.calendarWeekDaysType(CalendarUtils.getRepeatsOnDay(recurrence));
            final EnumOptionData repeatsOnNthDayOfMonth = CalendarEnumerations.calendarFrequencyNthDayType(CalendarUtils.getRepeatsOnNthDayOfMonth(recurrence));
            final Integer remindById = rs.getInt("remindById");
            EnumOptionData remindBy = null;
            if (remindById != null && remindById != 0) {
                remindBy = CalendarEnumerations.calendarRemindBy(remindById);
            }
            final Integer firstReminder = rs.getInt("firstReminder");
            final Integer secondReminder = rs.getInt("secondReminder");
            String humanReadable = null;
            if (startDate != null && recurrence != null) {
                humanReadable = CalendarUtils.getRRuleReadable(startDate, recurrence);
            }
            Integer monthOnDay = CalendarUtils.getMonthOnDay(recurrence);
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");
            final LocalDate lastUpdatedDate = JdbcSupport.getLocalDate(rs, "updatedDate");
            final Long createdByUserId = rs.getLong("creatingUserId");
            final String createdByUserName = rs.getString("creatingUserName");
            final Long lastUpdatedByUserId = rs.getLong("updatingUserId");
            final String lastUpdatedByUserName = rs.getString("updatingUserName");
            final LocalTime meetingTime = JdbcSupport.getLocalTime(rs,"meetingTime");

            return CalendarData.instance(id, calendarInstanceId, entityId, entityType, title, description, location, startDate, endDate,
                    duration, type, repeating, recurrence, frequency, interval, repeatsOnDay, repeatsOnNthDayOfMonth, remindBy, firstReminder, secondReminder,
                    humanReadable, createdDate, lastUpdatedDate, createdByUserId, createdByUserName, lastUpdatedByUserId,
                    lastUpdatedByUserName,meetingTime, monthOnDay);
        }
    }

    @Override
    public CalendarData retrieveCalendar(final Long calendarId, final Long entityId, final Integer entityTypeId) {

        try {
            final CalendarDataMapper rm = new CalendarDataMapper();

            final String sql = rm.schema() + " and c.id = ? and ci.entity_id = ? and ci.entity_type_enum = ? ";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { calendarId, entityId, entityTypeId });
        } catch (final EmptyResultDataAccessException e) {
            throw new CalendarNotFoundException(calendarId);
        }
    }

    @Override
    public Collection<CalendarData> retrieveCalendarsByEntity(final Long entityId, final Integer entityTypeId,
            final List<Integer> calendarTypeOptions) {
        final CalendarDataMapper rm = new CalendarDataMapper();

        Collection<CalendarData> result = null;

        String sql = "";

        if (calendarTypeOptions == null || calendarTypeOptions.isEmpty()) {
            sql = rm.schema() + " and ci.entity_id = ? and ci.entity_type_enum = ? order by c.start_date ";
            result = this.jdbcTemplate.query(sql, rm, new Object[] { entityId, entityTypeId });
        } else if (!calendarTypeOptions.isEmpty()) {
            final String sqlCalendarTypeOptions = CalendarUtils.getSqlCalendarTypeOptionsInString(calendarTypeOptions);
            sql = rm.schema() + " and ci.entity_id = ? and ci.entity_type_enum = ? and c.calendar_type_enum in ( " + sqlCalendarTypeOptions
                    + " ) order by c.start_date ";
            result = this.jdbcTemplate.query(sql, rm, new Object[] { entityId, entityTypeId });
        }
        return result;
    }

    @Override
    public CalendarData retrieveCollctionCalendarByEntity(final Long entityId, final Integer entityTypeId) {
        final CalendarDataMapper rm = new CalendarDataMapper();

        final String sql = rm.schema()
                + " and ci.entity_id = ? and ci.entity_type_enum = ? and calendar_type_enum = ? order by c.start_date ";
        final List<CalendarData> result = this.jdbcTemplate.query(sql, rm,
                new Object[] { entityId, entityTypeId, CalendarType.COLLECTION.getValue() });

        if (!result.isEmpty() && result.size() > 0) { return result.get(0); }

        return null;
    }

    @Override
    public Collection<CalendarData> retrieveParentCalendarsByEntity(final Long entityId, final Integer entityTypeId,
            final List<Integer> calendarTypeOptions) {

        final CalendarDataMapper rm = new CalendarDataMapper();
        Collection<CalendarData> result = null;
        String sql = "";
        final CalendarEntityType ceType = CalendarEntityType.fromInt(entityTypeId);
        final String parentHeirarchyCondition = getParentHierarchyCondition(ceType);

        // FIXME :AA center is the parent entity of group, change this code to
        // support more parent entity types.
        if (calendarTypeOptions == null || calendarTypeOptions.isEmpty()) {
            sql = rm.schema() + " " + parentHeirarchyCondition + " and ci.entity_type_enum = ? order by c.start_date ";
            result = this.jdbcTemplate.query(sql, rm, new Object[] { entityId, CalendarEntityType.CENTERS.getValue() });
        } else {
            final String sqlCalendarTypeOptions = CalendarUtils.getSqlCalendarTypeOptionsInString(calendarTypeOptions);
            sql = rm.schema() + " " + parentHeirarchyCondition + " and ci.entity_type_enum = ? and c.calendar_type_enum in ("
                    + sqlCalendarTypeOptions + ") order by c.start_date ";
            result = this.jdbcTemplate.query(sql, rm, new Object[] { entityId, CalendarEntityType.CENTERS.getValue() });
        }
        return result;
    }

    @Override
    public Collection<CalendarData> retrieveAllCalendars() {

        final CalendarDataMapper rm = new CalendarDataMapper();

        final String sql = rm.schema();

        return this.jdbcTemplate.query(sql, rm);
    }

    @Override
    public CalendarData retrieveNewCalendarDetails() {
        return CalendarData.sensibleDefaultsForNewCalendarCreation();
    }

    @Override
    public Collection<LocalDate> generateRecurringDates(final CalendarData calendarData, final boolean withHistory, final LocalDate tillDate) {
        final LocalDate fromDate = null;
        Collection<LocalDate> recurringDates = generateRecurringDate(calendarData, fromDate, tillDate, -1);

        if (withHistory) {
            final Collection<CalendarData> calendarHistorys = this.retrieveCalendarsFromHistory(calendarData.getId());
            for (CalendarData calendarHistory : calendarHistorys) {
                recurringDates.addAll(generateRecurringDate(calendarHistory, fromDate, tillDate, -1));
            }
        }

        return recurringDates;
    }

    @Override
    public Collection<LocalDate> generateNextTenRecurringDates(CalendarData calendarData) {
        final LocalDate tillDate = null;
        return generateRecurringDate(calendarData, DateUtils.getLocalDateOfTenant(), tillDate, 10);
    }

    private Collection<LocalDate> generateRecurringDate(final CalendarData calendarData, final LocalDate fromDate,
            final LocalDate tillDate, final int maxCount) {

        if (!calendarData.isRepeating()) { return null; }
        final String rrule = calendarData.getRecurrence();
        /**
         * Start date or effective from date of calendar recurrence.
         */
        final LocalDate seedDate = this.getSeedDate(calendarData.getStartDate());
        /**
         * periodStartDate date onwards recurring dates will be generated.
         */
        final LocalDate periodStartDate = this.getPeriodStartDate(seedDate, calendarData.getStartDate(), fromDate);
        /**
         * till periodEndDate recurring dates will be generated.
         */
        final LocalDate periodEndDate = this.getPeriodEndDate(calendarData.getEndDate(), tillDate);
         
		Integer numberOfDays = 0;
		boolean isSkipRepaymentOnFirstMonthEnabled = this.configurationDomainService
				.isSkippingMeetingOnFirstDayOfMonthEnabled();
		if (isSkipRepaymentOnFirstMonthEnabled) {
			numberOfDays = this.configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue();
		}

		final Collection<LocalDate> recurringDates = CalendarUtils.getRecurringDates(rrule, seedDate, periodStartDate,
				periodEndDate, maxCount, isSkipRepaymentOnFirstMonthEnabled, numberOfDays);
		return recurringDates;
	}

	@Override
	public Boolean isCalendarAssociatedWithEntity(final Long entityId, final Long calendarId, final Long entityTypeId) {
		String query = "Select COUNT(*) from m_calendar_instance ci where ci.entity_id = ? and ci.calendar_id = ? and "
				+ " ci.entity_type_enum = ?";
		try {
			int calendarInstaneId = this.jdbcTemplate.queryForObject(query,
					new Object[] { entityId, calendarId, entityTypeId }, Integer.class);
			if (calendarInstaneId > 0) {
				return true;
			}
			return false;
		} catch (final EmptyResultDataAccessException e) {
			return false;
		}
	}

    private LocalDate getSeedDate(LocalDate date) {
        return date;
    }

    private LocalDate getPeriodStartDate(final LocalDate seedDate, final LocalDate recurrenceStartDate, final LocalDate fromDate) {
        LocalDate periodStartDate = null;
        if (fromDate != null) {
            periodStartDate = fromDate;
        } else {
            final LocalDate currentDate = DateUtils.getLocalDateOfTenant();
            if (seedDate.isBefore(currentDate.minusYears(1))) {
                periodStartDate = currentDate.minusYears(1);
            } else {
                periodStartDate = recurrenceStartDate;
            }
        }
        return periodStartDate;
    }

    private LocalDate getPeriodEndDate(LocalDate endDate, LocalDate tillDate) {
        LocalDate periodEndDate = endDate;
        final LocalDate currentDate = DateUtils.getLocalDateOfTenant();

        if (tillDate != null) {
            if (endDate != null) {
                if (endDate.isAfter(tillDate)) {
                    // to retrieve meeting dates tillspecified date (tillDate)
                    periodEndDate = tillDate;
                }
            } else {
                // end date is null then fetch meeting dates tillDate
                periodEndDate = tillDate;
            }
        } else if (endDate == null || endDate.isAfter(currentDate.plusYears(1))) {
            periodEndDate = currentDate.plusYears(1);
        }
        return periodEndDate;
    }

    @Override
    public LocalDate generateNextEligibleMeetingDateForCollection(final CalendarData calendarData, final MeetingData lastMeetingData) {

        final LocalDate lastMeetingDate = (lastMeetingData == null) ? null : lastMeetingData.getMeetingDate();
        // get applicable calendar based on meeting date
        CalendarData applicableCalendarData = calendarData;
        LocalDate nextEligibleMeetingDate = null;
        /**
         * The next available meeting date for collection should be taken from
         * application calendar for that time period. e.g. If the previous
         * calendar details has weekly meeting starting from 1st of Oct 2013 on
         * every Tuesday, then meeting dates for collection are 1,8,15,22,29..
         * 
         * If meeting schedule has changed from Tuesday to Friday with effective
         * from 15th of Oct (calendar update has made on 2nd of Oct) , then
         * application should allow to generate collection sheet on 8th of Oct
         * which is still on Tuesday and next collection sheet date should be on
         * 18th of Oct as per current calendar
         */
        
       
		Integer numberOfDays = 0;
		boolean isSkipRepaymentOnFirstMonthEnabled = configurationDomainService
				.isSkippingMeetingOnFirstDayOfMonthEnabled();
		if (isSkipRepaymentOnFirstMonthEnabled) {
			numberOfDays = configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue();
		}

		if (lastMeetingDate != null && !calendarData.isBetweenStartAndEndDate(lastMeetingDate)
				&& !calendarData.isBetweenStartAndEndDate(DateUtils.getLocalDateOfTenant())) {
			applicableCalendarData = this.retrieveApplicableCalendarFromHistory(calendarData.getId(), lastMeetingDate);
			nextEligibleMeetingDate = CalendarUtils.getRecentEligibleMeetingDate(applicableCalendarData.getRecurrence(),
					lastMeetingDate, isSkipRepaymentOnFirstMonthEnabled, numberOfDays);
		}

		/**
		 * If nextEligibleMeetingDate is on or after current calendar startdate
		 * then regenerate the nextEligible meeting date based on
		 */
		if (nextEligibleMeetingDate == null) {
			final LocalDate seedDate = (lastMeetingDate != null) ? lastMeetingDate : calendarData.getStartDate();
			nextEligibleMeetingDate = CalendarUtils.getRecentEligibleMeetingDate(applicableCalendarData.getRecurrence(),
					seedDate, isSkipRepaymentOnFirstMonthEnabled, numberOfDays);
		} else if (calendarData.isBetweenStartAndEndDate(nextEligibleMeetingDate)) {
			nextEligibleMeetingDate = CalendarUtils.getRecentEligibleMeetingDate(applicableCalendarData.getRecurrence(),
					calendarData.getStartDate(), isSkipRepaymentOnFirstMonthEnabled, numberOfDays);
		}

		return nextEligibleMeetingDate;
	}

    @Override
    public Collection<CalendarData> updateWithRecurringDates(final Collection<CalendarData> calendarsData) {
        final Collection<CalendarData> recuCalendarsData = new ArrayList<>();
        final boolean withHistory = true;
        final LocalDate tillDate = null;
        for (final CalendarData calendarData : calendarsData) {
            final Collection<LocalDate> recurringDates = this.generateRecurringDates(calendarData, withHistory, tillDate);
            final Collection<LocalDate> nextTenRecurringDates = this.generateNextTenRecurringDates(calendarData);
            final LocalDate recentEligibleMeetingDate = null;
            final CalendarData updatedCalendarData = CalendarData.withRecurringDates(calendarData, recurringDates, nextTenRecurringDates,
                    recentEligibleMeetingDate);
            recuCalendarsData.add(updatedCalendarData);
        }

        return recuCalendarsData;
    }

    @Override
    public CalendarData retrieveLoanCalendar(final Long loanId) {
        final CalendarDataMapper rm = new CalendarDataMapper();

        final String sql = rm.schema() + " and ci.entity_id = ? and ci.entity_type_enum = ? order by c.start_date ";
        CalendarData calendarData = null;
        final Collection<CalendarData> calendars = this.jdbcTemplate.query(sql, rm,
                new Object[] { loanId, CalendarEntityType.LOANS.getValue() });

        if (!CollectionUtils.isEmpty(calendars)) {
            for (final CalendarData calendar : calendars) {
                calendarData = calendar;
                break;// Loans are associated with only one calendar
            }
        }

        return calendarData;
    }

    public static String getParentHierarchyCondition(final CalendarEntityType calendarEntityType) {
        String conditionSql = "";

        switch (calendarEntityType) {
            case CLIENTS:
                // TODO : AA : do we need to propagate to top level parent in
                // hierarchy?
                conditionSql = " and ci.entity_id in (select gc.group_id from m_client c join m_group_client gc "
                        + " on c.id=gc.client_id where c.id = ? ) ";
            break;

            case GROUPS:
                // TODO : AA: add parent hierarchy for groups
                conditionSql = " and ci.entity_id in (select g.parent_id from m_group g where g.id = ? ) ";
            break;

            case LOANS:
                // TODO : AA: do we need parent hierarchy calendars for loans?
                conditionSql = " and ci.entity_id = ?  ";
            break;

            default:
            break;
        }

        return conditionSql;
    }

    private CalendarData retrieveApplicableCalendarFromHistory(Long calendarId, LocalDate compareDate) {
        try {
            final CalendarDataFromHistoryMapper rm = new CalendarDataFromHistoryMapper();

            final String sql = rm.schema() + " where c.calendar_id = ? and date(?) between c.start_date and c.end_date limit 1";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { calendarId, compareDate.toDate() });
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Collection<CalendarData> retrieveCalendarsFromHistory(Long calendarId) {
        try {
            final CalendarDataFromHistoryMapper rm = new CalendarDataFromHistoryMapper();

            final String sql = rm.schema() + " where c.calendar_id = ? ";

            final Collection<CalendarData> calendars = this.jdbcTemplate.query(sql, rm, new Object[] { calendarId });
            return calendars;
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final class CalendarDataFromHistoryMapper implements RowMapper<CalendarData> {

        public String schema() {
            return " select c.calendar_id as id, c.title as title, c.description as description, c.location as location, c.start_date as startDate, "
                    + " c.end_date as endDate, c.duration as duration, c.calendar_type_enum as typeId, c.repeating as repeating, "
                    + " c.recurrence as recurrence, c.remind_by_enum as remindById, c.first_reminder as firstReminder, c.second_reminder as secondReminder "
                    + " from m_calendar_history c ";
        }

        @Override
        public CalendarData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long calendarInstanceId = null;
            final Long entityId = null;
            final EnumOptionData entityType = null;
            final String title = rs.getString("title");
            final String description = rs.getString("description");
            final String location = rs.getString("location");
            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");
            final Integer duration = rs.getInt("duration");
            final Integer typeId = rs.getInt("typeId");
            final EnumOptionData type = CalendarEnumerations.calendarType(typeId);
            final boolean repeating = rs.getBoolean("repeating");
            final String recurrence = rs.getString("recurrence");
            final EnumOptionData frequency = CalendarEnumerations.calendarFrequencyType(CalendarUtils.getFrequency(recurrence));
            final Integer interval = new Integer(CalendarUtils.getInterval(recurrence));
            final EnumOptionData repeatsOnDay = CalendarEnumerations.calendarWeekDaysType(CalendarUtils.getRepeatsOnDay(recurrence));
            final EnumOptionData repeatsOnNthDayOfMonth = CalendarEnumerations.calendarFrequencyNthDayType(CalendarUtils
                    .getRepeatsOnNthDayOfMonth(recurrence));
            final Integer remindById = rs.getInt("remindById");
            EnumOptionData remindBy = null;
            if (remindById != null && remindById != 0) {
                remindBy = CalendarEnumerations.calendarRemindBy(remindById);
            }
            final Integer firstReminder = rs.getInt("firstReminder");
            final Integer secondReminder = rs.getInt("secondReminder");
            String humanReadable = null;
            if (startDate != null && recurrence != null) {
                humanReadable = CalendarUtils.getRRuleReadable(startDate, recurrence);
            }

            final LocalDate createdDate = null;
            final LocalDate lastUpdatedDate = null;
            final Long createdByUserId = null;
            final String createdByUserName = null;
            final Long lastUpdatedByUserId = null;
            final String lastUpdatedByUserName = null;
            final LocalTime meetingTime = null;
            Integer monthOnDay = CalendarUtils.getMonthOnDay(recurrence);

            return CalendarData.instance(id, calendarInstanceId, entityId, entityType, title, description, location, startDate, endDate,
                    duration, type, repeating, recurrence, frequency, interval, repeatsOnDay, repeatsOnNthDayOfMonth, remindBy,
                    firstReminder, secondReminder, humanReadable, createdDate, lastUpdatedDate, createdByUserId, createdByUserName,
                    lastUpdatedByUserId, lastUpdatedByUserName, meetingTime, monthOnDay);
        }
    }
    
    
}