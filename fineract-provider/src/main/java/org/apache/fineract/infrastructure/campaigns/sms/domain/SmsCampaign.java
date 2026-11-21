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
package org.apache.fineract.infrastructure.campaigns.sms.domain;

import static org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignStatus.ACTIVE;
import static org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignStatus.CLOSED;
import static org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignStatus.PENDING;
import static org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType.DIRECT;
import static org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType.SCHEDULE;
import static org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType.TRIGGERED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.constants.CampaignType;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignStatus;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.serialization.SmsCampaignValidator;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarWeekDaysType;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "sms_campaign", uniqueConstraints = { @UniqueConstraint(columnNames = { "campaign_name" }, name = "campaign_name_UNIQUE") })
public class SmsCampaign extends AbstractPersistableCustom<Long> {

    @Column(name = "campaign_name", nullable = false)
    private String campaignName;

    @Column(name = "campaign_type", nullable = false)
    private Integer campaignType; // defines email or sms, etc..

    @Column(name = "campaign_trigger_type", nullable = false)
    private Integer triggerType; // defines direct, scheduled, transaction

    @Column(name = "provider_id", nullable = true) // null for notifications
    private Long providerId; // defined provider details

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private Report businessRuleId;

    @Column(name = "param_value")
    private String paramValue;

    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "closedon_date", nullable = true)
    private LocalDate closureDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "closedon_userid", nullable = true)
    private AppUser closedBy;

    @Column(name = "submittedon_date", nullable = true)
    private LocalDate submittedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "submittedon_userid", nullable = true)
    private AppUser submittedBy;

    @Column(name = "approvedon_date", nullable = true)
    private LocalDate approvedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "approvedon_userid", nullable = true)
    private AppUser approvedBy;

    @Column(name = "recurrence", nullable = true)
    private String recurrence;

    @Column(name = "next_trigger_date", nullable = true)
    private LocalDateTime nextTriggerDate;

    @Column(name = "last_trigger_date", nullable = true)
    private LocalDateTime lastTriggerDate;

    @Column(name = "recurrence_start_date", nullable = true)
    private LocalDateTime recurrenceStartDate;

    @Column(name = "is_visible", nullable = true)
    private boolean isVisible;

    @Column(name = "is_notification", nullable = true)
    private boolean isNotification;

    public SmsCampaign() {}

    private SmsCampaign(final String campaignName, final Integer campaignType, final Integer triggerType, final Report businessRuleId,
            final Long providerId, final String paramValue, final String message, final LocalDate submittedOnDate,
            final AppUser submittedBy, final String recurrence, final LocalDateTime recurrenceStartDate, final boolean isNotification) {
        this.campaignName = campaignName;
        this.campaignType = campaignType;
        this.triggerType = SmsCampaignTriggerType.fromInt(triggerType).getValue();
        this.businessRuleId = businessRuleId;
        this.providerId = providerId;
        this.paramValue = paramValue;
        this.status = SmsCampaignStatus.PENDING.getValue();
        this.message = message;
        this.submittedOnDate = submittedOnDate;
        this.submittedBy = submittedBy;
        this.recurrence = recurrence;
        this.isVisible = true;
        this.recurrenceStartDate = recurrenceStartDate;
        this.isNotification = isNotification;
    }

    public static SmsCampaign createNew(final String campaignName, final Integer campaignType, final Integer triggerType,
            final Report businessRuleId, final Long providerId, final String paramValue, final String message,
            final LocalDate submittedOnDate, final AppUser submittedBy, final String recurrence, final LocalDateTime recurrenceStartDate,
            final boolean isNotification) {
        return new SmsCampaign(campaignName, campaignType, triggerType, businessRuleId, providerId, paramValue, message, submittedOnDate,
                submittedBy, recurrence, recurrenceStartDate, isNotification);
    }

    public void updateCampaignName(final String campaignName) {
        this.campaignName = StringUtils.defaultIfEmpty(campaignName, null);
    }

    public void updateMessage(final String message) {
        this.message = StringUtils.defaultIfEmpty(message, null);
    }

    public void updateParamValue(final String paramValue) {
        this.paramValue = StringUtils.defaultIfEmpty(paramValue, null);
    }

    public void updateCampaignType(final CampaignType campaignType) {
        this.campaignType = campaignType.getValue();
    }

    public void updateTriggerType(final SmsCampaignTriggerType triggerType) {
        this.triggerType = triggerType.getValue();
    }

    public void updateRecurrence(final String recurrence) {
        this.recurrence = StringUtils.defaultIfEmpty(recurrence, null);
    }

    public void updateRecurrenceStartDate(final LocalDateTime recurrenceStartDate) {
        this.recurrenceStartDate = recurrenceStartDate;
    }

    public void activate(final AppUser currentUser, final DateTimeFormatter formatter, final LocalDate activationLocalDate) {
        if (isActive()) {
            // handle errors if already activated
            final String defaultUserMessage = "Cannot activate campaign. Campaign is already active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.already.active", defaultUserMessage,
                    SmsCampaignValidator.activationDateParamName, activationLocalDate.format(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        this.approvedOnDate = activationLocalDate;
        this.approvedBy = currentUser;
        this.status = ACTIVE.getValue();

        validate();
    }

    public void close(final AppUser currentUser, final DateTimeFormatter dateTimeFormatter, final LocalDate closureLocalDate) {
        if (isClosed()) {
            // handle errors if already activated
            final String defaultUserMessage = "Cannot close campaign. Campaign already in closed state.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.already.closed", defaultUserMessage,
                    SmsCampaignValidator.statusParamName, SmsCampaignStatus.fromInt(this.status).getCode());

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (this.triggerType.intValue() == SmsCampaignTriggerType.SCHEDULE.getValue()) {
            this.nextTriggerDate = null;
            this.lastTriggerDate = null;
        }
        this.closedBy = currentUser;
        this.closureDate = closureLocalDate;
        this.status = SmsCampaignStatus.CLOSED.getValue();
        validateClosureDate();
    }

    public void reactivate(final AppUser currentUser, final DateTimeFormatter dateTimeFormat, final LocalDate reactivateLocalDate) {
        if (!isClosed()) {
            // handle errors if already activated
            final String defaultUserMessage = "Cannot reactivate campaign. Campaign must be in closed state.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.must.be.closed", defaultUserMessage,
                    SmsCampaignValidator.statusParamName, SmsCampaignStatus.fromInt(this.status).getCode());

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        this.approvedOnDate = reactivateLocalDate;
        this.status = ACTIVE.getValue();
        this.approvedBy = currentUser;
        this.closureDate = null;
        this.isVisible = true;
        this.closedBy = null;

        validateReactivate();
    }

    public void delete() {
        if (!isClosed()) {
            // handle errors if already activated
            final String defaultUserMessage = "Cannot delete campaign. Campaign must be in closed state.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.must.be.closed", defaultUserMessage,
                    SmsCampaignValidator.statusParamName, SmsCampaignStatus.fromInt(this.status).getCode());

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        this.isVisible = false;
    }

    public boolean isActive() {
        return SmsCampaignStatus.fromInt(this.status).equals(ACTIVE);
    }

    public boolean isPending() {
        return SmsCampaignStatus.fromInt(this.status).equals(PENDING);
    }

    public boolean isClosed() {
        return SmsCampaignStatus.fromInt(this.status).equals(CLOSED);
    }

    public boolean isDirect() {
        return SmsCampaignTriggerType.fromInt(this.triggerType).equals(DIRECT);
    }

    public boolean isSchedule() {
        return SmsCampaignTriggerType.fromInt(this.triggerType).equals(SCHEDULE);
    }

    public boolean isTriggered() {
        return SmsCampaignTriggerType.fromInt(this.triggerType).equals(TRIGGERED);
    }

    private void validate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        validateActivationDate(dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateReactivate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        validateReactivationDate(dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateClosureDate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        validateClosureDate(dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateActivationDate(final List<ApiParameterError> dataValidationErrors) {
        if (DateUtils.isDateInTheFuture(getSubmittedOnDate())) {
            final String defaultUserMessage = "submitted date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.in.the.future",
                    defaultUserMessage, SmsCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }
        if (getActivationDate() != null && DateUtils.isAfter(getSubmittedOnDate(), getActivationDate())) {
            final String defaultUserMessage = "submitted date cannot be after the activation date";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.after.activation.date",
                    defaultUserMessage, SmsCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }
        if (DateUtils.isDateInTheFuture(getActivationDate())) {
            final String defaultUserMessage = "Activation date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.activationDate.in.the.future",
                    defaultUserMessage, SmsCampaignValidator.activationDateParamName, getActivationDate());

            dataValidationErrors.add(error);
        }
    }

    private void validateReactivationDate(final List<ApiParameterError> dataValidationErrors) {
        if (DateUtils.isDateInTheFuture(getActivationDate())) {
            final String defaultUserMessage = "Activation date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.activationDate.in.the.future",
                    defaultUserMessage, SmsCampaignValidator.activationDateParamName, getActivationDate());

            dataValidationErrors.add(error);
        }
        if (getActivationDate() != null && DateUtils.isAfter(getSubmittedOnDate(), getActivationDate())) {
            final String defaultUserMessage = "submitted date cannot be after the activation date";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.after.activation.date",
                    defaultUserMessage, SmsCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }
        if (DateUtils.isDateInTheFuture(getSubmittedOnDate())) {
            final String defaultUserMessage = "submitted date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.in.the.future",
                    defaultUserMessage, SmsCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }
    }

    private void validateClosureDate(final List<ApiParameterError> dataValidationErrors) {
        if (DateUtils.isDateInTheFuture(getClosureDate())) {
            final String defaultUserMessage = "closure date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.closureDate.in.the.future",
                    defaultUserMessage, SmsCampaignValidator.closureDateParamName, this.closureDate);

            dataValidationErrors.add(error);
        }
    }

    public LocalDate getSubmittedOnDate() {
        return this.submittedOnDate;

    }

    public LocalDate getClosureDate() {
        return this.closureDate;
    }

    public LocalDate getActivationDate() {
        return this.approvedOnDate;
    }

    public Report getBusinessRuleId() {
        return this.businessRuleId;
    }

    public String getCampaignName() {
        return this.campaignName;
    }

    public String getMessage() {
        return this.message;
    }

    public String getParamValue() {
        return this.paramValue;
    }

    public String getRecurrence() {
        return this.recurrence;
    }

    public LocalDateTime getRecurrenceStartDate() {
        return this.recurrenceStartDate;
    }

    public LocalDateTime getRecurrenceStartDateTime() {
        return this.recurrenceStartDate;
    }

    public void setLastTriggerDate(LocalDateTime lastTriggerDate) {
        this.lastTriggerDate = lastTriggerDate;
    }

    public void setNextTriggerDate(LocalDateTime nextTriggerDate) {
        this.nextTriggerDate = nextTriggerDate;
    }

    public LocalDateTime getNextTriggerDate() {
        return this.nextTriggerDate;
    }

    public LocalDateTime getLastTriggerDate() {
        return this.lastTriggerDate;
    }

    public void updateIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public void updateBusinessRuleId(final Report report) {
        this.businessRuleId = report;
    }

    public Long getProviderId() {
        return this.providerId;
    }

    public Integer getCampaignType() {
        return this.campaignType;
    }

    public Integer getTriggerType() {
        return this.triggerType;
    }

    public static String constructRecurrence(final CalendarFrequencyType frequencyType, final Integer interval,
            final Integer repeatsOnDay) {
        final StringBuilder recurrenceBuilder = new StringBuilder(200);

        recurrenceBuilder.append("FREQ=");
        recurrenceBuilder.append(frequencyType.toString().toUpperCase(java.util.Locale.ROOT));
        if (interval > 1) {
            recurrenceBuilder.append(";INTERVAL=");
            recurrenceBuilder.append(interval);
        }
        if (frequencyType.isWeekly()) {
            if (repeatsOnDay != null) {
                final CalendarWeekDaysType weekDays = CalendarWeekDaysType.fromInt(repeatsOnDay);
                if (weekDays != CalendarWeekDaysType.INVALID) {
                    recurrenceBuilder.append(";BYDAY=");
                    recurrenceBuilder.append(weekDays.toString().toUpperCase(java.util.Locale.ROOT));
                }
            }
        }
        return recurrenceBuilder.toString();
    }

    public boolean isNotification() {
        return this.isNotification;
    }

    public void setNotification(boolean isNotification) {
        this.isNotification = isNotification;
    }

}
