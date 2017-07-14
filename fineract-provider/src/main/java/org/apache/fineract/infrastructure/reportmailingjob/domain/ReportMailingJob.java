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
package org.apache.fineract.infrastructure.reportmailingjob.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.reportmailingjob.ReportMailingJobConstants;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobEmailAttachmentFileFormat;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobPreviousRunStatus;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Entity
@Table(name = "m_report_mailing_job", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unique_name") })
public class ReportMailingJob extends AbstractAuditableCustom<AppUser, Long> {
    private static final long serialVersionUID = -2197602941230009227L;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", nullable = true)
    private String description;
    
    @Column(name = "start_datetime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDateTime;
    
    @Column(name = "recurrence", nullable = true)
    private String recurrence;
    
    @Column(name = "email_recipients", nullable = false)
    private String emailRecipients;
    
    @Column(name = "email_subject", nullable = false)
    private String emailSubject;
    
    @Column(name = "email_message", nullable = false)
    private String emailMessage;
    
    @Column(name = "email_attachment_file_format", nullable = false)
    private String emailAttachmentFileFormat;
    
    @ManyToOne
    @JoinColumn(name = "stretchy_report_id", nullable = false)
    private Report stretchyReport;
    
    @Column(name = "stretchy_report_param_map", nullable = true)
    private String stretchyReportParamMap;
    
    @Column(name = "previous_run_datetime", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date previousRunDateTime;
    
    @Column(name = "next_run_datetime", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextRunDateTime;
    
    @Column(name = "previous_run_status", nullable = true)
    private String previousRunStatus;
    
    @Column(name = "previous_run_error_log", nullable = true)
    private String previousRunErrorLog;
    
    @Column(name = "previous_run_error_message", nullable = true)
    private String previousRunErrorMessage;
    
    @Column(name = "number_of_runs", nullable = false)
    private Integer numberOfRuns;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "run_as_userid", nullable = false)
    private AppUser runAsUser;
    
    /** 
     * ReportMailingJob protected constructor
     **/
    protected ReportMailingJob() { }
    
    /** 
     * ReportMailingJob private constructor 
     **/
    private ReportMailingJob(final String name, final String description, final LocalDateTime startDateTime, 
            final String recurrence, final String emailRecipients, final String emailSubject, 
            final String emailMessage, final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat, 
            final Report stretchyReport, final String stretchyReportParamMap, final LocalDateTime previousRunDateTime, final LocalDateTime nextRunDateTime, 
            final ReportMailingJobPreviousRunStatus previousRunStatus, final String previousRunErrorLog, final String previousRunErrorMessage, 
            final boolean isActive, final boolean isDeleted, final AppUser runAsUser) { 
        this.name = name;
        this.description = description;
        this.startDateTime = null;
        
        if (startDateTime != null) {
            this.startDateTime = startDateTime.toDate();
        }
        
        this.recurrence = recurrence;
        this.emailRecipients = emailRecipients;
        this.emailSubject = emailSubject;
        this.emailMessage = emailMessage;
        this.emailAttachmentFileFormat = emailAttachmentFileFormat.getValue();
        this.stretchyReport = stretchyReport;
        this.stretchyReportParamMap = stretchyReportParamMap;
        this.previousRunDateTime = null;
        
        if (previousRunDateTime != null) {
            this.previousRunDateTime = previousRunDateTime.toDate();
        }
        
        this.nextRunDateTime = null;
        
        if (nextRunDateTime != null) {
            this.nextRunDateTime = nextRunDateTime.toDate();
        }
        
        this.previousRunStatus = null;
        
        if (previousRunStatus != null) {
            this.previousRunStatus = previousRunStatus.getValue();
        }
        
        if (numberOfRuns == null) {
            this.numberOfRuns = 0;
        }
        
        this.previousRunErrorLog = previousRunErrorLog;
        this.previousRunErrorMessage = previousRunErrorMessage;
        this.isActive = isActive;
        this.isDeleted = isDeleted;
        this.runAsUser = runAsUser;
    }
    
    /** 
     * create a new instance of the ReportMailingJob for a new entry
     * 
     * @return ReportMailingJob object
     **/
    public static ReportMailingJob newInstance(final String name, final String description, final LocalDateTime startDateTime, final String recurrence, 
            final String emailRecipients, final String emailSubject, final String emailMessage, 
            final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat, final Report stretchyReport, final String stretchyReportParamMap, 
            final boolean isActive, final AppUser runAsUser) {
        return new ReportMailingJob(name, description, startDateTime, recurrence, emailRecipients, emailSubject, 
                emailMessage, emailAttachmentFileFormat, stretchyReport, stretchyReportParamMap, null, null, null, null, null, isActive, false, runAsUser);
    }
    
    /** 
     * create a new instance of the ReportmailingJob for a new entry 
     * 
     * @return ReportMailingJob object
     **/
    public static ReportMailingJob newInstance(JsonCommand jsonCommand, final Report stretchyReport, 
            final AppUser runAsUser) {
        final String name = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.NAME_PARAM_NAME);
        final String description = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.DESCRIPTION_PARAM_NAME);
        final String recurrence = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.RECURRENCE_PARAM_NAME);
        final boolean isActive = jsonCommand.booleanPrimitiveValueOfParameterNamed(ReportMailingJobConstants.IS_ACTIVE_PARAM_NAME);
        final String emailRecipients = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_RECIPIENTS_PARAM_NAME);
        final String emailSubject = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_SUBJECT_PARAM_NAME);
        final String emailMessage = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_MESSAGE_PARAM_NAME);
        final String stretchyReportParamMap = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.STRETCHY_REPORT_PARAM_MAP_PARAM_NAME);
        final Integer emailAttachmentFileFormatId = jsonCommand.integerValueOfParameterNamed(ReportMailingJobConstants.EMAIL_ATTACHMENT_FILE_FORMAT_ID_PARAM_NAME);
        final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat = ReportMailingJobEmailAttachmentFileFormat.newInstance(emailAttachmentFileFormatId);
        LocalDateTime startDateTime = new LocalDateTime();
        
        if (jsonCommand.hasParameter(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME)) {
            final String startDateTimeString = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME);
            
            if (startDateTimeString != null) {
                final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(jsonCommand.dateFormat()).withLocale(jsonCommand.extractLocale());
                startDateTime = LocalDateTime.parse(startDateTimeString, dateTimeFormatter); 
            }
        }
        
        return new ReportMailingJob(name, description, startDateTime, recurrence, emailRecipients, emailSubject, 
                emailMessage, emailAttachmentFileFormat, stretchyReport, stretchyReportParamMap, null, startDateTime, null, null, null, isActive, false, runAsUser);
    }
    
    /** 
     * Update the ReportMailingJob entity 
     * 
     * @param jsonCommand JsonCommand object
     * @return map of string to object
     **/
    public Map<String, Object> update(final JsonCommand jsonCommand) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();
        
        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.NAME_PARAM_NAME, this.name)) {
            final String name = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.NAME_PARAM_NAME);
            actualChanges.put(ReportMailingJobConstants.NAME_PARAM_NAME, name);
            
            this.name = name;
        }
        
        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.DESCRIPTION_PARAM_NAME, this.description)) {
            final String description = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.DESCRIPTION_PARAM_NAME);
            actualChanges.put(ReportMailingJobConstants.DESCRIPTION_PARAM_NAME, description);
            
            this.description = description;
        }
        
        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.RECURRENCE_PARAM_NAME, this.recurrence)) {
            final String recurrence = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.RECURRENCE_PARAM_NAME);
            actualChanges.put(ReportMailingJobConstants.RECURRENCE_PARAM_NAME, recurrence);
            
            this.recurrence = recurrence;
        }
        
        if (jsonCommand.isChangeInBooleanParameterNamed(ReportMailingJobConstants.IS_ACTIVE_PARAM_NAME, this.isActive)) {
            final boolean isActive = jsonCommand.booleanPrimitiveValueOfParameterNamed(ReportMailingJobConstants.IS_ACTIVE_PARAM_NAME);
            actualChanges.put(ReportMailingJobConstants.IS_ACTIVE_PARAM_NAME, isActive);
            
            this.isActive = isActive;
        }
        
        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.EMAIL_RECIPIENTS_PARAM_NAME, this.emailRecipients)) {
            final String emailRecipients = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_RECIPIENTS_PARAM_NAME);
            actualChanges.put(ReportMailingJobConstants.EMAIL_RECIPIENTS_PARAM_NAME, emailRecipients);
            
            this.emailRecipients = emailRecipients;
        }
        
        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.EMAIL_SUBJECT_PARAM_NAME, this.emailSubject)) {
            final String emailSubject = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_SUBJECT_PARAM_NAME);
            actualChanges.put(ReportMailingJobConstants.EMAIL_SUBJECT_PARAM_NAME, emailSubject);
            
            this.emailSubject = emailSubject;
        }
        
        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.EMAIL_MESSAGE_PARAM_NAME, this.emailMessage)) {
            final String emailMessage = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_MESSAGE_PARAM_NAME);
            actualChanges.put(ReportMailingJobConstants.EMAIL_MESSAGE_PARAM_NAME, emailMessage);
            
            this.emailMessage = emailMessage;
        }
        
        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.STRETCHY_REPORT_PARAM_MAP_PARAM_NAME, this.stretchyReportParamMap)) {
            final String stretchyReportParamMap = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.STRETCHY_REPORT_PARAM_MAP_PARAM_NAME);
            actualChanges.put(ReportMailingJobConstants.STRETCHY_REPORT_PARAM_MAP_PARAM_NAME, stretchyReportParamMap);
            
            this.stretchyReportParamMap = stretchyReportParamMap;
        }
        
        final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat = ReportMailingJobEmailAttachmentFileFormat.newInstance(this.emailAttachmentFileFormat);
        
        if (jsonCommand.isChangeInIntegerParameterNamed(ReportMailingJobConstants.EMAIL_ATTACHMENT_FILE_FORMAT_ID_PARAM_NAME, emailAttachmentFileFormat.getId())) {
            final Integer emailAttachmentFileFormatId = jsonCommand.integerValueOfParameterNamed(ReportMailingJobConstants.EMAIL_ATTACHMENT_FILE_FORMAT_ID_PARAM_NAME);
            actualChanges.put(ReportMailingJobConstants.EMAIL_ATTACHMENT_FILE_FORMAT_ID_PARAM_NAME, emailAttachmentFileFormatId);
            
            final ReportMailingJobEmailAttachmentFileFormat newEmailAttachmentFileFormat = ReportMailingJobEmailAttachmentFileFormat.newInstance(emailAttachmentFileFormatId);
            this.emailAttachmentFileFormat = newEmailAttachmentFileFormat.getValue();
        }
        
        final String newStartDateTimeString = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME);
        
        if (!StringUtils.isEmpty(newStartDateTimeString)) {
            final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(jsonCommand.dateFormat()).withLocale(jsonCommand.extractLocale());
            final LocalDateTime newStartDateTime = LocalDateTime.parse(newStartDateTimeString, dateTimeFormatter);
            final LocalDateTime oldStartDateTime = (this.startDateTime != null) ? new LocalDateTime(this.startDateTime) : null;
            
            if ((oldStartDateTime != null) && !newStartDateTime.equals(oldStartDateTime)) {
                actualChanges.put(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME, newStartDateTimeString);
                
                this.startDateTime = newStartDateTime.toDate();
            }
        }
        
        Long currentStretchyReportId = null;
        
        if (this.stretchyReport != null) {
            currentStretchyReportId = this.stretchyReport.getId();
        }
        
        if (jsonCommand.isChangeInLongParameterNamed(ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME, currentStretchyReportId)) {
            final Long updatedStretchyReportId = jsonCommand.longValueOfParameterNamed(
                    ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME);
            actualChanges.put(ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME, updatedStretchyReportId);
        }
        
        return actualChanges;
    }
    
    /** 
     * update the stretchy report entity associated with the credit check 
     * 
     * @param stretchyReport -- Report entity
     * 
     **/
    public void update(final Report stretchyReport) {
        if (stretchyReport != null) {
            this.stretchyReport = stretchyReport;
        }
    }
    
    /** 
     * delete the report mailing job, set the isDeleted property to 1 and alter the name 
     * 
     * 
     **/
    public void delete() {
        this.isDeleted = true;
        this.isActive = false;
        this.name = this.name + "_deleted_" + this.getId();
    }
    
    /** 
     * @return the value of the name property 
     **/
    public String getName() {
        return this.name;
    }
    
    /** 
     * @return the value of the description property 
     **/
    public String getDescription() {
        return this.description;
    }
    
    /** 
     * @return the value of the startDateTime property 
     **/
    public DateTime getStartDateTime() {
        return (this.startDateTime != null) ? new DateTime(this.startDateTime) : null;
    }
    
    /** 
     * @return the value of the recurrence property 
     **/
    public String getRecurrence() {
        return this.recurrence;
    }
    
    /** 
     * @return value of the isDeleted property 
     **/
    public boolean isDeleted() {
        return this.isDeleted;
    }
    
    /** 
     * @return boolean true if isDeleted property equals 0, else false 
     **/
    public boolean isNotDeleted() {
        return !this.isDeleted;
    }
    
    /** 
     * @return the value of the isActive property 
     **/
    public boolean isActive() {
        return this.isActive;
    }
    
    /** 
     * @return boolean true if isActive property equals 0, else false 
     **/
    public boolean isNotActive() {
        return !this.isActive;
    }

    /**
     * @return the emailRecipients
     */
    public String getEmailRecipients() {
        return emailRecipients;
    }

    /**
     * @return the emailSubject
     */
    public String getEmailSubject() {
        return emailSubject;
    }

    /**
     * @return the emailMessage
     */
    public String getEmailMessage() {
        return emailMessage;
    }

    /**
     * @return the emailAttachmentFileFormat
     */
    public String getEmailAttachmentFileFormat() {
        return emailAttachmentFileFormat;
    }

    /**
     * @return the stretchyReport
     */
    public Report getStretchyReport() {
        return stretchyReport;
    }

    /**
     * @return the stretchyReportParamMap
     */
    public String getStretchyReportParamMap() {
        return stretchyReportParamMap;
    }

    /**
     * @return the previousRunDateTime
     */
    public DateTime getPreviousRunDateTime() {
        return (this.previousRunDateTime != null) ? new DateTime(this.previousRunDateTime) : null;
    }

    /**
     * @return the nextRunDateTime
     */
    public DateTime getNextRunDateTime() {
        return (this.nextRunDateTime != null) ? new DateTime(this.nextRunDateTime) : null;
    }

    /**
     * @return the previousRunStatus
     */
    public String getPreviousRunStatus() {
        return previousRunStatus;
    }

    /**
     * @return the previousRunErrorLog
     */
    public String getPreviousRunErrorLog() {
        return previousRunErrorLog;
    }

    /**
     * @return the previousRunErrorMessage
     */
    public String getPreviousRunErrorMessage() {
        return previousRunErrorMessage;
    }

    /**
     * @return the numberOfRuns
     */
    public Integer getNumberOfRuns() {
        return numberOfRuns;
    }

    /**
     * @return the runAsUser
     */
    public AppUser getRunAsUser() {
        return runAsUser;
    }
    
    /** 
     * increase the numberOfRuns by 1 
     * 
     * 
     **/
    public void increaseNumberOfRunsByOne() {
        this.numberOfRuns++;
    }
    
    /** 
     * update the previousRunStatus 
     * 
     * @param previousRunStatus -- the status of the previous job execution
     * 
     **/
    public void updatePreviousRunStatus(final String previousRunStatus) {
        if (!StringUtils.isEmpty(previousRunStatus)) {
            this.previousRunStatus = previousRunStatus;
        }
    }
    
    /** 
     * update the previousRunDateTime
     * 
     * @param previousRunDateTime -- previous run date
     * 
     **/
    public void updatePreviousRunDateTime(final DateTime previousRunDateTime) {
        if (previousRunDateTime != null) {
            this.previousRunDateTime = previousRunDateTime.toDate();
        }
    }
    
    /** 
     * update the nextRunDateTime
     * 
     * @param nextRunDateTime -- the next run DateTime
     * 
     **/
    public void updateNextRunDateTime(final DateTime nextRunDateTime) {
        if (nextRunDateTime != null) {
            this.nextRunDateTime = nextRunDateTime.toDate();
        }
        
        else {
            this.nextRunDateTime = null;
        }
    }
    
    /** 
     * deactivate the report mailing job by setting the isActive property to 0 
     * 
     * 
     **/
    public void deactivate() {
        this.isActive = false;
    }
    
    /** 
     * update the previousRunErrorLog property
     * 
     * @param previousRunErrorLog -- the previous job run error log
     * 
     **/
    public void updatePreviousRunErrorLog(final String previousRunErrorLog) {
        this.previousRunErrorLog = previousRunErrorLog;
    }
}
