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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportRepositoryWrapper;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.FileSystemContentRepository;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.report.provider.ReportingProcessServiceProvider;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.apache.fineract.infrastructure.reportmailingjob.ReportMailingJobConstants;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobEmailAttachmentFileFormat;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobEmailData;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobPreviousRunStatus;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobStretchyReportParamDateOption;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJob;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRepository;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRepositoryWrapper;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRunHistory;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRunHistoryRepository;
import org.apache.fineract.infrastructure.reportmailingjob.util.ReportMailingJobDateUtil;
import org.apache.fineract.infrastructure.reportmailingjob.validation.ReportMailingJobValidator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.core.util.MultivaluedMapImpl;

@Service
public class ReportMailingJobWritePlatformServiceImpl implements ReportMailingJobWritePlatformService {
    
    private final static Logger logger = LoggerFactory.getLogger(ReportMailingJobWritePlatformServiceImpl.class);
    private final ReportRepositoryWrapper reportRepositoryWrapper;
    private final ReportMailingJobValidator reportMailingJobValidator;
    private final ReportMailingJobRepositoryWrapper reportMailingJobRepositoryWrapper;
    private final ReportMailingJobRepository reportMailingJobRepository;
    private final PlatformSecurityContext platformSecurityContext;
    private final ReportMailingJobEmailService reportMailingJobEmailService;
    private final ReadReportingService readReportingService;
    private final ReportingProcessServiceProvider reportingProcessServiceProvider;
    private final ReportMailingJobRunHistoryRepository reportMailingJobRunHistoryRepository;
    private final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    @Autowired
    public ReportMailingJobWritePlatformServiceImpl(final ReportRepositoryWrapper reportRepositoryWrapper, 
            final ReportMailingJobValidator reportMailingJobValidator, 
            final ReportMailingJobRepositoryWrapper reportMailingJobRepositoryWrapper, 
            final ReportMailingJobRepository reportMailingJobRepository, 
            final PlatformSecurityContext platformSecurityContext, 
            final ReportMailingJobEmailService reportMailingJobEmailService,  
            final ReadReportingService readReportingService, 
            final ReportMailingJobRunHistoryRepository reportMailingJobRunHistoryRepository, 
            final ReportingProcessServiceProvider reportingProcessServiceProvider) {
        this.reportRepositoryWrapper = reportRepositoryWrapper;
        this.reportMailingJobValidator = reportMailingJobValidator;
        this.reportMailingJobRepositoryWrapper = reportMailingJobRepositoryWrapper;
        this.reportMailingJobRepository = reportMailingJobRepositoryWrapper.getReportMailingJobRepository();
        this.platformSecurityContext = platformSecurityContext;
        this.reportMailingJobEmailService = reportMailingJobEmailService;
        this.readReportingService = readReportingService;
        this.reportMailingJobRunHistoryRepository = reportMailingJobRunHistoryRepository;
        this.reportingProcessServiceProvider = reportingProcessServiceProvider;
    }

