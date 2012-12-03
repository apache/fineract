package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.data.LoanBasicDetailsData;
import org.mifosplatform.portfolio.loanaccount.data.LoanPermissionData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;

public interface LoanReadPlatformService {

	LoanBasicDetailsData retrieveLoanAccountDetails(Long loanId);

	LoanScheduleData retrieveRepaymentSchedule(Long loanId, CurrencyData currency, DisbursementData disbursement, BigDecimal totalChargesAtDisbursement, BigDecimal inArrearsTolerance);

	Collection<LoanTransactionData> retrieveLoanTransactions(Long loanId);

	LoanPermissionData retrieveLoanPermissions(
			LoanBasicDetailsData loanBasicDetails, boolean isWaiverAllowed,
			int repaymentAndWaiveCount, boolean existsGuarantor);

	LoanBasicDetailsData retrieveClientAndProductDetails(Long clientId, Long productId);

    LoanBasicDetailsData retrieveGroupAndProductDetails(Long groupId, Long productId);

    LoanTransactionData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanTransactionData retrieveNewLoanWaiveInterestDetails(Long loanId);

	LoanTransactionData retrieveLoanTransactionDetails(Long loanId, Long transactionId);

	LoanTransactionData retrieveNewClosureDetails();
}