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

import static java.util.Objects.requireNonNull;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.attendanceTypeParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.clientIdParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.clientsAttendanceParamName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceUpdateRequest;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceUpdateResponse;
import org.apache.fineract.portfolio.meeting.domain.Meeting;
import org.apache.fineract.portfolio.meeting.domain.MeetingAttendance;
import org.apache.fineract.portfolio.meeting.domain.MeetingRepository;
import org.apache.fineract.portfolio.meeting.exception.MeetingNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnMissingBean(value = MeetingAttendanceWriteService.class, ignored = MeetingAttendanceWriteServiceImpl.class)
public class MeetingAttendanceWriteServiceImpl implements MeetingAttendanceWriteService {

    private final MeetingRepository meetingRepository;
    private final MeetingWriteServiceImpl writeService;

    @Override
    public MeetingAttendanceUpdateResponse updateMeetingAttendance(final MeetingAttendanceUpdateRequest request) {
        var meeting = meetingRepository.findById(request.getEntityId())
                .orElseThrow(() -> new MeetingNotFoundException(request.getEntityId()));
        var clientsAttendance = writeService.getClientsAttendance(meeting, request.getMeetingAttendance());
        var changes = updateAttendance(meeting, clientsAttendance);

        meetingRepository.saveAndFlush(meeting);

        var groupId = CalendarEntityType.isGroup(meeting.getCalendarInstance().getEntityTypeId())
                ? meeting.getCalendarInstance().getEntityId()
                : null;

        return MeetingAttendanceUpdateResponse.builder().entityId(meeting.getId()).groupId(groupId).changes(changes).build();
    }

    private Map<String, Object> updateAttendance(Meeting meeting, HashSet<MeetingAttendance> clientsAttendance) {
        var result = new ArrayList<MeetingAttendance>();

        var actualChanges = new HashMap<String, Object>();
        var clientAttendanceChanges = new HashMap<String, Object>();

        // TODO: never use "goto" statemements... ever; leaving it here, because the whole class will eventually
        // disappear
        updateAttendanceLoop: for (var clientAttendance : clientsAttendance) {
            if (meeting.getClientsAttendance() == null) {
                meeting.setClientsAttendance(new HashSet<>());
            }
            for (var clientAttendanceOriginal : meeting.getClientsAttendance()) {
                if (requireNonNull(clientAttendanceOriginal.getClient().getId()).equals(clientAttendance.getClient().getId())) {
                    if (!clientAttendance.getAttendanceTypeId().equals(clientAttendanceOriginal.getAttendanceTypeId())) {
                        clientAttendanceOriginal.setAttendanceTypeId(clientAttendance.getAttendanceTypeId());
                        var clientAttendanceChange = new HashMap<String, Object>();
                        clientAttendanceChange.put(clientIdParamName, clientAttendanceOriginal.getClient().getId());
                        clientAttendanceChange.put(attendanceTypeParamName, clientAttendance.getAttendanceTypeId());
                        clientAttendanceChanges.put(clientAttendanceOriginal.getClient().getId().toString(), clientAttendanceChange);
                    }
                    continue updateAttendanceLoop;
                }
            }

            var clientAttendanceChange = new HashMap<String, Object>();
            clientAttendanceChange.put(clientIdParamName, clientAttendance.getClient().getId());
            clientAttendanceChange.put(attendanceTypeParamName, clientAttendance.getAttendanceTypeId());
            clientAttendanceChanges.put(clientAttendance.getClient().getId().toString(), clientAttendanceChange);

            result.add(clientAttendance);
        }

        actualChanges.put(clientsAttendanceParamName, clientAttendanceChanges);

        if (!result.isEmpty()) {
            clientsAttendance.addAll(result);
        }

        return actualChanges;
    }
}
