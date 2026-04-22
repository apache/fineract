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
package org.apache.fineract.portfolio.meeting.data;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public final class MeetingAttendanceEnumerations {

    private MeetingAttendanceEnumerations() {}

    public static EnumOptionData attendanceType(final MeetingAttendanceType attendanceType) {

        EnumOptionData optionData = new EnumOptionData(MeetingAttendanceType.INVALID.getValue().longValue(),
                MeetingAttendanceType.INVALID.getCode(), "Invalid");

        switch (attendanceType) {
            case INVALID:
                optionData = new EnumOptionData(MeetingAttendanceType.INVALID.getValue().longValue(),
                        MeetingAttendanceType.INVALID.getCode(), "Invalid");
            break;
            case PRESENT:
                optionData = new EnumOptionData(MeetingAttendanceType.PRESENT.getValue().longValue(),
                        MeetingAttendanceType.PRESENT.getCode(), "Present");
            break;
            case ABSENT:
                optionData = new EnumOptionData(MeetingAttendanceType.ABSENT.getValue().longValue(), MeetingAttendanceType.ABSENT.getCode(),
                        "Absent");
            break;
            case APPROVED:
                optionData = new EnumOptionData(MeetingAttendanceType.APPROVED.getValue().longValue(),
                        MeetingAttendanceType.APPROVED.getCode(), "Approved");
            break;
            case LEAVE:
                optionData = new EnumOptionData(MeetingAttendanceType.LEAVE.getValue().longValue(), MeetingAttendanceType.LEAVE.getCode(),
                        "Leave");
            break;
            case LATE:
                optionData = new EnumOptionData(MeetingAttendanceType.LATE.getValue().longValue(), MeetingAttendanceType.LATE.getCode(),
                        "Late");
            break;
        }
        return optionData;
    }
}
