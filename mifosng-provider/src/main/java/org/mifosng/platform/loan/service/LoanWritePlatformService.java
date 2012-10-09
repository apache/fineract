package org.mifosng.platform.loan.service;

import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.LoanApplicationCommand;
import org.mifosng.platform.api.commands.UndoStateTransitionCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LoanWritePlatformService {
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_SUBMIT_NEW_LOAN_APPLICATION_ROLE', 'CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE')")
	EntityIdentifier submitLoanApplication(LoanApplicationCommand command);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_SUBMIT_NEW_LOAN_APPLICATION_ROLE', 'CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE')")
	EntityIdentifier modifyLoanApplication(LoanApplicationCommand command);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_DELETE_LOAN_THAT_IS_SUBMITTED_AND_NOT_APPROVED')")
	EntityIdentifier deleteLoan(Long loanId);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_APPROVE_LOAN_ROLE', 'CAN_APPROVE_LOAN_IN_THE_PAST_ROLE')")
	EntityIdentifier approveLoanApplication(LoanStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_UNDO_LOAN_APPROVAL_ROLE')")
	EntityIdentifier undoLoanApproval(UndoStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_REJECT_LOAN_ROLE', 'CAN_REJECT_LOAN_IN_THE_PAST_ROLE')")
	EntityIdentifier rejectLoan(LoanStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_WITHDRAW_LOAN_ROLE', 'CAN_WITHDRAW_LOAN_IN_THE_PAST_ROLE')")
	EntityIdentifier withdrawLoan(LoanStateTransitionCommand command);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_DISBURSE_LOAN_ROLE', 'CAN_DISBURSE_LOAN_IN_THE_PAST_ROLE')")
	EntityIdentifier disburseLoan(LoanStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_UNDO_LOAN_DISBURSAL_ROLE')")
	public EntityIdentifier undoLoanDisbursal(UndoStateTransitionCommand command);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_MAKE_LOAN_REPAYMENT_ROLE', 'CAN_MAKE_LOAN_REPAYMENT_IN_THE_PAST_ROLE')")
	public EntityIdentifier makeLoanRepayment(LoanTransactionCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier adjustLoanTransaction(AdjustLoanTransactionCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier waiveInterestOnLoan(LoanTransactionCommand command);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier writeOff(LoanTransactionCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier closeAsRescheduled(LoanTransactionCommand command);
	
    @PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
    EntityIdentifier addLoanCharge(LoanChargeCommand command);

    @PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
    EntityIdentifier updateLoanCharge(LoanChargeCommand command);

    @PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
    EntityIdentifier deleteLoanCharge(final Long loanId, final Long loanChargeId);
}