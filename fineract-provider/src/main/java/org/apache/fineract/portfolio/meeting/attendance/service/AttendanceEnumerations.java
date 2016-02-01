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
package org.apache.fineract.portfolio.meeting.attendance.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.meeting.attendance.AttendanceType;

public class AttendanceEnumerations {

    public static EnumOptionData attendanceType(final int attendanceType) {
        return attendanceType(AttendanceType.fromInt(attendanceType));
    }

    public static EnumOptionData attendanceType(final AttendanceType attendanceType) {

        EnumOptionData optionData = new EnumOptionData(AttendanceType.INVALID.getValue().longValue(), AttendanceType.INVALID.getCode(),
                "Invalid");

        switch (attendanceType) {
            case INVALID:
                optionData = new EnumOptionData(AttendanceType.INVALID.getValue().longValue(), AttendanceType.INVALID.getCode(), "Invalid");
            break;
            case PRESENT:
                optionData = new EnumOptionData(AttendanceType.PRESENT.getValue().longValue(), AttendanceType.PRESENT.getCode(), "Present");
            break;
            case ABSENT:
                optionData = new EnumOptionData(AttendanceType.ABSENT.getValue().longValue(), AttendanceType.ABSENT.getCode(), "Absent");
            break;
            case APPROVED:
                optionData = new EnumOptionData(AttendanceType.APPROVED.getValue().longValue(), AttendanceType.APPROVED.getCode(),
                        "Approved");
            break;
            case LEAVE:
                optionData = new EnumOptionData(AttendanceType.LEAVE.getValue().longValue(), AttendanceType.LEAVE.getCode(), "Leave");
            break;
            case LATE:
                optionData = new EnumOptionData(AttendanceType.LATE.getValue().longValue(), AttendanceType.LATE.getCode(), "Late");
            break;
        }
        return optionData;
    }

    public static List<EnumOptionData> attendanceType(final AttendanceType[] attendanceTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final AttendanceType attendanceType : attendanceTypes) {
            if (attendanceType.getValue().equals(AttendanceType.INVALID.getValue())) {
                continue;
            }
            optionDatas.add(attendanceType(attendanceType));
        }
        return optionDatas;
    }
}