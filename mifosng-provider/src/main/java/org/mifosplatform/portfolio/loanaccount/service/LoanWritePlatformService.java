package org.mifosplatform.portfolio.loanaccount.service;

import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.staff.command.BulkTransferLoanOfficerCommand;
import org.mifosplatform.portfolio.loanaccount.command.AdjustLoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanApplicationCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanStateTransitionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.UndoStateTransitionCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LoanWritePlatformService {
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_LOAN', 'CREATEHISTORIC_LOAN')")
	EntityIdentifier submitLoanApplication(LoanApplicationCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_LOAN', 'UPDATEHISTORIC_LOAN')")
	EntityIdentifier modifyLoanApplication(LoanApplicationCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_LOAN')")
	EntityIdentifier deleteLoan(Long loanId);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'APPROVE_LOAN', 'APPROVEINPAST_LOAN')")
	EntityIdentifier approveLoanApplication(LoanStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'APPROVALUNDO_LOAN')")
	EntityIdentifier undoLoanApproval(UndoStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'REJECT_LOAN', 'REJECTINPAST_LOAN')")
	EntityIdentifier rejectLoan(LoanStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'WITHDRAW_LOAN', 'WITHDRAWINPAST_LOAN')")
	EntityIdentifier withdrawLoan(LoanStateTransitionCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DISBURSE_LOAN', 'DISBURSEINPAST_LOAN')")
	EntityIdentifier disburseLoan(LoanStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DISBURSALUNDO_LOAN')")
	public EntityIdentifier undoLoanDisbursal(UndoStateTransitionCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'REPAYMENT_LOAN', 'REPAYMENTINPAST_LOAN')")
	public EntityIdentifier makeLoanRepayment(LoanTransactionCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'ADJUST_LOAN')")
	EntityIdentifier adjustLoanTransaction(AdjustLoanTransactionCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'WAIVEINTERESTPORTION_LOAN')")
	EntityIdentifier waiveInterestOnLoan(LoanTransactionCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'WRITEOFF_LOAN')")
	EntityIdentifier writeOff(LoanTransactionCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CLOSE_LOAN')")
	EntityIdentifier closeLoan(LoanTransactionCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CLOSEASRESCHEDULED_LOAN')")
	EntityIdentifier closeAsRescheduled(LoanTransactionCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_LOANCHARGE')")
    EntityIdentifier addLoanCharge(LoanChargeCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_LOANCHARGE')")
    EntityIdentifier updateLoanCharge(LoanChargeCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_LOANCHARGE')")
    EntityIdentifier deleteLoanCharge(final Long loanId, final Long loanChargeId);
    
    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'WAIVE_LOANCHARGE')")
	EntityIdentifier waiveLoanCharge(LoanChargeCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'BULKREASSIGN_LOAN')")
    EntityIdentifier bulkLoanReassignment(final BulkTransferLoanOfficerCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
    EntityIdentifier loanReassignment(final BulkTransferLoanOfficerCommand command);
}