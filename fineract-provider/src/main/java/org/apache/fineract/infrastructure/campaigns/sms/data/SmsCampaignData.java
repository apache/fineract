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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@SuppressWarnings("unused")
public final class SmsCampaignData {

    private Long id;
    private String campaignName;
    private EnumOptionData campaignType;
    private Long runReportId;
    private String reportName;
    private String paramValue;
    private EnumOptionData campaignStatus;
    private EnumOptionData triggerType;
    private String campaignMessage;
    private ZonedDateTime nextTriggerDate;
    private LocalDate lastTriggerDate;
    private SmsCampaignTimeLine smsCampaignTimeLine;
    private ZonedDateTime recurrenceStartDate;
    private String recurrence;
    private Long providerId;
    private boolean isNotification;

    private Collection<SmsProviderData> smsProviderOptions;

    private Collection<EnumOptionData> campaignTypeOptions;

    private Collection<EnumOptionData> triggerTypeOptions;

    private Collection<SmsBusinessRulesData> businessRulesOptions;

    private Collection<EnumOptionData> months;

    private Collection<EnumOptionData> weekDays;

    private Collection<EnumOptionData> frequencyTypeOptions;

    private Collection<EnumOptionData> periodFrequencyOptions;

    public static SmsCampaignData instance(final Long id, final String campaignName, final EnumOptionData campaignType,
            final EnumOptionData triggerType, final Long runReportId, final String reportName, final String paramValue,
            final EnumOptionData campaignStatus, final String message, final ZonedDateTime nextTriggerDate, final LocalDate lastTriggerDate,
            final SmsCampaignTimeLine smsCampaignTimeLine, final ZonedDateTime recurrenceStartDate, final String recurrence,
            final Long providerId, final boolean isNotification) {
        final Collection<SmsBusinessRulesData> businessRulesOptions = null;
        final Collection<SmsProviderData> smsProviderOptions = null;
        final Collection<EnumOptionData> campaignTypeOptions = null;
        final Collection<EnumOptionData> triggerTypeOptions = null;
        final Collection<EnumOptionData> months = null;
        final Collection<EnumOptionData> weekDays = null;
        final Collection<EnumOptionData> frequencyTypeOptions = null;
        final Collection<EnumOptionData> periodFrequencyOptions = null;

        return new SmsCampaignData().setId(id).setCampaignName(campaignName).setCampaignType(campaignType).setTriggerType(triggerType)
                .setRunReportId(runReportId).setReportName(reportName).setParamValue(paramValue).setCampaignStatus(campaignStatus)
                .setCampaignMessage(message).setNextTriggerDate(nextTriggerDate).setLastTriggerDate(lastTriggerDate)
                .setSmsCampaignTimeLine(smsCampaignTimeLine).setRecurrenceStartDate(recurrenceStartDate).setRecurrence(recurrence)
                .setProviderId(providerId).setBusinessRulesOptions(businessRulesOptions).setSmsProviderOptions(smsProviderOptions)
                .setCampaignTypeOptions(campaignTypeOptions).setTriggerTypeOptions(triggerTypeOptions).setMonths(months)
                .setWeekDays(weekDays).setFrequencyTypeOptions(frequencyTypeOptions).setPeriodFrequencyOptions(periodFrequencyOptions)
                .setNotification(isNotification);
    }

    public static SmsCampaignData template(final Collection<SmsProviderData> smsProviderOptions,
            final Collection<EnumOptionData> campaignTypeOptions, final Collection<SmsBusinessRulesData> businessRulesOptions,
            final Collection<EnumOptionData> triggerTypeOptions, final Collection<EnumOptionData> months,
            final Collection<EnumOptionData> weekDays, final Collection<EnumOptionData> frequencyTypeOptions,
            final Collection<EnumOptionData> periodFrequencyOptions) {
        final Long id = null;
        final String campaignName = null;
        final EnumOptionData campaignType = null;
        final Long runReportId = null;
        final String paramValue = null;
        final EnumOptionData campaignStatus = null;
        final String message = null;
        final ZonedDateTime nextTriggerDate = null;
        final LocalDate lastTriggerDate = null;
        final SmsCampaignTimeLine smsCampaignTimeLine = null;
        final ZonedDateTime recurrenceStartDate = null;
        final String recurrence = null;
        final EnumOptionData triggerType = null;
        final String reportName = null;
        final Long providerId = null;
        final boolean isNotification = false;
        return new SmsCampaignData().setId(id).setCampaignName(campaignName).setCampaignType(campaignType).setTriggerType(triggerType)
                .setRunReportId(runReportId).setReportName(reportName).setParamValue(paramValue).setCampaignStatus(campaignStatus)
                .setCampaignMessage(message).setNextTriggerDate(nextTriggerDate).setLastTriggerDate(lastTriggerDate)
                .setSmsCampaignTimeLine(smsCampaignTimeLine).setRecurrenceStartDate(recurrenceStartDate).setRecurrence(recurrence)
                .setProviderId(providerId).setBusinessRulesOptions(businessRulesOptions).setSmsProviderOptions(smsProviderOptions)
                .setCampaignTypeOptions(campaignTypeOptions).setTriggerTypeOptions(triggerTypeOptions).setMonths(months)
                .setWeekDays(weekDays).setFrequencyTypeOptions(frequencyTypeOptions).setPeriodFrequencyOptions(periodFrequencyOptions)
                .setNotification(isNotification);
    }
}
