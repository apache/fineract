package org.mifosng.platform.accounting.service;

import org.mifosng.platform.accounting.api.commands.ChartOfAccountCommand;

public interface ChartOfAccountsWritePlatformService {

	Long createAccount(ChartOfAccountCommand command);

}
