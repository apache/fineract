/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.data;

import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.meeting.attendance.data.ClientAttendanceData;

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
