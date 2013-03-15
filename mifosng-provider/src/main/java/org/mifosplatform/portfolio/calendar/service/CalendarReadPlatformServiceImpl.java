/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.calendar.exception.CalendarNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CalendarReadPlatformServiceImpl implements CalendarReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CalendarReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class CalendarDataMapper implements RowMapper<CalendarData> {

        public String schema() {
            return " select c.id as id, c.entity_id as entityId, c.entity_type_enum as entityTypeId, c.title as title, "
                    + " c.description as description, c.location as location, c.start_date as startDate, c.end_date as endDate, "
                    + " c.created_date as createdDate, c.duration as duration, c.calendar_type_enum as typeId, c.repeating as repeating, "
                    + " c.recurrence as recurrence, c.remind_by_enum as remindById, c.first_reminder as firstReminder, c.second_reminder as secondReminder "
                    + " from m_calendar c ";
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
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");
            final Integer duration = rs.getInt("duration");
            final Integer typeId = rs.getInt("typeId");
            final EnumOptionData type = CalendarEnumerations.calendarType(typeId);
            final boolean repeating = rs.getBoolean("repeating");
            final String recurrence = rs.getString("recurrence");
            final Integer remindById = rs.getInt("remindById");
            EnumOptionData remindBy = null;
            if(remindById != null) remindBy = CalendarEnumerations.calendarRemindBy(remindById);
            final Integer firstReminder = rs.getInt("firstReminder");
            final Integer secondReminder = rs.getInt("secondReminder");

            return new CalendarData(id, entityId, entityType, title, description, location, startDate, endDate, createdDate, duration,
                    type, repeating, recurrence, remindBy, firstReminder, secondReminder);
        }
    }

    @Override
    public CalendarData retrieveCalendar(final Long calendarId, Long entityId, Integer entityTypeId) {

        try {
            final CalendarDataMapper rm = new CalendarDataMapper();

            final String sql = rm.schema() + "where c.id = ? and c.entity_id = ? and c.entity_type_enum = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { calendarId, entityId, entityTypeId });
        } catch (final EmptyResultDataAccessException e) {
            throw new CalendarNotFoundException(calendarId);
        }
    }

    @Override
    public Collection<CalendarData> retrieveCalendarsByEntity(final Long entityId, final Integer entityTypeId) {
        final CalendarDataMapper rm = new CalendarDataMapper();

        final String sql = rm.schema() + "where c.entity_id = ? and c.entity_type_enum = ?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { entityId, entityTypeId });
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

}
