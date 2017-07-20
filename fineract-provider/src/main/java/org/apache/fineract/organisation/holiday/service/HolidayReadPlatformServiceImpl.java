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
package org.apache.fineract.organisation.holiday.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.data.HolidayData;
import org.apache.fineract.organisation.holiday.domain.RescheduleType;
import org.apache.fineract.organisation.holiday.exception.HolidayNotFoundException;
import org.joda.time.LocalDate;
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
            sqlBuilder.append("h.repayments_rescheduled_to as repaymentsScheduleTO, h.rescheduling_type as reschedulingType, h.status_enum as statusEnum ");
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
            final Integer reschedulingType = JdbcSupport.getInteger(rs, "reschedulingType");
            final EnumOptionData status = HolidayEnumerations.holidayStatusType(statusEnum);

            return new HolidayData(id, name, description, fromDate, toDate, repaymentsScheduleTO, status, reschedulingType);
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
    
    @Override
    public List<EnumOptionData> retrieveRepaymentScheduleUpdationTyeOptions(){
        
        final List<EnumOptionData> repSchUpdationTypeOptions = Arrays.asList(
                HolidayEnumerations.rescheduleType(RescheduleType.RESCHEDULETOSPECIFICDATE),
                HolidayEnumerations.rescheduleType(RescheduleType.RESCHEDULETONEXTREPAYMENTDATE));
        return repSchUpdationTypeOptions;
    }

}
