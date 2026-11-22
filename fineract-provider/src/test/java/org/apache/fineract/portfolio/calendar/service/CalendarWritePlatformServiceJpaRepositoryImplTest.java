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
package org.apache.fineract.portfolio.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.calendar.data.CalendarCreateRequest;
import org.apache.fineract.portfolio.calendar.data.CalendarDeleteRequest;
import org.apache.fineract.portfolio.calendar.data.CalendarUpdateRequest;
import org.apache.fineract.portfolio.calendar.data.CalendarUpdateResponse;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarHistoryRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.exception.CalendarDeleteNotAllowedException;
import org.apache.fineract.portfolio.calendar.exception.CalendarEntityTypeNotSupportedException;
import org.apache.fineract.portfolio.calendar.exception.CalendarNotFoundException;
import org.apache.fineract.portfolio.calendar.exception.CalendarParameterUpdateNotSupportedException;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.meeting.domain.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CalendarWritePlatformServiceJpaRepositoryImplTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final Long GROUP_ID = 7L;
    private static final Long CALENDAR_ID = 11L;

    private CalendarRepository calendarRepository;
    private CalendarInstanceRepository calendarInstanceRepository;
    private GroupRepositoryWrapper groupRepository;
    private MeetingRepository meetingRepository;
    private CalendarWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        calendarRepository = mock(CalendarRepository.class);
        calendarInstanceRepository = mock(CalendarInstanceRepository.class);
        groupRepository = mock(GroupRepositoryWrapper.class);
        meetingRepository = mock(MeetingRepository.class);
        final ConfigurationDomainService configurationDomainService = mock(ConfigurationDomainService.class);
        service = new CalendarWritePlatformServiceJpaRepositoryImpl(calendarRepository, mock(CalendarHistoryRepository.class),
                calendarInstanceRepository, mock(LoanWritePlatformService.class), configurationDomainService, groupRepository,
                mock(LoanRepositoryWrapper.class), mock(ClientRepositoryWrapper.class), meetingRepository);

        final Group group = mock(Group.class);
        when(group.getId()).thenReturn(GROUP_ID);
        when(group.getActivationDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(group.isGroup()).thenReturn(true);
        when(groupRepository.findOneWithNotFoundDetection(anyLong())).thenReturn(group);
        when(calendarRepository.saveAndFlush(any(Calendar.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(calendarInstanceRepository.countOfLoansSyncedWithCalendar(any(), any())).thenReturn(0);
    }

    @Test
    void createKeepsEveryFieldOfARepeatingCalendar() {
        service.createCalendar(createRequest().description("Weekly collection").location("Village hall").endDate("31 December 2024")
                .duration(60).repeating(true).frequency(CalendarFrequencyType.WEEKLY.getValue()).interval(2).repeatsOnDay(1).remindById(1)
                .firstReminder(10).secondReminder(5).meetingtime("10:30").timeFormat("HH:mm").build());

        final Calendar calendar = savedCalendar();
        assertThat(calendar.getTitle()).isEqualTo("Collection meeting");
        assertThat(calendar.getDescription()).isEqualTo("Weekly collection");
        assertThat(calendar.getLocation()).isEqualTo("Village hall");
        assertThat(calendar.getStartDate()).isEqualTo(LocalDate.of(2024, 3, 4));
        assertThat(calendar.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(calendar.getDuration()).isEqualTo(60);
        assertThat(calendar.getTypeId()).isEqualTo(1);
        assertThat(calendar.isRepeating()).isTrue();
        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=WEEKLY;INTERVAL=2;BYDAY=MO");
        assertThat(calendar.getRemindById()).isEqualTo(1);
        assertThat(calendar.getFirstReminder()).isEqualTo(10);
        assertThat(calendar.getSecondReminder()).isEqualTo(5);
        assertThat(calendar.getMeetingTime()).isEqualTo(LocalTime.of(10, 30));
    }

    @Test
    void createBuildsAMonthlyOnDayRecurrence() {
        service.createCalendar(createRequest().repeating(true).frequency(CalendarFrequencyType.MONTHLY.getValue())
                .repeatsOnNthDayOfMonth(-2).repeatsOnDayOfMonth(15).build());

        assertThat(savedCalendar().getRecurrence()).isEqualTo("FREQ=MONTHLY;BYMONTHDAY=15");
    }

    @Test
    void createWithoutRepeatingStoresNoRecurrence() {
        service.createCalendar(createRequest().typeId(4).frequency(CalendarFrequencyType.WEEKLY.getValue()).repeatsOnDay(1).build());

        final Calendar calendar = savedCalendar();
        assertThat(calendar.isRepeating()).isFalse();
        assertThat(calendar.getRecurrence()).isNull();
    }

    @Test
    void createRejectsARepeatingCalendarWithoutFrequency() {
        assertValidationError(() -> service.createCalendar(createRequest().repeating(true).build()), "frequency");
    }

    @Test
    void createRejectsAWeeklyCalendarWithoutRepeatsOnDay() {
        assertValidationError(
                () -> service.createCalendar(createRequest().repeating(true).frequency(CalendarFrequencyType.WEEKLY.getValue()).build()),
                "repeatsOnDay");
    }

    @Test
    void createRejectsAStartDateWithoutDateFormat() {
        assertValidationError(() -> service.createCalendar(createRequest().dateFormat(null).build()), "startDate");
    }

    @Test
    void createRejectsAStartDateThatDoesNotMatchTheDateFormat() {
        assertValidationError(() -> service.createCalendar(createRequest().startDate("2024-03-04").build()), "startDate");
    }

    @Test
    void createRejectsAMeetingTimeWithoutTimeFormat() {
        assertValidationError(() -> service.createCalendar(weeklyCreateRequest().meetingtime("10:30").build()), "meetingtime");
    }

    @Test
    void createRejectsAnUnknownEntityType() {
        assertThatThrownBy(() -> service.createCalendar(weeklyCreateRequest().entityType("staff").build()))
                .isInstanceOf(CalendarEntityTypeNotSupportedException.class);
    }

    @Test
    void updateWithOnlyIntervalKeepsTheStoredDay() {
        final Calendar calendar = storedWeeklyCalendar();

        final CalendarUpdateResponse response = service.updateCalendar(updateRequest().interval(3).build());

        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=WEEKLY;INTERVAL=3;BYDAY=MO");
        assertThat(response.getChanges()).containsEntry("recurrence", "FREQ=WEEKLY;INTERVAL=3;BYDAY=MO");
    }

    @Test
    void updateWithOnlyRepeatsOnDayKeepsTheStoredInterval() {
        final Calendar calendar = storedWeeklyCalendar();

        service.updateCalendar(updateRequest().repeatsOnDay(3).build());

        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=WEEKLY;INTERVAL=2;BYDAY=WE");
    }

    @Test
    void updateOfTheTitleLeavesTheRecurrenceAlone() {
        final Calendar calendar = storedWeeklyCalendar();

        final CalendarUpdateResponse response = service.updateCalendar(updateRequest().title("Renamed").build());

        assertThat(calendar.getTitle()).isEqualTo("Renamed");
        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=WEEKLY;INTERVAL=2;BYDAY=MO");
        assertThat(response.getChanges()).containsOnlyKeys("title");
    }

    @Test
    void updateWithAnEmptyDescriptionOrLocationClearsIt() {
        final Calendar calendar = new Calendar("Meeting", "Old description", "Old location", LocalDate.of(2024, 3, 4), null, null, 4, false,
                null, null, null, null, null);
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(calendar));

        service.updateCalendar(updateRequest().description("").build());
        assertThat(calendar.getDescription()).isNull();
        assertThat(calendar.getLocation()).isEqualTo("Old location");

        service.updateCalendar(updateRequest().location("").build());
        assertThat(calendar.getLocation()).isNull();
    }

    @Test
    void updateRejectsAnIntervalChangeWhileLoansAreSynced() {
        storedWeeklyCalendar();
        when(calendarInstanceRepository.countOfLoansSyncedWithCalendar(any(), any())).thenReturn(1);

        assertThatThrownBy(() -> service.updateCalendar(updateRequest().interval(3).build()))
                .isInstanceOf(CalendarParameterUpdateNotSupportedException.class);
    }

    @Test
    void updateRejectsABlankTitle() {
        storedWeeklyCalendar();

        assertValidationError(() -> service.updateCalendar(updateRequest().title(" ").build()), "title");
    }

    @Test
    void updateRejectsARescheduleWithoutMeetingDates() {
        storedWeeklyCalendar();

        assertValidationError(() -> service.updateCalendar(updateRequest().reschedulebasedOnMeetingDates(true).build()),
                "presentMeetingDate");
    }

    @Test
    void updateRejectsTurningOnRepeatingWithoutFrequency() {
        final Calendar calendar = new Calendar("Meeting", null, null, LocalDate.of(2024, 3, 4), null, null, 4, false, null, null, null,
                null, null);
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(calendar));

        assertValidationError(() -> service.updateCalendar(updateRequest().repeating(true).build()), "frequency");
    }

    @Test
    void updateWithOnlyTheNthDayKeepsTheStoredWeekday() {
        final Calendar calendar = storedCalendar("FREQ=MONTHLY;BYSETPOS=2;BYDAY=TU");

        service.updateCalendar(updateRequest().repeatsOnNthDayOfMonth(3).build());

        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=MONTHLY;BYSETPOS=3;BYDAY=TU");
    }

    @Test
    void updateWithOnlyTheMonthlyWeekdayKeepsTheStoredNthDay() {
        final Calendar calendar = storedCalendar("FREQ=MONTHLY;BYSETPOS=2;BYDAY=TU");

        service.updateCalendar(updateRequest().repeatsOnLastWeekdayOfMonth(4).build());

        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=MONTHLY;BYSETPOS=2;BYDAY=TH");
    }

    @Test
    void updateWithOnlyIntervalKeepsTheStoredMonthlyDay() {
        final Calendar calendar = storedCalendar("FREQ=MONTHLY;BYSETPOS=2;BYDAY=TU");

        service.updateCalendar(updateRequest().interval(2).build());

        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=MONTHLY;INTERVAL=2;BYSETPOS=2;BYDAY=TU");
    }

    @Test
    void updateWithOnlyTheDayOfMonthKeepsMeetingOnADay() {
        final Calendar calendar = storedCalendar("FREQ=MONTHLY;BYMONTHDAY=15");

        service.updateCalendar(updateRequest().repeatsOnDayOfMonth(20).build());

        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=MONTHLY;BYMONTHDAY=20");
    }

    @Test
    void updateOfTheLastDayOfTheMonthToALastWeekday() {
        final Calendar calendar = storedCalendar("FREQ=MONTHLY;BYMONTHDAY=-1");

        service.updateCalendar(updateRequest().repeatsOnLastWeekdayOfMonth(5).build());

        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=MONTHLY;BYSETPOS=-1;BYDAY=FR");
    }

    @Test
    void updateToTheLastWithoutAWeekdayMeansTheLastDayOfTheMonth() {
        final Calendar calendar = storedCalendar("FREQ=MONTHLY;BYSETPOS=2;BYDAY=TU");

        service.updateCalendar(updateRequest().repeatsOnNthDayOfMonth(-1).build());

        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=MONTHLY;BYMONTHDAY=-1");
    }

    @Test
    void updateToTheLastWithAWeekdayMeansTheLastWeekday() {
        final Calendar calendar = storedCalendar("FREQ=MONTHLY;BYSETPOS=2;BYDAY=TU");

        service.updateCalendar(updateRequest().repeatsOnNthDayOfMonth(-1).repeatsOnLastWeekdayOfMonth(2).build());

        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=MONTHLY;BYSETPOS=-1;BYDAY=TU");
    }

    @Test
    void updateRejectsAnNthDayWithoutAWeekday() {
        final Calendar calendar = storedCalendar("FREQ=MONTHLY;BYMONTHDAY=15");

        assertValidationError(() -> service.updateCalendar(updateRequest().repeatsOnNthDayOfMonth(2).build()),
                "repeatsOnLastWeekdayOfMonth");
        assertThat(calendar.getRecurrence()).isEqualTo("FREQ=MONTHLY;BYMONTHDAY=15");
    }

    @Test
    void updateRejectsMeetingOnADayWithoutTheDay() {
        storedCalendar("FREQ=MONTHLY;BYSETPOS=2;BYDAY=TU");

        assertValidationError(() -> service.updateCalendar(updateRequest().repeatsOnNthDayOfMonth(-2).build()), "repeatsOnDayOfMonth");
    }

    private Calendar storedCalendar(final String recurrence) {
        final Calendar calendar = Calendar.createRepeatingCalendar("Meeting", LocalDate.of(2024, 3, 4), 1, recurrence);
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(calendar));
        return calendar;
    }

    private Calendar storedWeeklyCalendar() {
        final Calendar calendar = Calendar.createRepeatingCalendar("Meeting", LocalDate.of(2024, 3, 4), 1, CalendarFrequencyType.WEEKLY, 2,
                1, null);
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(calendar));
        return calendar;
    }

    @Test
    void deleteRemovesCalendarInstancesAndCalendar() {
        final Calendar calendar = existingCalendar();
        final List<CalendarInstance> instances = List.of(instance(21L, calendar, CalendarEntityType.GROUPS),
                instance(22L, calendar, CalendarEntityType.CENTERS));
        when(calendarInstanceRepository.findByCalendarId(CALENDAR_ID)).thenReturn(instances);
        when(meetingRepository.existsByCalendarInstanceIdIn(List.of(21L, 22L))).thenReturn(false);

        assertThat(service.deleteCalendar(deleteRequest()).getResourceId()).isEqualTo(CALENDAR_ID);

        verify(calendarInstanceRepository).deleteAll(instances);
        verify(calendarRepository).delete(calendar);
    }

    @Test
    void deleteRejectsCalendarLinkedToLoan() {
        final Calendar calendar = existingCalendar();
        final List<CalendarInstance> instances = List.of(instance(21L, calendar, CalendarEntityType.GROUPS),
                instance(23L, calendar, CalendarEntityType.LOANS));
        when(calendarInstanceRepository.findByCalendarId(CALENDAR_ID)).thenReturn(instances);

        assertThatThrownBy(() -> service.deleteCalendar(deleteRequest())).isInstanceOfSatisfying(CalendarDeleteNotAllowedException.class,
                e -> assertThat(e.getGlobalisationMessageCode()).isEqualTo("error.msg.calendar.delete.not.allowed.linked.to.account"));
        verify(calendarRepository, never()).delete(any(Calendar.class));
    }

    @Test
    void deleteRejectsCalendarWithMeetings() {
        final Calendar calendar = existingCalendar();
        final List<CalendarInstance> instances = List.of(instance(21L, calendar, CalendarEntityType.GROUPS));
        when(calendarInstanceRepository.findByCalendarId(CALENDAR_ID)).thenReturn(instances);
        when(meetingRepository.existsByCalendarInstanceIdIn(List.of(21L))).thenReturn(true);

        assertThatThrownBy(() -> service.deleteCalendar(deleteRequest())).isInstanceOfSatisfying(CalendarDeleteNotAllowedException.class,
                e -> assertThat(e.getGlobalisationMessageCode()).isEqualTo("error.msg.calendar.delete.not.allowed.has.meetings"));
        verify(calendarRepository, never()).delete(any(Calendar.class));
    }

    @Test
    void deleteOfUnknownCalendarFailsWithNotFound() {
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCalendar(deleteRequest())).isInstanceOf(CalendarNotFoundException.class);
    }

    private Calendar existingCalendar() {
        final Calendar calendar = new Calendar("Meeting", null, null, LocalDate.of(2024, 3, 4), null, null, 4, false, null, null, null,
                null, null);
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(calendar));
        return calendar;
    }

    private static CalendarInstance instance(final Long id, final Calendar calendar, final CalendarEntityType entityType) {
        final CalendarInstance instance = spy(CalendarInstance.from(calendar, 5L, entityType.getValue()));
        doReturn(id).when(instance).getId();
        return instance;
    }

    private static CalendarDeleteRequest deleteRequest() {
        return CalendarDeleteRequest.builder().calendarId(CALENDAR_ID).build();
    }

    private Calendar savedCalendar() {
        final ArgumentCaptor<Calendar> captor = ArgumentCaptor.forClass(Calendar.class);
        verify(calendarRepository).saveAndFlush(captor.capture());
        return captor.getValue();
    }

    private static CalendarCreateRequest.CalendarCreateRequestBuilder createRequest() {
        return CalendarCreateRequest.builder().entityType("groups").entityId(GROUP_ID).title("Collection meeting")
                .startDate("04 March 2024").typeId(1).dateFormat(DATE_FORMAT).locale("en");
    }

    private static CalendarCreateRequest.CalendarCreateRequestBuilder weeklyCreateRequest() {
        return createRequest().repeating(true).frequency(CalendarFrequencyType.WEEKLY.getValue()).repeatsOnDay(1);
    }

    private static CalendarUpdateRequest.CalendarUpdateRequestBuilder updateRequest() {
        return CalendarUpdateRequest.builder().entityType("clients").entityId(3L).calendarId(CALENDAR_ID).dateFormat(DATE_FORMAT)
                .locale("en");
    }

    private static void assertValidationError(final Runnable call, final String parameterName) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(PlatformApiDataValidationException.class,
                e -> assertThat(e.getErrors()).extracting(ApiParameterError::getParameterName).contains(parameterName));
    }
}
