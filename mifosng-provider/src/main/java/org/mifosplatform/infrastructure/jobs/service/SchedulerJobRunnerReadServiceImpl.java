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
    public Page<JobDetailHistoryData> retrieveJobHistory(Long jobId) {
        retrieveOne(jobId);
        JobHistoryMapper jobHistoryMapper = new JobHistoryMapper();
        String sql = jobHistoryMapper.schema() + " where job.id=?";
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return paginationHelper.fetchPage(jdbcTemplate, sqlCountRows, sql, new Object[] { jobId }, jobHistoryMapper);
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
            Date nextRunTime = rs.getDate("nextRunTime");
            String initializingError = rs.getString("initializingError");
            boolean active = rs.getBoolean("active");
            boolean currentlyRunning = rs.getBoolean("currentlyRunning");

            Long version = rs.getLong("version");
            Date jobRunStartTime = rs.getDate("lastRunStartTime");
            Date jobRunEndTime = rs.getDate("lastRunEndTime");
            String status = rs.getString("status");
            String jobRunErrorMessage = rs.getString("jobRunErrorMessage");
            String triggerType = rs.getString("triggerType");
            String jobRunErrorLog = rs.getString("jobRunErrorLog");

            JobDetailHistoryData lastRunHistory = new JobDetailHistoryData(version, jobRunStartTime, jobRunEndTime, status,
                    jobRunErrorMessage, triggerType, jobRunErrorLog);
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
            Date jobRunStartTime = rs.getDate("runStartTime");
            Date jobRunEndTime = rs.getDate("runEndTime");
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
