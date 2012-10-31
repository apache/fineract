package org.mifosng.platform.api.data;

import java.util.Collection;

/**
 * Immutable data object returned for loan-officer reassignment screens.
 */
public class LoanReassignmentData {

    @SuppressWarnings("unused")
	private final Long officeId;
    @SuppressWarnings("unused")
    private final Long fromLoanOfficerId;

    //template
    @SuppressWarnings("unused")
    private final Collection<OfficeLookup> officeOptions;
    @SuppressWarnings("unused")
    private final Collection<StaffData> loanOfficerOptions;
    @SuppressWarnings("unused")
    private final StaffAccountSummaryCollectionData accountSummaryCollection;

    public LoanReassignmentData(
    		final Long officeId, 
    		final Long fromLoanOfficerId,
            final Collection<OfficeLookup> officeOptions, 
            final Collection<StaffData> loanOfficerOptions,
            final StaffAccountSummaryCollectionData accountSummaryCollection) {
        this.officeId = officeId;
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.officeOptions = officeOptions;
        this.loanOfficerOptions = loanOfficerOptions;
        this.accountSummaryCollection = accountSummaryCollection;
    }
}