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
package org.apache.fineract.infrastructure.campaigns.email.service;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.campaigns.email.data.ScheduledEmailEnumerations;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailBusinessRuleNotFound;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailCampaignNotFound;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailBusinessRulesData;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignData;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignTimeLine;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignStatus;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignStatusEnumerations;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class EmailCampaignReadPlatformServiceImpl implements EmailCampaignReadPlatformService {


    private final JdbcTemplate jdbcTemplate;

    private final BusinessRuleMapper businessRuleMapper;

    private final EmailCampaignMapper emailCampaignMapper;

    @Autowired
    public EmailCampaignReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.businessRuleMapper = new BusinessRuleMapper();
        this.emailCampaignMapper = new EmailCampaignMapper();
    }


    private static final class EmailCampaignMapper implements RowMapper<EmailCampaignData>{

        final String schema;

        private EmailCampaignMapper() {
            final StringBuilder sql = new StringBuilder(400);
            sql.append("ec.id as id, ");
            sql.append("ec.campaign_name as campaignName, ");
            sql.append("ec.campaign_type as campaignType, ");
            sql.append("ec.businessRule_id as businessRuleId, ");
            sql.append("ec.email_subject as emailSubject, ");
            sql.append("ec.email_message as emailMessage, ");
            sql.append("ec.email_attachment_file_format as emailAttachmentFileFormat, ");
            sql.append("sr.id as stretchyReportId, ");
            sql.append("sr.report_name as reportName, sr.report_type as reportType, sr.report_subtype as reportSubType, ");
            sql.append("sr.report_category as reportCategory, sr.report_sql as reportSql, sr.description as reportDescription, ");
            sql.append("sr.core_report as coreReport, sr.use_report as useReport, ");
            sql.append("ec.stretchy_report_param_map as stretchyReportParamMap, ");
            sql.append("ec.param_value as paramValue, ");
            sql.append("ec.status_enum as statusEnum, ");
            sql.append("ec.recurrence as recurrence, ");
            sql.append("ec.recurrence_start_date as recurrenceStartDate, ");
            sql.append("ec.next_trigger_date as nextTriggerDate, ");
            sql.append("ec.last_trigger_date as lastTriggerDate, ");
            sql.append("ec.submittedon_date as submittedOnDate, ");
            sql.append("sbu.username as submittedByUsername, ");
            sql.append("ec.closedon_date as closedOnDate, ");
            sql.append("clu.username as closedByUsername, ");
            sql.append("acu.username as activatedByUsername, ");
            sql.append("ec.approvedon_date as activatedOnDate ");
            sql.append("from scheduled_email_campaign ec ");
            sql.append("left join m_appuser sbu on sbu.id = ec.submittedon_userid ");
            sql.append("left join m_appuser acu on acu.id = ec.approvedon_userid ");
            sql.append("left join m_appuser clu on clu.id = ec.closedon_userid ");
            sql.append("left join stretchy_report sr on ec.stretchy_report_id = sr.id");

            this.schema = sql.toString();
        }
        public String schema() {
            return this.schema;
        }

        @Override
        public EmailCampaignData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String campaignName = rs.getString("campaignName");
            final Integer campaignType = JdbcSupport.getInteger(rs, "campaignType");
            final Long businessRuleId = JdbcSupport.getLong(rs, "businessRuleId");
            final String paramValue = rs.getString("paramValue");
            final String emailSubject = rs.getString("emailSubject");
            final String emailMessage = rs.getString("emailMessage");
            final String emailAttachmentFileFormatString = rs.getString("emailAttachmentFileFormat");
            final String stretchyReportParamMap = rs.getString("stretchyReportParamMap");
            EnumOptionData emailAttachmentFileFormat = null;
            if (emailAttachmentFileFormatString != null) {
                emailAttachmentFileFormat = ScheduledEmailEnumerations.emailAttachementFileFormat(emailAttachmentFileFormatString);
            }
            final Long reportId = JdbcSupport.getLong(rs, "stretchyReportId");
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

            final Integer statusId = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = EmailCampaignStatusEnumerations.status(statusId);
            final DateTime nextTriggerDate = JdbcSupport.getDateTime(rs, "nextTriggerDate");
            final LocalDate  lastTriggerDate = JdbcSupport.getLocalDate(rs, "lastTriggerDate");


            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");


            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");

            final LocalDate activatedOnDate = JdbcSupport.getLocalDate(rs, "activatedOnDate");
            final String activatedByUsername = rs.getString("activatedByUsername");
            final String recurrence  =rs.getString("recurrence");
            final DateTime recurrenceStartDate = JdbcSupport.getDateTime(rs, "recurrenceStartDate");
            final EmailCampaignTimeLine emailCampaignTimeLine = new EmailCampaignTimeLine(submittedOnDate,submittedByUsername,
                    activatedOnDate,activatedByUsername,closedOnDate,closedByUsername);



            return EmailCampaignData.instance(id,campaignName,campaignType,businessRuleId,paramValue,status,emailSubject,emailMessage,
                    emailAttachmentFileFormatString,reportId,stretchyReportParamMap,nextTriggerDate,lastTriggerDate,emailCampaignTimeLine,
                    recurrenceStartDate,recurrence);
        }
    }


    private static final class BusinessRuleMapper implements ResultSetExtractor<List<EmailBusinessRulesData>>{

        final String schema;

        private BusinessRuleMapper() {
            final StringBuilder sql = new StringBuilder(300);
            sql.append("sr.id as id, ");
            sql.append("sr.report_name as reportName, ");
            sql.append("sr.report_type as reportType, ");
            sql.append("sr.report_subtype as reportSubType, ");
            sql.append("sr.description as description, ");
            sql.append("sp.parameter_variable as params, ");
            sql.append("sp.parameter_FormatType as paramType, ");
            sql.append("sp.parameter_label as paramLabel, ");
            sql.append("sp.parameter_name as paramName ");
            sql.append("from stretchy_report sr ");
            sql.append("left join stretchy_report_parameter as srp on srp.report_id = sr.id ");
            sql.append("left join stretchy_parameter as sp on sp.id = srp.parameter_id ");

            this.schema = sql.toString();
        }

        public String schema(){
            return this.schema;
        }

        @Override
        public List<EmailBusinessRulesData> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<EmailBusinessRulesData> emailBusinessRulesDataList = new ArrayList<EmailBusinessRulesData>();

            EmailBusinessRulesData emailBusinessRulesData = null;

            Map<Long,EmailBusinessRulesData> mapOfSameObjects = new HashMap<Long, EmailBusinessRulesData>();

            while(rs.next()){
                final Long id = rs.getLong("id");
                emailBusinessRulesData  = mapOfSameObjects.get(id);
                if(emailBusinessRulesData == null){
                    final String reportName = rs.getString("reportName") ;
                    final String reportType = rs.getString("reportType");
                    final String reportSubType = rs.getString("reportSubType");
                    final String paramName  = rs.getString("paramName");
                    final String paramLabel = rs.getString("paramLabel");
                    final String description = rs.getString("description");

                    Map<String,Object> hashMap = new HashMap<String, Object>();
                    hashMap.put(paramLabel,paramName);
                    emailBusinessRulesData = EmailBusinessRulesData.instance(id,reportName,reportType,hashMap,reportSubType,description);
                    mapOfSameObjects.put(id,emailBusinessRulesData);
                    //add to the list
                    emailBusinessRulesDataList.add(emailBusinessRulesData);
                }
                //add new paramType to the existing object
                Map<String,Object> hashMap = new HashMap<String, Object>();
                final String paramName  = rs.getString("paramName");
                final String paramLabel = rs.getString("paramLabel");
                hashMap.put(paramLabel,paramName);

                //get existing map and add new items to it
                emailBusinessRulesData.getReportParamName().putAll(hashMap);
            }

            return emailBusinessRulesDataList;
        }
    }

    @Override
    public Collection<EmailBusinessRulesData> retrieveAll() {
        final String searchType = "Email";
        final String sql = "select " + this.businessRuleMapper.schema() + " where sr.report_type = ?";

        return this.jdbcTemplate.query(sql, this.businessRuleMapper, searchType);
    }

    @Override
    public EmailBusinessRulesData retrieveOneTemplate(Long resourceId) {
        final String searchType = "Email";

        final String sql = "select " + this.businessRuleMapper.schema() + " where sr.report_type = ? and sr.id = ?";

        List<EmailBusinessRulesData> retrieveOne =  this.jdbcTemplate.query(sql, this.businessRuleMapper, searchType,resourceId);
        try{
            EmailBusinessRulesData emailBusinessRulesData = retrieveOne.get(0);
            return emailBusinessRulesData;
        }
        catch (final IndexOutOfBoundsException e){
            throw new EmailBusinessRuleNotFound(resourceId);
        }

    }

    @Override
    public EmailCampaignData retrieveOne(Long resourceId) {
        final Integer isVisible =1;
        try{
            final String sql = "select " + this.emailCampaignMapper.schema + " where ec.id = ? and ec.is_visible = ?";
            return this.jdbcTemplate.queryForObject(sql, this.emailCampaignMapper, resourceId,isVisible);
        } catch (final EmptyResultDataAccessException e) {
            throw new EmailCampaignNotFound(resourceId);
        }
    }

    @Override
    public Collection<EmailCampaignData> retrieveAllCampaign() {
        final Integer visible = 1;
        final String sql = "select " + this.emailCampaignMapper.schema() + " where ec.is_visible = ?";
        return this.jdbcTemplate.query(sql, this.emailCampaignMapper, visible);
    }

    @Override
    public Collection<EmailCampaignData> retrieveAllScheduleActiveCampaign() {
        final Integer scheduleCampaignType = EmailCampaignType.SCHEDULE.getValue();
        final Integer statusEnum  = EmailCampaignStatus.ACTIVE.getValue();
        final Integer visible     = 1;
        final String sql = "select " + this.emailCampaignMapper.schema() + " where ec.status_enum = ? and ec.campaign_type = ? and ec.is_visible = ?";
        return this.jdbcTemplate.query(sql,this.emailCampaignMapper, statusEnum,scheduleCampaignType,visible);
    }



}
