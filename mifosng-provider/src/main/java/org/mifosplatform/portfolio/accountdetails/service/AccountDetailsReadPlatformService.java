package org.mifosplatform.portfolio.accountdetails.service;

import java.util.Collection;

import org.mifosplatform.portfolio.accountdetails.data.AccountSummaryCollectionData;
import org.mifosplatform.portfolio.accountdetails.data.LoanAccountSummaryData;

public interface AccountDetailsReadPlatformService {

    public AccountSummaryCollectionData retrieveClientAccountDetails(final Long clientId);

    public AccountSummaryCollectionData retrieveGroupAccountDetails(final Long groupId);

    public Collection<LoanAccountSummaryData> retrieveClientLoanAccountsByLoanOfficerId(final Long clientId, final Long loanOfficerId);

    public Collection<LoanAccountSummaryData> retrieveGroupLoanAccountsByLoanOfficerId(final Long groupId, final Long loanOfficerId);
}
