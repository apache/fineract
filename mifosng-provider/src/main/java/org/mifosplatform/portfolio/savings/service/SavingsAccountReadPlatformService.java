/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.SearchParameters;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionData;

public interface SavingsAccountReadPlatformService {

    Page<SavingsAccountData> retrieveAll(SearchParameters searchParameters);

    Collection<SavingsAccountData> retrieveAllForLookup(Long clientId);

    Collection<SavingsAccountData> retrieveActiveForLookup(Long clientId, DepositAccountType depositAccountType);

    SavingsAccountData retrieveOne(Long savingsId);

    SavingsAccountData retrieveTemplate(Long clientId, Long groupId, Long productId, boolean staffInSelectedOfficeOnly);

    SavingsAccountTransactionData retrieveDepositTransactionTemplate(Long savingsId, DepositAccountType depositAccountType);

    Collection<SavingsAccountTransactionData> retrieveAllTransactions(Long savingsId, DepositAccountType depositAccountType);

    // Collection<SavingsAccountAnnualFeeData>
    // retrieveAccountsWithAnnualFeeDue();

    SavingsAccountTransactionData retrieveSavingsTransaction(Long savingsId, Long transactionId, DepositAccountType depositAccountType);

    Collection<SavingsAccountData> retrieveForLookup(Long clientId, Boolean overdraft);
}