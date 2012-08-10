package org.mifosng.platform.savingproduct.service;

import java.util.Collection;

import org.mifosng.platform.api.data.DepositAccountData;

public interface DepositAccountReadPlatformService {

	Collection<DepositAccountData> retrieveAllDepositAccounts();

	DepositAccountData retrieveDepositAccount(Long accountId);

	DepositAccountData retrieveNewDepositAccountDetails();	
}
