/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.data.AccountTransferDTO;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;

public interface AccountTransfersWritePlatformService {

    CommandProcessingResult create(JsonCommand command);

    void reverseTransfersWithFromAccountType(Long accountNumber, PortfolioAccountType accountTypeId);

    Long transferFunds(AccountTransferDTO accountTransferDTO);

    void reverseAllTransactions(Long accountId, PortfolioAccountType accountTypeId);

    void updateLoanTransaction(Long loanTransactionId, LoanTransaction newLoanTransaction);
    
    CommandProcessingResult refundByTransfer(JsonCommand command);
}