/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountForLookup;

public interface SavingAccountWritePlatformService {

    CommandProcessingResult createSavingAccount(JsonCommand command);

    CommandProcessingResult updateSavingAccount(Long accountId, JsonCommand command);

    CommandProcessingResult rejectSavingApplication(JsonCommand command);

    CommandProcessingResult withdrawSavingApplication(JsonCommand command);

    CommandProcessingResult undoSavingAccountApproval(JsonCommand undoCommand);

    CommandProcessingResult approveSavingAccount(JsonCommand command);

    CommandProcessingResult depositMoney(JsonCommand command);

    CommandProcessingResult withdrawSavingAmount(JsonCommand command);
    
    CommandProcessingResult deleteSavingAccount(Long accountId);

	CommandProcessingResult postInterest(Collection<SavingAccountForLookup> savingAccounts);
	
}