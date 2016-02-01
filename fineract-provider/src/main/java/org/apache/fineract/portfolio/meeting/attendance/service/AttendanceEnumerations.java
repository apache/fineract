/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.attendance.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.meeting.attendance.AttendanceType;

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