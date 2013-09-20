/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.attendance.data;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object representing a ClientAttendance.
 */
public class ClientAttendanceData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long clientId;
    @SuppressWarnings("unused")
    private final String clientName;
    @SuppressWarnings("unused")
    private final EnumOptionData attendanceType;

    public static ClientAttendanceData instance(final Long id, final Long clientId, final String clientName,
            final EnumOptionData attendanceType) {
        return new ClientAttendanceData(id, clientId, clientName, attendanceType);
    }

    private ClientAttendanceData(final Long id, final Long clientId, final String clientName, final EnumOptionData attendanceType) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.attendanceType = attendanceType;
    }

}
