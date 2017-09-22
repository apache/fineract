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
package org.apache.fineract.infrastructure.campaigns.sms.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.campaigns.constants.CampaignType;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsBusinessRulesData;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsCampaignData;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsCampaignTimeLine;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsProviderData;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaignStatusEnumerations;
import org.apache.fineract.infrastructure.campaigns.sms.exception.SmsCampaignNotFound;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.portfolio.calendar.service.CalendarDropdownReadPlatformService;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SmsCampaignReadPlatformServiceImpl implements SmsCampaignReadPlatformService {

    private final BusinessRuleMapper businessRuleMapper;
    private final JdbcTemplate jdbcTemplate;
    private final SmsCampaignDropdownReadPlatformService smsCampaignDropdownReadPlatformService;
    private final SmsCampaignMapper smsCampaignMapper;
    private final CalendarDropdownReadPlatformService calendarDropdownReadPlatformService;
    private final PaginationHelper<SmsCampaignData> paginationHelper = new PaginationHelper<>();

    @Autowired
    public SmsCampaignReadPlatformServiceImpl(final RoutingDataSource dataSource,
            SmsCampaignDropdownReadPlatformService smsCampaignDropdownReadPlatformService, 
            final CalendarDropdownReadPlatformService calendarDropdownReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        businessRuleMapper = new BusinessRuleMapper();
        this.smsCampaignDropdownReadPlatformService = smsCampaignDropdownReadPlatformService;
        smsCampaignMapper = new SmsCampaignMapper();
        this.calendarDropdownReadPlatformService = calendarDropdownReadPlatformService;
    }

    @Override
    public SmsCampaignData retrieveOne(Long campaignId) {
        final Integer isVisible = 1;
        try {
            final String sql = "select " + this.smsCampaignMapper.schema + " where sc.id = ? and sc.is_visible = ?";
            return this.jdbcTemplate.queryForObject(sql, this.smsCampaignMapper, new Object[] { campaignId, isVisible });
        } catch (final EmptyResultDataAccessException e) {
            throw new SmsCampaignNotFound(campaignId);
        }
    }

    @Override
    public Page<SmsCampaignData> retrieveAll(final SearchParameters searchParameters) {
        final Integer visible = 1;
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.smsCampaignMapper.schema() + " where sc.is_visible = ? ");
        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(jdbcTemplate, sqlCountRows, sqlBuilder.toString(), new Object[] { visible },
                this.smsCampaignMapper);
    }

    @Override
    public SmsCampaignData retrieveTemplate(final String reportType) {
        String sql = "select " + this.businessRuleMapper.schema();
        if (!StringUtils.isEmpty(reportType)) {
            sql = sql + " where sr.report_type = ?";
        }
        final Collection<SmsBusinessRulesData> businessRulesOptions = this.jdbcTemplate.query(sql, this.businessRuleMapper,
                new Object[] { reportType });
        final Collection<SmsProviderData> smsProviderOptions = this.smsCampaignDropdownReadPlatformService.retrieveSmsProviders();
        final Collection<EnumOptionData> campaignTypeOptions = this.smsCampaignDropdownReadPlatformService.retrieveCampaignTypes();
        final Collection<EnumOptionData> campaignTriggerTypeOptions = this.smsCampaignDropdownReadPlatformService
                .retrieveCampaignTriggerTypes();
        final Collection<EnumOptionData> months = this.smsCampaignDropdownReadPlatformService.retrieveMonths();
        final Collection<EnumOptionData> weekDays = this.smsCampaignDropdownReadPlatformService.retrieveWeeks();
        final Collection<EnumOptionData> frequencyTypeOptions = this.calendarDropdownReadPlatformService
                .retrieveCalendarFrequencyTypeOptions();
        final Collection<EnumOptionData> periodFrequencyOptions = this.smsCampaignDropdownReadPlatformService.retrivePeriodFrequencyTypes();
        //final Collection<TriggerTypeWithSubTypesData> triggerTypeSubTypeOptions = this.smsCampaignDropdownReadPlatformService.getTriggerTypeAndSubTypes();
        return SmsCampaignData.template(smsProviderOptions, campaignTypeOptions, businessRulesOptions, campaignTriggerTypeOptions, months,
                weekDays, frequencyTypeOptions, periodFrequencyOptions);
    }

    @Override
    public Collection<SmsCampaignData> retrieveAllScheduleActiveCampaign() {
        return null;
    }

    private static final class BusinessRuleMapper implements ResultSetExtractor<List<SmsBusinessRulesData>> {

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

        public String schema() {
            return this.schema;
        }

        @Override
        public List<SmsBusinessRulesData> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<SmsBusinessRulesData> smsBusinessRulesDataList = new ArrayList<SmsBusinessRulesData>();

            SmsBusinessRulesData smsBusinessRulesData = null;

            Map<Long, SmsBusinessRulesData> mapOfSameObjects = new HashMap<Long, SmsBusinessRulesData>();

            while (rs.next()) {
                final Long id = rs.getLong("id");
                smsBusinessRulesData = mapOfSameObjects.get(id);
                if (smsBusinessRulesData == null) {
                    final String reportName = rs.getString("reportName");
                    final String reportType = rs.getString("reportType");
                    final String reportSubType = rs.getString("reportSubType");
                    final String paramName = rs.getString("paramName");
                    final String paramLabel = rs.getString("paramLabel");
                    final String description = rs.getString("description");

                    Map<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put(paramLabel, paramName);
                    smsBusinessRulesData = SmsBusinessRulesData.instance(id, reportName, reportType, reportSubType, hashMap, description);
                    mapOfSameObjects.put(id, smsBusinessRulesData);
                    // add to the list
                    smsBusinessRulesDataList.add(smsBusinessRulesData);
                }
                // add new paramType to the existing object
                Map<String, Object> hashMap = new HashMap<String, Object>();
                final String paramName = rs.getString("paramName");
                final String paramLabel = rs.getString("paramLabel");
                hashMap.put(paramLabel, paramName);

                // get existing map and add new items to it
                smsBusinessRulesData.getReportParamName().putAll(hashMap);
            }

            return smsBusinessRulesDataList;
        }
    }
    
    private static final class SmsCampaignMapper implements RowMapper<SmsCampaignData> {

        final String schema;

        private SmsCampaignMapper() {
            final StringBuilder sql = new StringBuilder(400);
            sql.append("sc.id as id, ");
            sql.append("sc.campaign_name as campaignName, ");
            sql.append("sc.campaign_type as campaignType, ");
            sql.append("sc.campaign_trigger_type as triggerType, ");
            sql.append("sc.report_id as runReportId, ");
            sql.append("sc.message as message, ");
            sql.append("sc.param_value as paramValue, ");
            sql.append("sc.status_enum as status, ");
            sql.append("sc.recurrence as recurrence, ");
            sql.append("sc.recurrence_start_date as recurrenceStartDate, ");
            sql.append("sc.next_trigger_date as nextTriggerDate, ");
            sql.append("sc.last_trigger_date as lastTriggerDate, ");
            sql.append("sc.submittedon_date as submittedOnDate, ");
            sql.append("sbu.username as submittedByUsername, ");
            sql.append("sc.closedon_date as closedOnDate, ");
            sql.append("clu.username as closedByUsername, ");
            sql.append("acu.username as activatedByUsername, ");
            sql.append("sc.approvedon_date as activatedOnDate, ");
            sql.append("sr.report_name as reportName, ");
            sql.append("provider_id as providerId, ");
            sql.append("sc.is_notification as isNotification ");
            sql.append("from sms_campaign sc ");
            sql.append("left join m_appuser sbu on sbu.id = sc.submittedon_userid ");
            sql.append("left join m_appuser acu on acu.id = sc.approvedon_userid ");
            sql.append("left join m_appuser clu on clu.id = sc.closedon_userid ");
            sql.append("left join stretchy_report sr on sr.id = sc.report_id ");

            this.schema = sql.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public SmsCampaignData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String campaignName = rs.getString("campaignName");
            final Integer campaignType = JdbcSupport.getInteger(rs, "campaignType");
            final EnumOptionData campaignTypeEnum = CampaignType.campaignType(campaignType);
            final Long runReportId = JdbcSupport.getLong(rs, "runReportId");
            final String paramValue = rs.getString("paramValue");
            final String message = rs.getString("message");

            final Integer statusId = JdbcSupport.getInteger(rs, "status");
            final EnumOptionData status = SmsCampaignStatusEnumerations.status(statusId);
            final Integer triggerType = JdbcSupport.getInteger(rs, "triggerType");
            final EnumOptionData triggerTypeEnum = SmsCampaignTriggerType.triggerType(triggerType);

            final DateTime nextTriggerDate = JdbcSupport.getDateTime(rs, "nextTriggerDate");
            final LocalDate lastTriggerDate = JdbcSupport.getLocalDate(rs, "lastTriggerDate");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");

            final LocalDate activatedOnDate = JdbcSupport.getLocalDate(rs, "activatedOnDate");
            final String activatedByUsername = rs.getString("activatedByUsername");
            final String recurrence = rs.getString("recurrence");
            final DateTime recurrenceStartDate = JdbcSupport.getDateTime(rs, "recurrenceStartDate");
            final SmsCampaignTimeLine smsCampaignTimeLine = new SmsCampaignTimeLine(submittedOnDate, submittedByUsername, activatedOnDate,
                    activatedByUsername, closedOnDate, closedByUsername);
            final String reportName = rs.getString("reportName");
            final Long providerId = rs.getLong("providerId");
            final Boolean isNotification = rs.getBoolean("isNotification");
            return SmsCampaignData.instance(id, campaignName, campaignTypeEnum, triggerTypeEnum, runReportId, reportName, paramValue, status, message, nextTriggerDate, lastTriggerDate,
                    smsCampaignTimeLine, recurrenceStartDate, recurrence, providerId, isNotification);
        }
    }

}
