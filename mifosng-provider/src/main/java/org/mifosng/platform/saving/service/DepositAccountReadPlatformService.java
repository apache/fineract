package org.mifosng.platform.saving.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosng.platform.api.data.DepositAccountData;
import org.mifosng.platform.api.data.DepositPermissionData;

public interface DepositAccountReadPlatformService {

	Collection<DepositAccountData> retrieveAllDepositAccounts();

	DepositAccountData retrieveDepositAccount(Long accountId);

	DepositAccountData retrieveNewDepositAccountDetails(Long clientId, Long productId);

	DepositPermissionData retrieveDepositAccountsPermissions(DepositAccountData depositAccountData);

	BigDecimal retrieveAvailableInterestForWithdrawal(DepositAccountData account);	
}
