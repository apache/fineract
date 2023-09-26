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
package org.apache.fineract.portfolio.group.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.staff.domain.Staff;

@Entity
@Table(name = "m_staff_assignment_history")
public class StaffAssignmentHistory extends AbstractAuditableCustom {

    @ManyToOne
    @JoinColumn(name = "centre_id", nullable = true)
    private Group center;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = true)
    private Staff staff;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public static StaffAssignmentHistory createNew(final Group center, final Staff staff, final LocalDate startDate) {
        return new StaffAssignmentHistory(center, staff, startDate, null);
    }

    protected StaffAssignmentHistory() {
        //
    }

    private StaffAssignmentHistory(final Group center, final Staff staff, final LocalDate startDate, final LocalDate endDate) {
        this.center = center;
        this.staff = staff;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateStaff(final Staff staff) {
        this.staff = staff;
    }

    public void updateStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    public void updateEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean matchesStartDateOf(final LocalDate matchingDate) {
        return DateUtils.isEqual(matchingDate, getStartDate());
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public boolean isCurrentRecord() {
        return this.endDate == null;
    }
}
