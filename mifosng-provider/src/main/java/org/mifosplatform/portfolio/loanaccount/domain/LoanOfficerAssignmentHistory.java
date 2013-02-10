package org.mifosplatform.portfolio.loanaccount.domain;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.useradministration.domain.AppUser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "m_loan_officer_assignment_history")
public class LoanOfficerAssignmentHistory extends AbstractAuditableCustom<AppUser, Long> {

	@SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @SuppressWarnings("unused")
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

    public void updateLoanOfficer(Staff loanOfficer) {
        this.loanOfficer = loanOfficer;
    }

    public void updateStartDate(LocalDate startDate) {
        this.startDate = startDate.toDate();
    }

    public void updateEndDate(LocalDate endDate) {
        this.endDate = endDate.toDate();
    }

	public boolean matchesStartDateOf(final LocalDate matchingDate) {
		return getStartDate().isEqual(matchingDate);
	}
	
	public LocalDate getStartDate() {
		return new LocalDate(startDate);
	}

	public boolean hasStartDateBefore(final LocalDate matchingDate) {
		return matchingDate.isBefore(getStartDate());
	}

	public boolean isCurrentRecord() {
		return this.endDate == null;
	}
}