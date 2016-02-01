/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.data.AccountTransferData;
import org.mifosplatform.infrastructure.core.service.SearchParameters;

public interface AccountTransfersReadPlatformService {

    AccountTransferData retrieveTemplate(Long fromOfficeId, Long fromClientId, Long fromAccountId, Integer fromAccountType,
            Long toOfficeId, Long toClientId, Long toAccountId, Integer toAccountType);

    Page<AccountTransferData> retrieveAll(SearchParameters searchParameters, Long accountDetailId);

    AccountTransferData retrieveOne(Long transferId);

    boolean isAccountTransfer(Long transactionId, PortfolioAccountType accountType);

    Page<AccountTransferData> retrieveByStandingInstruction(Long id, SearchParameters searchParameters);

    Collection<Long> fetchPostInterestTransactionIds(Long accountId);
    
    AccountTransferData retrieveRefundByTransferTemplate(Long fromOfficeId, Long fromClientId, Long fromAccountId, Integer fromAccountType,
            Long toOfficeId, Long toClientId, Long toAccountId, Integer toAccountType);
}