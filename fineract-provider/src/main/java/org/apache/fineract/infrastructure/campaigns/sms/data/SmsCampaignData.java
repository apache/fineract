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
package org.apache.fineract.infrastructure.campaigns.sms.data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

@Builder
@Getter
@SuppressWarnings("unused")
public final class SmsCampaignData {

    private Long id;
    private final String campaignName;
    private final EnumOptionData campaignType;
    private final Long runReportId;
    private final String reportName;
    private final String paramValue;
    private final EnumOptionData campaignStatus;
    private final EnumOptionData triggerType;
    private final String campaignMessage;
    private final ZonedDateTime nextTriggerDate;
    private final LocalDate lastTriggerDate;
    private final SmsCampaignTimeLine smsCampaignTimeLine;
    private final ZonedDateTime recurrenceStartDate;
    private final String recurrence;
    private final Long providerId;
    private final boolean isNotification;

    private final Collection<SmsProviderData> smsProviderOptions;
    private final Collection<EnumOptionData> campaignTypeOptions;
    private final Collection<EnumOptionData> triggerTypeOptions;
    private final Collection<SmsBusinessRulesData> businessRulesOptions;
    private final Collection<EnumOptionData> months;
    private final Collection<EnumOptionData> weekDays;
    private final Collection<EnumOptionData> frequencyTypeOptions;
    private final Collection<EnumOptionData> periodFrequencyOptions;

    public static SmsCampaignData instance(final Long id, final String campaignName, final EnumOptionData campaignType,
            final EnumOptionData triggerType, final Long runReportId, final String reportName, final String paramValue,
            final EnumOptionData campaignStatus, final String message, final ZonedDateTime nextTriggerDate, final LocalDate lastTriggerDate,
            final SmsCampaignTimeLine smsCampaignTimeLine, final ZonedDateTime recurrenceStartDate, final String recurrence,
            final Long providerId, final boolean isNotification) {

        return SmsCampaignData.builder().id(id).campaignName(campaignName).campaignType(campaignType).triggerType(triggerType)
                .runReportId(runReportId).reportName(reportName).paramValue(paramValue).campaignStatus(campaignStatus)
                .campaignMessage(message).nextTriggerDate(nextTriggerDate).lastTriggerDate(lastTriggerDate)
                .smsCampaignTimeLine(smsCampaignTimeLine).recurrenceStartDate(recurrenceStartDate).recurrence(recurrence)
                .providerId(providerId).isNotification(isNotification).build();
    }

    public static SmsCampaignData template(final Collection<SmsProviderData> smsProviderOptions,
            final Collection<EnumOptionData> campaignTypeOptions, final Collection<SmsBusinessRulesData> businessRulesOptions,
            final Collection<EnumOptionData> triggerTypeOptions, final Collection<EnumOptionData> months,
            final Collection<EnumOptionData> weekDays, final Collection<EnumOptionData> frequencyTypeOptions,
            final Collection<EnumOptionData> periodFrequencyOptions) {
        return SmsCampaignData.builder().smsProviderOptions(smsProviderOptions).businessRulesOptions(businessRulesOptions)
                .campaignTypeOptions(campaignTypeOptions).triggerTypeOptions(triggerTypeOptions).months(months).weekDays(weekDays)
                .frequencyTypeOptions(frequencyTypeOptions).periodFrequencyOptions(periodFrequencyOptions).build();
    }

}
