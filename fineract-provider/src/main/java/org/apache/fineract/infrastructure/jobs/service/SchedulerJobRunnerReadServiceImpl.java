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
package org.apache.fineract.infrastructure.jobs.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.jobs.data.JobDetailData;
import org.apache.fineract.infrastructure.jobs.data.JobDetailHistoryData;
import org.apache.fineract.infrastructure.jobs.exception.JobNotFoundException;
import org.apache.fineract.infrastructure.jobs.exception.OperationNotAllowedException;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SchedulerJobRunnerReadServiceImpl implements SchedulerJobRunnerReadService {

    private final JdbcTemplate jdbcTemplate;
    private final ColumnValidator columnValidator;

    private final PaginationHelper<JobDetailHistoryData> paginationHelper = new PaginationHelper<>();

    @Autowired
    public SchedulerJobRunnerReadServiceImpl(final RoutingDataSource dataSource,
    		final ColumnValidator columnValidator) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.columnValidator = columnValidator;
    }

    @Override
    public List<JobDetailData> findAllJobDeatils() {
        final JobDetailMapper detailMapper = new JobDetailMapper();
        final String sql = detailMapper.schema();
        final List<JobDetailData> JobDeatils = this.jdbcTemplate.query(sql, detailMapper, new Object[] {});
        return JobDeatils;

    }

    @Override
    public JobDetailData retrieveOne(final Long jobId) {
        try {
            final JobDetailMapper detailMapper = new JobDetailMapper();
            final String sql = detailMapper.schema() + " where job.id=?";
            return this.jdbcTemplate.queryForObject(sql, detailMapper, new Object[] { jobId });
        } catch (final EmptyResultDataAccessException e) {
            throw new JobNotFoundException(String.valueOf(jobId));
        }
    }

    @Override
    public Page<JobDetailHistoryData> retrieveJobHistory(final Long jobId, final SearchParameters searchParameters) {
        if (!isJobExist(jobId)) { throw new JobNotFoundException(String.valueOf(jobId)); }
        final JobHistoryMapper jobHistoryMapper = new JobHistoryMapper();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(jobHistoryMapper.schema());
        sqlBuilder.append(" where job.id=?");
        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
            this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
            }
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), new Object[] { jobId },
                jobHistoryMapper);
    }

    @Override
    public boolean isUpdatesAllowed() {
        final String sql = "select job.display_name from job job where job.currently_running=true and job.updates_allowed=false";
        final List<String> names = this.jdbcTemplate.queryForList(sql, String.class);
        if (names != null && names.size() > 0) {
            final String listVals = names.toString();
            final String jobNames = listVals.substring(listVals.indexOf("[") + 1, listVals.indexOf("]"));
            throw new OperationNotAllowedException(jobNames);
        }
        return true;
    }

    private boolean isJobExist(final Long jobId) {
        boolean isJobPresent = false;
        final String sql = "select count(*) from job job where job.id=" + jobId;
        @SuppressWarnings("deprecation")
        final int count = this.jdbcTemplate.queryForInt(sql);
        if (count == 1) {
            isJobPresent = true;
        }
        return isJobPresent;
    }

    private static final class JobDetailMapper implements RowMapper<JobDetailData> {

        private final StringBuilder sqlBuilder = new StringBuilder("select")
                .append(" job.id,job.display_name as displayName,job.next_run_time as nextRunTime,job.initializing_errorlog as initializingError,job.cron_expression as cronExpression,job.is_active as active,job.currently_running as currentlyRunning,")
                .append(" runHistory.version,runHistory.start_time as lastRunStartTime,runHistory.end_time as lastRunEndTime,runHistory.`status`,runHistory.error_message as jobRunErrorMessage,runHistory.trigger_type as triggerType,runHistory.error_log as jobRunErrorLog ")
                .append(" from job job  left join job_run_history runHistory ON job.id=runHistory.job_id and job.previous_run_start_time=runHistory.start_time ");

        public String schema() {
            return this.sqlBuilder.toString();
        }

        @Override
        public JobDetailData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String displayName = rs.getString("displayName");
            final Date nextRunTime = rs.getTimestamp("nextRunTime");
            final String initializingError = rs.getString("initializingError");
            final String cronExpression = rs.getString("cronExpression");
            final boolean active = rs.getBoolean("active");
            final boolean currentlyRunning = rs.getBoolean("currentlyRunning");

            final Long version = rs.getLong("version");
            final Date jobRunStartTime = rs.getTimestamp("lastRunStartTime");
            final Date jobRunEndTime = rs.getTimestamp("lastRunEndTime");
            final String status = rs.getString("status");
            final String jobRunErrorMessage = rs.getString("jobRunErrorMessage");
            final String triggerType = rs.getString("triggerType");
            final String jobRunErrorLog = rs.getString("jobRunErrorLog");

            JobDetailHistoryData lastRunHistory = null;
            if (version > 0) {
                lastRunHistory = new JobDetailHistoryData(version, jobRunStartTime, jobRunEndTime, status, jobRunErrorMessage, triggerType,
                        jobRunErrorLog);
            }
            final JobDetailData jobDetail = new JobDetailData(id, displayName, nextRunTime, initializingError, cronExpression, active,
                    currentlyRunning, lastRunHistory);
            return jobDetail;
        }

    }

    private static final class JobHistoryMapper implements RowMapper<JobDetailHistoryData> {

        private final StringBuilder sqlBuilder = new StringBuilder(200)
                .append(" runHistory.version,runHistory.start_time as runStartTime,runHistory.end_time as runEndTime,runHistory.`status`,runHistory.error_message as jobRunErrorMessage,runHistory.trigger_type as triggerType,runHistory.error_log as jobRunErrorLog ")
                .append(" from job job join job_run_history runHistory ON job.id=runHistory.job_id");

        public String schema() {
            return this.sqlBuilder.toString();
        }

        @Override
        public JobDetailHistoryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long version = rs.getLong("version");
            final Date jobRunStartTime = rs.getTimestamp("runStartTime");
            final Date jobRunEndTime = rs.getTimestamp("runEndTime");
            final String status = rs.getString("status");
            final String jobRunErrorMessage = rs.getString("jobRunErrorMessage");
            final String triggerType = rs.getString("triggerType");
            final String jobRunErrorLog = rs.getString("jobRunErrorLog");
            final JobDetailHistoryData jobDetailHistory = new JobDetailHistoryData(version, jobRunStartTime, jobRunEndTime, status,
                    jobRunErrorMessage, triggerType, jobRunErrorLog);
            return jobDetailHistory;
        }

    }

}
