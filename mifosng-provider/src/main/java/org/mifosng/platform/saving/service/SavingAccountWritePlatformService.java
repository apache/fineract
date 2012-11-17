package org.mifosng.platform.saving.service;

import org.mifosng.platform.api.commands.SavingAccountCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SavingAccountWritePlatformService {
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_SAVINGSACCOUNT')")
	EntityIdentifier createSavingAccount(SavingAccountCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_SAVINGSACCOUNT')")
	EntityIdentifier updateSavingAccount(SavingAccountCommand command);
}