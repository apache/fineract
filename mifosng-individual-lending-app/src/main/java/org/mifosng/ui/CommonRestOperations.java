package org.mifosng.ui;

import java.util.Collection;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.LoanProductData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.data.command.UndoLoanApprovalCommand;
import org.mifosng.data.command.UndoLoanDisbursalCommand;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;

public interface CommonRestOperations {

	void logout(String accessToken);

	void updateProtectedResource(ProtectedResourceDetails loadProtectedResourceDetailsById);

	Collection<LoanProductData> retrieveAllLoanProducts();

	LoanSchedule calculateLoanSchedule(CalculateLoanScheduleCommand command);

	NewLoanWorkflowStepOneData retrieveNewLoanApplicationStepOneDetails(Long clientId);

	Long submitLoanApplication(SubmitLoanApplicationCommand command);

	EntityIdentifier deleteLoan(Long loanId);

	EntityIdentifier approveLoan(LoanStateTransitionCommand command);

	EntityIdentifier undoLoanApproval(UndoLoanApprovalCommand command);

	EntityIdentifier rejectLoan(LoanStateTransitionCommand command);

	EntityIdentifier withdrawLoan(LoanStateTransitionCommand command);

	EntityIdentifier disburseLoan(LoanStateTransitionCommand command);

	EntityIdentifier undloLoanDisbursal(UndoLoanDisbursalCommand command);

	LoanRepaymentData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanRepaymentData retrieveLoanRepaymentDetails(Long loanId, Long repaymentId);

	EntityIdentifier makeLoanRepayment(LoanTransactionCommand command);

	EntityIdentifier adjustLoanRepayment(AdjustLoanTransactionCommand command);

	LoanRepaymentData retrieveNewLoanWaiverDetails(Long loanId);

	EntityIdentifier waiveLoanAmount(LoanTransactionCommand command);

	NewLoanWorkflowStepOneData retrieveNewLoanApplicationDetails(Long clientId, Long productId);
}