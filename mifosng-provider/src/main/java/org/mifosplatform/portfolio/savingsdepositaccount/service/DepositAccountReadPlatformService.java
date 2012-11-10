package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountData;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountsForLookup;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositPermissionData;

public interface DepositAccountReadPlatformService {

    Collection<DepositAccountData> retrieveAllDepositAccounts();

    DepositAccountData retrieveDepositAccount(Long accountId);

    DepositAccountData retrieveNewDepositAccountDetails(Long clientId, Long productId);

    DepositPermissionData retrieveDepositAccountsPermissions(DepositAccountData depositAccountData);

    BigDecimal retrieveAvailableInterestForWithdrawal(DepositAccountData account);

    Collection<DepositAccountsForLookup> retrieveDepositAccountForLookup();
}

