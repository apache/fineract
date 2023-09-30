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
package org.apache.fineract.portfolio.loanaccount.domain;

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
@Table(name = "m_loan_officer_assignment_history")
public class LoanOfficerAssignmentHistory extends AbstractAuditableCustom {

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "loan_officer_id", nullable = true)
    private Staff loanOfficer;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public static LoanOfficerAssignmentHistory createNew(final Loan loan, final Staff loanOfficer, final LocalDate startDate) {
        return new LoanOfficerAssignmentHistory(loan, loanOfficer, startDate, null);
    }

    protected LoanOfficerAssignmentHistory() {
        //
    }

    private LoanOfficerAssignmentHistory(final Loan loan, final Staff loanOfficer, final LocalDate startDate, final LocalDate endDate) {
        this.loan = loan;
        this.loanOfficer = loanOfficer;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateLoanOfficer(final Staff loanOfficer) {
        this.loanOfficer = loanOfficer;
    }

    public void updateStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    public void updateEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public boolean matchesStartDateOf(final LocalDate matchingDate) {
        return DateUtils.isEqual(matchingDate, getStartDate());
    }

    public boolean isBeforeStartDate(final LocalDate matchingDate) {
        return DateUtils.isBefore(matchingDate, getStartDate());
    }

    public boolean isCurrentRecord() {
        return this.endDate == null;
    }

    /**
     * If endDate is null then return false.
     *
     * @param compareDate
     * @return
     */
    public boolean isEndDateAfter(final LocalDate compareDate) {
        return DateUtils.isAfter(this.endDate, compareDate);
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public boolean isSameLoanOfficer(final Staff staff) {
        return this.loanOfficer.identifiedBy(staff);
    }
}
