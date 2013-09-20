/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.ObjectUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_loan_officer_assignment_history")
public class LoanOfficerAssignmentHistory extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "loan_officer_id", nullable = true)
    private Staff loanOfficer;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date")
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date")
    private Date endDate;

    public static LoanOfficerAssignmentHistory createNew(final Loan loan, final Staff loanOfficer, final LocalDate startDate) {
        return new LoanOfficerAssignmentHistory(loan, loanOfficer, startDate.toDate(), null);
    }

    protected LoanOfficerAssignmentHistory() {
        //
    }

    private LoanOfficerAssignmentHistory(final Loan loan, final Staff loanOfficer, final Date startDate, final Date endDate) {
        this.loan = loan;
        this.loanOfficer = loanOfficer;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateLoanOfficer(final Staff loanOfficer) {
        this.loanOfficer = loanOfficer;
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

    public boolean hasStartDateBefore(final LocalDate matchingDate) {
        return matchingDate.isBefore(getStartDate());
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
        return this.endDate == null ? false : new LocalDate(this.endDate).isAfter(compareDate);
    }

    public LocalDate getEndDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.endDate), null);
    }

    public boolean isSameLoanOfficer(final Staff staff) {
        return this.loanOfficer.identifiedBy(staff);
    }
}