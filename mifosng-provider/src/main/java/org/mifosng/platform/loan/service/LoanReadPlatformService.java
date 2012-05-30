package org.mifosng.platform.loan.service;

import org.mifosng.data.LoanAccountData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.platform.api.data.NewLoanWorkflowStepOneData;

public interface LoanReadPlatformService {

	LoanAccountData retrieveLoanAccountDetails(Long loanId);
	
	NewLoanWorkflowStepOneData retrieveClientAndProductDetails(Long clientId, Long productId);
	
	LoanRepaymentData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanRepaymentData retrieveNewLoanWaiverDetails(Long loanId);

	LoanRepaymentData retrieveLoanRepaymentDetails(Long loanId, Long transactionId);
}