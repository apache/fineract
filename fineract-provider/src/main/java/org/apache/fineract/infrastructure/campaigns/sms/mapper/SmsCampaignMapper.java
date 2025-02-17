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
package org.apache.fineract.infrastructure.campaigns.sms.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import lombok.Setter;
import org.apache.fineract.infrastructure.campaigns.constants.CampaignType;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsCampaignData;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsCampaignTimeLine;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaignStatusEnumerations;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Setter
@Component
public class SmsCampaignMapper implements RowMapper<SmsCampaignData> {

    private String schema;

    public SmsCampaignMapper() {
        setSchema(buildCampaignColumn());
    }

    public String schema() {
        return this.schema;
    }

    private String buildCampaignColumn() {
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
        return sql.toString();
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
        final SmsCampaignTimeLine smsCampaignTimeLine = new SmsCampaignTimeLine(submittedOnDate, submittedByUsername, activatedOnDate,
                activatedByUsername, closedOnDate, closedByUsername);
        final String reportName = rs.getString("reportName");
        final Long providerId = rs.getLong("providerId");
        final boolean isNotification = rs.getBoolean("isNotification");
        return SmsCampaignData.instance(id, campaignName, campaignTypeEnum, triggerTypeEnum, runReportId, reportName, paramValue, status,
                message, nextTriggerDate, lastTriggerDate, smsCampaignTimeLine, recurrenceStartDate, recurrence, providerId,
                isNotification);
    }
}
