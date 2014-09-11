/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.service.LoanRescheduleRequestWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApproveLoanRescheduleRequestCommandHandler implements NewCommandSourceHandler {
	private final LoanRescheduleRequestWritePlatformService loanRescheduleRequestWritePlatformService;
	
	@Autowired
	public ApproveLoanRescheduleRequestCommandHandler(
			LoanRescheduleRequestWritePlatformService loanRescheduleRequestWritePlatformService) {
		this.loanRescheduleRequestWritePlatformService = loanRescheduleRequestWritePlatformService;
	}
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand jsonCommand) {
		return this.loanRescheduleRequestWritePlatformService.approve(jsonCommand);
	}
}
