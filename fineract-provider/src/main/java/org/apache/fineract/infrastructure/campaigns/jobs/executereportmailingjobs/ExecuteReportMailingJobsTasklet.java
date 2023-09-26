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
package org.apache.fineract.infrastructure.campaigns.jobs.executereportmailingjobs;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.report.provider.ReportingProcessServiceProvider;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobEmailAttachmentFileFormat;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobEmailData;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobPreviousRunStatus;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobStretchyReportParamDateOption;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJob;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRepository;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRunHistory;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRunHistoryRepository;
import org.apache.fineract.infrastructure.reportmailingjob.service.ReportMailingJobEmailService;
import org.apache.fineract.infrastructure.reportmailingjob.util.ReportMailingJobDateUtil;
import org.apache.fineract.infrastructure.reportmailingjob.validation.ReportMailingJobValidator;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class ExecuteReportMailingJobsTasklet implements Tasklet {

    private final ReportMailingJobRepository reportMailingJobRepository;
    private final ReportMailingJobValidator reportMailingJobValidator;
    private final ReadReportingService readReportingService;
    private final ReportingProcessServiceProvider reportingProcessServiceProvider;
    private final ReportMailingJobEmailService reportMailingJobEmailService;
    private final ReportMailingJobRunHistoryRepository reportMailingJobRunHistoryRepository;
    private final FineractProperties fineractProperties;

    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final Collection<ReportMailingJob> reportMailingJobCollection = reportMailingJobRepository.findByIsActiveTrueAndIsDeletedFalse();

        for (ReportMailingJob reportMailingJob : reportMailingJobCollection) {
            final LocalDateTime localDateTimeOftenant = DateUtils.getLocalDateTimeOfTenant();
            final LocalDateTime nextRunDateTime = reportMailingJob.getNextRunDateTime();

            if (nextRunDateTime != null && DateUtils.isBefore(nextRunDateTime, localDateTimeOftenant)) {
                final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat = ReportMailingJobEmailAttachmentFileFormat
                        .newInstance(reportMailingJob.getEmailAttachmentFileFormat());

                if (emailAttachmentFileFormat != null && emailAttachmentFileFormat.isValid()) {
                    final Report stretchyReport = reportMailingJob.getStretchyReport();
                    final String reportName = (stretchyReport != null) ? stretchyReport.getReportName() : null;
                    final StringBuilder errorLog = new StringBuilder();
                    final Map<String, String> validateStretchyReportParamMap = reportMailingJobValidator
                            .validateStretchyReportParamMap(reportMailingJob.getStretchyReportParamMap());
                    MultivaluedMap<String, String> reportParams = new MultivaluedStringMap();

                    if (validateStretchyReportParamMap != null) {

                        for (Map.Entry<String, String> validateStretchyReportParamMapEntry : validateStretchyReportParamMap.entrySet()) {
                            String key = validateStretchyReportParamMapEntry.getKey();
                            String value = validateStretchyReportParamMapEntry.getValue();

                            if (StringUtils.containsIgnoreCase(key, "date")) {
                                ReportMailingJobStretchyReportParamDateOption reportMailingJobStretchyReportParamDateOption = ReportMailingJobStretchyReportParamDateOption
                                        .newInstance(value);

                                if (reportMailingJobStretchyReportParamDateOption.isValid()) {
                                    value = ReportMailingJobDateUtil.getDateAsString(reportMailingJobStretchyReportParamDateOption);
                                }
                            }

                            reportParams.add(key, value);
                        }
                    }

                    generateReportOutputStream(reportMailingJob, emailAttachmentFileFormat, reportParams, reportName, errorLog);

                    updateReportMailingJobAfterJobExecution(reportMailingJob, errorLog, localDateTimeOftenant);
                }
            }
        }
        return RepeatStatus.FINISHED;
    }

    private void generateReportOutputStream(final ReportMailingJob reportMailingJob,
            final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat, final MultivaluedMap<String, String> reportParams,
            final String reportName, final StringBuilder errorLog) {
        try {
            final boolean isSelfServiceUserReport = false;
            final String reportType = readReportingService.getReportType(reportName, isSelfServiceUserReport, false);
            final ReportingProcessService reportingProcessService = reportingProcessServiceProvider.findReportingProcessService(reportType);

            if (reportingProcessService != null) {
                final Response processReport = reportingProcessService.processRequest(reportName, reportParams);
                final Object responseObject = (processReport != null) ? processReport.getEntity() : null;

                if (responseObject != null && responseObject.getClass().equals(ByteArrayOutputStream.class)) {
                    final ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) responseObject;
                    final String fileLocation = fineractProperties.getContent().getFilesystem().getRootFolder() + File.separator + "";
                    final String fileNameWithoutExtension = fileLocation + File.separator + reportName;

                    if (!new File(fileLocation).isDirectory()) {
                        new File(fileLocation).mkdirs();
                    }

                    if (byteArrayOutputStream.size() == 0) {
                        errorLog.append("Report processing failed, empty output stream created");
                    } else if ((errorLog != null && errorLog.length() == 0) && (byteArrayOutputStream.size() > 0)) {
                        final String fileName = fileNameWithoutExtension + "." + emailAttachmentFileFormat.getValue();

                        sendReportFileToEmailRecipients(reportMailingJob, fileName, byteArrayOutputStream, errorLog);
                    }
                } else {
                    errorLog.append("Response object entity is not equal to ByteArrayOutputStream ---------- ");
                }
            } else {
                errorLog.append(ReportingProcessServiceProvider.SERVICE_MISSING).append(reportType);
            }
        } catch (Exception e) {
            errorLog.append("The ReportMailingJobWritePlatformServiceImpl.generateReportOutputStream method threw an Exception: ").append(e)
                    .append(" ---------- ");
        }
    }

    private void updateReportMailingJobAfterJobExecution(final ReportMailingJob reportMailingJob, final StringBuilder errorLog,
            final LocalDateTime jobStartDateTime) {
        final String recurrence = reportMailingJob.getRecurrence();
        final LocalDateTime nextRunDateTime = reportMailingJob.getNextRunDateTime();
        ReportMailingJobPreviousRunStatus reportMailingJobPreviousRunStatus = ReportMailingJobPreviousRunStatus.SUCCESS;

        reportMailingJob.setPreviousRunErrorLog(null);

        if (errorLog != null && errorLog.length() > 0) {
            reportMailingJobPreviousRunStatus = ReportMailingJobPreviousRunStatus.ERROR;
            reportMailingJob.setPreviousRunErrorLog(errorLog.toString());
        }

        reportMailingJob.increaseNumberOfRunsByOne();
        reportMailingJob.setPreviousRunStatus(reportMailingJobPreviousRunStatus.getValue());
        reportMailingJob.setPreviousRunDateTime(reportMailingJob.getNextRunDateTime());

        if (StringUtils.isEmpty(recurrence)) {
            reportMailingJob.setActive(false);

            reportMailingJob.setNextRunDateTime(null);
        } else if (nextRunDateTime != null) {
            final LocalDateTime nextRecurringDateTime = createNextRecurringDateTime(recurrence, nextRunDateTime);

            reportMailingJob.setNextRunDateTime(nextRecurringDateTime);
        }

        reportMailingJobRepository.save(reportMailingJob);

        createReportMailingJobRunHistroryAfterJobExecution(reportMailingJob, errorLog, jobStartDateTime,
                reportMailingJobPreviousRunStatus.getValue());
    }

    private void sendReportFileToEmailRecipients(final ReportMailingJob reportMailingJob, final String fileName,
            final ByteArrayOutputStream byteArrayOutputStream, final StringBuilder errorLog) {
        final Set<String> emailRecipients = this.reportMailingJobValidator.validateEmailRecipients(reportMailingJob.getEmailRecipients());

        try {
            final File file = new File(fileName);
            final FileOutputStream outputStream = new FileOutputStream(file);
            byteArrayOutputStream.writeTo(outputStream);

            for (String emailRecipient : emailRecipients) {
                final ReportMailingJobEmailData reportMailingJobEmailData = new ReportMailingJobEmailData().setTo(emailRecipient)
                        .setText(reportMailingJob.getEmailMessage()).setSubject(reportMailingJob.getEmailSubject()).setAttachment(file);

                reportMailingJobEmailService.sendEmailWithAttachment(reportMailingJobEmailData);
            }

            outputStream.close();

        } catch (IOException e) {
            errorLog.append("The ReportMailingJobWritePlatformServiceImpl.sendReportFileToEmailRecipients method threw an IOException "
                    + "exception: ").append(e).append(" ---------- ");
        }
    }

    private LocalDateTime createNextRecurringDateTime(final String recurrencePattern, final LocalDateTime startDateTime) {
        LocalDateTime nextRecurringDateTime = null;

        if (StringUtils.isNotBlank(recurrencePattern) && startDateTime != null) {
            final LocalDate nextDayLocalDate = startDateTime.plus(Duration.ofDays(1)).toLocalDate();
            final LocalDate nextRecurringLocalDate = CalendarUtils.getNextRecurringDate(recurrencePattern, startDateTime.toLocalDate(),
                    nextDayLocalDate);
            final String nextDateTimeString = nextRecurringLocalDate + " " + startDateTime.getHour() + ":" + startDateTime.getMinute() + ":"
                    + startDateTime.getSecond();
            final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

            nextRecurringDateTime = LocalDateTime.parse(nextDateTimeString, dateTimeFormatter);
        }

        return nextRecurringDateTime;
    }

    private void createReportMailingJobRunHistroryAfterJobExecution(final ReportMailingJob reportMailingJob, final StringBuilder errorLog,
            final LocalDateTime jobStartDateTime, final String jobRunStatus) {
        final LocalDateTime jobEndDateTime = DateUtils.getLocalDateTimeOfTenant();
        final String errorLogToString = (errorLog != null) ? errorLog.toString() : null;
        final ReportMailingJobRunHistory reportMailingJobRunHistory = ReportMailingJobRunHistory.newInstance(reportMailingJob,
                jobStartDateTime, jobEndDateTime, jobRunStatus, null, errorLogToString);

        reportMailingJobRunHistoryRepository.save(reportMailingJobRunHistory);
    }
}
