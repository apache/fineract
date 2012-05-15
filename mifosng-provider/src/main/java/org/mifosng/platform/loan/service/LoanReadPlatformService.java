package org.mifosng.platform.loan.service;

import org.mifosng.data.LoanAccountData;

public interface LoanReadPlatformService {

	LoanAccountData retrieveLoanAccountDetails(Long loanId);
}