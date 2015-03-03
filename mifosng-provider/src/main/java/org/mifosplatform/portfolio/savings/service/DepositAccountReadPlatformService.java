/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.Collection;
import java.util.Map;

import org.mifosplatform.infrastructure.core.data.PaginationParameters;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.portfolio.account.data.AccountTransferDTO;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.data.DepositAccountData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionData;

public interface DepositAccountReadPlatformService {

    Collection<DepositAccountData> retrieveAll(final DepositAccountType depositAccountType, final PaginationParameters paginationParameters);

    Page<DepositAccountData> retrieveAllPaged(final DepositAccountType depositAccountType, final PaginationParameters paginationParameters);

    Collection<DepositAccountData> retrieveAllForLookup(final DepositAccountType depositAccountType);

    DepositAccountData retrieveOne(final DepositAccountType depositAccountType, Long accountId);

    DepositAccountData retrieveOneWithClosureTemplate(final DepositAccountType depositAccountType, Long accountId);

    DepositAccountData retrieveOneWithChartSlabs(final DepositAccountType depositAccountType, Long productId);

    Collection<SavingsAccountTransactionData> retrieveAllTransactions(final DepositAccountType depositAccountType, Long accountId);

    DepositAccountData retrieveTemplate(final DepositAccountType depositAccountType, Long clientId, Long groupId, Long productId,
            boolean staffInSelectedOfficeOnly);

    Collection<DepositAccountData> retrieveForMaturityUpdate();

    SavingsAccountTransactionData retrieveRecurringAccountDepositTransactionTemplate(final Long accountId);

    Collection<AccountTransferDTO> retrieveDataForInterestTransfer();

    Collection<Map<String, Object>> retriveDataForRDScheduleCreation();
}
