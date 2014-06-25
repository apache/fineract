/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.portfolio.meeting.domain;

import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.attendanceTypeParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.clientIdParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.clientsAttendanceParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.meetingDateParamName;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.exception.NotValidRecurringDateException;
import org.mifosplatform.portfolio.meeting.attendance.domain.ClientAttendance;
import org.mifosplatform.portfolio.meeting.exception.MeetingDateException;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_meeting", uniqueConstraints = { @UniqueConstraint(columnNames = { "calendar_instance_id", "meeting_date" }, name = "unique_calendar_instance_id_meeting_date") })
public class Meeting extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "calendar_instance_id", nullable = false)
    private CalendarInstance calendarInstance;

    @Column(name = "meeting_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date meetingDate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "meeting", orphanRemoval = true)
    private Set<ClientAttendance> clientsAttendance;

    protected Meeting() {
        //
    }

    private Meeting(final CalendarInstance calendarInstance, final Date meetingDate) {
        this.calendarInstance = calendarInstance;
        this.meetingDate = meetingDate;
    }

    public static Meeting createNew(final CalendarInstance calendarInstance, final Date meetingDate) {

        if (!isValidMeetingDate(calendarInstance, meetingDate)) { throw new NotValidRecurringDateException("meeting", "The date '"
                + meetingDate + "' is not a valid meeting date.", meetingDate); }
        return new Meeting(calendarInstance, meetingDate);
    }

    public void associateClientsAttendance(final Collection<ClientAttendance> clientsAttendance) {
        // do not allow to capture attendance in advance.
        if (isMeetingDateAfter(DateUtils.getLocalDateOfTenant())) {
            final String errorMessage = "Attendance cannot be in the future.";
            throw new MeetingDateException("cannot.be.a.future.date", errorMessage, getMeetingDateLocalDate());
        }
        this.clientsAttendance = new HashSet<>(clientsAttendance);
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);
        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        if (command.isChangeInLocalDateParameterNamed(meetingDateParamName, getMeetingDateLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(meetingDateParamName);
            final LocalDate newValue = command.localDateValueOfParameterNamed(meetingDateParamName);
            actualChanges.put(meetingDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);
            this.meetingDate = newValue.toDate();

            if (!isValidMeetingDate(this.calendarInstance, this.meetingDate)) { throw new NotValidRecurringDateException("meeting",
                    "Not a valid meeting date", this.meetingDate); }

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
        LocalDate meetingDateLocalDate = null;
        if (this.meetingDate != null) {
            meetingDateLocalDate = LocalDate.fromDateFields(this.meetingDate);
        }
        return meetingDateLocalDate;
    }

    public Date getMeetingDate() {
        return this.meetingDate;
    }

    public boolean isMeetingDateBefore(final LocalDate newStartDate) {
        return this.meetingDate != null && newStartDate != null && getMeetingDateLocalDate().isBefore(newStartDate) ? true : false;
    }

    private static boolean isValidMeetingDate(final CalendarInstance calendarInstance, final Date meetingDate) {
        final Calendar calendar = calendarInstance.getCalendar();
        LocalDate meetingDateLocalDate = null;
        if (meetingDate != null) {
            meetingDateLocalDate = LocalDate.fromDateFields(meetingDate);
        }

        if (meetingDateLocalDate == null || !calendar.isValidRecurringDate(meetingDateLocalDate)) { return false; }
        return true;
    }

    private boolean isMeetingDateAfter(final LocalDate date) {
        return getMeetingDateLocalDate().isAfter(date);
    }

    public Collection<ClientAttendance> getClientsAttendance() {
        return this.clientsAttendance;
    }

}
