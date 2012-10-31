package org.mifosng.platform.api.data;

import java.util.Collection;

public class LoanReassignmentData {

    private final Long officeId;
    private final Long fromLoanOfficerId;

    //template
    private final Collection<OfficeLookup> officeOptions;

    private final Collection<StaffData> loanOfficerOptions;

    private final StaffAccountSummaryCollectionData accountSummaryCollection;

    public LoanReassignmentData(Long officeId, Long fromLoanOfficerId,
                                Collection<OfficeLookup> officeOptions, Collection<StaffData> loanOfficerOptions,
                                StaffAccountSummaryCollectionData accountSummaryCollection) {
        this.officeId = officeId;
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.officeOptions = officeOptions;
        this.loanOfficerOptions = loanOfficerOptions;
        this.accountSummaryCollection = accountSummaryCollection;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public Long getFromLoanOfficerId() {
        return fromLoanOfficerId;
    }

    public Collection<OfficeLookup> getOfficeOptions() {
        return officeOptions;
    }

    public Collection<StaffData> getLoanOfficerOptions() {
        return loanOfficerOptions;
    }

    public StaffAccountSummaryCollectionData getAccountSummaryCollection() {
        return accountSummaryCollection;
    }
}
