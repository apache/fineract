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
package org.apache.fineract.infrastructure.reportmailingjob.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportRepositoryWrapper;
import org.apache.fineract.infrastructure.reportmailingjob.ReportMailingJobConstants;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobEmailAttachmentFileFormat;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJob;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRepository;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRepositoryWrapper;
import org.apache.fineract.infrastructure.reportmailingjob.validation.ReportMailingJobValidator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportMailingJobWritePlatformServiceImpl implements ReportMailingJobWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportMailingJobWritePlatformServiceImpl.class);
    private final ReportRepositoryWrapper reportRepositoryWrapper;
    private final ReportMailingJobValidator reportMailingJobValidator;
    private final ReportMailingJobRepositoryWrapper reportMailingJobRepositoryWrapper;
    private final ReportMailingJobRepository reportMailingJobRepository;
    private final PlatformSecurityContext platformSecurityContext;

    @Autowired
    public ReportMailingJobWritePlatformServiceImpl(final ReportRepositoryWrapper reportRepositoryWrapper,
            final ReportMailingJobValidator reportMailingJobValidator,
            final ReportMailingJobRepositoryWrapper reportMailingJobRepositoryWrapper,
            final PlatformSecurityContext platformSecurityContext) {
        this.reportRepositoryWrapper = reportRepositoryWrapper;
        this.reportMailingJobValidator = reportMailingJobValidator;
        this.reportMailingJobRepositoryWrapper = reportMailingJobRepositoryWrapper;
        this.reportMailingJobRepository = reportMailingJobRepositoryWrapper.getReportMailingJobRepository();
        this.platformSecurityContext = platformSecurityContext;
    }

    @Override
    @Transactional
    public CommandProcessingResult createReportMailingJob(JsonCommand jsonCommand) {
        try {
            // validate the create request
            this.reportMailingJobValidator.validateCreateRequest(jsonCommand);

            final AppUser appUser = this.platformSecurityContext.authenticatedUser();

            // get the stretchy Report object
            final Report stretchyReport = this.reportRepositoryWrapper.findOneThrowExceptionIfNotFound(
                    jsonCommand.longValueOfParameterNamed(ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME));

            // create an instance of ReportMailingJob class from the JsonCommand
            // object
            final ReportMailingJob reportMailingJob = newReportMailingJob(jsonCommand, stretchyReport, appUser);

            // save entity
            this.reportMailingJobRepository.saveAndFlush(reportMailingJob);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(reportMailingJob.getId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleDataIntegrityIssues(jsonCommand, throwable, dve);

            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateReportMailingJob(Long reportMailingJobId, JsonCommand jsonCommand) {
        try {
            // validate the update request
            this.reportMailingJobValidator.validateUpdateRequest(jsonCommand);

            // retrieve the ReportMailingJob object from the database
            final ReportMailingJob reportMailingJob = this.reportMailingJobRepositoryWrapper
                    .findOneThrowExceptionIfNotFound(reportMailingJobId);

            final Map<String, Object> changes = applyChanges(reportMailingJob, jsonCommand);

            // get the recurrence rule string
            final String recurrence = reportMailingJob.getRecurrence();

            // get the next run LocalDateTime from the ReportMailingJob entity
            LocalDateTime nextRunDateTime = reportMailingJob.getNextRunDateTime();

            // check if the stretchy report id was updated
            if (changes.containsKey(ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME)) {
                final Long stretchyReportId = (Long) changes.get(ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME);
                final Report stretchyReport = this.reportRepositoryWrapper.findOneThrowExceptionIfNotFound(stretchyReportId);

                // update the stretchy report
                reportMailingJob.setStretchyReport(stretchyReport);
            }

            // check if the recurrence was updated
            if (changes.containsKey(ReportMailingJobConstants.RECURRENCE_PARAM_NAME)) {

                // go ahead if the recurrence is not null
                if (StringUtils.isNotBlank(recurrence)) {
                    // set the start LocalDateTime to the current tenant date time
                    LocalDateTime startDateTime = DateUtils.getLocalDateTimeOfTenant();

                    // check if the start LocalDateTime was updated
                    if (changes.containsKey(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME)) {
                        // get the updated start DateTime
                        startDateTime = reportMailingJob.getStartDateTime();
                    }

                    startDateTime = reportMailingJob.getStartDateTime();

                    // get the next recurring DateTime
                    final LocalDateTime nextRecurringDateTime = this.createNextRecurringDateTime(recurrence, startDateTime);

                    // update the next run time property
                    reportMailingJob.setNextRunDateTime(nextRecurringDateTime);

                    // check if the next run LocalDateTime is not empty and the
                    // recurrence is empty
                } else if (StringUtils.isBlank(recurrence) && (nextRunDateTime != null)) {
                    // the next run LocalDateTime should be set to null
                    reportMailingJob.setNextRunDateTime(null);
                }
            }

            if (changes.containsKey(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME)) {
                final LocalDateTime startDateTime = reportMailingJob.getStartDateTime();

                // initially set the next recurring date time to the new start
                // date time
                LocalDateTime nextRecurringDateTime = startDateTime;

                // ensure that the recurrence pattern string is not empty
                if (StringUtils.isNotBlank(recurrence)) {
                    // get the next recurring DateTime
                    nextRecurringDateTime = this.createNextRecurringDateTime(recurrence, startDateTime);
                }

                // update the next run time property
                reportMailingJob.setNextRunDateTime(nextRecurringDateTime);
            }

            if (!changes.isEmpty()) {
                // save and flush immediately so any data integrity exception
                // can be handled in the "catch" block
                this.reportMailingJobRepository.saveAndFlush(reportMailingJob);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(reportMailingJob.getId()) //
                    .with(changes) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleDataIntegrityIssues(jsonCommand, throwable, dve);

            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteReportMailingJob(Long reportMailingJobId) {
        // retrieve the ReportMailingJob object from the database
        final ReportMailingJob reportMailingJob = this.reportMailingJobRepositoryWrapper
                .findOneThrowExceptionIfNotFound(reportMailingJobId);

        // delete the report mailing job by setting the isDeleted property to 1
        // and altering the name
        reportMailingJob.delete();

        // save the report mailing job entity
        this.reportMailingJobRepository.save(reportMailingJob);

        return new CommandProcessingResultBuilder() //
                .withEntityId(reportMailingJobId) //
                .build();
    }

    private static ReportMailingJob newReportMailingJob(final JsonCommand jsonCommand, final Report stretchyReport,
            final AppUser runAsUser) {
        final String name = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.NAME_PARAM_NAME);
        final String description = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.DESCRIPTION_PARAM_NAME);
        final String recurrence = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.RECURRENCE_PARAM_NAME);
        final boolean isActive = jsonCommand.booleanPrimitiveValueOfParameterNamed(ReportMailingJobConstants.IS_ACTIVE_PARAM_NAME);
        final String emailRecipients = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_RECIPIENTS_PARAM_NAME);
        final String emailSubject = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_SUBJECT_PARAM_NAME);
        final String emailMessage = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_MESSAGE_PARAM_NAME);
        final String stretchyReportParamMap = jsonCommand
                .stringValueOfParameterNamed(ReportMailingJobConstants.STRETCHY_REPORT_PARAM_MAP_PARAM_NAME);
        final Integer emailAttachmentFileFormatId = jsonCommand
                .integerValueOfParameterNamed(ReportMailingJobConstants.EMAIL_ATTACHMENT_FILE_FORMAT_ID_PARAM_NAME);
        final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat = ReportMailingJobEmailAttachmentFileFormat
                .newInstance(emailAttachmentFileFormatId);
        LocalDateTime startDateTime = LocalDateTime.now(DateUtils.getDateTimeZoneOfTenant());

        if (jsonCommand.hasParameter(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME)) {
            final String startDateTimeString = jsonCommand
                    .stringValueOfParameterNamed(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME);

            if (startDateTimeString != null) {
                final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(jsonCommand.dateFormat())
                        .withLocale(jsonCommand.extractLocale());
                startDateTime = LocalDateTime.parse(startDateTimeString, dateTimeFormatter);
            }
        }

        // on create, the next run is the start date time
        return ReportMailingJob
                .newInstance(name, description, startDateTime, recurrence, emailRecipients, emailSubject, emailMessage,
                        emailAttachmentFileFormat, stretchyReport, stretchyReportParamMap, isActive, runAsUser)
                .setNextRunDateTime(startDateTime);
    }

    private static Map<String, Object> applyChanges(final ReportMailingJob reportMailingJob, final JsonCommand jsonCommand) {
        final Map<String, Object> changes = new LinkedHashMap<>();

        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.NAME_PARAM_NAME, reportMailingJob.getName())) {
            final String name = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.NAME_PARAM_NAME);
            changes.put(ReportMailingJobConstants.NAME_PARAM_NAME, name);
            reportMailingJob.setName(name);
        }

        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.DESCRIPTION_PARAM_NAME,
                reportMailingJob.getDescription())) {
            final String description = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.DESCRIPTION_PARAM_NAME);
            changes.put(ReportMailingJobConstants.DESCRIPTION_PARAM_NAME, description);
            reportMailingJob.setDescription(description);
        }

        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.RECURRENCE_PARAM_NAME, reportMailingJob.getRecurrence())) {
            final String recurrence = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.RECURRENCE_PARAM_NAME);
            changes.put(ReportMailingJobConstants.RECURRENCE_PARAM_NAME, recurrence);
            reportMailingJob.setRecurrence(recurrence);
        }

        if (jsonCommand.isChangeInBooleanParameterNamed(ReportMailingJobConstants.IS_ACTIVE_PARAM_NAME, reportMailingJob.isActive())) {
            final boolean isActive = jsonCommand.booleanPrimitiveValueOfParameterNamed(ReportMailingJobConstants.IS_ACTIVE_PARAM_NAME);
            changes.put(ReportMailingJobConstants.IS_ACTIVE_PARAM_NAME, isActive);
            reportMailingJob.setActive(isActive);
        }

        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.EMAIL_RECIPIENTS_PARAM_NAME,
                reportMailingJob.getEmailRecipients())) {
            final String emailRecipients = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_RECIPIENTS_PARAM_NAME);
            changes.put(ReportMailingJobConstants.EMAIL_RECIPIENTS_PARAM_NAME, emailRecipients);
            reportMailingJob.setEmailRecipients(emailRecipients);
        }

        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.EMAIL_SUBJECT_PARAM_NAME,
                reportMailingJob.getEmailSubject())) {
            final String emailSubject = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_SUBJECT_PARAM_NAME);
            changes.put(ReportMailingJobConstants.EMAIL_SUBJECT_PARAM_NAME, emailSubject);
            reportMailingJob.setEmailSubject(emailSubject);
        }

        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.EMAIL_MESSAGE_PARAM_NAME,
                reportMailingJob.getEmailMessage())) {
            final String emailMessage = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.EMAIL_MESSAGE_PARAM_NAME);
            changes.put(ReportMailingJobConstants.EMAIL_MESSAGE_PARAM_NAME, emailMessage);
            reportMailingJob.setEmailMessage(emailMessage);
        }

        if (jsonCommand.isChangeInStringParameterNamed(ReportMailingJobConstants.STRETCHY_REPORT_PARAM_MAP_PARAM_NAME,
                reportMailingJob.getStretchyReportParamMap())) {
            final String stretchyReportParamMap = jsonCommand
                    .stringValueOfParameterNamed(ReportMailingJobConstants.STRETCHY_REPORT_PARAM_MAP_PARAM_NAME);
            changes.put(ReportMailingJobConstants.STRETCHY_REPORT_PARAM_MAP_PARAM_NAME, stretchyReportParamMap);
            reportMailingJob.setStretchyReportParamMap(stretchyReportParamMap);
        }

        final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat = ReportMailingJobEmailAttachmentFileFormat
                .newInstance(reportMailingJob.getEmailAttachmentFileFormat());

        if (jsonCommand.isChangeInIntegerParameterNamed(ReportMailingJobConstants.EMAIL_ATTACHMENT_FILE_FORMAT_ID_PARAM_NAME,
                emailAttachmentFileFormat.getId())) {
            final Integer emailAttachmentFileFormatId = jsonCommand
                    .integerValueOfParameterNamed(ReportMailingJobConstants.EMAIL_ATTACHMENT_FILE_FORMAT_ID_PARAM_NAME);
            changes.put(ReportMailingJobConstants.EMAIL_ATTACHMENT_FILE_FORMAT_ID_PARAM_NAME, emailAttachmentFileFormatId);

            final ReportMailingJobEmailAttachmentFileFormat newEmailAttachmentFileFormat = ReportMailingJobEmailAttachmentFileFormat
                    .newInstance(emailAttachmentFileFormatId);
            reportMailingJob.setEmailAttachmentFileFormat(newEmailAttachmentFileFormat.getValue());
        }

        final String newStartDateTimeString = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME);

        if (!StringUtils.isEmpty(newStartDateTimeString)) {
            final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(jsonCommand.dateFormat())
                    .withLocale(jsonCommand.extractLocale());
            final LocalDateTime newStartDateTime = LocalDateTime.parse(newStartDateTimeString, dateTimeFormatter);
            final LocalDateTime oldStartDateTime = reportMailingJob.getStartDateTime();

            if ((oldStartDateTime != null) && !newStartDateTime.equals(oldStartDateTime)) {
                changes.put(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME, newStartDateTimeString);
                reportMailingJob.setStartDateTime(newStartDateTime);
            }
        }

        Long currentStretchyReportId = null;

        if (reportMailingJob.getStretchyReport() != null) {
            currentStretchyReportId = reportMailingJob.getStretchyReport().getId();
        }

        // the new stretchy report is only reported here, and applied by the caller
        if (jsonCommand.isChangeInLongParameterNamed(ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME, currentStretchyReportId)) {
            final Long updatedStretchyReportId = jsonCommand
                    .longValueOfParameterNamed(ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME);
            changes.put(ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME, updatedStretchyReportId);
        }

        return changes;
    }

    /**
     * create the next recurring LocalDateTime from recurrence pattern, start LocalDateTime and current DateTime
     *
     * @param recurrencePattern
     * @param startDateTime
     * @return LocalDateTime object
     */
    private LocalDateTime createNextRecurringDateTime(final String recurrencePattern, final LocalDateTime startDateTime) {
        LocalDateTime nextRecurringDateTime = null;

        // the recurrence pattern/rule cannot be empty
        if (StringUtils.isNotBlank(recurrencePattern) && startDateTime != null) {
            final LocalDateTime nextDayLocalDate = startDateTime.plus(Duration.ofDays(1));
            nextRecurringDateTime = CalendarUtils.getNextRecurringDate(recurrencePattern, startDateTime, nextDayLocalDate);
        }

        return nextRecurringDateTime;
    }

    /**
     * Handle any SQL data integrity issue
     *
     * @param jsonCommand
     *            -- JsonCommand object
     * @param dve
     *            -- data integrity exception object
     *
     **/
    private void handleDataIntegrityIssues(final JsonCommand jsonCommand, final Throwable realCause,
            final NonTransientDataAccessException dve) {
        if (realCause.getMessage().contains(ReportMailingJobConstants.NAME_PARAM_NAME)) {
            final String name = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.NAME_PARAM_NAME);
            throw new PlatformDataIntegrityException("error.msg.report.mailing.job.duplicate.name",
                    "Report mailing job with name `" + name + "` already exists", ReportMailingJobConstants.NAME_PARAM_NAME, name);
        }

        LOG.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
