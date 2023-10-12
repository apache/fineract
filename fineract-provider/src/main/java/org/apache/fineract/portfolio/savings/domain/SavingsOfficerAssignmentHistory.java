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
package org.apache.fineract.portfolio.savings.domain;

import static org.apache.fineract.infrastructure.core.service.DateUtils.getSystemZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.staff.domain.Staff;

@Entity
@Table(name = "m_savings_officer_assignment_history")
public class SavingsOfficerAssignmentHistory extends AbstractAuditableWithUTCDateTimeCustom {

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private SavingsAccount savingsAccount;

    @ManyToOne
    @JoinColumn(name = "savings_officer_id", nullable = true)
    private Staff savingsOfficer;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Deprecated
    @Column(name = "created_date")
    private LocalDateTime createdDateToRemove;

    @Deprecated
    @Column(name = "lastmodified_date")
    private LocalDateTime lastModifiedDateToRemove;

    protected SavingsOfficerAssignmentHistory() {
        //
    }

    private SavingsOfficerAssignmentHistory(final SavingsAccount account, final Staff savingsOfficer, final LocalDate startDate,
            final LocalDate endDate) {
        this.savingsAccount = account;
        this.savingsOfficer = savingsOfficer;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static SavingsOfficerAssignmentHistory createNew(final SavingsAccount account, final Staff savingsOfficer,
            final LocalDate startDate) {
        return new SavingsOfficerAssignmentHistory(account, savingsOfficer, startDate, null);
    }

    public void setSavingsOfficer(final Staff savingsOfficer) {
        this.savingsOfficer = savingsOfficer;
    }

    public boolean isSameSavingsOfficer(final Staff staff) {
        return this.savingsOfficer.identifiedBy(staff);
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean matchesStartDateOf(final LocalDate matchingDate) {
        return DateUtils.isEqual(matchingDate, getStartDate());
    }

    public boolean isBeforeStartDate(final LocalDate matchingDate) {
        return DateUtils.isBefore(matchingDate, getStartDate());
    }

    /**
     * If endDate is null then return false.
     *
     * @param compareDate
     * @return
     */
    public boolean isBeforeEndDate(final LocalDate compareDate) {
        return DateUtils.isBefore(compareDate, this.endDate);
    }

    public boolean isCurrentRecord() {
        return this.endDate == null;
    }

    @Override
    public Optional<OffsetDateTime> getCreatedDate() {
        // #audit backward compatibility keep system datetime
        return Optional.ofNullable(super.getCreatedDate()
                .orElse(createdDateToRemove == null ? null : createdDateToRemove.atZone(getSystemZoneId()).toOffsetDateTime()));
    }

    @Override
    public Optional<OffsetDateTime> getLastModifiedDate() {
        // #audit backward compatibility keep system datetime
        return Optional.ofNullable(super.getLastModifiedDate()
                .orElse(lastModifiedDateToRemove == null ? null : lastModifiedDateToRemove.atZone(getSystemZoneId()).toOffsetDateTime()));
    }
}
