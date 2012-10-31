package org.mifosng.platform.api.commands;


import java.util.Set;

public class BulkLoanReassignmentCommand {

    private final Long fromLoanOfficerId;
    private final Long toLoanOfficerId;

    private final String[] loans;

    private final Set<String> modifiedParameters;

    public BulkLoanReassignmentCommand(Set<String> modifiedParameters, Long fromLoanOfficerId,
                                       Long toLoanOfficerId, String[] loans) {
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.toLoanOfficerId = toLoanOfficerId;
        this.loans = loans;
        this.modifiedParameters = modifiedParameters;
    }

    public Long getFromLoanOfficerId() {
        return fromLoanOfficerId;
    }

    public Long getToLoanOfficerId() {
        return toLoanOfficerId;
    }

    public String[] getLoans() {
        return loans;
    }

}
