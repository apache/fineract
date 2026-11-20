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
package org.apache.fineract.infrastructure.campaigns.sms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.apache.fineract.infrastructure.campaigns.constants.CampaignType;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.sms.serialization.SmsCampaignValidator;
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
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.scheduler.SmsMessageScheduledJobService;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SmsCampaignWritePlatformServiceJpaImplTest {

    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 1, 15);
    private static final Long CAMPAIGN_ID = 12L;
    private static final Long REPORT_ID = 3L;
    private static final Long OTHER_REPORT_ID = 7L;

    private static final String DIRECT_CAMPAIGN = """
            {"campaignName": "Reminder", "campaignType": 1, "triggerType": 1, "providerId": 5, "runReportId": 3,
             "paramValue": {"officeId": 1}, "message": "Hello"}
            """;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private SmsCampaignRepository smsCampaignRepository;
    @Mock
    private SmsCampaignValidator smsCampaignValidator;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private SmsMessageRepository smsMessageRepository;
    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private ReadReportingService readReportingService;
    @Mock
    private GenericDataService genericDataService;
    @Mock
    private FromJsonHelper fromJsonHelper;
    @Mock
    private SmsMessageScheduledJobService smsMessageScheduledJobService;

    private final AppUser currentUser = mock(AppUser.class);
    private final Report report = mock(Report.class);
    private final Report otherReport = mock(Report.class);

    private SmsCampaignWritePlatformServiceJpaImpl service;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
        service = new SmsCampaignWritePlatformServiceJpaImpl(context, smsCampaignRepository, smsCampaignValidator, reportRepository,
                smsMessageRepository, clientRepositoryWrapper, groupRepository, readReportingService, genericDataService, fromJsonHelper,
                smsMessageScheduledJobService);
        lenient().when(context.authenticatedUser()).thenReturn(currentUser);
        lenient().when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
        lenient().when(reportRepository.findById(OTHER_REPORT_ID)).thenReturn(Optional.of(otherReport));
        lenient().when(report.getId()).thenReturn(REPORT_ID);
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

    private SmsCampaign create(final String json) {
        service.create(command(json));
        final ArgumentCaptor<SmsCampaign> saved = ArgumentCaptor.forClass(SmsCampaign.class);
        verify(smsCampaignRepository).saveAndFlush(saved.capture());
        return saved.getValue();
    }

    private SmsCampaign existing(final String json) {
        final SmsCampaign campaign = create(json);
        clearInvocations(smsCampaignRepository);
        when(smsCampaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        return campaign;
    }

    private CommandProcessingResult update(final String json) {
        return service.update(CAMPAIGN_ID, command(json));
    }

    private static Object field(final SmsCampaign campaign, final String name) {
        return ReflectionTestUtils.getField(campaign, name);
    }

    @Test
    void createTakesTheValuesFromTheCommand() {
        final SmsCampaign campaign = create(DIRECT_CAMPAIGN);

        assertEquals("Reminder", campaign.getCampaignName());
        assertEquals(1, field(campaign, "campaignType"));
        assertEquals(1, field(campaign, "triggerType"));
        assertSame(report, campaign.getBusinessRuleId());
        assertEquals(5L, campaign.getProviderId());
        assertEquals("{\"officeId\":1}", campaign.getParamValue());
        assertEquals("Hello", campaign.getMessage());
        assertEquals(BUSINESS_DATE, campaign.getSubmittedOnDate());
        assertSame(currentUser, field(campaign, "submittedBy"));
        assertFalse(campaign.isNotification());
        assertNull(campaign.getRecurrence());
        assertNull(campaign.getRecurrenceStartDate());
        assertTrue(campaign.isPending());
    }

    @Test
    void createIgnoresTheProviderForANotificationCampaign() {
        final SmsCampaign campaign = create("""
                {"campaignName": "Reminder", "campaignType": 2, "triggerType": 1, "providerId": 5, "runReportId": 3,
                 "paramValue": {"officeId": 1}, "message": "Hello", "isNotification": true}
                """);

        assertTrue(campaign.isNotification());
        assertNull(campaign.getProviderId());
    }

    @Test
    void createUsesTheSubmittedOnDateFromTheCommand() {
        final SmsCampaign campaign = create("""
                {"campaignName": "Reminder", "campaignType": 1, "triggerType": 1, "providerId": 5, "runReportId": 3,
                 "paramValue": {"officeId": 1}, "message": "Hello",
                 "submittedOnDate": "10 January 2026", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertEquals(LocalDate.of(2026, 1, 10), campaign.getSubmittedOnDate());
    }

    @Test
    void createOfAScheduledCampaignBuildsTheRecurrenceAndParsesTheStartDate() {
        final SmsCampaign campaign = create("""
                {"campaignName": "Reminder", "campaignType": 1, "triggerType": 2, "providerId": 5, "runReportId": 3,
                 "paramValue": {"officeId": 1}, "message": "Hello", "locale": "en",
                 "dateTimeFormat": "dd MMMM yyyy HH:mm", "recurrenceStartDate": "10 January 2099 10:30",
                 "frequency": 2, "interval": 2, "repeatsOnDay": 3}
                """);

        assertTrue(campaign.isSchedule());
        assertEquals("FREQ=WEEKLY;INTERVAL=2;BYDAY=WE", campaign.getRecurrence());
        assertEquals(LocalDateTime.of(2099, 1, 10, 10, 30), campaign.getRecurrenceStartDate());
    }

    @Test
    void createOfAScheduledCampaignWithoutADateTimeFormatStartsNowWithoutRecurrence() {
        final SmsCampaign campaign = create("""
                {"campaignName": "Reminder", "campaignType": 1, "triggerType": 2, "providerId": 5, "runReportId": 3,
                 "paramValue": {"officeId": 1}, "message": "Hello", "locale": "en"}
                """);

        assertNull(campaign.getRecurrence());
        assertNotNull(campaign.getRecurrenceStartDate());
    }

    @Test
    void updateAppliesAndReportsTheChangedNameAndMessage() {
        final SmsCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"message": "Hi there", "campaignName": "Renamed"}
                """);

        assertEquals(List.of("campaignName", "message"), List.copyOf(result.getChanges().keySet()));
        assertEquals("Renamed", result.getChanges().get("campaignName"));
        assertEquals("Hi there", result.getChanges().get("message"));
        assertEquals("Renamed", campaign.getCampaignName());
        assertEquals("Hi there", campaign.getMessage());
        verify(smsCampaignRepository).existsByCampaignNameAndIdNot("Renamed", CAMPAIGN_ID);
        verify(smsCampaignRepository).saveAndFlush(campaign);
    }

    @Test
    void updateWithUnchangedValuesReportsNothingAndDoesNotSave() {
        existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"campaignName": "Reminder", "message": "Hello", "campaignType": 1, "triggerType": 1, "providerId": 5,
                 "runReportId": 3, "isNotification": false, "locale": "en"}
                """);

        assertEquals(Map.of(), changesOf(result));
        verify(smsCampaignRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateWithAnEmptyMessageClearsItAndReportsTheEmptyString() {
        final SmsCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"message": ""}
                """);

        assertEquals(Map.of("message", ""), result.getChanges());
        assertNull(campaign.getMessage());
    }

    @Test
    void updateReportsTheCampaignAndTriggerTypesAsEnums() {
        final SmsCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"campaignType": 2, "triggerType": 3, "locale": "en"}
                """);

        assertEquals(CampaignType.NOTIFICATION, result.getChanges().get("campaignType"));
        assertEquals(SmsCampaignTriggerType.TRIGGERED, result.getChanges().get("triggerType"));
        assertEquals(2, field(campaign, "campaignType"));
        assertEquals(3, field(campaign, "triggerType"));
    }

    @Test
    void updateOfTheReportReportsTheIdAsTextAndLinksTheNewReport() {
        final SmsCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"runReportId": 7}
                """);

        assertEquals(Map.of("runReportId", "7"), result.getChanges());
        assertSame(otherReport, campaign.getBusinessRuleId());
    }

    /**
     * Documents existing behaviour: a changed provider is reported, but not applied to the campaign.
     */
    @Test
    void updateReportsAChangedProviderButDoesNotApplyIt() {
        final SmsCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"providerId": 9}
                """);

        assertEquals(Map.of("providerId", 9L), result.getChanges());
        assertEquals(5L, campaign.getProviderId());
    }

    @Test
    void updateAppliesAndReportsTheNotificationFlag() {
        final SmsCampaign campaign = existing(DIRECT_CAMPAIGN);

        final CommandProcessingResult result = update("""
                {"isNotification": true}
                """);

        assertEquals(Map.of("isNotification", true), result.getChanges());
        assertTrue(campaign.isNotification());
    }

    /**
     * Documents existing behaviour: a scheduled campaign always reports and re-parses the recurrence start date, even
     * when nothing else changed.
     */
    @Test
    void updateOfAScheduledCampaignAlwaysReportsAndParsesTheStartDate() {
        final SmsCampaign campaign = existing("""
                {"campaignName": "Reminder", "campaignType": 1, "triggerType": 2, "providerId": 5, "runReportId": 3,
                 "paramValue": {"officeId": 1}, "message": "Hello", "locale": "en",
                 "dateTimeFormat": "dd MMMM yyyy HH:mm", "recurrenceStartDate": "10 January 2099 10:30",
                 "frequency": 1, "interval": 1}
                """);

        final CommandProcessingResult result = update("""
                {"recurrenceStartDate": "11 January 2099 08:00", "dateTimeFormat": "dd MMMM yyyy HH:mm",
                 "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """);

        assertEquals(List.of("recurrenceStartDate", "dateFormat", "dateTimeFormat", "locale"), List.copyOf(result.getChanges().keySet()));
        assertEquals("11 January 2099 08:00", result.getChanges().get("recurrenceStartDate"));
        assertEquals("dd MMMM yyyy", result.getChanges().get("dateFormat"));
        assertEquals("dd MMMM yyyy HH:mm", result.getChanges().get("dateTimeFormat"));
        assertEquals("en", result.getChanges().get("locale"));
        assertEquals(LocalDateTime.of(2099, 1, 11, 8, 0), campaign.getRecurrenceStartDate());
        assertEquals("FREQ=DAILY", campaign.getRecurrence());
        verify(smsCampaignRepository).saveAndFlush(campaign);
    }
}
