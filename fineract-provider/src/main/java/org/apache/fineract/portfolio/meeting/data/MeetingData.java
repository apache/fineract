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

import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.meeting.attendance.data.ClientAttendanceData;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing a Meeting.
 */
public class MeetingData {

    private final Long id;
    private final LocalDate meetingDate;
    private final Collection<ClientAttendanceData> clientsAttendance;

    // template data
    private final Collection<ClientData> clients;
    private final CalendarData calendarData;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> attendanceTypeOptions;

    public static MeetingData instance(final Long id, final LocalDate meetingDate) {
        final Collection<ClientAttendanceData> clientsAttendance = null;
        final Collection<ClientData> clients = null;
        final CalendarData calendarData = null;
        final List<EnumOptionData> attendanceTypeOptions = null;
        return new MeetingData(id, meetingDate, clientsAttendance, clients, calendarData, attendanceTypeOptions);
    }

    public static MeetingData withClientsAttendanceAndAttendanceTypeOptions(final MeetingData meetingData,
            final Collection<ClientAttendanceData> clientsAttendance, final List<EnumOptionData> attendanceTypesOptions) {
        return new MeetingData(meetingData.id, meetingData.meetingDate, clientsAttendance, meetingData.clients, meetingData.calendarData,
                attendanceTypesOptions);
    }

    public static MeetingData template(final Collection<ClientData> clients, final CalendarData calendarData,
            final List<EnumOptionData> attendanceTypeOptions) {
        final Long id = null;
        final LocalDate meetingDate = null;
        final Collection<ClientAttendanceData> clientsAttendance = null;
        return new MeetingData(id, meetingDate, clientsAttendance, clients, calendarData, attendanceTypeOptions);
    }

    public static MeetingData withAttendanceTypeOptions(final MeetingData meetingData, final List<EnumOptionData> attendanceTypeOptions) {

        return new MeetingData(meetingData.id, meetingData.meetingDate, meetingData.clientsAttendance, meetingData.clients,
                meetingData.calendarData, attendanceTypeOptions);
    }

    private MeetingData(final Long id, final LocalDate meetingDate, final Collection<ClientAttendanceData> clientsAttendance,
            final Collection<ClientData> clients, final CalendarData calendarData, final List<EnumOptionData> attendanceTypeOptions) {
        this.id = id;
        this.meetingDate = meetingDate;
        this.clientsAttendance = clientsAttendance;
        this.clients = clients;
        this.calendarData = calendarData;
        this.attendanceTypeOptions = attendanceTypeOptions;
    }

    public LocalDate getMeetingDate() {
        return this.meetingDate;
    }

}
