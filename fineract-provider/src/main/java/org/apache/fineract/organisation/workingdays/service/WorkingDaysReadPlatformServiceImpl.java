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
package org.apache.fineract.organisation.workingdays.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysData;
import org.apache.fineract.organisation.workingdays.domain.RepaymentRescheduleType;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysEnumerations;
import org.apache.fineract.organisation.workingdays.exception.WorkingDaysNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class WorkingDaysReadPlatformServiceImpl implements WorkingDaysReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public WorkingDaysReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class WorkingDaysMapper implements RowMapper<WorkingDaysData> {

        private final String schema;

        public WorkingDaysMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(100);
            sqlBuilder.append("w.id as id,w.recurrence as recurrence,w.repayment_rescheduling_enum as status_enum,");
            sqlBuilder.append("w.extend_term_daily_repayments as extendTermForDailyRepayments,");
            sqlBuilder.append("w.extend_term_holiday_repayment as extendTermForRepaymentsOnHolidays ");
            sqlBuilder.append("from m_working_days w");

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public WorkingDaysData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String recurrence = rs.getString("recurrence");
            final Integer statusEnum = JdbcSupport.getInteger(rs, "status_enum");
            final EnumOptionData status = WorkingDaysEnumerations.workingDaysStatusType(statusEnum);
            final Boolean extendTermForDailyRepayments = rs.getBoolean("extendTermForDailyRepayments");
            final Boolean extendTermForRepaymentsOnHolidays = rs.getBoolean("extendTermForRepaymentsOnHolidays");

            return new WorkingDaysData(id, recurrence, status, extendTermForDailyRepayments, extendTermForRepaymentsOnHolidays);
        }
    }

    @Override
    public WorkingDaysData retrieve() {
    	//Check whether template is enabled or not?
        try {
            final WorkingDaysMapper rm = new WorkingDaysMapper();
            final String sql = " select " + rm.schema();
            WorkingDaysData data = this.jdbcTemplate.queryForObject(sql, rm, new Object[] {});
            Collection<EnumOptionData> repaymentRescheduleOptions = repaymentRescheduleTypeOptions() ;
            return new WorkingDaysData(data, repaymentRescheduleOptions) ;
        } catch (final EmptyResultDataAccessException e) {
            throw new WorkingDaysNotFoundException();
        }
    }

    @Override
    public WorkingDaysData repaymentRescheduleType() {
        Collection<EnumOptionData> repaymentRescheduleOptions = repaymentRescheduleTypeOptions();
        return new WorkingDaysData(null, null, null, repaymentRescheduleOptions, null, null);
    }
    
    private Collection<EnumOptionData> repaymentRescheduleTypeOptions() {
    	 return Arrays.asList(
                 WorkingDaysEnumerations.repaymentRescheduleType(RepaymentRescheduleType.SAME_DAY),
                 WorkingDaysEnumerations.repaymentRescheduleType(RepaymentRescheduleType.MOVE_TO_NEXT_WORKING_DAY),
                 WorkingDaysEnumerations.repaymentRescheduleType(RepaymentRescheduleType.MOVE_TO_NEXT_REPAYMENT_MEETING_DAY),
                 WorkingDaysEnumerations.repaymentRescheduleType(RepaymentRescheduleType.MOVE_TO_PREVIOUS_WORKING_DAY));
    }
}
