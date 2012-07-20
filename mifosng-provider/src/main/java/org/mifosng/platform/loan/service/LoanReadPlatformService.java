package org.mifosng.platform.loan.service;

import java.util.Collection;

import org.mifosng.platform.api.data.LoanAccountSummaryData;
import org.mifosng.platform.api.data.LoanBasicDetailsData;
import org.mifosng.platform.api.data.LoanPermissionData;
import org.mifosng.platform.api.data.LoanRepaymentPeriodData;
import org.mifosng.platform.api.data.LoanTransactionData;
import org.mifosng.platform.api.data.LoanTransactionDataDTO;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.NewLoanData;

public interface LoanReadPlatformService {

	LoanBasicDetailsData retrieveLoanAccountDetails(Long loanId);

	Collection<LoanRepaymentPeriodData> retrieveRepaymentSchedule(Long loanId);

	Collection<LoanTransactionDataDTO> retrieveLoanPayments(Long loanId);

	LoanAccountSummaryData retrieveSummary(MoneyData principal,
			Collection<LoanRepaymentPeriodData> repaymentSchedule,
			Collection<LoanTransactionDataDTO> loanRepayments);

	LoanPermissionData retrieveLoanPermissions(
			LoanBasicDetailsData loanBasicDetails, boolean isWaiverAllowed,
			int repaymentAndWaiveCount);

	NewLoanData retrieveClientAndProductDetails(Long clientId, Long productId);

	LoanTransactionData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanTransactionData retrieveNewLoanWaiverDetails(Long loanId);

	LoanTransactionData retrieveLoanTransactionDetails(Long loanId,
			Long transactionId);

}