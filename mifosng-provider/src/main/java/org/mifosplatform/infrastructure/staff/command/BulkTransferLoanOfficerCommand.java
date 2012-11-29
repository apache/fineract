package org.mifosplatform.infrastructure.staff.command;

import org.joda.time.LocalDate;

/**
 * Immutable data object for bulk transferring same loan officers out and in on loans.
 */
public class BulkTransferLoanOfficerCommand {

    private final Long loanId;

    private final Long fromLoanOfficerId;
    private final Long toLoanOfficerId;
    private final LocalDate assignmentDate;

    private final String[] loans;

    public BulkTransferLoanOfficerCommand(final Long loanId, final Long fromLoanOfficerId, final Long toLoanOfficerId, final LocalDate assignmentDate) {
        this.loanId = loanId;
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.toLoanOfficerId = toLoanOfficerId;
        this.assignmentDate = assignmentDate;
        this.loans = null;
    }

    public BulkTransferLoanOfficerCommand(final Long fromLoanOfficerId, final Long toLoanOfficerId, final LocalDate assignmentDate, final String[] loans) {
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.toLoanOfficerId = toLoanOfficerId;
        this.assignmentDate = assignmentDate;
        this.loans = loans;
        this.loanId = null;
    }

    public Long getLoanId() {
        return loanId;
    }

    public Long getFromLoanOfficerId() {
        return fromLoanOfficerId;
    }

    public Long getToLoanOfficerId() {
        return toLoanOfficerId;
    }

    public LocalDate getAssignmentDate() {
        return assignmentDate;
    }

    public String[] getLoans() {
        return loans;
    }
}