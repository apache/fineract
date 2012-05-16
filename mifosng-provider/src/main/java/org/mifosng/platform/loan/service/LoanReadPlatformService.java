package org.mifosng.platform.loan.service;

import org.mifosng.data.LoanAccountData;
import org.mifosng.data.NewLoanWorkflowStepOneData;

public interface LoanReadPlatformService {

	LoanAccountData retrieveLoanAccountDetails(Long loanId);
	
	NewLoanWorkflowStepOneData retrieveClientAndProductDetails(Long clientId, Long productId);
}