    @Override
    @Transactional
    public CommandProcessingResult createReportMailingJob(JsonCommand jsonCommand) {
        try {
            // validate the create request
            this.reportMailingJobValidator.validateCreateRequest(jsonCommand);
            
            final AppUser appUser = this.platformSecurityContext.authenticatedUser();
            
            // get the stretchy Report object
            final Report stretchyReport = this.reportRepositoryWrapper.findOneThrowExceptionIfNotFound(jsonCommand.longValueOfParameterNamed(
                    ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME));
            
            // create an instance of ReportMailingJob class from the JsonCommand object
            final ReportMailingJob reportMailingJob = ReportMailingJob.newInstance(jsonCommand, stretchyReport, appUser);
            
            // save entity
            this.reportMailingJobRepository.save(reportMailingJob);
            
            return new CommandProcessingResultBuilder().withCommandId(jsonCommand.commandId()).
                    withEntityId(reportMailingJob.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            
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
            final ReportMailingJob reportMailingJob = this.reportMailingJobRepositoryWrapper.findOneThrowExceptionIfNotFound(reportMailingJobId);
            
            final Map<String, Object> changes = reportMailingJob.update(jsonCommand);
            
            // get the recurrence rule string
            final String recurrence = reportMailingJob.getRecurrence();
            
            // get the next run DateTime from the ReportMailingJob entity
            DateTime nextRunDateTime = reportMailingJob.getNextRunDateTime();
            
            // check if the stretchy report id was updated
            if (changes.containsKey(ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME)) {
                final Long stretchyReportId = (Long) changes.get(ReportMailingJobConstants.STRETCHY_REPORT_ID_PARAM_NAME);
                final Report stretchyReport = this.reportRepositoryWrapper.findOneThrowExceptionIfNotFound(stretchyReportId);
                
                // update the stretchy report
                reportMailingJob.update(stretchyReport);
            }
            
            // check if the recurrence was updated
            if (changes.containsKey(ReportMailingJobConstants.RECURRENCE_PARAM_NAME)) {
                
                // go ahead if the recurrence is not null
                if (StringUtils.isNotBlank(recurrence)) {
                    // set the start DateTime to the current tenant date time
                    DateTime startDateTime = DateUtils.getLocalDateTimeOfTenant().toDateTime();
                    
                    // check if the start DateTime was updated
                    if (changes.containsKey(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME)) {
                        // get the updated start DateTime
                        startDateTime = reportMailingJob.getStartDateTime();
                    }
                    
                    startDateTime = reportMailingJob.getStartDateTime();
                    
                    // get the next recurring DateTime
                    final DateTime nextRecurringDateTime = this.createNextRecurringDateTime(recurrence, startDateTime);
                    
                    // update the next run time property
                    reportMailingJob.updateNextRunDateTime(nextRecurringDateTime);
                    
                 // check if the next run DateTime is not empty and the recurrence is empty
                } else if (StringUtils.isBlank(recurrence) && (nextRunDateTime != null)) {
                    // the next run DateTime should be set to null
                    reportMailingJob.updateNextRunDateTime(null);
                }
            }
            
            if (changes.containsKey(ReportMailingJobConstants.START_DATE_TIME_PARAM_NAME)) {
                final DateTime startDateTime = reportMailingJob.getStartDateTime();
                
                // initially set the next recurring date time to the new start date time
                DateTime nextRecurringDateTime = startDateTime;
                
                // ensure that the recurrence pattern string is not empty
                if (StringUtils.isNotBlank(recurrence)) {
                    // get the next recurring DateTime
                    nextRecurringDateTime = this.createNextRecurringDateTime(recurrence, startDateTime);
                }
                
                // update the next run time property
                reportMailingJob.updateNextRunDateTime(nextRecurringDateTime);
            }
            
            if (!changes.isEmpty()) {
                // save and flush immediately so any data integrity exception can be handled in the "catch" block
                this.reportMailingJobRepository.saveAndFlush(reportMailingJob);
            }
            
            return new CommandProcessingResultBuilder().
                    withCommandId(jsonCommand.commandId()).
                    withEntityId(reportMailingJob.getId()).
                    with(changes).
                    build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteReportMailingJob(Long reportMailingJobId) {
        // retrieve the ReportMailingJob object from the database
        final ReportMailingJob reportMailingJob = this.reportMailingJobRepositoryWrapper.findOneThrowExceptionIfNotFound(reportMailingJobId);
        
        // delete the report mailing job by setting the isDeleted property to 1 and altering the name
        reportMailingJob.delete();
        
        // save the report mailing job entity
        this.reportMailingJobRepository.save(reportMailingJob);
        
        return new CommandProcessingResultBuilder().withEntityId(reportMailingJobId).build();
    }
    
    @Override
    @CronTarget(jobName = JobName.EXECUTE_REPORT_MAILING_JOBS)
    public void executeReportMailingJobs() throws JobExecutionException {
        final Collection<ReportMailingJob> reportMailingJobCollection = this.reportMailingJobRepository.findByIsActiveTrueAndIsDeletedFalse();
        
        for (ReportMailingJob reportMailingJob : reportMailingJobCollection) {
            // get the tenant's date as a DateTime object
            final DateTime localDateTimeOftenant = DateUtils.getLocalDateTimeOfTenant().toDateTime();
            final DateTime nextRunDateTime = reportMailingJob.getNextRunDateTime();
            
            if (nextRunDateTime != null && nextRunDateTime.isBefore(localDateTimeOftenant)) {
                // get the emailAttachmentFileFormat enum object
                final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat = ReportMailingJobEmailAttachmentFileFormat.
                        newInstance(reportMailingJob.getEmailAttachmentFileFormat());
                
                if (emailAttachmentFileFormat != null && emailAttachmentFileFormat.isValid()) {
                    final Report stretchyReport = reportMailingJob.getStretchyReport();
                    final String reportName = (stretchyReport != null) ? stretchyReport.getReportName() : null;
                    final StringBuilder errorLog = new StringBuilder();
                    final Map<String, String> validateStretchyReportParamMap = this.reportMailingJobValidator.
                            validateStretchyReportParamMap(reportMailingJob.getStretchyReportParamMap());
                    MultivaluedMap<String, String> reportParams = new MultivaluedMapImpl();
                    
                    if (validateStretchyReportParamMap != null) {
                        Iterator<Map.Entry<String, String>> validateStretchyReportParamMapEntries = validateStretchyReportParamMap.entrySet().iterator();
                        
                        while (validateStretchyReportParamMapEntries.hasNext()) {
                            Map.Entry<String, String> validateStretchyReportParamMapEntry = validateStretchyReportParamMapEntries.next();
                            String key = validateStretchyReportParamMapEntry.getKey();
                            String value = validateStretchyReportParamMapEntry.getValue();
                            
                            if (StringUtils.containsIgnoreCase(key, "date")) {
                                ReportMailingJobStretchyReportParamDateOption reportMailingJobStretchyReportParamDateOption = 
                                        ReportMailingJobStretchyReportParamDateOption.newInstance(value);
                                
                                if (reportMailingJobStretchyReportParamDateOption.isValid()) {
                                    value = ReportMailingJobDateUtil.getDateAsString(reportMailingJobStretchyReportParamDateOption);
                                }
                            }
                            
                            reportParams.add(key, value);
                        }
                    }
                    
                    // generate the report output stream, method in turn call another that sends the file to the email recipients
                    this.generateReportOutputStream(reportMailingJob, emailAttachmentFileFormat, reportParams, reportName, errorLog);
                    
                    // update the previous run time, next run time, status, error log properties
                    this.updateReportMailingJobAfterJobExecution(reportMailingJob, errorLog, localDateTimeOftenant);
                }
            }
        }
    }
    
    /** 
     * update the report mailing job entity after job execution 
     * 
     * @param reportMailingJob -- the report mailing job entity
     * @param errorLog -- StringBuilder object containing the error log if any
     * @param jobStartDateTime -- the start DateTime of the job
     * 
     **/
    private void updateReportMailingJobAfterJobExecution(final ReportMailingJob reportMailingJob, final StringBuilder errorLog, 
            final DateTime jobStartDateTime) {
        final String recurrence = reportMailingJob.getRecurrence();
        final DateTime nextRunDateTime = reportMailingJob.getNextRunDateTime();
        ReportMailingJobPreviousRunStatus reportMailingJobPreviousRunStatus = ReportMailingJobPreviousRunStatus.SUCCESS;
        
        reportMailingJob.updatePreviousRunErrorLog(null);
        
        if (errorLog != null && errorLog.length() > 0) {
            reportMailingJobPreviousRunStatus = ReportMailingJobPreviousRunStatus.ERROR;
            reportMailingJob.updatePreviousRunErrorLog(errorLog.toString());
        }
        
        reportMailingJob.increaseNumberOfRunsByOne();
        reportMailingJob.updatePreviousRunStatus(reportMailingJobPreviousRunStatus.getValue());
        reportMailingJob.updatePreviousRunDateTime(reportMailingJob.getNextRunDateTime());
        
        // check if the job has a recurrence pattern, if not deactivate the job. The job will only run once
        if (StringUtils.isEmpty(recurrence)) {
            // deactivate job
            reportMailingJob.deactivate();
            
            // job will only run once, no next run time
            reportMailingJob.updateNextRunDateTime(null);
        } else if (nextRunDateTime != null) {
            final DateTime nextRecurringDateTime = this.createNextRecurringDateTime(recurrence, nextRunDateTime);
            
            // finally update the next run date time property
            reportMailingJob.updateNextRunDateTime(nextRecurringDateTime);
        }
        
        // save the ReportMailingJob entity
        this.reportMailingJobRepository.save(reportMailingJob);
        
        // create a new report mailing job run history entity
        this.createReportMailingJobRunHistroryAfterJobExecution(reportMailingJob, errorLog, jobStartDateTime, 
                reportMailingJobPreviousRunStatus.getValue());
    }
    
    /**
     * create the next recurring DateTime from recurrence pattern, start DateTime and current DateTime
     * 
     * @param recurrencePattern
     * @param startDateTime
     * @return DateTime object
     */
    private DateTime createNextRecurringDateTime(final String recurrencePattern, final DateTime startDateTime) {
        DateTime nextRecurringDateTime = null;
        
        // the recurrence pattern/rule cannot be empty
        if (StringUtils.isNotBlank(recurrencePattern) && startDateTime != null) {
            final LocalDate nextDayLocalDate = startDateTime.plus(1).toLocalDate();
            final LocalDate nextRecurringLocalDate = CalendarUtils.getNextRecurringDate(recurrencePattern, startDateTime.toLocalDate(), 
                    nextDayLocalDate);
            final String nextDateTimeString = nextRecurringLocalDate + " " + startDateTime.getHourOfDay() + ":" + startDateTime.getMinuteOfHour() 
                    + ":" + startDateTime.getSecondOfMinute();
            final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATETIME_FORMAT);
            
            nextRecurringDateTime = DateTime.parse(nextDateTimeString, dateTimeFormatter);
        }
        
        return nextRecurringDateTime;
    }
    
    /** 
     * create a new report mailing job run history entity after job execution
     * 
     * @param reportMailingJob -- the report mailing job entity
     * @param errorLog -- StringBuilder object containing the error log if any
     * @param jobStartDateTime -- the start DateTime of the job
     * @param jobRunStatus -- the status of the job (success/error)
     * 
     **/
    private void createReportMailingJobRunHistroryAfterJobExecution(final ReportMailingJob reportMailingJob, final StringBuilder errorLog, 
            final DateTime jobStartDateTime, final String jobRunStatus) {
        final DateTime jobEndDateTime = DateUtils.getLocalDateTimeOfTenant().toDateTime();
        final String errorLogToString = (errorLog != null) ? errorLog.toString() : null;
        final ReportMailingJobRunHistory reportMailingJobRunHistory = ReportMailingJobRunHistory.newInstance(reportMailingJob, jobStartDateTime, 
                jobEndDateTime, jobRunStatus, null, errorLogToString);
        
        this.reportMailingJobRunHistoryRepository.save(reportMailingJobRunHistory);
    }

    /** 
     * Handle any SQL data integrity issue 
     *
     * @param jsonCommand -- JsonCommand object
     * @param dve -- data integrity exception object
     * 
     **/
    private void handleDataIntegrityIssues(final JsonCommand jsonCommand, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        
        if (realCause.getMessage().contains(ReportMailingJobConstants.NAME_PARAM_NAME)) {
            final String name = jsonCommand.stringValueOfParameterNamed(ReportMailingJobConstants.NAME_PARAM_NAME);
            throw new PlatformDataIntegrityException("error.msg.report.mailing.job.duplicate.name", "Report mailing job with name `" + name + "` already exists",
                    ReportMailingJobConstants.NAME_PARAM_NAME, name);
        }

        logger.error(dve.getMessage(), dve);
        
        throw new PlatformDataIntegrityException("error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
    
    /** 
     * generate the report output stream
     * 
     * @param reportMailingJob
     * @param emailAttachmentFileFormat
     * @param reportParams
     * @param reportName
     * @param errorLog
     * @return the error log StringBuilder object
     */
    private StringBuilder generateReportOutputStream(final ReportMailingJob reportMailingJob, final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat, 
            final MultivaluedMap<String, String> reportParams, final String reportName, final StringBuilder errorLog) {
        
        try {
            final String reportType = this.readReportingService.getReportType(reportName);
            final ReportingProcessService reportingProcessService = this.reportingProcessServiceProvider.findReportingProcessService(reportType);
            
            if (reportingProcessService != null) {
                final Response processReport = reportingProcessService.processRequest(reportName, reportParams);
                final Object reponseObject = (processReport != null) ? processReport.getEntity() : null;
                
                if (reponseObject != null && reponseObject.getClass().equals(ByteArrayOutputStream.class)) {
                    final ByteArrayOutputStream byteArrayOutputStream = ByteArrayOutputStream.class.cast(reponseObject);
                    final String fileLocation = FileSystemContentRepository.FINERACT_BASE_DIR + File.separator + "";
                    final String fileNameWithoutExtension = fileLocation + File.separator + reportName;
                    
                    // check if file directory exists, if not create directory
                    if (!new File(fileLocation).isDirectory()) {
                        new File(fileLocation).mkdirs();
                    }
                    
                    if ((byteArrayOutputStream == null) || byteArrayOutputStream.size() == 0) {
                        errorLog.append("Report processing failed, empty output stream created");
                    } else if ((errorLog != null && errorLog.length() == 0) && (byteArrayOutputStream.size() > 0)) {
                        final String fileName = fileNameWithoutExtension + "." + emailAttachmentFileFormat.getValue();
                        
                        // send the file to email recipients
                        this.sendReportFileToEmailRecipients(reportMailingJob, fileName, byteArrayOutputStream, errorLog);
                    }
                } else {
                    errorLog.append("Response object entity is not equal to ByteArrayOutputStream ---------- ");
                }
            } else {
                errorLog.append("ReportingProcessService object is null ---------- ");
            }
        } catch (Exception e) {
            errorLog.append("The ReportMailingJobWritePlatformServiceImpl.generateReportOutputStream method threw an Exception: "
                    + e + " ---------- ");
        }
        
        return errorLog;
    }
    
    /** 
     * send report file to email recipients
     * 
     * @param reportMailingJob
     * @param fileName
     * @param byteArrayOutputStream
     * @param errorLog
     */
    private void sendReportFileToEmailRecipients(final ReportMailingJob reportMailingJob, final String fileName, 
            final ByteArrayOutputStream byteArrayOutputStream, final StringBuilder errorLog) {
        final Set<String> emailRecipients = this.reportMailingJobValidator.validateEmailRecipients(reportMailingJob.getEmailRecipients());
        
        try {
            final File file = new File(fileName);
            final FileOutputStream outputStream = new FileOutputStream(file);
            byteArrayOutputStream.writeTo(outputStream);
            
            for (String emailRecipient : emailRecipients) {
                final ReportMailingJobEmailData reportMailingJobEmailData = new ReportMailingJobEmailData(emailRecipient, 
                        reportMailingJob.getEmailMessage(), reportMailingJob.getEmailSubject(), file);
                
                this.reportMailingJobEmailService.sendEmailWithAttachment(reportMailingJobEmailData);
            }
            
            outputStream.close();
            
        } catch (IOException e) {
            errorLog.append("The ReportMailingJobWritePlatformServiceImpl.sendReportFileToEmailRecipients method threw an IOException "
                    + "exception: " + e + " ---------- ");
        }
    }
}
