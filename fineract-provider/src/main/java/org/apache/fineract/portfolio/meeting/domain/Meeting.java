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
package org.apache.fineract.portfolio.meeting.domain;

import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.attendanceTypeParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.clientIdParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.clientsAttendanceParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.meetingDateParamName;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.exception.NotValidRecurringDateException;
import org.apache.fineract.portfolio.meeting.attendance.domain.ClientAttendance;
import org.apache.fineract.portfolio.meeting.exception.MeetingDateException;

@Entity
@Table(name = "m_meeting", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "calendar_instance_id", "meeting_date" }, name = "unique_calendar_instance_id_meeting_date") })
public class Meeting extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "calendar_instance_id", nullable = false)
    private CalendarInstance calendarInstance;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "meeting", orphanRemoval = true)
    private Set<ClientAttendance> clientsAttendance;

    protected Meeting() {
        //
    }

    private Meeting(final CalendarInstance calendarInstance, final LocalDate meetingDate) {
        this.calendarInstance = calendarInstance;
        this.meetingDate = meetingDate;
    }

    public static Meeting createNew(final CalendarInstance calendarInstance, final LocalDate meetingDate,
            Boolean isTransactionDateOnNonMeetingDate, final boolean isSkipRepaymentOnFirstMonth, final int numberOfDays) {

        if (!isTransactionDateOnNonMeetingDate
                && !isValidMeetingDate(calendarInstance, meetingDate, isSkipRepaymentOnFirstMonth, numberOfDays)) {
            throw new NotValidRecurringDateException("meeting", "The date '" + meetingDate + "' is not a valid meeting date.", meetingDate);
        }
        return new Meeting(calendarInstance, meetingDate);
    }

    public void associateClientsAttendance(final Collection<ClientAttendance> clientsAttendance) {
        // do not allow to capture attendance in advance.
        if (isMeetingDateAfter(DateUtils.getBusinessLocalDate())) {
            final String errorMessage = "Attendance cannot be in the future.";
            throw new MeetingDateException("cannot.be.a.future.date", errorMessage, getMeetingDateLocalDate());
        }
        this.clientsAttendance = new HashSet<>(clientsAttendance);
    }

    public Map<String, Object> update(final JsonCommand command, final boolean isSkipRepaymentOnFirstMonth, final int numberOfDays) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);
        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        if (command.isChangeInLocalDateParameterNamed(meetingDateParamName, getMeetingDateLocalDate())) {
            actualChanges.put(meetingDateParamName, command.stringValueOfParameterNamed(meetingDateParamName));
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);
            this.meetingDate = command.localDateValueOfParameterNamed(meetingDateParamName);

            if (!isValidMeetingDate(this.calendarInstance, this.meetingDate, isSkipRepaymentOnFirstMonth, numberOfDays)) {
                throw new NotValidRecurringDateException("meeting", "Not a valid meeting date", this.meetingDate);
            }

        }

        return actualChanges;
    }

    public Map<String, Object> updateAttendance(final Collection<ClientAttendance> clientsAttendance) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(1);
        final Map<String, Object> clientAttendanceChanges = new LinkedHashMap<>(clientsAttendance.size());

        updateAttendanceLoop: for (final ClientAttendance clientAttendance : clientsAttendance) {
            if (this.clientsAttendance == null) {
                this.clientsAttendance = new HashSet<>();
            }
            for (final ClientAttendance clientAttendanceOriginal : this.clientsAttendance) {
                if (clientAttendanceOriginal.clientId().equals(clientAttendance.clientId())) {
                    final Integer newValue = clientAttendance.getAttendanceTypeId();
                    if (!newValue.equals(clientAttendanceOriginal.getAttendanceTypeId())) {
                        clientAttendanceOriginal.updateAttendanceTypeId(newValue);
                        final Map<String, Object> clientAttendanceChange = new LinkedHashMap<>(2);
                        clientAttendanceChange.put(clientIdParamName, clientAttendanceOriginal.clientId());
                        clientAttendanceChange.put(attendanceTypeParamName, newValue);
                        clientAttendanceChanges.put(clientAttendanceOriginal.clientId().toString(), clientAttendanceChange);
                    }
                    continue updateAttendanceLoop;
                }
            }

            final Map<String, Object> clientAttendanceChange = new LinkedHashMap<>();
            clientAttendanceChange.put(clientIdParamName, clientAttendance.clientId());
            clientAttendanceChange.put(attendanceTypeParamName, clientAttendance.getAttendanceTypeId());
            clientAttendanceChanges.put(clientAttendance.clientId().toString(), clientAttendanceChange);
            // New attendance record
            this.clientsAttendance.add(clientAttendance);
        }

        actualChanges.put(clientsAttendanceParamName, clientAttendanceChanges);

        return actualChanges;
    }

    public Long entityId() {
        return this.calendarInstance.getEntityId();
    }

    public boolean isCenterEntity() {
        return CalendarEntityType.isCenter(this.calendarInstance.getEntityTypeId());
    }

    public boolean isGroupEntity() {
        return CalendarEntityType.isGroup(this.calendarInstance.getEntityTypeId());
    }

    public LocalDate getMeetingDateLocalDate() {
        return this.meetingDate;
    }

    public LocalDate getMeetingDate() {
        return this.meetingDate;
    }

    public boolean isMeetingDateBefore(final LocalDate newStartDate) {
        return this.meetingDate != null && DateUtils.isBefore(getMeetingDateLocalDate(), newStartDate);
    }

    private static boolean isValidMeetingDate(final CalendarInstance calendarInstance, final LocalDate meetingDate,
            final boolean isSkipRepaymentOnFirstMonth, final int numberOfDays) {
        final Calendar calendar = calendarInstance.getCalendar();

        if (meetingDate == null || !calendar.isValidRecurringDate(meetingDate, isSkipRepaymentOnFirstMonth, numberOfDays)) {
            return false;
        }
        return true;
    }

    private boolean isMeetingDateAfter(final LocalDate date) {
        return DateUtils.isAfter(getMeetingDateLocalDate(), date);
    }

    public Collection<ClientAttendance> getClientsAttendance() {
        return this.clientsAttendance;
    }

}
