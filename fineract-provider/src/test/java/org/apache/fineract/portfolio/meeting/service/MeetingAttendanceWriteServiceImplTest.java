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
package org.apache.fineract.portfolio.meeting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceData;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceUpdateRequest;
import org.apache.fineract.portfolio.meeting.domain.Meeting;
import org.apache.fineract.portfolio.meeting.domain.MeetingAttendance;
import org.apache.fineract.portfolio.meeting.domain.MeetingRepository;
import org.apache.fineract.portfolio.meeting.exception.MeetingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeetingAttendanceWriteServiceImplTest {

    private static final Long GROUP_ID = 2L;
    private static final Long MEETING_ID = 3L;
    private static final Long CLIENT_ID = 5L;
    private static final int PRESENT = 1;
    private static final int ABSENT = 2;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingWriteServiceImpl meetingWriteService;

    @Mock
    private CalendarInstance calendarInstance;

    @Mock
    private Client client;

    private MeetingAttendanceWriteServiceImpl service;
    private Meeting meeting;
    private List<MeetingAttendanceData> requestedAttendance;
    private MeetingAttendanceUpdateRequest request;

    @BeforeEach
    void setUp() {
        service = new MeetingAttendanceWriteServiceImpl(meetingRepository, meetingWriteService);
        meeting = new Meeting();
        meeting.setId(MEETING_ID);
        meeting.setCalendarInstance(calendarInstance);
        meeting.setClientsAttendance(new HashSet<>());
        requestedAttendance = List.of(MeetingAttendanceData.builder().clientId(CLIENT_ID).build());
        request = MeetingAttendanceUpdateRequest.builder().id(MEETING_ID).entityId(GROUP_ID).entityType(CalendarEntityType.GROUPS)
                .meetingAttendance(requestedAttendance).build();
    }

    @Test
    void updatesTheMeetingFromTheUrl() {
        givenMeetingWithRequestedAttendance(ABSENT);

        var response = service.updateMeetingAttendance(request);

        verify(meetingRepository, never()).findById(GROUP_ID);
        verify(meetingRepository).saveAndFlush(meeting);
        assertEquals(MEETING_ID, response.getEntityId());
        assertEquals(GROUP_ID, response.getGroupId());
    }

    @Test
    void unknownMeetingIsNotFound() {
        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(MeetingNotFoundException.class, () -> service.updateMeetingAttendance(request));

        assertEquals("Meeting with identifier 3 does not exist", exception.getDefaultUserMessage());
    }

    @Test
    void newAttendeeIsAddedToTheMeeting() {
        var newAttendance = givenMeetingWithRequestedAttendance(PRESENT);

        service.updateMeetingAttendance(request);

        assertEquals(Set.of(newAttendance), meeting.getClientsAttendance());
    }

    @Test
    void existingAttendeeIsUpdated() {
        var existingAttendance = new MeetingAttendance(client, meeting, PRESENT);
        meeting.getClientsAttendance().add(existingAttendance);
        givenMeetingWithRequestedAttendance(ABSENT);

        service.updateMeetingAttendance(request);

        assertEquals(1, meeting.getClientsAttendance().size());
        assertSame(existingAttendance, meeting.getClientsAttendance().iterator().next());
        assertEquals(ABSENT, existingAttendance.getAttendanceTypeId());
    }

    @Test
    void clientsAttendanceIsAcceptedAsTheAttendanceList() throws Exception {
        var json = "{\"clientsAttendance\": [{\"clientId\": 5}]}";

        var parsed = new ObjectMapper().readValue(json, MeetingAttendanceUpdateRequest.class);

        assertEquals(1, parsed.getMeetingAttendance().size());
        assertEquals(CLIENT_ID, parsed.getMeetingAttendance().get(0).getClientId());
    }

    private MeetingAttendance givenMeetingWithRequestedAttendance(int attendanceType) {
        var attendance = new MeetingAttendance(client, meeting, attendanceType);
        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.of(meeting));
        when(meetingWriteService.getClientsAttendance(meeting, requestedAttendance)).thenReturn(new HashSet<>(Set.of(attendance)));
        when(client.getId()).thenReturn(CLIENT_ID);
        when(calendarInstance.getEntityTypeId()).thenReturn(CalendarEntityType.GROUPS.getValue());
        when(calendarInstance.getEntityId()).thenReturn(GROUP_ID);
        return attendance;
    }
}
