package org.mifosng.platform.loan.domain;


import org.joda.time.LocalDate;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.staff.domain.Staff;
import org.mifosng.platform.user.domain.AppUser;

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

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "end_date")
    private Date endDate;

    public static LoanOfficerAssignmentHistory createNew(Loan loan, Staff loanOfficer,
                                                         LocalDate startDate){
        return new LoanOfficerAssignmentHistory(loan, loanOfficer, startDate.toDate(), null);
    }

    @SuppressWarnings("unused")
    protected LoanOfficerAssignmentHistory() {
        this.loan = null;
        this.loanOfficer = null;
        this.startDate = null;
        this.endDate = null;
    }

    private LoanOfficerAssignmentHistory(Loan loan, Staff loanOfficer,
                                        Date startDate, Date endDate) {
        this.loan = loan;
        this.loanOfficer = loanOfficer;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Loan getLoan() {
        return loan;
    }

    public LocalDate getStartDate() {
        return new LocalDate(startDate);
    }

    public Date getEndDate() {
        return endDate;
    }

    public void updateLoanOfficer(Staff loanOfficer) {
        this.loanOfficer = loanOfficer;
    }

    public void updateStartDate(LocalDate startDate){
        this.startDate = startDate.toDate();
    }

    public void updateEndDate(LocalDate endDate){
        this.endDate = endDate.toDate();
    }

}
