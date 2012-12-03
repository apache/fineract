package org.mifosplatform.portfolio.savingsaccount.service;

import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SavingAccountWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_SAVINGSACCOUNT')")
    EntityIdentifier createSavingAccount(SavingAccountCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_SAVINGSACCOUNT')")
    EntityIdentifier updateSavingAccount(SavingAccountCommand command);
}