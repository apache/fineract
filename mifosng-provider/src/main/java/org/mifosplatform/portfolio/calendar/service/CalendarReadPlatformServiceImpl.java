/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.exception.CalendarNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class CalendarReadPlatformServiceImpl implements CalendarReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CalendarReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class CalendarDataMapper implements RowMapper<CalendarData> {

        public String schema() {
            return " select c.id as id, ci.entity_id as entityId, ci.entity_type_enum as entityTypeId, c.title as title, "
                    + " c.description as description, c.location as location, c.start_date as startDate, c.end_date as endDate, "
                    + " c.duration as duration, c.calendar_type_enum as typeId, c.repeating as repeating, "
                    + " c.recurrence as recurrence, c.remind_by_enum as remindById, c.first_reminder as firstReminder, c.second_reminder as secondReminder, "
                    + " c.created_date as createdDate, c.lastmodified_date as updatedDate, creatingUser.id as creatingUserId, creatingUser.username as creatingUserName, "
                    + " updatingUser.id as updatingUserId, updatingUser.username as updatingUserName "
                    + " from m_calendar c join m_calendar_instance ci on ci.calendar_id=c.id, m_appuser as creatingUser, m_appuser as updatingUser"
                    + " where c.createdby_id=creatingUser.id and c.lastmodifiedby_id=updatingUser.id ";
        }

        @Override
        public CalendarData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
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
            final Integer remindById = rs.getInt("remindById");
            EnumOptionData remindBy = null;
            if(remindById != null && remindById != 0) remindBy = CalendarEnumerations.calendarRemindBy(remindById);
            final Integer firstReminder = rs.getInt("firstReminder");
            final Integer secondReminder = rs.getInt("secondReminder");
            final String humanReadable = CalendarHelper.getRRuleReadable(startDate, recurrence);
            
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");
            final LocalDate lastUpdatedDate = JdbcSupport.getLocalDate(rs, "updatedDate");
            final Long createdByUserId = rs.getLong("creatingUserId");
            final String createdByUserName = rs.getString("creatingUserName");
            final Long lastUpdatedByUserId = rs.getLong("updatingUserId");
            final String lastUpdatedByUserName = rs.getString("updatingUserName");

            return new CalendarData(id, entityId, entityType, title, description, location, startDate, endDate, duration, type, repeating, recurrence, remindBy, firstReminder, secondReminder, humanReadable, createdDate, lastUpdatedDate, createdByUserId, createdByUserName, lastUpdatedByUserId, lastUpdatedByUserName);
        }
    }

    @Override
    public CalendarData retrieveCalendar(final Long calendarId, Long entityId, Integer entityTypeId) {

        try {
            final CalendarDataMapper rm = new CalendarDataMapper();

            final String sql = rm.schema() + " and c.id = ? and ci.entity_id = ? and ci.entity_type_enum = ? ";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { calendarId, entityId, entityTypeId });
        } catch (final EmptyResultDataAccessException e) {
            throw new CalendarNotFoundException(calendarId);
        }
    }

    @Override
    public Collection<CalendarData> retrieveCalendarsByEntity(final Long entityId, final Integer entityTypeId) {
        final CalendarDataMapper rm = new CalendarDataMapper();

        final String sql = rm.schema() + " and ci.entity_id = ? and ci.entity_type_enum = ? order by c.start_date ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { entityId, entityTypeId });
    }
    
    @Override
    public Collection<CalendarData> retrieveParentCalendarsByEntity(Long entityId, Integer entityTypeId) {
        
        final CalendarDataMapper rm = new CalendarDataMapper();
        CalendarEntityType ceType = CalendarEntityType.fromInt(entityTypeId);
        String parentHeirarchyCondition = getParentHierarchyCondition(ceType);
        final String sql = rm.schema() + " " + parentHeirarchyCondition        		
                + " and ci.entity_type_enum = ? order by c.start_date ";
        //FIXME :AA center is the parent entity of group, change this code to support more parent entity types.
        return this.jdbcTemplate.query(sql, rm, new Object[] { entityId, CalendarEntityType.CENTERS.getValue() });
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
    public CalendarData generateRecurringDate(CalendarData calendarData) {
        if(!calendarData.isRepeating()) return calendarData;
        final String rrule = calendarData.getRecurrence();
        final LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        LocalDate seedDate = calendarData.getStartDate();
        LocalDate endDate = calendarData.getEndDate();
        LocalDate startDate = calendarData.getStartDate();
        if(seedDate.isBefore(currentDate.minusYears(1))){
            startDate = currentDate.minusYears(1);
        }
        
        if(endDate == null || endDate.isAfter(currentDate.plusYears(1))){
            endDate = currentDate.plusYears(1);
        }
        
        final Collection<LocalDate> recurringDates = CalendarHelper.getRecurringDates(rrule, seedDate, startDate, endDate, -1);
        final Collection<LocalDate> nextTenRecurringDates = CalendarHelper.getRecurringDates(rrule, seedDate, endDate);
        return new CalendarData(calendarData, recurringDates, nextTenRecurringDates);
    }

    @Override
    public Collection<CalendarData> generateRecurringDates(Collection<CalendarData> calendarsData) {
        Collection<CalendarData> recuCalendarsData = new ArrayList<CalendarData>();

        for (CalendarData calendarData : calendarsData) {
            recuCalendarsData.add(generateRecurringDate(calendarData));
        }

        return recuCalendarsData;
    }
    
    @Override
    public CalendarData retrieveLoanCalendar(Long loanId) {
        final CalendarDataMapper rm = new CalendarDataMapper();

        final String sql = rm.schema() + " and ci.entity_id = ? and ci.entity_type_enum = ? order by c.start_date ";
        CalendarData calendarData = null;
        final Collection<CalendarData> calendars = this.jdbcTemplate.query(sql, rm, new Object[] { loanId, CalendarEntityType.LOANS.getValue() });
        
        if(!CollectionUtils.isEmpty(calendars)){
            for (CalendarData calendar : calendars) {
                calendarData = calendar;
                break;//Loans are associated with only one calendar
            } 
        }
        
        return calendarData;
    }

    public static String getParentHierarchyCondition(final CalendarEntityType calendarEntityType) {
        String conditionSql = "";
        
        switch (calendarEntityType) {
            case CLIENTS:
                //TODO : AA : do we need to propagate to top level parent in hierarchy?
                conditionSql = " and ci.entity_id in (select gc.group_id from m_client c join m_group_client gc "
                        + " on c.id=gc.client_id where c.id = ? ) ";
            break;
            
            case GROUPS:
                //TODO : AA: add parent hierarchy for groups                
                conditionSql = " and ci.entity_id in (select g.parent_id from m_group g where g.id = ? ) ";
            break;
            
            case LOANS:
              //TODO : AA: do we need parent hierarchy calendars for loans?
                conditionSql = " and ci.entity_id = ?  ";
            break;

            default:
            break;
        }

        return conditionSql;
    }

}
