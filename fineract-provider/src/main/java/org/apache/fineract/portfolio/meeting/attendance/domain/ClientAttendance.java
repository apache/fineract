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
package org.apache.fineract.portfolio.meeting.attendance.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.meeting.domain.Meeting;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_client_attendance", uniqueConstraints = { @UniqueConstraint(columnNames = { "client_id", "meeting_id" }, name = "unique_client_meeting_attendance") })
public class ClientAttendance extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "attendance_type_enum", nullable = false)
    private Integer attendanceTypeId;

    protected ClientAttendance() {

    }

    public static ClientAttendance createClientAttendance(final Client client, final Meeting meeting, final Integer attendanceTypeId) {
        return new ClientAttendance(client, meeting, attendanceTypeId);
    }

    private ClientAttendance(final Client client, final Meeting meeting, final Integer attendanceTypeId) {
        this.client = client;
        this.meeting = meeting;
        this.attendanceTypeId = attendanceTypeId;
    }

    public Long clientId() {
        return this.client.getId();
    }

    public void updateAttendanceTypeId(final Integer attendanceTypeId) {
        this.attendanceTypeId = attendanceTypeId;
    }

    public Integer getAttendanceTypeId() {
        return this.attendanceTypeId;
    }
}
