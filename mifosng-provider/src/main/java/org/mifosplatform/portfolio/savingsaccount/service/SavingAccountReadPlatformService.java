package org.mifosplatform.portfolio.savingsaccount.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountForLookup;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountTransactionsData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingPermissionData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;

public interface SavingAccountReadPlatformService {

    Collection<SavingAccountData> retrieveAllSavingsAccounts();

    SavingAccountData retrieveSavingsAccount(Long accountId);
    
    SavingAccountData retrieveNewSavingsAccountDetails(Long clientId, Long productId);
    
    SavingPermissionData retrieveSavingAccountPermissions(SavingAccountData data);

	BigDecimal deriveSavingDueAmount(SavingAccountData account);

	SavingScheduleData retrieveSavingsAccountSchedule(Long accountId, CurrencyData currencyData);

	Collection<SavingAccountForLookup> retrieveSavingAccountsForLookUp();

	Collection<SavingAccountTransactionsData> retrieveSavingsAccountTransactions(Long accountId);
}
