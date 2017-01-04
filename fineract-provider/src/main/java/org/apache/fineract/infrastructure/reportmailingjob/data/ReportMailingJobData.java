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
package org.apache.fineract.infrastructure.reportmailingjob.data;

import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.joda.time.DateTime;

/** 
 * Immutable data object representing report mailing job data. 
 **/
public class ReportMailingJobData {
    private final Long id;
    private final String name;
    private final String description;
    private final DateTime startDateTime;
    private final String recurrence;
    private final ReportMailingJobTimelineData timeline;
    private final String emailRecipients;
    private final String emailSubject;
    private final String emailMessage;
    private final EnumOptionData emailAttachmentFileFormat;
    private final ReportData stretchyReport;
    private final String stretchyReportParamMap;
    private final DateTime previousRunDateTime;
    private final DateTime nextRunDateTime;
    private final String previousRunStatus;
    private final String previousRunErrorLog;
    private final String previousRunErrorMessage;
    private final Integer numberOfRuns;
    private final boolean isActive;
    private final List<EnumOptionData> emailAttachmentFileFormatOptions;
    private final List<EnumOptionData> stretchyReportParamDateOptions;
    private final Long runAsUserId;
    
    private ReportMailingJobData(final Long id, final String name, final String description, final DateTime startDateTime, 
            final String recurrence, final ReportMailingJobTimelineData timeline, final String emailRecipients, final String emailSubject, 
            final String emailMessage, final EnumOptionData emailAttachmentFileFormat, final ReportData stretchyReport, final String stretchyReportParamMap, 
            final DateTime previousRunDateTime, final DateTime nextRunDateTime, final String previousRunStatus, final String previousRunErrorLog, 
            final String previousRunErrorMessage, final Integer numberOfRuns, final boolean isActive, final List<EnumOptionData> emailAttachmentFileFormatOptions, 
            final List<EnumOptionData> stretchyReportParamDateOptions, final Long runAsUserId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDateTime = startDateTime;
        this.recurrence = recurrence;
        this.timeline = timeline;
        this.emailRecipients = emailRecipients;
        this.emailMessage = emailMessage;
        this.emailSubject = emailSubject;
        this.emailAttachmentFileFormat = emailAttachmentFileFormat;
        this.stretchyReport = stretchyReport;
        this.stretchyReportParamMap = stretchyReportParamMap;
        this.previousRunDateTime = previousRunDateTime;
        this.nextRunDateTime = nextRunDateTime;
        this.previousRunStatus = previousRunStatus;
        this.previousRunErrorLog = previousRunErrorLog;
        this.previousRunErrorMessage = previousRunErrorMessage;
        this.isActive = isActive;
        this.numberOfRuns = numberOfRuns;
        this.emailAttachmentFileFormatOptions = emailAttachmentFileFormatOptions;
        this.stretchyReportParamDateOptions = stretchyReportParamDateOptions;
        this.runAsUserId = runAsUserId;
    }
    
    /** 
     * @return an instance of the ReportMailingJobData class 
     **/
    public static ReportMailingJobData newInstance(final Long id, final String name, final String description, final DateTime startDateTime, 
            final String recurrence, final ReportMailingJobTimelineData timeline, final String emailRecipients, final String emailSubject, 
            final String emailMessage, final EnumOptionData emailAttachmentFileFormat, final ReportData stretchyReport, final String stretchyReportParamMap, 
            final DateTime previousRunDateTime, final DateTime nextRunDateTime, final String previousRunStatus, final String previousRunErrorLog, 
            final String previousRunErrorMessage, final Integer numberOfRuns, final boolean isActive, final Long runAsUserId) {
        return new ReportMailingJobData(id, name, description, startDateTime, recurrence, timeline, emailRecipients, emailSubject, 
                emailMessage, emailAttachmentFileFormat, stretchyReport, stretchyReportParamMap, previousRunDateTime, nextRunDateTime, 
                previousRunStatus, previousRunErrorLog, previousRunErrorMessage, numberOfRuns, isActive, null, null, runAsUserId);
    }
    
    /** 
     * @return an instance of the ReportMailingJobData class 
     **/
    public static ReportMailingJobData newInstance(final List<EnumOptionData> emailAttachmentFileFormatOptions, 
            final List<EnumOptionData> stretchyReportParamDateOptions) {
        return new ReportMailingJobData(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
                null, false, emailAttachmentFileFormatOptions, stretchyReportParamDateOptions, null);
    }
    
    /** 
     * @return an instance of the ReportMailingJobData class 
     **/
    public static ReportMailingJobData newInstance(final ReportMailingJobData dataWithoutEnumOptions, final ReportMailingJobData dataWithEnumOptions) {
        return new ReportMailingJobData(dataWithoutEnumOptions.id, dataWithoutEnumOptions.name, dataWithoutEnumOptions.description, dataWithoutEnumOptions.startDateTime, 
                dataWithoutEnumOptions.recurrence, dataWithoutEnumOptions.timeline, dataWithoutEnumOptions.emailRecipients, dataWithoutEnumOptions.emailSubject, 
                dataWithoutEnumOptions.emailMessage, dataWithoutEnumOptions.emailAttachmentFileFormat, dataWithoutEnumOptions.stretchyReport, 
                dataWithoutEnumOptions.stretchyReportParamMap, dataWithoutEnumOptions.previousRunDateTime, dataWithoutEnumOptions.nextRunDateTime, 
                dataWithoutEnumOptions.previousRunStatus, dataWithoutEnumOptions.previousRunErrorLog, dataWithoutEnumOptions.previousRunErrorMessage, 
                dataWithoutEnumOptions.numberOfRuns, dataWithoutEnumOptions.isActive, dataWithEnumOptions.emailAttachmentFileFormatOptions, 
                dataWithEnumOptions.stretchyReportParamDateOptions, dataWithoutEnumOptions.runAsUserId);
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the startDateTime
     */
    public DateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * @return the recurrence
     */
    public String getRecurrence() {
        return recurrence;
    }

    /**
     * @return the timeline
     */
    public ReportMailingJobTimelineData getTimeline() {
        return timeline;
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
    public EnumOptionData getEmailAttachmentFileFormat() {
        return emailAttachmentFileFormat;
    }

    /**
     * @return the stretchyReport
     */
    public ReportData getStretchyReport() {
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
        return previousRunDateTime;
    }

    /**
     * @return the nextRunDateTime
     */
    public DateTime getNextRunDateTime() {
        return nextRunDateTime;
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
     * @return the isActive
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * @return the emailAttachmentFileFormatOptions
     */
    public List<EnumOptionData> getEmailAttachmentFileFormatOptions() {
        return emailAttachmentFileFormatOptions;
    }
    
    /** 
     * @return the stretchyReportParamDateOptions
     **/
    public List<EnumOptionData> getStretchyReportParamDateOptions() {
        return this.stretchyReportParamDateOptions;
    }

    /**
     * @return the numberOfRuns
     */
    public Integer getNumberOfRuns() {
        return numberOfRuns;
    }

    /**
     * @return the runAsUserId
     */
    public Long getRunAsUserId() {
        return runAsUserId;
    }
}
