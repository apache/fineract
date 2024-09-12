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

import jakarta.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.IdTypeResolver;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.data.JobDetailData;
import org.apache.fineract.infrastructure.jobs.data.JobDetailHistoryData;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetailRepository;
import org.apache.fineract.infrastructure.jobs.exception.JobNotFoundException;
import org.apache.fineract.infrastructure.jobs.exception.OperationNotAllowedException;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class SchedulerJobRunnerReadServiceImpl implements SchedulerJobRunnerReadService {

    private final JdbcTemplate jdbcTemplate;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final ScheduledJobDetailRepository jobDetailRepository;

    private final PaginationHelper paginationHelper;

    @Autowired
    public SchedulerJobRunnerReadServiceImpl(final JdbcTemplate jdbcTemplate, final ColumnValidator columnValidator,
            DatabaseSpecificSQLGenerator sqlGenerator, ScheduledJobDetailRepository jobDetailRepository,
            PaginationHelper paginationHelper) {
        this.jdbcTemplate = jdbcTemplate;
        this.columnValidator = columnValidator;
        this.sqlGenerator = sqlGenerator;
        this.jobDetailRepository = jobDetailRepository;
        this.paginationHelper = paginationHelper;
    }

    @Override
    public List<JobDetailData> findAllJobDetails() {
        return jobDetailRepository.getAllData();

    }

    @Override
    public JobDetailData retrieveOne(@NotNull IdTypeResolver.IdType idType, String identifier) {
        JobDetailData jobDetail = switch (idType) {
            case ID -> jobDetailRepository.getDataById(Long.valueOf(identifier));
            case SHORT_NAME -> jobDetailRepository.getDataByShortName(identifier);
            default -> null;
        };
        if (jobDetail == null) {
            throw new JobNotFoundException(idType, identifier);
        }
        return jobDetail;
    }

    @Override
    public Page<JobDetailHistoryData> retrieveJobHistory(@NotNull IdTypeResolver.IdType idType, String identifier,
            SearchParameters searchParameters) {
        if (!isJobExist(idType, identifier)) {
            throw new JobNotFoundException(idType, identifier);
        }
        final JobHistoryMapper jobHistoryMapper = new JobHistoryMapper(sqlGenerator);
        final StringBuilder sqlBuilder = new StringBuilder("select " + sqlGenerator.calcFoundRows() + " ").append(jobHistoryMapper.schema())
                .append(" where job.");
        Object idParam;
        switch (idType) {
            case ID -> {
                sqlBuilder.append("id");
                idParam = Long.valueOf(identifier);
            }
            case SHORT_NAME -> {
                sqlBuilder.append("short_name");
                idParam = identifier;
            }
            default -> throw new JobNotFoundException(idType, identifier);
        }
        sqlBuilder.append(" = ?");
        if (searchParameters.hasOrderBy()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
            this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
            if (searchParameters.hasSortOrder()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
            }
        }

        if (searchParameters.hasLimit()) {
            sqlBuilder.append(" ");
            if (searchParameters.hasOffset()) {
                sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
            } else {
                sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
            }
        }

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), new Object[] { idParam }, jobHistoryMapper);
    }

    @Override
    @NotNull
    public Long retrieveId(@NotNull IdTypeResolver.IdType idType, String identifier) {
        return switch (idType) {
            case ID -> Long.valueOf(identifier);
            case SHORT_NAME ->
                jobDetailRepository.findIdByShortName(identifier).orElseThrow(() -> new JobNotFoundException(idType, identifier));
            default -> throw new JobNotFoundException(idType, identifier);
        };
    }

    @Override
    public boolean isUpdatesAllowed() {
        final String sql = "select job.display_name from job job where job.currently_running=true and job.updates_allowed=false";
        final List<String> names = this.jdbcTemplate.queryForList(sql, String.class);
        if (names.size() > 0) {
            final String listVals = names.toString();
            final String jobNames = listVals.substring(listVals.indexOf("[") + 1, listVals.indexOf("]"));
            throw new OperationNotAllowedException(jobNames);
        }
        return true;
    }

    private boolean isJobExist(@NotNull IdTypeResolver.IdType idType, @NotNull String jobId) {
        return switch (idType) {
            case ID -> jobDetailRepository.existsById(Long.valueOf(jobId));
            case SHORT_NAME -> jobDetailRepository.existsByShortName(jobId);
            default -> false;
        };
    }

    private static final class JobHistoryMapper implements RowMapper<JobDetailHistoryData> {

        private final String sql;

        JobHistoryMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            sql = " runHistory.version, runHistory.start_time as runStartTime, runHistory.end_time as runEndTime, " + "runHistory."
                    + sqlGenerator.escape("status")
                    + ", runHistory.error_message as jobRunErrorMessage, runHistory.trigger_type as triggerType, runHistory.error_log as jobRunErrorLog "
                    + "from job job join job_run_history runHistory ON job.id=runHistory.job_id";
        }

        public String schema() {
            return sql;
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
            return new JobDetailHistoryData().setVersion(version).setJobRunStartTime(jobRunStartTime).setJobRunEndTime(jobRunEndTime)
                    .setStatus(status).setJobRunErrorMessage(jobRunErrorMessage).setTriggerType(triggerType)
                    .setJobRunErrorLog(jobRunErrorLog);
        }
    }
}
