package org.mifosng.platform.saving.service;

import java.util.Collection;

import org.mifosng.platform.api.data.SavingAccountData;

public interface SavingAccountReadPlatformService {
	
	Collection<SavingAccountData> retrieveAllSavingsAccounts();
	
	SavingAccountData retrieveSavingsAccount(Long accountId);
	
	SavingAccountData retrieveNewSavingsAccountDetails(Long clientId, Long productId);

}
