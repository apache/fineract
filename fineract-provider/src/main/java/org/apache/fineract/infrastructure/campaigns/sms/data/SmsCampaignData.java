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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

@SuppressWarnings("unused")
public class SmsCampaignData {

    private Long id;
    private final String campaignName;
    private final EnumOptionData campaignType;
    private final Long runReportId;
    private final String reportName;
    private final String paramValue;
    private final EnumOptionData campaignStatus;
    private final EnumOptionData triggerType;
    private final String campaignMessage;
    private final DateTime nextTriggerDate;
    private final LocalDate lastTriggerDate;
    private final SmsCampaignTimeLine smsCampaignTimeLine;
    private final DateTime recurrenceStartDate;
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
    
    private SmsCampaignData(final Long id, final String campaignName, final EnumOptionData campaignType, final EnumOptionData triggerType,
            final Long runReportId, 
            final String reportName, final String paramValue, final EnumOptionData campaignStatus,
            final String message, final DateTime nextTriggerDate, final LocalDate lastTriggerDate,
            final SmsCampaignTimeLine smsCampaignTimeLine, final DateTime recurrenceStartDate, final String recurrence,
            final Long providerId, final Collection<SmsBusinessRulesData> businessRulesOptions,
            final Collection<SmsProviderData> smsProviderOptions, final Collection<EnumOptionData> campaignTypeOptions,
            final Collection<EnumOptionData> triggerTypeOptions, final Collection<EnumOptionData> months, 
            final Collection<EnumOptionData> weekDays, final Collection<EnumOptionData> frequencyTypeOptions, 
            final Collection<EnumOptionData> periodFrequencyOptions, final boolean isNotification) {
        this.id = id;
        this.campaignName = campaignName;
        this.campaignType = campaignType;
        this.triggerType = triggerType;
        this.runReportId = runReportId;
        this.reportName = reportName;
        this.paramValue = paramValue;
        this.campaignStatus = campaignStatus;
        this.campaignMessage = message;
        if (nextTriggerDate != null) {
            this.nextTriggerDate = nextTriggerDate;
        } else {
            this.nextTriggerDate = null;
        }
        if (lastTriggerDate != null) {
            this.lastTriggerDate = lastTriggerDate;
        } else {
            this.lastTriggerDate = null;
        }
        this.isNotification = isNotification;
        this.smsCampaignTimeLine = smsCampaignTimeLine;
        this.recurrenceStartDate = recurrenceStartDate;
        this.recurrence = recurrence;
        this.providerId = providerId;
        this.businessRulesOptions = businessRulesOptions;
        this.smsProviderOptions = smsProviderOptions;
        this.campaignTypeOptions = campaignTypeOptions;
        this.triggerTypeOptions = triggerTypeOptions;
        this.months = months;
        this.weekDays = weekDays;
        this.frequencyTypeOptions = frequencyTypeOptions;
        this.periodFrequencyOptions = periodFrequencyOptions;
    }

    public static SmsCampaignData instance(final Long id, final String campaignName, final EnumOptionData campaignType,
            final EnumOptionData triggerType, 
            final Long runReportId, final String reportName, final String paramValue, final EnumOptionData campaignStatus,
            final String message, final DateTime nextTriggerDate, final LocalDate lastTriggerDate,
            final SmsCampaignTimeLine smsCampaignTimeLine, final DateTime recurrenceStartDate, final String recurrence,
            final Long providerId, final boolean isNotification) {
        final Collection<SmsBusinessRulesData> businessRulesOptions = null;
        final Collection<SmsProviderData> smsProviderOptions = null;
        final Collection<EnumOptionData> campaignTypeOptions = null;
        final Collection<EnumOptionData> triggerTypeOptions = null;
        final Collection<EnumOptionData> months = null;
        final Collection<EnumOptionData> weekDays = null;
        final Collection<EnumOptionData> frequencyTypeOptions = null;
        final Collection<EnumOptionData> periodFrequencyOptions = null;

        return new SmsCampaignData(id, campaignName, campaignType, triggerType, runReportId,
                reportName, paramValue, campaignStatus, message, nextTriggerDate, lastTriggerDate, smsCampaignTimeLine,
                recurrenceStartDate, recurrence, providerId, businessRulesOptions, smsProviderOptions, campaignTypeOptions,
                triggerTypeOptions, months, weekDays, frequencyTypeOptions, periodFrequencyOptions, isNotification);
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
        final DateTime nextTriggerDate = null;
        final LocalDate lastTriggerDate = null;
        final SmsCampaignTimeLine smsCampaignTimeLine = null;
        final DateTime recurrenceStartDate = null;
        final String recurrence = null;
        final EnumOptionData triggerType = null;
        final String reportName = null;
        final Long providerId = null;
        final boolean isNotification = false;
        return new SmsCampaignData(id, campaignName, campaignType, triggerType, runReportId,
                reportName, paramValue, campaignStatus, message, nextTriggerDate, lastTriggerDate, smsCampaignTimeLine,
                recurrenceStartDate, recurrence, providerId, businessRulesOptions, smsProviderOptions, campaignTypeOptions,
                triggerTypeOptions, months, weekDays, frequencyTypeOptions, periodFrequencyOptions, isNotification);
    }

    public Long getId() {
        return id;
    }

    public String getCampaignName() {
        return this.campaignName;
    }

    public EnumOptionData getCampaignType() {
        return this.campaignType;
    }

    public Long getRunReportId() {
        return this.runReportId;
    }

    public String getParamValue() {
        return this.paramValue;
    }

    public EnumOptionData getCampaignStatus() {
        return this.campaignStatus;
    }

    public String getMessage() {
        return this.campaignMessage;
    }

    public DateTime getNextTriggerDate() {
        return this.nextTriggerDate;
    }

    public LocalDate getLastTriggerDate() {
        return this.lastTriggerDate;
    }

    public String getRecurrence() {
        return this.recurrence;
    }

    public DateTime getRecurrenceStartDate() {
        return this.recurrenceStartDate;
    }

    public String getReportName() {
        return this.reportName;
    }

    public Long providerId() {
        return this.providerId;
    }

	public boolean isNotification() {
		return this.isNotification;
	}

}
