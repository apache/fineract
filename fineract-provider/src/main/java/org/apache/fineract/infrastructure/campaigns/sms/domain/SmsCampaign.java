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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.constants.CampaignType;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignStatus;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.serialization.SmsCampaignValidator;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarWeekDaysType;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Entity
@Table(name = "sms_campaign", uniqueConstraints = {@UniqueConstraint(columnNames = { "campaign_name" }, name = "campaign_name_UNIQUE")})
public class SmsCampaign extends AbstractPersistableCustom<Long> {

    @Column(name = "campaign_name", nullable = false)
    private String campaignName;

    @Column(name = "campaign_type", nullable = false)
    private Integer campaignType ; //defines email or sms, etc..
    
    @Column(name = "campaign_trigger_type", nullable = false)
    private Integer triggerType; //defines direct, scheduled, transaction
    
    @Column(name = "provider_id", nullable = true)//null for notifications
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
    @Temporal(TemporalType.DATE)
    private Date closureDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "closedon_userid", nullable = true)
    private AppUser closedBy;

    @Column(name = "submittedon_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date submittedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "submittedon_userid", nullable = true)
    private AppUser submittedBy;

    @Column(name = "approvedon_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date approvedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "approvedon_userid", nullable = true)
    private AppUser approvedBy;

    @Column(name = "recurrence", nullable = true)
    private String recurrence;

    @Column(name = "next_trigger_date", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextTriggerDate;

    @Column(name = "last_trigger_date", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastTriggerDate;

    @Column(name = "recurrence_start_date", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date recurrenceStartDate;

    @Column(name = "is_visible", nullable = true)
    private boolean isVisible;
    
    @Column(name = "is_notification", nullable = true)
    private boolean isNotification;

    public SmsCampaign() {}

    private SmsCampaign(final String campaignName, final Integer campaignType, 
            final Integer triggerType, final Report businessRuleId, final Long providerId, final String paramValue,
            final String message, final LocalDate submittedOnDate, final AppUser submittedBy, final String recurrence,
            final LocalDateTime localDateTime, final boolean isNotification) {
        this.campaignName = campaignName;
        this.campaignType = campaignType;
        this.triggerType = SmsCampaignTriggerType.fromInt(triggerType).getValue();
        this.businessRuleId = businessRuleId;
        this.providerId = providerId;
        this.paramValue = paramValue;
        this.status = SmsCampaignStatus.PENDING.getValue();
        this.message = message;
        this.submittedOnDate = submittedOnDate.toDate();
        this.submittedBy = submittedBy;
        this.recurrence = recurrence;
        LocalDateTime recurrenceStartDate = new LocalDateTime();
        this.isVisible = true;
        if (localDateTime != null) {
            this.recurrenceStartDate = localDateTime.toDate();
        } else {
            this.recurrenceStartDate = recurrenceStartDate.toDate();
        }
        this.isNotification = isNotification;
    }

    public static SmsCampaign instance(final AppUser submittedBy, final Report report, final JsonCommand command) {

        final String campaignName = command.stringValueOfParameterNamed(SmsCampaignValidator.campaignName);
        final Long campaignType = command.longValueOfParameterNamed(SmsCampaignValidator.campaignType);
        final Long triggerType = command.longValueOfParameterNamed(SmsCampaignValidator.triggerType);
        boolean isNotification = false;
        if(command.parameterExists(SmsCampaignValidator.isNotificationParamName)){
        	isNotification = command.booleanPrimitiveValueOfParameterNamed(SmsCampaignValidator.isNotificationParamName);
        }
        Long providerId = null;
        if(!isNotification){
        	providerId = command.longValueOfParameterNamed(SmsCampaignValidator.providerId);
        }
        
        final String paramValue = command.jsonFragment(SmsCampaignValidator.paramValue);

        final String message = command.stringValueOfParameterNamed(SmsCampaignValidator.message);
        LocalDate submittedOnDate = new LocalDate();
        if (command.hasParameter(SmsCampaignValidator.submittedOnDateParamName)) {
            submittedOnDate = command.localDateValueOfParameterNamed(SmsCampaignValidator.submittedOnDateParamName);
        }
        String recurrence = null;
        

        LocalDateTime recurrenceStartDate = new LocalDateTime();
        if (SmsCampaignTriggerType.fromInt(triggerType.intValue()).isSchedule()) {
            final Locale locale = command.extractLocale();
            String dateTimeFormat = null;
            if (command.hasParameter(SmsCampaignValidator.dateTimeFormat)) {
                dateTimeFormat = command.stringValueOfParameterNamed(SmsCampaignValidator.dateTimeFormat);
                final DateTimeFormatter fmt = DateTimeFormat.forPattern(dateTimeFormat).withLocale(locale);
                if (command.hasParameter(SmsCampaignValidator.recurrenceStartDate)) {
                    recurrenceStartDate = LocalDateTime.parse(
                            command.stringValueOfParameterNamed(SmsCampaignValidator.recurrenceStartDate), fmt);
                }
                recurrence = constructRecurrence(command);
            }
        } else {
            recurrenceStartDate = null;
        }

        return new SmsCampaign(campaignName, campaignType.intValue(), triggerType.intValue(), report,
                providerId, paramValue, message, submittedOnDate, submittedBy, recurrence, recurrenceStartDate, isNotification);
    }

    public Map<String, Object> update(JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);

        if (command.isChangeInStringParameterNamed(SmsCampaignValidator.campaignName, this.campaignName)) {
            final String newValue = command.stringValueOfParameterNamed(SmsCampaignValidator.campaignName);
            actualChanges.put(SmsCampaignValidator.campaignName, newValue);
            this.campaignName = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(SmsCampaignValidator.message, this.message)) {
            final String newValue = command.stringValueOfParameterNamed(SmsCampaignValidator.message);
            actualChanges.put(SmsCampaignValidator.message, newValue);
            this.message = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(SmsCampaignValidator.paramValue, this.paramValue)) {
            final String newValue = command.jsonFragment(SmsCampaignValidator.paramValue);
            actualChanges.put(SmsCampaignValidator.paramValue, newValue);
            this.paramValue = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInIntegerParameterNamed(SmsCampaignValidator.campaignType, this.campaignType)) {
            final Integer newValue = command.integerValueOfParameterNamed(SmsCampaignValidator.campaignType);
            actualChanges.put(SmsCampaignValidator.campaignType, CampaignType.fromInt(newValue));
            this.campaignType = CampaignType.fromInt(newValue).getValue();
        }

        if (command.isChangeInIntegerParameterNamed(SmsCampaignValidator.triggerType, this.triggerType)) {
            final Integer newValue = command.integerValueOfParameterNamed(SmsCampaignValidator.triggerType);
            actualChanges.put(SmsCampaignValidator.triggerType, SmsCampaignTriggerType.fromInt(newValue));
            this.triggerType = SmsCampaignTriggerType.fromInt(newValue).getValue();
        }

        if (command.isChangeInLongParameterNamed(SmsCampaignValidator.runReportId,
                (this.businessRuleId != null) ? this.businessRuleId.getId() : null)) {
            final String newValue = command.stringValueOfParameterNamed(SmsCampaignValidator.runReportId);
            actualChanges.put(SmsCampaignValidator.runReportId, newValue);
        }
        if (command.isChangeInStringParameterNamed(SmsCampaignValidator.recurrenceParamName, this.recurrence)) {
            final String newValue = command.stringValueOfParameterNamed(SmsCampaignValidator.recurrenceParamName);
            actualChanges.put(SmsCampaignValidator.recurrenceParamName, newValue);
            this.recurrence = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInLongParameterNamed(SmsCampaignValidator.providerId, this.providerId)) {
            final Long newValue = command.longValueOfParameterNamed(SmsCampaignValidator.providerId);
            actualChanges.put(SmsCampaignValidator.providerId, newValue);
        }
        if (command.isChangeInBooleanParameterNamed(SmsCampaignValidator.isNotificationParamName, this.isNotification)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(SmsCampaignValidator.isNotificationParamName);
            this.isNotification = newValue;
            actualChanges.put(SmsCampaignValidator.isNotificationParamName, newValue);
        }

        if (SmsCampaignTriggerType.fromInt(this.triggerType).isSchedule()) {
            final String dateFormatAsInput = command.dateFormat();
            final String dateTimeFormatAsInput = command.stringValueOfParameterNamed(SmsCampaignValidator.dateTimeFormat);
            final String localeAsInput = command.locale();
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(dateTimeFormatAsInput).withLocale(locale);
            final String valueAsInput = command.stringValueOfParameterNamed(SmsCampaignValidator.recurrenceStartDate);
            actualChanges.put(SmsCampaignValidator.recurrenceStartDate, valueAsInput);
            actualChanges.put(SmsCampaignValidator.dateFormatParamName, dateFormatAsInput);
            actualChanges.put(SmsCampaignValidator.dateTimeFormat, dateTimeFormatAsInput);
            actualChanges.put(SmsCampaignValidator.localeParamName, localeAsInput);

            final LocalDateTime newValue = LocalDateTime.parse(valueAsInput, fmt);

            this.recurrenceStartDate = newValue.toDate();
        }

        return actualChanges;
    }

    public void activate(final AppUser currentUser, final DateTimeFormatter formatter, final LocalDate activationLocalDate) {

        if (isActive()) {
            // handle errors if already activated
            final String defaultUserMessage = "Cannot activate campaign. Campaign is already active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.already.active", defaultUserMessage,
                    SmsCampaignValidator.activationDateParamName, activationLocalDate.toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        this.approvedOnDate = activationLocalDate.toDate();
        this.approvedBy = currentUser;
        this.status = SmsCampaignStatus.ACTIVE.getValue();

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
        this.closureDate = closureLocalDate.toDate();
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

        this.approvedOnDate = reactivateLocalDate.toDate();
        this.status = SmsCampaignStatus.ACTIVE.getValue();
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
        return SmsCampaignStatus.fromInt(this.status).isActive();
    }

    public boolean isPending() {
        return SmsCampaignStatus.fromInt(this.status).isPending();
    }

    public boolean isClosed() {
        return SmsCampaignStatus.fromInt(this.status).isClosed();
    }

    public boolean isDirect() {
        return SmsCampaignTriggerType.fromInt(this.triggerType).isDirect();
    }

    public boolean isSchedule() {
        return SmsCampaignTriggerType.fromInt(this.triggerType).isSchedule();
    }

    public boolean isTriggered() {
        return SmsCampaignTriggerType.fromInt(this.triggerType).isTriggered();
    }

    private void validate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        validateActivationDate(dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validateReactivate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        validateReactivationDate(dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validateClosureDate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        validateClosureDate(dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validateActivationDate(final List<ApiParameterError> dataValidationErrors) {

        if (getSubmittedOnDate() != null && isDateInTheFuture(getSubmittedOnDate())) {

            final String defaultUserMessage = "submitted date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.in.the.future",
                    defaultUserMessage, SmsCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }

        if (getActivationLocalDate() != null && getSubmittedOnDate() != null && getSubmittedOnDate().isAfter(getActivationLocalDate())) {

            final String defaultUserMessage = "submitted date cannot be after the activation date";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.after.activation.date",
                    defaultUserMessage, SmsCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }

        if (getActivationLocalDate() != null && isDateInTheFuture(getActivationLocalDate())) {

            final String defaultUserMessage = "Activation date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.activationDate.in.the.future",
                    defaultUserMessage, SmsCampaignValidator.activationDateParamName, getActivationLocalDate());

            dataValidationErrors.add(error);
        }

    }

    private void validateReactivationDate(final List<ApiParameterError> dataValidationErrors) {
        if (getActivationLocalDate() != null && isDateInTheFuture(getActivationLocalDate())) {

            final String defaultUserMessage = "Activation date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.activationDate.in.the.future",
                    defaultUserMessage, SmsCampaignValidator.activationDateParamName, getActivationLocalDate());

            dataValidationErrors.add(error);
        }
        if (getActivationLocalDate() != null && getSubmittedOnDate() != null && getSubmittedOnDate().isAfter(getActivationLocalDate())) {

            final String defaultUserMessage = "submitted date cannot be after the activation date";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.after.activation.date",
                    defaultUserMessage, SmsCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }
        if (getSubmittedOnDate() != null && isDateInTheFuture(getSubmittedOnDate())) {

            final String defaultUserMessage = "submitted date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.in.the.future",
                    defaultUserMessage, SmsCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }

    }

    private void validateClosureDate(final List<ApiParameterError> dataValidationErrors) {
        if (getClosureDate() != null && isDateInTheFuture(getClosureDate())) {
            final String defaultUserMessage = "closure date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.closureDate.in.the.future",
                    defaultUserMessage, SmsCampaignValidator.closureDateParamName, this.closureDate);

            dataValidationErrors.add(error);
        }
    }

    public LocalDate getSubmittedOnDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.submittedOnDate), null);

    }

    public LocalDate getClosureDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.closureDate), null);
    }

    public LocalDate getActivationLocalDate() {
        LocalDate activationLocalDate = null;
        if (this.approvedOnDate != null) {
            activationLocalDate = LocalDate.fromDateFields(this.approvedOnDate);
        }
        return activationLocalDate;
    }

    private boolean isDateInTheFuture(final LocalDate localDate) {
        return localDate.isAfter(DateUtils.getLocalDateOfTenant());
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

    public LocalDate getRecurrenceStartDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.recurrenceStartDate), null);
    }

    public LocalDateTime getRecurrenceStartDateTime() {
        return (LocalDateTime) ObjectUtils.defaultIfNull(new LocalDateTime(this.recurrenceStartDate), null);
    }

    public void setLastTriggerDate(Date lastTriggerDate) {
        this.lastTriggerDate = lastTriggerDate;
    }

    public void setNextTriggerDate(Date nextTriggerDate) {
        this.nextTriggerDate = nextTriggerDate;
    }

    public LocalDateTime getNextTriggerDate() {
        return (LocalDateTime) ObjectUtils.defaultIfNull(new LocalDateTime(this.nextTriggerDate), null);

    }

    public Date getNextTriggerDateInDate() {
        return this.nextTriggerDate;
    }

    public LocalDate getLastTriggerDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.lastTriggerDate), null);
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

    private static String constructRecurrence(final JsonCommand command) {
        final Integer frequency = command.integerValueOfParameterNamed(SmsCampaignValidator.frequencyParamName);
        final CalendarFrequencyType frequencyType = CalendarFrequencyType.fromInt(frequency);
        final Integer interval = command.integerValueOfParameterNamed(SmsCampaignValidator.intervalParamName);
        Integer repeatsOnDay = null;
        if (frequencyType.isWeekly()) {
            repeatsOnDay = command.integerValueOfParameterNamed(SmsCampaignValidator.repeatsOnDayParamName);
        }
        return constructRecurrence(frequencyType, interval, repeatsOnDay);
    }

    private static String constructRecurrence(final CalendarFrequencyType frequencyType, final Integer interval, final Integer repeatsOnDay) {
        final StringBuilder recurrenceBuilder = new StringBuilder(200);

        recurrenceBuilder.append("FREQ=");
        recurrenceBuilder.append(frequencyType.toString().toUpperCase());
        if (interval > 1) {
            recurrenceBuilder.append(";INTERVAL=");
            recurrenceBuilder.append(interval);
        }
        if (frequencyType.isWeekly()) {
            if (repeatsOnDay != null) {
                final CalendarWeekDaysType weekDays = CalendarWeekDaysType.fromInt(repeatsOnDay);
                if (!weekDays.isInvalid()) {
                    recurrenceBuilder.append(";BYDAY=");
                    recurrenceBuilder.append(weekDays.toString().toUpperCase());
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
