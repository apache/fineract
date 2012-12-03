package org.mifosplatform.organisation.staff.data;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.office.data.OfficeLookup;

import java.util.Collection;

/**
 * Immutable data object returned for loan-officer bulk transfer screens.
 */
public class BulkTransferLoanOfficerData {

    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final Long fromLoanOfficerId;
    @SuppressWarnings("unused")
    private final LocalDate assignmentDate;

    // template
    @SuppressWarnings("unused")
    private final Collection<OfficeLookup> officeOptions;
    @SuppressWarnings("unused")
    private final Collection<StaffData> loanOfficerOptions;
    @SuppressWarnings("unused")
    private final StaffAccountSummaryCollectionData accountSummaryCollection;

    public static BulkTransferLoanOfficerData templateForBulk(final Long officeId, final Long fromLoanOfficerId,
            final LocalDate assignmentDate, final Collection<OfficeLookup> officeOptions, final Collection<StaffData> loanOfficerOptions,
            final StaffAccountSummaryCollectionData accountSummaryCollection) {
        return new BulkTransferLoanOfficerData(officeId, fromLoanOfficerId, assignmentDate, officeOptions, loanOfficerOptions,
                accountSummaryCollection);
    }

    public static BulkTransferLoanOfficerData template(final Long fromLoanOfficerId, final Collection<StaffData> loanOfficerOptions,
            final LocalDate assignmentDate) {
        return new BulkTransferLoanOfficerData(null, fromLoanOfficerId, assignmentDate, null, loanOfficerOptions, null);
    }

    private BulkTransferLoanOfficerData(final Long officeId, final Long fromLoanOfficerId, final LocalDate assignmentDate,
            final Collection<OfficeLookup> officeOptions, final Collection<StaffData> loanOfficerOptions,
            final StaffAccountSummaryCollectionData accountSummaryCollection) {
        this.officeId = officeId;
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.assignmentDate = assignmentDate;
        this.officeOptions = officeOptions;
        this.loanOfficerOptions = loanOfficerOptions;
        this.accountSummaryCollection = accountSummaryCollection;
    }
}