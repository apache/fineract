/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_staff_assignment_history")
public class StaffAssignmentHistory extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "centre_id", nullable = true)
    private Group center;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = true)
    private Staff staff;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date")
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date")
    private Date endDate;

    public static StaffAssignmentHistory createNew(final Group center, final Staff staff, final LocalDate startDate) {
        return new StaffAssignmentHistory(center, staff, startDate.toDate(), null);
    }

    protected StaffAssignmentHistory() {
        //
    }

    private StaffAssignmentHistory(final Group center, final Staff staff, final Date startDate, final Date endDate) {
        this.center = center;
        this.staff = staff;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateStaff(final Staff staff) {
        this.staff = staff;
    }

    public void updateStartDate(final LocalDate startDate) {
        this.startDate = startDate.toDate();
    }

    public void updateEndDate(final LocalDate endDate) {
        this.endDate = endDate.toDate();
    }

    public boolean matchesStartDateOf(final LocalDate matchingDate) {
        return getStartDate().isEqual(matchingDate);
    }

    public LocalDate getStartDate() {
        return new LocalDate(this.startDate);
    }

    public boolean isCurrentRecord() {
        return this.endDate == null;
    }
}
