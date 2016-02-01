/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.attendance.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.meeting.domain.Meeting;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_client_attendance", uniqueConstraints = { @UniqueConstraint(columnNames = { "client_id", "meeting_id" }, name = "unique_client_meeting_attendance") })
public class ClientAttendance extends AbstractPersistable<Long> {

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
