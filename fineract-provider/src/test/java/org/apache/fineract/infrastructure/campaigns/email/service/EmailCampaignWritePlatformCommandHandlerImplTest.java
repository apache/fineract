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
package org.apache.fineract.infrastructure.campaigns.email.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignValidator;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaign;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignType;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportRepository;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailCampaignWritePlatformCommandHandlerImplTest {

    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 1, 15);
    private static final Long CAMPAIGN_ID = 21L;
    private static final Long BUSINESS_RULE_ID = 3L;
    private static final Long OTHER_BUSINESS_RULE_ID = 7L;
    private static final Long STRETCHY_REPORT_ID = 4L;

    private static final String DIRECT_CAMPAIGN = """
            {"campaignName": "Newsletter", "campaignType": 1, "businessRuleId": 3, "paramValue": "{\\"officeId\\":1}",
             "emailSubject": "Hello", "emailMessage": "Dear client", "dateFormat": "dd MMMM yyyy", "locale": "en"}
            """;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private EmailCampaignRepository emailCampaignRepository;
    @Mock
    private EmailCampaignValidator emailCampaignValidator;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private EmailMessageRepository emailMessageRepository;
    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;
    @Mock
    private ReadReportingService readReportingService;
    @Mock
    private GenericDataService genericDataService;
    @Mock
    private FromJsonHelper fromJsonHelper;

    private final AppUser currentUser = mock(AppUser.class);
    private final Report businessRule = mock(Report.class);
    private final Report otherBusinessRule = mock(Report.class);
    private final Report stretchyReport = mock(Report.class);

    private EmailCampaignWritePlatformCommandHandlerImpl service;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
        service = new EmailCampaignWritePlatformCommandHandlerImpl(context, emailCampaignRepository, emailCampaignValidator,
                reportRepository, emailMessageRepository, clientRepositoryWrapper, readReportingService, genericDataService,
                fromJsonHelper);
        lenient().when(context.authenticatedUser()).thenReturn(currentUser);
        lenient().when(reportRepository.findById(BUSINESS_RULE_ID)).thenReturn(Optional.of(businessRule));
        lenient().when(reportRepository.findById(OTHER_BUSINESS_RULE_ID)).thenReturn(Optional.of(otherBusinessRule));
        lenient().when(reportRepository.findById(STRETCHY_REPORT_ID)).thenReturn(Optional.of(stretchyReport));
        lenient().when(businessRule.getId()).thenReturn(BUSINESS_RULE_ID);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    // the result builder leaves the changes null when nothing changed
    private static Map<String, Object> changesOf(final CommandProcessingResult result) {
        return result.getChanges() == null ? Map.of() : result.getChanges();
    }

    private EmailCampaign create(final String json) {
        service.create(command(json));
        final ArgumentCaptor<EmailCampaign> saved = ArgumentCaptor.forClass(EmailCampaign.class);
        verify(emailCampaignRepository).saveAndFlush(saved.capture());
        return saved.getValue();
    }

    private EmailCampaign existing(final String json) {
        final EmailCampaign campaign = create(json);
        clearInvocations(emailCampaignRepository);
        when(emailCampaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        return campaign;
    }

    private CommandProcessingResult update(final String json) {
        return service.update(CAMPAIGN_ID, command(json));
    }

    @Test
    void createTakesTheValuesFromTheCommand() {
        final EmailCampaign campaign = create(DIRECT_CAMPAIGN);

        assertEquals("Newsletter", campaign.getCampaignName());
        assertEquals(1, campaign.getCampaignType());
        assertSame(businessRule, campaign.getBusinessRuleId());
        assertEquals("{\"officeId\":1}", campaign.getParamValue());
        assertEquals("Hello", campaign.getEmailSubject());
        assertEquals("Dear client", campaign.getEmailMessage());
        assertEquals(BUSINESS_DATE, campaign.getSubmittedOnDate());
        assertSame(currentUser, campaign.getSubmittedBy());
        assertTrue(campaign.isPending());
        assertEquals("pdf", campaign.getEmailAttachmentFileFormat());
        assertEquals("", campaign.getStretchyReportParamMap());
        assertNull(campaign.getStretchyReport());
        assertEquals("", campaign.getRecurrence());
        assertNull(campaign.getRecurrenceStartDate());
    }

    @Test
    void createUsesTheAttachmentFormatAndStretchyReportFromTheCommand() {
        final EmailCampaign campaign = create("""
                {"campaignName": "Newsletter", "campaignType": 1, "businessRuleId": 3, "paramValue": "{}",
                 "emailSubject": "Hello", "emailMessage": "Dear client", "stretchyReportId": 4,
                 "emailAttachmentFileFormatId": 3, "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertEquals("csv", campaign.getEmailAttachmentFileFormat());
        assertSame(stretchyReport, campaign.getStretchyReport());
        assertEquals("{}", campaign.getStretchyReportParamMap());
    }

    @Test
    void createUsesTheSubmittedOnDateFromTheCommand() {
        final EmailCampaign campaign = create("""
                {"campaignName": "Newsletter", "campaignType": 1, "businessRuleId": 3, "paramValue": "{}",
                 "emailSubject": "Hello", "emailMessage": "Dear client", "submittedOnDate": "10 January 2026",
                 "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertEquals(LocalDate.of(2026, 1, 10), campaign.getSubmittedOnDate());
    }

    @Test
    void createOfAScheduledCampaignParsesTheStartDateWithTheDateFormat() {
        final EmailCampaign campaign = create("""
                {"campaignName": "Newsletter", "campaignType": 2, "businessRuleId": 3, "paramValue": "{}",
                 "emailSubject": "Hello", "emailMessage": "Dear client", "recurrence": "FREQ=DAILY",
                 "recurrenceStartDate": "10 January 2099 10:30", "dateFormat": "dd MMMM yyyy HH:mm", "locale": "en"}
                """);

        assertEquals("FREQ=DAILY", campaign.getRecurrence());
        assertEquals(LocalDateTime.of(2099, 1, 10, 10, 30), campaign.getRecurrenceStartDate());
    }

    @Test
    void createOfAScheduledCampaignWithoutAStartDateStartsNow() {
        final EmailCampaign campaign = create("""
                {"campaignName": "Newsletter", "campaignType": 2, "businessRuleId": 3, "paramValue": "{}",
                 "emailSubject": "Hello", "emailMessage": "Dear client", "recurrence": "FREQ=DAILY",
                 "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertNotNull(campaign.getRecurrenceStartDate());
    }

    @Test
    void updateAppliesAndReportsTheChangedNameAndMessage() {
        final EmailCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"emailMessage": "Hi there", "campaignName": "Renamed", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertEquals(List.of("campaignName", "emailMessage"), List.copyOf(result.getChanges().keySet()));
        assertEquals("Renamed", campaign.getCampaignName());
        assertEquals("Hi there", campaign.getEmailMessage());
        verify(emailCampaignRepository).saveAndFlush(campaign);
    }

    @Test
    void updateWithUnchangedValuesReportsNothingAndDoesNotSave() {
        existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"campaignName": "Newsletter", "emailMessage": "Dear client", "campaignType": 1, "businessRuleId": 3,
                 "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertEquals(Map.of(), changesOf(result));
        verify(emailCampaignRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateWithAnEmptyMessageClearsItAndReportsTheEmptyString() {
        final EmailCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"emailMessage": "", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertEquals(Map.of("emailMessage", ""), result.getChanges());
        assertNull(campaign.getEmailMessage());
    }

    @Test
    void updateReportsTheCampaignTypeAsAnEnum() {
        final EmailCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"campaignType": 3, "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertEquals(Map.of("campaignType", EmailCampaignType.TRIGGERED), result.getChanges());
        assertEquals(3, campaign.getCampaignType());
    }

    @Test
    void updateOfTheBusinessRuleReportsTheIdAsTextAndLinksTheNewReport() {
        final EmailCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"businessRuleId": 7, "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertEquals(Map.of("businessRuleId", "7"), result.getChanges());
        assertSame(otherBusinessRule, campaign.getBusinessRuleId());
    }

    /**
     * Documents existing behaviour: the email subject is not part of the update.
     */
    @Test
    void updateIgnoresTheEmailSubject() {
        final EmailCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"emailSubject": "New subject", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertEquals(Map.of(), changesOf(result));
        assertEquals("Hello", campaign.getEmailSubject());
    }

    /**
     * Documents existing behaviour: the start date is reported as changed whenever it is sent, because the change check
     * compares the stored date-time with a time of day.
     */
    @Test
    void updateOfTheStartDateReportsItWithTheDateFormatAndLocale() {
        final EmailCampaign campaign = existing("""
                {"campaignName": "Newsletter", "campaignType": 2, "businessRuleId": 3, "paramValue": "{}",
                 "emailSubject": "Hello", "emailMessage": "Dear client", "recurrence": "FREQ=DAILY",
                 "recurrenceStartDate": "10 January 2099 10:30", "dateFormat": "dd MMMM yyyy HH:mm", "locale": "en"}
                """);

        final CommandProcessingResult result = update("""
                {"recurrenceStartDate": "11 January 2099 08:00", "dateFormat": "dd MMMM yyyy HH:mm",
                 "timeFormat": "dd MMMM yyyy HH:mm", "locale": "en"}
                """);

        assertEquals(List.of("recurrenceStartDate", "dateFormat", "locale"), List.copyOf(result.getChanges().keySet()));
        assertEquals("11 January 2099 08:00", result.getChanges().get("recurrenceStartDate"));
        assertEquals("dd MMMM yyyy HH:mm", result.getChanges().get("dateFormat"));
        assertEquals("en", result.getChanges().get("locale"));
        assertEquals(LocalDateTime.of(2099, 1, 11, 8, 0), campaign.getRecurrenceStartDate());
    }

    /**
     * Documents a known defect, not intended behaviour: the start date change check reads the value with the separate
     * {@code timeFormat} parameter, so an update of the start date without it fails. Because the update validator does
     * not accept {@code timeFormat}, updating a scheduled campaign through the API returns 500. Tracked in
     * <a href="https://issues.apache.org/jira/browse/FINERACT-3315">FINERACT-3315</a>; update this test when that is
     * fixed.
     */
    @Test
    void updateOfTheStartDateWithoutATimeFormatFails() {
        existing(DIRECT_CAMPAIGN);

        assertThrows(NullPointerException.class, () -> update("""
                {"recurrenceStartDate": "11 January 2099 08:00", "dateFormat": "dd MMMM yyyy HH:mm", "locale": "en"}
                """));
        verify(emailCampaignRepository, never()).saveAndFlush(any());
    }
}
