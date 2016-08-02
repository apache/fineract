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
package org.apache.fineract.infrastructure.reportmailingjob.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobData;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobEmailAttachmentFileFormat;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobStretchyReportParamDateOption;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobTimelineData;
import org.apache.fineract.infrastructure.reportmailingjob.exception.ReportMailingJobNotFoundException;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ReportMailingJobReadPlatformServiceImpl implements ReportMailingJobReadPlatformService {
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public ReportMailingJobReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<ReportMailingJobData> retrieveAllReportMailingJobs() {
        final ReportMailingJobMapper mapper = new ReportMailingJobMapper();
        final String sql = "select " + mapper.ReportMailingJobSchema() + " where rmj.is_deleted = 0 order by rmj.name";
        
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }
    
    @Override
    public Collection<ReportMailingJobData> retrieveAllActiveReportMailingJobs() {
        final ReportMailingJobMapper mapper = new ReportMailingJobMapper();
        final String sql = "select " + mapper.ReportMailingJobSchema() + " where rmj.is_deleted = 0 and is_active = 1"
                + " order by rmj.name";
        
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    @Override
    public ReportMailingJobData retrieveReportMailingJob(final Long reportMailingJobId) {
        try {
            final ReportMailingJobMapper mapper = new ReportMailingJobMapper();
            final String sql = "select " + mapper.ReportMailingJobSchema() + " where rmj.id = ? and rmj.is_deleted = 0";
            
            return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { reportMailingJobId });
        }
        
        catch (final EmptyResultDataAccessException ex) {
            throw new ReportMailingJobNotFoundException(reportMailingJobId);
        }
    }

    @Override
    public ReportMailingJobData retrieveReportMailingJobEnumOptions() {
        final List<EnumOptionData> emailAttachmentFileFormatOptions = ReportMailingJobEmailAttachmentFileFormat.validOptions();
        final List<EnumOptionData> stretchyReportParamDateOptions = ReportMailingJobStretchyReportParamDateOption.validOptions();
        
        return ReportMailingJobData.newInstance(emailAttachmentFileFormatOptions, stretchyReportParamDateOptions);
    }
    
    private static final class ReportMailingJobMapper implements RowMapper<ReportMailingJobData> {
        public String ReportMailingJobSchema() {
            return "rmj.id, rmj.name, rmj.description, rmj.start_datetime as startDateTime, rmj.recurrence, rmj.created_on_date as createdOnDate, "
                    + "cbu.username as createdByUsername, cbu.firstname as createdByFirstname, cbu.lastname as createdByLastname, "
                    + "rmj.email_recipients as emailRecipients, "
                    + "rmj.email_subject as emailSubject, rmj.email_message as emailMessage, "
                    + "rmj.email_attachment_file_format as emailAttachmentFileFormat, "
                    + "rmj.stretchy_report_param_map as stretchyReportParamMap, rmj.previous_run_datetime as previousRunDateTime, "
                    + "rmj.next_run_datetime as nextRunDateTime, rmj.previous_run_status as previousRunStatus, "
                    + "rmj.previous_run_error_log as previousRunErrorLog, rmj.previous_run_error_message as previousRunErrorMessage, "
                    + "rmj.number_of_runs as numberOfRuns, rmj.is_active as isActive, rmj.run_as_userid as runAsUserId, "
                    + "sr.id as reportId, sr.report_name as reportName, sr.report_type as reportType, sr.report_subtype as reportSubType, "
                    + "sr.report_category as reportCategory, sr.report_sql as reportSql, sr.description as reportDescription, "
                    + "sr.core_report as coreReport, sr.use_report as useReport "
                    + "from m_report_mailing_job rmj "
                    + "inner join m_appuser cbu "
                    + "on cbu.id = rmj.created_by_userid "
                    + "left join stretchy_report sr "
                    + "on rmj.stretchy_report_id = sr.id";
        }

        @Override
        public ReportMailingJobData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final DateTime startDateTime = JdbcSupport.getDateTime(rs, "startDateTime");
            final String recurrence = rs.getString("recurrence");
            final LocalDate createdOnDate = JdbcSupport.getLocalDate(rs, "createdOnDate");
            final String emailRecipients = rs.getString("emailRecipients");
            final String emailSubject = rs.getString("emailSubject");
            final String emailMessage = rs.getString("emailMessage");
            final String emailAttachmentFileFormatString = rs.getString("emailAttachmentFileFormat");
            EnumOptionData emailAttachmentFileFormat = null;
            
            if (emailAttachmentFileFormatString != null) {
                ReportMailingJobEmailAttachmentFileFormat format = ReportMailingJobEmailAttachmentFileFormat.newInstance(emailAttachmentFileFormatString);
                
                emailAttachmentFileFormat = format.toEnumOptionData();
            }
            
            final String stretchyReportParamMap = rs.getString("stretchyReportParamMap");
            final DateTime previousRunDateTime = JdbcSupport.getDateTime(rs, "previousRunDateTime");
            final DateTime nextRunDateTime = JdbcSupport.getDateTime(rs, "nextRunDateTime");
            final String previousRunStatus = rs.getString("previousRunStatus");
            final String previousRunErrorLog = rs.getString("previousRunErrorLog");
            final String previousRunErrorMessage = rs.getString("previousRunErrorMessage");
            final Integer numberOfRuns = JdbcSupport.getInteger(rs, "numberOfRuns");
            final boolean isActive = rs.getBoolean("isActive");
            final String createdByUsername = rs.getString("createdByUsername");
            final String createdByFirstname = rs.getString("createdByFirstname");
            final String createdByLastname = rs.getString("createdByLastname");
            final ReportMailingJobTimelineData timeline = new ReportMailingJobTimelineData(createdOnDate, createdByUsername, 
                    createdByFirstname, createdByLastname);
            final Long runAsUserId = JdbcSupport.getLong(rs, "runAsUserId");
            
            final Long reportId = JdbcSupport.getLong(rs, "reportId");
            final String reportName = rs.getString("reportName");
            final String reportType = rs.getString("reportType");
            final String reportSubType = rs.getString("reportSubType");
            final String reportCategory = rs.getString("reportCategory");
            final String reportSql = rs.getString("reportSql");
            final String reportDescription = rs.getString("reportDescription");
            final boolean coreReport = rs.getBoolean("coreReport");
            final boolean useReport = rs.getBoolean("useReport");
            
            final ReportData stretchyReport = new ReportData(reportId, reportName, reportType, reportSubType, reportCategory, 
                    reportDescription, reportSql, coreReport, useReport, null);
            
            return ReportMailingJobData.newInstance(id, name, description, startDateTime, recurrence, timeline, emailRecipients, 
                    emailSubject, emailMessage, emailAttachmentFileFormat, stretchyReport, stretchyReportParamMap, previousRunDateTime, 
                    nextRunDateTime, previousRunStatus, previousRunErrorLog, previousRunErrorMessage, numberOfRuns, isActive, 
                    runAsUserId);
        }
    }
}
