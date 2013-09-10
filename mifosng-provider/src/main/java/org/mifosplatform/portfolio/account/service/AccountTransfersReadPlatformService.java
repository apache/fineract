/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.data.AccountTransferData;
import org.mifosplatform.portfolio.group.service.SearchParameters;

public interface AccountTransfersReadPlatformService {

    AccountTransferData retrieveTemplate(Long fromOfficeId, Long fromClientId, Long fromAccountId, Integer fromAccountType,
            Long toOfficeId, Long toClientId, Long toAccountId, Integer toAccountType);

    Page<AccountTransferData> retrieveAll(SearchParameters searchParameters);

    AccountTransferData retrieveOne(Long transferId);
    
    boolean isAccountTransfer(Long transactionId,PortfolioAccountType accountType);
}