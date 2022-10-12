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

import java.time.ZonedDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;

/**
 * Immutable data object representing report mailing job data.
 **/
@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class ReportMailingJobData {

    private Long id;
    private String name;
    private String description;
    private ZonedDateTime startDateTime;
    private String recurrence;
    private ReportMailingJobTimelineData timeline;
    private String emailRecipients;
    private String emailSubject;
    private String emailMessage;
    private EnumOptionData emailAttachmentFileFormat;
    private ReportData stretchyReport;
    private String stretchyReportParamMap;
    private ZonedDateTime previousRunDateTime;
    private ZonedDateTime nextRunDateTime;
    private String previousRunStatus;
    private String previousRunErrorLog;
    private String previousRunErrorMessage;
    private Integer numberOfRuns;
    private boolean isActive;
    private List<EnumOptionData> emailAttachmentFileFormatOptions;
    private List<EnumOptionData> stretchyReportParamDateOptions;
    private Long runAsUserId;

    /**
     * @return an instance of the ReportMailingJobData class
     **/
    public static ReportMailingJobData newInstance(final Long id, final String name, final String description,
            final ZonedDateTime startDateTime, final String recurrence, final ReportMailingJobTimelineData timeline,
            final String emailRecipients, final String emailSubject, final String emailMessage,
            final EnumOptionData emailAttachmentFileFormat, final ReportData stretchyReport, final String stretchyReportParamMap,
            final ZonedDateTime previousRunDateTime, final ZonedDateTime nextRunDateTime, final String previousRunStatus,
            final String previousRunErrorLog, final String previousRunErrorMessage, final Integer numberOfRuns, final boolean isActive,
            final Long runAsUserId) {
        return new ReportMailingJobData().setId(id).setName(name).setDescription(description).setStartDateTime(startDateTime)
                .setRecurrence(recurrence).setTimeline(timeline).setEmailRecipients(emailRecipients).setEmailSubject(emailSubject)
                .setEmailMessage(emailMessage).setEmailAttachmentFileFormat(emailAttachmentFileFormat).setStretchyReport(stretchyReport)
                .setStretchyReportParamMap(stretchyReportParamMap).setPreviousRunDateTime(previousRunDateTime)
                .setNextRunDateTime(nextRunDateTime).setPreviousRunStatus(previousRunStatus).setPreviousRunErrorLog(previousRunErrorLog)
                .setPreviousRunErrorMessage(previousRunErrorMessage).setNumberOfRuns(numberOfRuns).setActive(isActive)
                .setRunAsUserId(runAsUserId);
    }

    /**
     * @return an instance of the ReportMailingJobData class
     **/
    public static ReportMailingJobData newInstance(final List<EnumOptionData> emailAttachmentFileFormatOptions,
            final List<EnumOptionData> stretchyReportParamDateOptions) {
        return new ReportMailingJobData().setEmailAttachmentFileFormatOptions(emailAttachmentFileFormatOptions)
                .setStretchyReportParamDateOptions(stretchyReportParamDateOptions);
    }

    /**
     * @return an instance of the ReportMailingJobData class
     **/
    public static ReportMailingJobData newInstance(final ReportMailingJobData dataWithoutEnumOptions,
            final ReportMailingJobData dataWithEnumOptions) {
        return new ReportMailingJobData().setId(dataWithoutEnumOptions.id).setName(dataWithoutEnumOptions.name)
                .setDescription(dataWithoutEnumOptions.description).setStartDateTime(dataWithoutEnumOptions.startDateTime)
                .setRecurrence(dataWithoutEnumOptions.recurrence).setTimeline(dataWithoutEnumOptions.timeline)
                .setEmailRecipients(dataWithoutEnumOptions.emailRecipients).setEmailSubject(dataWithoutEnumOptions.emailSubject)
                .setEmailMessage(dataWithoutEnumOptions.emailMessage)
                .setEmailAttachmentFileFormat(dataWithoutEnumOptions.emailAttachmentFileFormat)
                .setStretchyReport(dataWithoutEnumOptions.stretchyReport)
                .setStretchyReportParamMap(dataWithoutEnumOptions.stretchyReportParamMap)
                .setPreviousRunDateTime(dataWithoutEnumOptions.previousRunDateTime)
                .setNextRunDateTime(dataWithoutEnumOptions.nextRunDateTime).setPreviousRunStatus(dataWithoutEnumOptions.previousRunStatus)
                .setPreviousRunErrorLog(dataWithoutEnumOptions.previousRunErrorLog)
                .setPreviousRunErrorMessage(dataWithoutEnumOptions.previousRunErrorMessage)
                .setNumberOfRuns(dataWithoutEnumOptions.numberOfRuns).setActive(dataWithoutEnumOptions.isActive)
                .setEmailAttachmentFileFormatOptions(dataWithEnumOptions.emailAttachmentFileFormatOptions)
                .setStretchyReportParamDateOptions(dataWithEnumOptions.stretchyReportParamDateOptions)
                .setRunAsUserId(dataWithoutEnumOptions.runAsUserId);
    }
}
