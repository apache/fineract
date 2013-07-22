package org.mifosplatform.infrastructure.jobs.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.jobs.data.JobDetailData;
import org.mifosplatform.infrastructure.jobs.data.JobDetailHistoryData;
import org.mifosplatform.infrastructure.jobs.exception.JobNotFoundException;
import org.mifosplatform.infrastructure.jobs.exception.OperationNotAllowedException;
import org.mifosplatform.portfolio.group.service.SearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SchedulerJobRunnerReadServiceImpl implements SchedulerJobRunnerReadService {

    private final JdbcTemplate jdbcTemplate;

    private final PaginationHelper<JobDetailHistoryData> paginationHelper = new PaginationHelper<JobDetailHistoryData>();

    @Autowired
    public SchedulerJobRunnerReadServiceImpl(final RoutingDataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<JobDetailData> findAllJobDeatils() {
        JobDetailMapper detailMapper = new JobDetailMapper();
        String sql = detailMapper.schema();
        List<JobDetailData> JobDeatils = jdbcTemplate.query(sql, detailMapper, new Object[] {});
        return JobDeatils;

    }

    @Override
    public JobDetailData retrieveOne(Long jobId) {
        try {
            JobDetailMapper detailMapper = new JobDetailMapper();
            String sql = detailMapper.schema() + " where job.id=?";
            return jdbcTemplate.queryForObject(sql, detailMapper, new Object[] { jobId });
        } catch (EmptyResultDataAccessException e) {
            throw new JobNotFoundException(String.valueOf(jobId));
        }
    }

    @Override
    public Page<JobDetailHistoryData> retrieveJobHistory(Long jobId, SearchParameters searchParameters) {
        if (!isJobExist(jobId)) { throw new JobNotFoundException(String.valueOf(jobId)); }
        JobHistoryMapper jobHistoryMapper = new JobHistoryMapper();
        StringBuilder sqlBuilder = new StringBuilder(jobHistoryMapper.schema());
        sqlBuilder.append(" where job.id=?");
        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());

            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
            }
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return paginationHelper.fetchPage(jdbcTemplate, sqlCountRows, sqlBuilder.toString(), new Object[] { jobId }, jobHistoryMapper);
    }

    @Override
    public boolean isUpdatesAllowed() {
        String sql = "select job.display_name from job job where job.currently_running=true and job.updates_allowed=false";
        List<String> names = jdbcTemplate.queryForList(sql, String.class);
        if (names != null && names.size() > 0) {
            String listVals = names.toString();
            String jobNames = listVals.substring(listVals.indexOf("[") + 1, listVals.indexOf("]"));
            throw new OperationNotAllowedException(jobNames);
        }
        return true;
    }

    private boolean isJobExist(Long jobId) {
        boolean isJobPresent = false;
        String sql = "select count(*) from job job where job.id=" + jobId;
        int count = jdbcTemplate.queryForInt(sql);
        if (count == 1) {
            isJobPresent = true;
        }
        return isJobPresent;
    }

    private static final class JobDetailMapper implements RowMapper<JobDetailData> {

        private StringBuilder sqlBuilder = new StringBuilder("select")
                .append(" job.id,job.display_name as displayName,job.next_run_time as nextRunTime,job.initializing_errorlog as initializingError,job.is_active as active,job.currently_running as currentlyRunning,")
                .append(" runHistory.version,runHistory.start_time as lastRunStartTime,runHistory.end_time as lastRunEndTime,runHistory.`status`,runHistory.error_message as jobRunErrorMessage,runHistory.trigger_type as triggerType,runHistory.error_log as jobRunErrorLog ")
                .append(" from job job  left join job_run_history runHistory ON job.id=runHistory.job_id and job.previous_run_start_time=runHistory.start_time ");

        public String schema() {
            return sqlBuilder.toString();
        }

        @Override
        public JobDetailData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String displayName = rs.getString("displayName");
            Date nextRunTime = rs.getTimestamp("nextRunTime");
            String initializingError = rs.getString("initializingError");
            boolean active = rs.getBoolean("active");
            boolean currentlyRunning = rs.getBoolean("currentlyRunning");

            Long version = rs.getLong("version");
            Date jobRunStartTime = rs.getTimestamp("lastRunStartTime");
            Date jobRunEndTime = rs.getTimestamp("lastRunEndTime");
            String status = rs.getString("status");
            String jobRunErrorMessage = rs.getString("jobRunErrorMessage");
            String triggerType = rs.getString("triggerType");
            String jobRunErrorLog = rs.getString("jobRunErrorLog");

            JobDetailHistoryData lastRunHistory = null;
            if (version > 0) {
                lastRunHistory = new JobDetailHistoryData(version, jobRunStartTime, jobRunEndTime, status, jobRunErrorMessage, triggerType,
                        jobRunErrorLog);
            }
            JobDetailData jobDetail = new JobDetailData(id, displayName, nextRunTime, initializingError, active, currentlyRunning,
                    lastRunHistory);
            return jobDetail;
        }

    }

    private static final class JobHistoryMapper implements RowMapper<JobDetailHistoryData> {

        private StringBuilder sqlBuilder = new StringBuilder("select")
                .append(" runHistory.version,runHistory.start_time as runStartTime,runHistory.end_time as runEndTime,runHistory.`status`,runHistory.error_message as jobRunErrorMessage,runHistory.trigger_type as triggerType,runHistory.error_log as jobRunErrorLog ")
                .append(" from job job join job_run_history runHistory ON job.id=runHistory.job_id");

        public String schema() {
            return sqlBuilder.toString();
        }

        @Override
        public JobDetailHistoryData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            Long version = rs.getLong("version");
            Date jobRunStartTime = rs.getTimestamp("runStartTime");
            Date jobRunEndTime = rs.getTimestamp("runEndTime");
            String status = rs.getString("status");
            String jobRunErrorMessage = rs.getString("jobRunErrorMessage");
            String triggerType = rs.getString("triggerType");
            String jobRunErrorLog = rs.getString("jobRunErrorLog");
            JobDetailHistoryData jobDetailHistory = new JobDetailHistoryData(version, jobRunStartTime, jobRunEndTime, status,
                    jobRunErrorMessage, triggerType, jobRunErrorLog);
            return jobDetailHistory;
        }

    }

}
