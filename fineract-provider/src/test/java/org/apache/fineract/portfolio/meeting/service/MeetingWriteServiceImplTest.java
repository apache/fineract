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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceData;
import org.apache.fineract.portfolio.meeting.domain.Meeting;
import org.apache.fineract.portfolio.meeting.domain.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeetingWriteServiceImplTest {

    private static final Long CLIENT_ID = 7L;
    private static final Long CENTER_ID = 5L;
    private static final Long GROUP_ID = 2L;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private CalendarInstanceRepository calendarInstanceRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private CalendarInstance calendarInstance;

    @Mock
    private Client client;

    @Mock
    private Group group;

    private MeetingWriteServiceImpl service;
    private Meeting meeting;

    @BeforeEach
    void setUp() {
        service = new MeetingWriteServiceImpl(meetingRepository, calendarInstanceRepository, calendarRepository, clientRepository,
                groupRepository, configurationDomainService);
        meeting = new Meeting();
        meeting.setCalendarInstance(calendarInstance);
    }

    @Test
    void centerAttendanceAcceptsClientOfChildGroup() {
        givenCenterMeeting();
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(groupRepository.findByParentId(CENTER_ID)).thenReturn(List.of(group));
        when(group.isChildClient(CLIENT_ID)).thenReturn(true);

        var attendances = service.getClientsAttendance(meeting, List.of(presentAttendance()));

        assertEquals(1, attendances.size());
        var attendance = attendances.iterator().next();
        assertSame(client, attendance.getClient());
        assertSame(meeting, attendance.getMeeting());
        assertEquals(1, attendance.getAttendanceTypeId());
    }

    @Test
    void centerAttendanceRejectsClientNotInCenterWithClientId() {
        givenCenterMeeting();
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(groupRepository.findByParentId(CENTER_ID)).thenReturn(List.of(group));
        when(group.isChildClient(CLIENT_ID)).thenReturn(false);

        var attendances = List.of(presentAttendance());
        var exception = assertThrows(ClientNotInGroupException.class, () -> service.getClientsAttendance(meeting, attendances));

        assertEquals("error.msg.client.not.in.center", exception.getGlobalisationMessageCode());
        assertEquals("Client with identifier 7 is not in center 5", exception.getDefaultUserMessage());
    }

    @Test
    void groupAttendanceRejectsClientNotInGroupWithClientId() {
        when(calendarInstance.getEntityTypeId()).thenReturn(CalendarEntityType.GROUPS.getValue());
        when(calendarInstance.getEntityId()).thenReturn(GROUP_ID);
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(client.isChildOfGroup(GROUP_ID)).thenReturn(false);

        var attendances = List.of(presentAttendance());
        var exception = assertThrows(ClientNotInGroupException.class, () -> service.getClientsAttendance(meeting, attendances));

        assertEquals("Client with identifier 7 is not in Group with identifier 2", exception.getDefaultUserMessage());
    }

    @Test
    void missingAttendanceListMeansNoAttendance() {
        var attendances = service.getClientsAttendance(meeting, null);

        assertTrue(attendances.isEmpty());
        verifyNoInteractions(clientRepository, groupRepository);
    }

    private void givenCenterMeeting() {
        when(calendarInstance.getEntityTypeId()).thenReturn(CalendarEntityType.CENTERS.getValue());
        when(calendarInstance.getEntityId()).thenReturn(CENTER_ID);
    }

    private static MeetingAttendanceData presentAttendance() {
        return MeetingAttendanceData.builder().clientId(CLIENT_ID)
                .attendanceType(new EnumOptionData(1L, "attendanceType.present", "Present")).build();
    }
}
