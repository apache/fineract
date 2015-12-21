/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.handler;

import org.mifosplatform.commands.annotation.CommandType;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.LoanScheduleWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@CommandType(entity = "LOAN", action = "DELETESCHEDULEEXCEPTIONS")
public class LoanScheduleDeleteVariationCommandHandler implements NewCommandSourceHandler {

    private final LoanScheduleWritePlatformService loanScheduleWritePlatformService;

    @Autowired
    public LoanScheduleDeleteVariationCommandHandler(final LoanScheduleWritePlatformService loanScheduleWritePlatformService) {
        this.loanScheduleWritePlatformService = loanScheduleWritePlatformService;
    }

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return this.loanScheduleWritePlatformService.deleteLoanScheduleVariations(command.getLoanId());
    }

}
