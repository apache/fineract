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

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceEnumerations;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnMissingBean(value = MeetingAttendanceDropdownReadService.class, ignored = MeetingAttendanceDropdownReadServiceImpl.class)
public class MeetingAttendanceDropdownReadServiceImpl implements MeetingAttendanceDropdownReadService {

    @Override
    public List<EnumOptionData> retrieveAttendanceTypeOptions() {
        return Arrays.stream(MeetingAttendanceType.values())
                .filter(meetingAttendanceType -> !meetingAttendanceType.equals(MeetingAttendanceType.INVALID))
                .map(MeetingAttendanceEnumerations::attendanceType).toList();
    }

}
