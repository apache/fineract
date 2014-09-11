/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface LoanRescheduleRequestWritePlatformService {
	
	CommandProcessingResult create(JsonCommand jsonCommand);

	CommandProcessingResult approve(JsonCommand jsonCommand);
	
	CommandProcessingResult reject(JsonCommand jsonCommand);
}
