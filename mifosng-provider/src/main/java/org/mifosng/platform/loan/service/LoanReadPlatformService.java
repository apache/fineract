package org.mifosng.platform.loan.service;

import org.mifosng.platform.api.data.LoanAccountData;
import org.mifosng.platform.api.data.LoanTransactionData;
import org.mifosng.platform.api.data.NewLoanData;

public interface LoanReadPlatformService {

	LoanAccountData retrieveLoanAccountDetails(Long loanId);
	
	NewLoanData retrieveClientAndProductDetails(Long clientId, Long productId);
	
	LoanTransactionData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanTransactionData retrieveNewLoanWaiverDetails(Long loanId);

	LoanTransactionData retrieveLoanTransactionDetails(Long loanId, Long transactionId);
}