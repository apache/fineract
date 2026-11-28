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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportRepositoryWrapper;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobEmailAttachmentFileFormat;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJob;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRepository;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRepositoryWrapper;
import org.apache.fineract.infrastructure.reportmailingjob.validation.ReportMailingJobValidator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportMailingJobWritePlatformServiceImplTest {

    private static final Long JOB_ID = 4L;
    private static final Long REPORT_ID = 11L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 2, 10, 30);

    @Mock
    private ReportRepositoryWrapper reportRepositoryWrapper;
    @Mock
    private ReportMailingJobValidator reportMailingJobValidator;
    @Mock
    private ReportMailingJobRepositoryWrapper reportMailingJobRepositoryWrapper;
    @Mock
    private ReportMailingJobRepository reportMailingJobRepository;
    @Mock
    private PlatformSecurityContext platformSecurityContext;

    private final Report stretchyReport = mock(Report.class);
    private ReportMailingJobWritePlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        when(reportMailingJobRepositoryWrapper.getReportMailingJobRepository()).thenReturn(reportMailingJobRepository);
        service = new ReportMailingJobWritePlatformServiceImpl(reportRepositoryWrapper, reportMailingJobValidator,
                reportMailingJobRepositoryWrapper, platformSecurityContext);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void createReportMailingJobTakesItsValuesFromTheCommand() {
        final AppUser user = mock(AppUser.class);
        when(platformSecurityContext.authenticatedUser()).thenReturn(user);
        when(reportRepositoryWrapper.findOneThrowExceptionIfNotFound(REPORT_ID)).thenReturn(stretchyReport);

        service.createReportMailingJob(command("""
                {"name": "Daily clients", "description": "Client list", "startDateTime": "02 January 2026 10:30",
                 "dateFormat": "dd MMMM yyyy HH:mm", "locale": "en", "recurrence": "FREQ=DAILY;INTERVAL=1",
                 "emailRecipients": "ops@example.com", "emailSubject": "Clients", "emailMessage": "Attached",
                 "emailAttachmentFileFormatId": 2, "stretchyReportId": 11, "stretchyReportParamMap": "{}", "isActive": true}
                """));

        final ReportMailingJob saved = savedJob();
        assertEquals("Daily clients", saved.getName());
        assertEquals("Client list", saved.getDescription());
        assertEquals(START, saved.getStartDateTime());
        // on create the next run is the start date time, even with a recurrence
        assertEquals(START, saved.getNextRunDateTime());
        assertEquals("FREQ=DAILY;INTERVAL=1", saved.getRecurrence());
        assertEquals("ops@example.com", saved.getEmailRecipients());
        assertEquals("Clients", saved.getEmailSubject());
        assertEquals("Attached", saved.getEmailMessage());
        assertEquals(ReportMailingJobEmailAttachmentFileFormat.PDF.getValue(), saved.getEmailAttachmentFileFormat());
        assertSame(stretchyReport, saved.getStretchyReport());
        assertEquals("{}", saved.getStretchyReportParamMap());
        assertTrue(saved.isActive());
        assertFalse(saved.isDeleted());
        assertSame(user, saved.getRunAsUser());
    }

    @Test
    void createReportMailingJobParsesTheStartDateWithTheLocaleOfTheRequest() {
        when(reportRepositoryWrapper.findOneThrowExceptionIfNotFound(REPORT_ID)).thenReturn(stretchyReport);

        service.createReportMailingJob(command("""
                {"name": "Daily clients", "startDateTime": "02 Januar 2026 10:30", "dateFormat": "dd MMMM yyyy HH:mm", "locale": "de",
                 "emailRecipients": "ops@example.com", "emailSubject": "Clients", "emailMessage": "Attached",
                 "emailAttachmentFileFormatId": 2, "stretchyReportId": 11}
                """));

        assertEquals(START, savedJob().getStartDateTime());
    }

    @Test
    void createReportMailingJobStoresMissingOptionalValuesAsEmptyStrings() {
        when(reportRepositoryWrapper.findOneThrowExceptionIfNotFound(REPORT_ID)).thenReturn(stretchyReport);

        service.createReportMailingJob(command("""
                {"name": "Daily clients", "startDateTime": "02 January 2026 10:30", "dateFormat": "dd MMMM yyyy HH:mm", "locale": "en",
                 "emailRecipients": "ops@example.com", "emailSubject": "Clients", "emailMessage": "Attached",
                 "emailAttachmentFileFormatId": 3, "stretchyReportId": 11}
                """));

        final ReportMailingJob saved = savedJob();
        assertEquals("", saved.getDescription());
        assertEquals("", saved.getRecurrence());
        assertEquals("", saved.getStretchyReportParamMap());
        assertFalse(saved.isActive());
    }

    @Test
    void updateReportMailingJobAppliesAndReportsTheChangedValuesInOrder() {
        final ReportMailingJob job = existingJob();

        final CommandProcessingResult result = service.updateReportMailingJob(JOB_ID, command("""
                {"emailAttachmentFileFormatId": 3, "stretchyReportParamMap": "{\\"x\\": 1}", "emailMessage": "New message",
                 "emailSubject": "New subject", "emailRecipients": "new@example.com", "isActive": false, "description": "New description",
                 "name": "Weekly clients", "locale": "en"}
                """));

        assertEquals(List.of("name", "description", "isActive", "emailRecipients", "emailSubject", "emailMessage", "stretchyReportParamMap",
                "emailAttachmentFileFormatId"), List.copyOf(result.getChanges().keySet()));
        assertEquals("Weekly clients", job.getName());
        assertEquals("New description", job.getDescription());
        assertFalse(job.isActive());
        assertEquals("new@example.com", job.getEmailRecipients());
        assertEquals("New subject", job.getEmailSubject());
        assertEquals("New message", job.getEmailMessage());
        assertEquals("{\"x\": 1}", job.getStretchyReportParamMap());
        assertEquals(ReportMailingJobEmailAttachmentFileFormat.CSV.getValue(), job.getEmailAttachmentFileFormat());
        assertEquals(3, result.getChanges().get("emailAttachmentFileFormatId"));
        verify(reportMailingJobRepository).saveAndFlush(job);
    }

    /**
     * Pins an existing behaviour: the update validator reads the attachment file format without a locale, but the
     * change check reads it with one, so changing the file format without a locale parameter is rejected.
     */
    @Test
    void updateOfTheFileFormatWithoutALocaleIsRejected() {
        final ReportMailingJob job = existingJob();

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.updateReportMailingJob(JOB_ID, command("""
                        {"emailAttachmentFileFormatId": 3}
                        """)));

        assertEquals("validation.msg.missing.locale.parameter", exception.getErrors().get(0).getUserMessageGlobalisationCode());
        assertEquals(ReportMailingJobEmailAttachmentFileFormat.PDF.getValue(), job.getEmailAttachmentFileFormat());
        verify(reportMailingJobRepository, never()).saveAndFlush(any(ReportMailingJob.class));
    }

    @Test
    void updateReportMailingJobStoresAnEmptyDescriptionAsAnEmptyString() {
        final ReportMailingJob job = existingJob();

        final CommandProcessingResult result = service.updateReportMailingJob(JOB_ID, command("""
                {"description": ""}
                """));

        assertEquals(Map.of("description", ""), result.getChanges());
        assertEquals("", job.getDescription());
    }

    @Test
    void updateReportMailingJobWithoutChangesDoesNotSave() {
        existingJob();

        final CommandProcessingResult result = service.updateReportMailingJob(JOB_ID, command("""
                {"name": "Daily clients", "isActive": true, "startDateTime": "02 January 2026 10:30", "dateFormat": "dd MMMM yyyy HH:mm",
                 "locale": "en", "stretchyReportId": 11}
                """));

        // CommandProcessingResult reports an empty change set as null
        assertNull(result.getChanges());
        verify(reportMailingJobRepository, never()).saveAndFlush(any(ReportMailingJob.class));
    }

    @Test
    void updateReportMailingJobMovesTheStartAndTheNextRun() {
        final ReportMailingJob job = existingJob();
        job.setRecurrence(null);

        final CommandProcessingResult result = service.updateReportMailingJob(JOB_ID, command("""
                {"startDateTime": "05 Februar 2026 08:15", "dateFormat": "dd MMMM yyyy HH:mm", "locale": "de"}
                """));

        // the change set reports the start date time as sent
        assertEquals(Map.of("startDateTime", "05 Februar 2026 08:15"), result.getChanges());
        assertEquals(LocalDateTime.of(2026, 2, 5, 8, 15), job.getStartDateTime());
        assertEquals(LocalDateTime.of(2026, 2, 5, 8, 15), job.getNextRunDateTime());
    }

    @Test
    void updateReportMailingJobReportsAndAppliesANewStretchyReport() {
        final ReportMailingJob job = existingJob();
        final Report otherReport = mock(Report.class);
        when(reportRepositoryWrapper.findOneThrowExceptionIfNotFound(12L)).thenReturn(otherReport);

        final CommandProcessingResult result = service.updateReportMailingJob(JOB_ID, command("""
                {"stretchyReportId": 12}
                """));

        assertEquals(Map.of("stretchyReportId", 12L), result.getChanges());
        assertSame(otherReport, job.getStretchyReport());
        verify(reportMailingJobRepository).saveAndFlush(job);
    }

    @Test
    void updateReportMailingJobClearingTheRecurrenceClearsTheNextRun() {
        final ReportMailingJob job = existingJob();

        final CommandProcessingResult result = service.updateReportMailingJob(JOB_ID, command("""
                {"recurrence": ""}
                """));

        assertEquals(Map.of("recurrence", ""), result.getChanges());
        assertEquals("", job.getRecurrence());
        assertNull(job.getNextRunDateTime());
    }

    private ReportMailingJob existingJob() {
        when(stretchyReport.getId()).thenReturn(REPORT_ID);
        final ReportMailingJob job = ReportMailingJob.newInstance("Daily clients", "Client list", START, "FREQ=DAILY;INTERVAL=1",
                "ops@example.com", "Clients", "Attached", ReportMailingJobEmailAttachmentFileFormat.PDF, stretchyReport, "{}", true,
                mock(AppUser.class));
        job.setNextRunDateTime(START);
        when(reportMailingJobRepositoryWrapper.findOneThrowExceptionIfNotFound(JOB_ID)).thenReturn(job);
        return job;
    }

    private ReportMailingJob savedJob() {
        final ArgumentCaptor<ReportMailingJob> saved = ArgumentCaptor.forClass(ReportMailingJob.class);
        verify(reportMailingJobRepository).saveAndFlush(saved.capture());
        return saved.getValue();
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), "reportmailingjobs", JOB_ID, null, null, null,
                null, null, null, null, null, null, null, null, null);
    }
}
