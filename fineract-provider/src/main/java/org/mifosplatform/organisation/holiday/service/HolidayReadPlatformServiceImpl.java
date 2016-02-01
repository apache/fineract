/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.holiday.data.HolidayData;
import org.mifosplatform.organisation.holiday.exception.HolidayNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class HolidayReadPlatformServiceImpl implements HolidayReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HolidayReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class HolidayMapper implements RowMapper<HolidayData> {

        private final String schema;

        public HolidayMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("h.id as id, h.name as name, h.description as description, h.from_date as fromDate, h.to_date as toDate, ");
            sqlBuilder.append("h.repayments_rescheduled_to as repaymentsScheduleTO, h.status_enum as statusEnum ");
            sqlBuilder.append("from m_holiday h ");
            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public HolidayData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final LocalDate fromDate = JdbcSupport.getLocalDate(rs, "fromDate");
            final LocalDate toDate = JdbcSupport.getLocalDate(rs, "toDate");
            final LocalDate repaymentsScheduleTO = JdbcSupport.getLocalDate(rs, "repaymentsScheduleTO");
            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = HolidayEnumerations.holidayStatusType(statusEnum);

            return new HolidayData(id, name, description, fromDate, toDate, repaymentsScheduleTO, status);
        }

    }

    @Override
    public Collection<HolidayData> retrieveAllHolidaysBySearchParamerters(final Long officeId, final Date fromDate, final Date toDate) {
        this.context.authenticatedUser();

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        final Object[] objectArray = new Object[3];
        int arrayPos = 0;

        final HolidayMapper rm = new HolidayMapper();
        String sql = "select " + rm.schema() + " join m_holiday_office hf on h.id = hf.holiday_id and hf.office_id = ? ";

        objectArray[arrayPos] = officeId;
        arrayPos = arrayPos + 1;

        if (fromDate != null || toDate != null) {
            sql += "and ";

            if (fromDate != null) {
                sql += "h.from_Date >= ? ";

                objectArray[arrayPos] = df.format(fromDate);
                arrayPos = arrayPos + 1;
            }

            if (toDate != null) {
                sql += fromDate != null ? "and " : "";
                sql += "h.to_date <= ? ";
                objectArray[arrayPos] = df.format(toDate);
                arrayPos = arrayPos + 1;
            }
        }

        final Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);

        return this.jdbcTemplate.query(sql, rm, finalObjectArray);
    }

    @Override
    public HolidayData retrieveHoliday(Long holidayId) {
        try {
            final HolidayMapper rm = new HolidayMapper();

            final String sql = " select " + rm.schema() + " where h.id = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { holidayId });
        } catch (final EmptyResultDataAccessException e) {
            throw new HolidayNotFoundException(holidayId);
        }
    }

}
