/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MeetingApiConstants {

    public static final String MEETING_RESOURCE_NAME = "meeting";

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // meetings parameters
    public static final String idParamName = "id";
    public static final String meetingDateParamName = "meetingDate";
    public static final String calendarIdParamName = "calendarId";
    public static final String clientsAttendanceParamName = "clientsAttendance";

    // attendance parameters
    public static final String clientIdParamName = "clientId";
    public static final String attendanceTypeParamName = "attendanceType";

    // attendance response parameters
    public static final String clientsAttendance = "clientsAttendance";

    // template response parameters
    public static final String clients = "clients";
    public static final String calendarData = "calendarData";
    public static final String attendanceTypeOptions = "attendanceTypeOptions";

    public static final Set<String> MEETING_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(meetingDateParamName,
            localeParamName, dateFormatParamName, calendarIdParamName, clientsAttendanceParamName));

    public static final Set<String> MEETING_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(idParamName, meetingDateParamName,
            clientsAttendance, clients, calendarData, attendanceTypeOptions));

}