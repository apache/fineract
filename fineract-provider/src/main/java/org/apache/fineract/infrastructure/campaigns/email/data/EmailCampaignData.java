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
package org.apache.fineract.infrastructure.campaigns.email.data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class EmailCampaignData {

    @SuppressWarnings("unused")
    private Long id;
    @SuppressWarnings("unused")
    private String campaignName;
    @SuppressWarnings("unused")
    private Integer campaignType;
    @SuppressWarnings("unused")
    private Long businessRuleId;
    @SuppressWarnings("unused")
    private String paramValue;
    @SuppressWarnings("unused")
    private EnumOptionData campaignStatus;
    @SuppressWarnings("unused")
    private String emailSubject;
    @SuppressWarnings("unused")
    private String emailMessage;
    @SuppressWarnings("unused")
    private String emailAttachmentFileFormat;
    @SuppressWarnings("unused")
    private Long stretchyReportId;
    @SuppressWarnings("unused")
    private String stretchyReportParamMap;
    @SuppressWarnings("unused")
    private ZonedDateTime nextTriggerDate;
    @SuppressWarnings("unused")
    private LocalDate lastTriggerDate;
    @SuppressWarnings("unused")
    private EmailCampaignTimeLine emailCampaignTimeLine;

    @SuppressWarnings("unused")
    private ZonedDateTime recurrenceStartDate;

    private String recurrence;

    public static EmailCampaignData instance(final Long id, final String campaignName, final Integer campaignType,
            final Long businessRuleId, final String paramValue, final EnumOptionData campaignStatus, final String emailSubject,
            final String message, final String emailAttachmentFileFormat, final Long stretchyReportId, final String stretchyReportParamMap,
            final ZonedDateTime nextTriggerDate, final LocalDate lastTriggerDate, final EmailCampaignTimeLine emailCampaignTimeLine,
            final ZonedDateTime recurrenceStartDate, final String recurrence) {
        return new EmailCampaignData().setId(id).setCampaignName(campaignName).setCampaignType(campaignType)
                .setBusinessRuleId(businessRuleId).setParamValue(paramValue).setCampaignStatus(campaignStatus).setEmailSubject(emailSubject)
                .setEmailMessage(message).setEmailAttachmentFileFormat(emailAttachmentFileFormat).setStretchyReportId(stretchyReportId)
                .setStretchyReportParamMap(stretchyReportParamMap).setNextTriggerDate(nextTriggerDate).setLastTriggerDate(lastTriggerDate)
                .setEmailCampaignTimeLine(emailCampaignTimeLine).setRecurrenceStartDate(recurrenceStartDate).setRecurrence(recurrence);
    }
}
