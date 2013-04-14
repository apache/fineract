package org.mifosplatform.portfolio.loanaccount.service;

import org.mifosplatform.organisation.staff.data.StaffAccountSummaryCollectionData;


public interface BulkLoansReadPlatformService {

    StaffAccountSummaryCollectionData retrieveLoanOfficerAccountSummary(final Long loanOfficerId);
}
