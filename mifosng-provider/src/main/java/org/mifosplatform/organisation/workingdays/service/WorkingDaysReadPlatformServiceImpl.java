/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.workingdays.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.organisation.workingdays.data.WorkingDaysData;
import org.mifosplatform.organisation.workingdays.domain.RepaymentRescheduleType;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysEnumerations;
import org.mifosplatform.organisation.workingdays.exception.WorkingDaysNotFoundException;
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
            sqlBuilder.append("w.id as id,w.recurrence as recurrence,w.repayment_rescheduling_enum as status_enum  ");
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

            return new WorkingDaysData(id, recurrence, status);
        }
    }

    @Override
    public WorkingDaysData retrieve() {
        try {
            final WorkingDaysMapper rm = new WorkingDaysMapper();
            final String sql = " select " + rm.schema();
            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] {});
        } catch (final EmptyResultDataAccessException e) {
            throw new WorkingDaysNotFoundException();
        }
    }

    @Override
    public WorkingDaysData repaymentRescheduleType() {
        Collection<EnumOptionData> repaymentRescheduleOptions = Arrays.asList(
                WorkingDaysEnumerations.repaymentRescheduleType(RepaymentRescheduleType.SAME_DAY),
                WorkingDaysEnumerations.repaymentRescheduleType(RepaymentRescheduleType.MOVE_TO_NEXT_WORKING_DAY),
                WorkingDaysEnumerations.repaymentRescheduleType(RepaymentRescheduleType.MOVE_TO_NEXT_REPAYMENT_MEETING_DAY),
                WorkingDaysEnumerations.repaymentRescheduleType(RepaymentRescheduleType.MOVE_TO_PREVIOUS_WORKING_DAY));
        return new WorkingDaysData(null, null, null, repaymentRescheduleOptions);
    }
}
