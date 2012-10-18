package org.mifosng.platform.loan.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosng.platform.api.LoanScheduleData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.DisbursementData;
import org.mifosng.platform.api.data.LoanBasicDetailsData;
import org.mifosng.platform.api.data.LoanPermissionData;
import org.mifosng.platform.api.data.LoanTransactionData;

public interface LoanReadPlatformService {

	LoanBasicDetailsData retrieveLoanAccountDetails(Long loanId);

	LoanScheduleData retrieveRepaymentSchedule(Long loanId, CurrencyData currency, DisbursementData disbursement, BigDecimal totalChargesAtDisbursement, BigDecimal inArrearsTolerance);

	Collection<LoanTransactionData> retrieveLoanTransactions(Long loanId);

	LoanPermissionData retrieveLoanPermissions(
			LoanBasicDetailsData loanBasicDetails, boolean isWaiverAllowed,
			int repaymentAndWaiveCount);

	LoanBasicDetailsData retrieveClientAndProductDetails(Long clientId, Long productId);

    LoanBasicDetailsData retrieveGroupAndProductDetails(Long groupId, Long productId);

    LoanTransactionData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanTransactionData retrieveNewLoanWaiveInterestDetails(Long loanId);

	LoanTransactionData retrieveLoanTransactionDetails(Long loanId, Long transactionId);

	LoanTransactionData retrieveNewClosureDetails();
}