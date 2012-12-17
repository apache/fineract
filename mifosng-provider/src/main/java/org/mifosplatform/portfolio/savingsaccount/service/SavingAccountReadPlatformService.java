package org.mifosplatform.portfolio.savingsaccount.service;

import java.util.Collection;

import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountData;

public interface SavingAccountReadPlatformService {

    Collection<SavingAccountData> retrieveAllSavingsAccounts();

    SavingAccountData retrieveSavingsAccount(Long accountId);
    
    SavingAccountData retrieveNewSavingsAccountDetails(Long clientId, Long productId);
}
