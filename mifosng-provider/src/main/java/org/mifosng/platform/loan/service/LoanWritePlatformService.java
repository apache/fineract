package org.mifosng.platform.loan.service;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LoanWritePlatformService {
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_SUBMIT_NEW_LOAN_APPLICATION_ROLE', 'CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE')")
	EntityIdentifier submitLoanApplication(SubmitLoanApplicationCommand command);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_DELETE_LOAN_THAT_IS_SUBMITTED_AND_NOT_APPROVED')")
	EntityIdentifier deleteLoan(Long loanId);
}