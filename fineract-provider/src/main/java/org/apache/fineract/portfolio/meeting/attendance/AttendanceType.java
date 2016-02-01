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
package org.apache.fineract.portfolio.meeting.attendance;

import org.apache.fineract.portfolio.meeting.attendance.domain.ClientAttendance;

/**
 * An enumeration of {@link ClientAttendance} type.
 */
public enum AttendanceType {

    INVALID(0, "attendanceType.invalid"), //
    PRESENT(1, "attendanceType.present"), //
    ABSENT(2, "attendanceType.absent"), //
    APPROVED(3, "attendanceType.approved"), //
    LEAVE(4, "attendanceType.leave"), //
    LATE(5, "attendanceType.late");

    private final Integer value;
    private final String code;

    private AttendanceType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static AttendanceType fromInt(final Integer attendanceTypeId) {

        if (attendanceTypeId == null) { return AttendanceType.INVALID; }

        AttendanceType attendanceType = AttendanceType.INVALID;
        switch (attendanceTypeId) {
            case 1:
                attendanceType = AttendanceType.PRESENT;
            break;
            case 2:
                attendanceType = AttendanceType.ABSENT;
            break;
            case 3:
                attendanceType = AttendanceType.APPROVED;
            break;
            case 4:
                attendanceType = AttendanceType.LEAVE;
            break;
            case 5:
                attendanceType = AttendanceType.LATE;
            break;
        }
        return attendanceType;
    }

}