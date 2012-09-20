package org.mifosng.platform.saving.service;

import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.commands.DepositAccountWithdrawInterestCommand;
import org.mifosng.platform.api.commands.DepositAccountWithdrawalCommand;
import org.mifosng.platform.api.commands.DepositStateTransitionApprovalCommand;
import org.mifosng.platform.api.commands.DepositStateTransitionCommand;
import org.mifosng.platform.api.commands.UndoStateTransitionCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DepositAccountWritePlatformService {
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	EntityIdentifier createDepositAccount(DepositAccountCommand command);
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	EntityIdentifier deleteDepositAccount(Long productId);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_APPROVE_DEPOSIT_ROLE', 'CAN_APPROVE_DEPOSIT_IN_THE_PAST_ROLE')")
	EntityIdentifier approveDepositApplication(DepositStateTransitionApprovalCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_APPROVE_DEPOSIT_ROLE', 'CAN_APPROVE_DEPOSIT_IN_THE_PAST_ROLE')")
	EntityIdentifier rejectDepositApplication(DepositStateTransitionCommand command);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_APPROVE_DEPOSIT_ROLE', 'CAN_APPROVE_DEPOSIT_IN_THE_PAST_ROLE')")
	EntityIdentifier withdrawDepositApplication(DepositStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_APPROVE_DEPOSIT_ROLE', 'CAN_APPROVE_DEPOSIT_IN_THE_PAST_ROLE')")
	EntityIdentifier undoDepositApproval(UndoStateTransitionCommand undoCommand);

	/*@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_APPROVE_DEPOSIT_ROLE', 'CAN_APPROVE_DEPOSIT_IN_THE_PAST_ROLE')")
	EntityIdentifier matureDepositApplication(DepositStateTransitionCommand command);*/

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_APPROVE_DEPOSIT_ROLE', 'CAN_APPROVE_DEPOSIT_IN_THE_PAST_ROLE')")
	EntityIdentifier withdrawDepositAccountMoney(DepositAccountWithdrawalCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_APPROVE_DEPOSIT_ROLE', 'CAN_APPROVE_DEPOSIT_IN_THE_PAST_ROLE')")
	EntityIdentifier withdrawDepositAccountInterestMoney(DepositAccountWithdrawInterestCommand command);	

}
