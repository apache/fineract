/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountsForLookup;

public interface DepositAccountWritePlatformService {

    CommandProcessingResult createDepositAccount(JsonCommand command);

    CommandProcessingResult updateDepositAccount(Long accountId, JsonCommand command);

    CommandProcessingResult deleteDepositAccount(Long entityId);

    CommandProcessingResult approveDepositApplication(JsonCommand command);

    CommandProcessingResult rejectDepositApplication(JsonCommand command);

    CommandProcessingResult withdrawDepositApplication(JsonCommand command);

    CommandProcessingResult undoDepositApproval(JsonCommand undoCommand);

    CommandProcessingResult withdrawDepositAccountMoney(JsonCommand command);

    CommandProcessingResult withdrawDepositAccountInterestMoney(JsonCommand command);

    CommandProcessingResult renewDepositAccount(JsonCommand command);

    CommandProcessingResult postInterestToDepositAccount(Collection<DepositAccountsForLookup> accounts);

}