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
package org.apache.fineract.infrastructure.campaigns.email.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignValidator;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "scheduled_email_campaign")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class EmailCampaign extends AbstractPersistableCustom<Long> {

    @Column(name = "campaign_name", nullable = false)
    private String campaignName;

    @Column(name = "campaign_type", nullable = false)
    private Integer campaignType;

    @ManyToOne
    @JoinColumn(name = "business_rule_id", nullable = false)
    private Report businessRuleId;

    @Column(name = "param_value")
    private String paramValue;

    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Column(name = "email_subject", nullable = false)
    private String emailSubject;

    @Column(name = "email_message", nullable = false)
    private String emailMessage;

    @Column(name = "email_attachment_file_format")
    private String emailAttachmentFileFormat;

    @ManyToOne
    @JoinColumn(name = "stretchy_report_id")
    private Report stretchyReport;

    @Column(name = "stretchy_report_param_map", nullable = true)
    private String stretchyReportParamMap;

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

    @Column(name = "recurrence")
    private String recurrence;

    @Column(name = "next_trigger_date")
    private LocalDateTime nextTriggerDate;

    @Column(name = "last_trigger_date")
    private LocalDateTime lastTriggerDate;

    @Column(name = "recurrence_start_date")
    private LocalDateTime recurrenceStartDate;

    @Column(name = "is_visible", nullable = true)
    private boolean isVisible;

    @Column(name = "previous_run_status", nullable = true)
    private String previousRunStatus;

    @Column(name = "previous_run_error_log", nullable = true)
    private String previousRunErrorLog;

    @Column(name = "previous_run_error_message", nullable = true)
    private String previousRunErrorMessage;

    public static EmailCampaign createNew(final String campaignName, final Integer campaignType, final Report businessRuleId,
            final String paramValue, final String emailSubject, final String emailMessage, final LocalDate submittedOnDate,
            final AppUser submittedBy, final Report stretchyReport, final String emailAttachmentFileFormat, final String recurrence,
            final LocalDateTime recurrenceStartDate, final String stretchyReportParamMap) {
        return new EmailCampaign().setCampaignName(campaignName).setCampaignType(campaignType).setBusinessRuleId(businessRuleId)
                .setParamValue(paramValue).setStatus(EmailCampaignStatus.PENDING.getValue()).setEmailSubject(emailSubject)
                .setEmailMessage(emailMessage).setSubmittedOnDate(submittedOnDate).setSubmittedBy(submittedBy)
                .setStretchyReport(stretchyReport).setEmailAttachmentFileFormat(emailAttachmentFileFormat).setRecurrence(recurrence)
                .setRecurrenceStartDate(recurrenceStartDate).setStretchyReportParamMap(stretchyReportParamMap);
    }

    public void updateCampaignName(final String campaignName) {
        this.campaignName = StringUtils.defaultIfEmpty(campaignName, null);
    }

    public void updateEmailMessage(final String emailMessage) {
        this.emailMessage = StringUtils.defaultIfEmpty(emailMessage, null);
    }

    public void updateParamValue(final String paramValue) {
        this.paramValue = StringUtils.defaultIfEmpty(paramValue, null);
    }

    public void updateCampaignType(final EmailCampaignType campaignType) {
        this.campaignType = campaignType.getValue();
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
                    EmailCampaignValidator.activationDateParamName, activationLocalDate.format(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        this.approvedOnDate = activationLocalDate;
        this.approvedBy = currentUser;
        this.status = EmailCampaignStatus.ACTIVE.getValue();

        validate();
    }

    public void close(final AppUser currentUser, final DateTimeFormatter dateTimeFormatter, final LocalDate closureLocalDate) {
        if (isClosed()) {
            // handle errors if already activated
            final String defaultUserMessage = "Cannot close campaign. Campaign already in closed state.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.already.closed", defaultUserMessage,
                    EmailCampaignValidator.statusParamName, EmailCampaignStatus.fromInt(this.status).getCode());

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (this.campaignType.intValue() == EmailCampaignType.SCHEDULE.getValue()) {
            this.nextTriggerDate = null;
            this.lastTriggerDate = null;
        }
        this.closedBy = currentUser;
        this.closureDate = closureLocalDate;
        this.status = EmailCampaignStatus.CLOSED.getValue();
        validateClosureDate();
    }

    public void reactivate(final AppUser currentUser, final DateTimeFormatter dateTimeFormat, final LocalDate reactivateLocalDate) {
        if (!isClosed()) {
            // handle errors if already activated
            final String defaultUserMessage = "Cannot reactivate campaign. Campaign must be in closed state.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.must.be.closed", defaultUserMessage,
                    EmailCampaignValidator.statusParamName, EmailCampaignStatus.fromInt(this.status).getCode());

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        this.approvedOnDate = reactivateLocalDate;
        this.status = EmailCampaignStatus.ACTIVE.getValue();
        this.approvedBy = currentUser;
        this.isVisible = true;

        validateReactivate();
    }

    public void delete() {
        if (!isClosed()) {
            // handle errors if already activated
            final String defaultUserMessage = "Cannot delete campaign. Campaign must be in closed state.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.must.be.closed", defaultUserMessage,
                    EmailCampaignValidator.statusParamName, EmailCampaignStatus.fromInt(this.status).getCode());

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        this.isVisible = false;
    }

    public boolean isActive() {
        return EmailCampaignStatus.fromInt(this.status).isActive();
    }

    public boolean isPending() {
        return EmailCampaignStatus.fromInt(this.status).isPending();
    }

    public boolean isClosed() {
        return EmailCampaignStatus.fromInt(this.status).isClosed();
    }

    public boolean isDirect() {
        return EmailCampaignType.fromInt(this.campaignType).isDirect();
    }

    public boolean isSchedule() {
        return EmailCampaignType.fromInt(this.campaignType).isSchedule();
    }

    private void validate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        validateActivationDate(dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateReactivate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        validateReactivationDate(dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateClosureDate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        validateClosureDate(dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateActivationDate(final List<ApiParameterError> dataValidationErrors) {
        if (getSubmittedOnDate() != null && DateUtils.isDateInTheFuture(getSubmittedOnDate())) {
            final String defaultUserMessage = "submitted date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.in.the.future",
                    defaultUserMessage, EmailCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }
        if (getApprovedOnDate() != null && DateUtils.isAfter(getSubmittedOnDate(), getApprovedOnDate())) {
            final String defaultUserMessage = "submitted date cannot be after the activation date";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.after.activation.date",
                    defaultUserMessage, EmailCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }
        if (DateUtils.isDateInTheFuture(getApprovedOnDate())) {
            final String defaultUserMessage = "Activation date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.activationDate.in.the.future",
                    defaultUserMessage, EmailCampaignValidator.activationDateParamName, getApprovedOnDate());

            dataValidationErrors.add(error);
        }
    }

    private void validateReactivationDate(final List<ApiParameterError> dataValidationErrors) {
        if (DateUtils.isDateInTheFuture(getApprovedOnDate())) {
            final String defaultUserMessage = "Activation date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.activationDate.in.the.future",
                    defaultUserMessage, EmailCampaignValidator.activationDateParamName, getApprovedOnDate());

            dataValidationErrors.add(error);
        }
        if (getApprovedOnDate() != null && DateUtils.isAfter(getSubmittedOnDate(), getApprovedOnDate())) {
            final String defaultUserMessage = "submitted date cannot be after the activation date";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.after.activation.date",
                    defaultUserMessage, EmailCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }
        if (DateUtils.isDateInTheFuture(getSubmittedOnDate())) {
            final String defaultUserMessage = "submitted date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.submittedOnDate.in.the.future",
                    defaultUserMessage, EmailCampaignValidator.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }

    }

    private void validateClosureDate(final List<ApiParameterError> dataValidationErrors) {
        if (getClosureDate() != null && DateUtils.isDateInTheFuture(getClosureDate())) {
            final String defaultUserMessage = "closure date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.campaign.closureDate.in.the.future",
                    defaultUserMessage, EmailCampaignValidator.closureDateParamName, this.closureDate);

            dataValidationErrors.add(error);
        }
    }
}
