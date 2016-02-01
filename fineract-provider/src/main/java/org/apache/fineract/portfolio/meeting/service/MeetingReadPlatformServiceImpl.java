/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.meeting.data.MeetingData;
import org.mifosplatform.portfolio.meeting.exception.MeetingNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class MeetingReadPlatformServiceImpl implements MeetingReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MeetingReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class MeetingDataMapper implements RowMapper<MeetingData> {

        public String schema() {

            return " select m.id as id, m.meeting_date as meetingDate from m_meeting m "
                    + "inner join m_calendar_instance ci on m.calendar_instance_id = ci.id ";
        }

        @Override
        public MeetingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final LocalDate meetingDate = JdbcSupport.getLocalDate(rs, "meetingDate");

            return MeetingData.instance(id, meetingDate);
        }
    }

    @Override
    public MeetingData retrieveMeeting(final Long meetingId, final Long entityId, final Integer entityTypeId) {

        try {
            final MeetingDataMapper rm = new MeetingDataMapper();

            final String sql = rm.schema() + " where m.id = ? and ci.entity_id = ? and ci.entity_type_enum = ? ";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { meetingId, entityId, entityTypeId });
        } catch (final EmptyResultDataAccessException e) {
            throw new MeetingNotFoundException(meetingId);
        }
    }

    @Override
    public Collection<MeetingData> retrieveMeetingsByEntity(final Long entityId, final Integer entityTypeId, final Integer limit) {
        final MeetingDataMapper rm = new MeetingDataMapper();
        String sql = rm.schema() + " where ci.entity_id = ? and ci.entity_type_enum = ? ";
        if (limit != null && limit > 0) {
            sql = sql + " order by m.meeting_date desc " + " limit " + limit;
        }

        return this.jdbcTemplate.query(sql, rm, new Object[] { entityId, entityTypeId });
    }

    @Override
    public Collection<MeetingData> retrieveMeetingsByEntityByCalendarType(final Long entityId, final Integer entityTypeId,
            final List<Integer> calendarTypeOptions) {
        final MeetingDataMapper rm = new MeetingDataMapper();
        final String sqlCalendarTypeOptions = CalendarUtils.getSqlCalendarTypeOptionsInString(calendarTypeOptions);
        final String sql = rm.schema()
                + " inner join m_calendar c on ci.calendar_id=c.id  where ci.entity_id = ? and ci.entity_type_enum = ? and c.calendar_type_enum in ("
                + sqlCalendarTypeOptions + ") order by c.start_date ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { entityId, entityTypeId });
    }

    @Override
    public MeetingData retrieveLastMeeting(Long calendarInstanceId) {
        try {
            final MeetingDataMapper rm = new MeetingDataMapper();

            final String sql = rm.schema() + " where ci.id = ? order by m.meeting_date desc, m.id desc limit 1";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { calendarInstanceId });
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

}