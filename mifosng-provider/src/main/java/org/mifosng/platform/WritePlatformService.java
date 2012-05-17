package org.mifosng.platform;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.UpdateUsernamePasswordCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface WritePlatformService {

	void updateUsernamePasswordOnFirstTimeLogin(UpdateUsernamePasswordCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_MAKE_LOAN_REPAYMENT_ROLE', 'CAN_MAKE_LOAN_REPAYMENT_IN_THE_PAST_ROLE')")
	public EntityIdentifier makeLoanRepayment(LoanTransactionCommand command);

	EntityIdentifier adjustLoanTransaction(AdjustLoanTransactionCommand command);

	EntityIdentifier waiveLoanAmount(LoanTransactionCommand command);
}