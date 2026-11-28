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

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailBusinessRulesData;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignData;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignTimeLine;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignStatus;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignStatusEnumerations;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignType;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailBusinessRuleNotFound;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailCampaignNotFound;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.query.QStretchyParameter;
import org.apache.fineract.query.QStretchyReport;
import org.apache.fineract.query.QStretchyReportParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class EmailCampaignReadPlatformServiceImpl implements EmailCampaignReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final SQLQueryFactory sqlQueryFactory;

    private final EmailCampaignMapper emailCampaignMapper;

    @Autowired
    public EmailCampaignReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate, final SQLQueryFactory sqlQueryFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueryFactory = sqlQueryFactory;
        this.emailCampaignMapper = new EmailCampaignMapper();
    }

    private static final class EmailCampaignMapper implements RowMapper<EmailCampaignData> {

        private static final String EMAIL_CAMPAIGN_SCHEMA = """
                ec.id as id,
                ec.campaign_name as campaignName,
                ec.campaign_type as campaignType,
                ec.business_rule_id as businessRuleId,
                ec.email_subject as emailSubject,
                ec.email_message as emailMessage,
                ec.email_attachment_file_format as emailAttachmentFileFormat,
                sr.id as stretchyReportId,
                sr.report_name as reportName, sr.report_type as reportType, sr.report_subtype as reportSubType,
                sr.report_category as reportCategory, sr.report_sql as reportSql, sr.description as reportDescription,
                sr.core_report as coreReport, sr.use_report as useReport,
                ec.stretchy_report_param_map as stretchyReportParamMap,
                ec.param_value as paramValue,
                ec.status_enum as statusEnum,
                ec.recurrence as recurrence,
                ec.recurrence_start_date as recurrenceStartDate,
                ec.next_trigger_date as nextTriggerDate,
                ec.last_trigger_date as lastTriggerDate,
                ec.submittedon_date as submittedOnDate,
                sbu.username as submittedByUsername,
                ec.closedon_date as closedOnDate,
                clu.username as closedByUsername,
                acu.username as activatedByUsername,
                ec.approvedon_date as activatedOnDate
                from scheduled_email_campaign ec
                left join m_appuser sbu on sbu.id = ec.submittedon_userid
                left join m_appuser acu on acu.id = ec.approvedon_userid
                left join m_appuser clu on clu.id = ec.closedon_userid
                left join stretchy_report sr on ec.stretchy_report_id = sr.id\s""";

        private EmailCampaignMapper() {}

        private String schema() {
            return EMAIL_CAMPAIGN_SCHEMA;
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
            final Long reportId = JdbcSupport.getLong(rs, "stretchyReportId");

            final Integer statusId = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = EmailCampaignStatusEnumerations.status(statusId);
            final ZonedDateTime nextTriggerDate = JdbcSupport.getDateTime(rs, "nextTriggerDate");
            final LocalDate lastTriggerDate = JdbcSupport.getLocalDate(rs, "lastTriggerDate");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");

            final LocalDate activatedOnDate = JdbcSupport.getLocalDate(rs, "activatedOnDate");
            final String activatedByUsername = rs.getString("activatedByUsername");
            final String recurrence = rs.getString("recurrence");
            final ZonedDateTime recurrenceStartDate = JdbcSupport.getDateTime(rs, "recurrenceStartDate");
            final EmailCampaignTimeLine emailCampaignTimeLine = new EmailCampaignTimeLine().setSubmittedOnDate(submittedOnDate)
                    .setActivatedByUsername(submittedByUsername).setActivatedOnDate(activatedOnDate)
                    .setActivatedByUsername(activatedByUsername).setClosedOnDate(closedOnDate).setClosedByUsername(closedByUsername);

            return EmailCampaignData.instance(id, campaignName, campaignType, businessRuleId, paramValue, status, emailSubject,
                    emailMessage, emailAttachmentFileFormatString, reportId, stretchyReportParamMap, nextTriggerDate, lastTriggerDate,
                    emailCampaignTimeLine, recurrenceStartDate, recurrence);
        }
    }

    @Override
    public Collection<EmailBusinessRulesData> retrieveAll() {
        return retrieveBusinessRules(null);
    }

    @Override
    public EmailBusinessRulesData retrieveOneTemplate(final Long resourceId) {
        if (resourceId == null) {
            throw new EmailBusinessRuleNotFound(resourceId);
        }
        final Collection<EmailBusinessRulesData> businessRules = retrieveBusinessRules(resourceId);
        if (businessRules.isEmpty()) {
            throw new EmailBusinessRuleNotFound(resourceId);
        }
        return businessRules.iterator().next();
    }

    /**
     * Retrieves the email business rules (stretchy reports of type "Email") together with their parameters. A report
     * with several parameters comes back as several rows, which are merged into one {@link EmailBusinessRulesData}.
     *
     * @param reportId
     *            the report to retrieve, or {@code null} to retrieve all email business rules
     */
    private Collection<EmailBusinessRulesData> retrieveBusinessRules(final Long reportId) {
        final String searchType = "Email";
        final QStretchyReport report = new QStretchyReport("sr");
        final QStretchyReportParameter reportParameter = new QStretchyReportParameter("srp");
        final QStretchyParameter parameter = new QStretchyParameter("sp");

        // QueryDSL ignores a null predicate, so the id condition is only added when a report id is given
        final List<Tuple> rows = this.sqlQueryFactory
                .select(report.id, report.reportName, report.reportType, report.reportSubtype, report.description,
                        parameter.parameterVariable, parameter.parameterFormatType, parameter.parameterLabel, parameter.parameterName)
                .from(report).leftJoin(reportParameter).on(reportParameter.reportId.eq(report.id)).leftJoin(parameter)
                .on(parameter.id.eq(reportParameter.parameterId))
                .where(report.reportType.eq(searchType), reportId != null ? report.id.eq(reportId.intValue()) : null).fetch();

        final Map<Long, EmailBusinessRulesData> businessRulesById = new LinkedHashMap<>();
        for (final Tuple row : rows) {
            final Integer reportIdValue = row.get(report.id);
            final Long id = reportIdValue != null ? reportIdValue.longValue() : null;
            final String paramName = row.get(parameter.parameterName);
            final String paramLabel = row.get(parameter.parameterLabel);

            EmailBusinessRulesData businessRule = businessRulesById.get(id);
            if (businessRule == null) {
                businessRule = EmailBusinessRulesData.instance(id, row.get(report.reportName), row.get(report.reportType), new HashMap<>(),
                        row.get(report.reportSubtype), row.get(report.description));
                businessRulesById.put(id, businessRule);
            }
            if (paramLabel != null && paramName != null) {
                businessRule.getReportParamName().put(paramLabel, paramName);
            }
        }
        return businessRulesById.values();
    }

    @Override
    public EmailCampaignData retrieveOne(Long resourceId) {
        final boolean isVisible = true;
        try {
            final String sql = "select " + this.emailCampaignMapper.schema() + " where ec.id = ? and ec.is_visible = ?";
            return this.jdbcTemplate.queryForObject(sql, this.emailCampaignMapper, new Object[] { resourceId, isVisible }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new EmailCampaignNotFound(resourceId, e);
        }
    }

    @Override
    public Collection<EmailCampaignData> retrieveAllCampaign() {
        final boolean visible = true;
        final String sql = "select " + this.emailCampaignMapper.schema() + " where ec.is_visible = ?";
        return this.jdbcTemplate.query(sql, this.emailCampaignMapper, visible); // NOSONAR
    }

    @Override
    public Collection<EmailCampaignData> retrieveAllScheduleActiveCampaign() {
        final Integer scheduleCampaignType = EmailCampaignType.SCHEDULE.getValue();
        final Integer statusEnum = EmailCampaignStatus.ACTIVE.getValue();
        final boolean visible = true;
        final String sql = "select " + this.emailCampaignMapper.schema()
                + " where ec.status_enum = ? and ec.campaign_type = ? and ec.is_visible = ?";
        return this.jdbcTemplate.query(sql, this.emailCampaignMapper, new Object[] { statusEnum, scheduleCampaignType, visible }); // NOSONAR
    }

}
