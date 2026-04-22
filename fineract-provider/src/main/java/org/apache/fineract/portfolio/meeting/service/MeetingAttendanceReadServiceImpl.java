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

import java.sql.ResultSet;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceData;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceEnumerations;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnMissingBean(value = MeetingAttendanceReadService.class, ignored = MeetingAttendanceReadServiceImpl.class)
public class MeetingAttendanceReadServiceImpl implements MeetingAttendanceReadService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<MeetingAttendanceData> retrieveClientAttendanceByMeetingId(final Long meetingId) {
        return jdbcTemplate.query(
                """
                                SELECT
                                    ca.id as id,
                                    ca.client_id as clientId,
                                    ca.attendance_type_enum as attendanceTypeId,
                                    c.display_name as clientName
                                FROM m_meeting m INNER JOIN m_client_attendance ca ON m.id = ca.meeting_id INNER JOIN m_client c on ca.client_id=c.id
                                WHERE m.id = ?
                        """,
                (ResultSet rs, int rowNum) -> {
                    var id = rs.getLong("id");
                    var clientId = rs.getLong("clientId");
                    var attendanceTypeId = rs.getInt("attendanceTypeId");
                    var clientName = rs.getString("clientName");
                    var attendanceType = MeetingAttendanceEnumerations.attendanceType(MeetingAttendanceType.fromInt(attendanceTypeId));

                    return MeetingAttendanceData.builder().id(id).clientId(clientId).clientName(clientName).attendanceType(attendanceType)
                            .build();
                }, meetingId);
    }
}
