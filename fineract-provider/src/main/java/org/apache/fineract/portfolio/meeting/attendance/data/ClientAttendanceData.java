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
package org.apache.fineract.portfolio.meeting.attendance.data;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

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
