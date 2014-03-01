/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.handler;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplyAnnualFeeSavingsAccountCommandHandler implements NewCommandSourceHandler {

    @SuppressWarnings("unused")
    private final SavingsAccountWritePlatformService writePlatformService;

    @Autowired
    public ApplyAnnualFeeSavingsAccountCommandHandler(final SavingsAccountWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        @SuppressWarnings("unused")
        final LocalDate annualFeeTransactionDate = command.localDateValueOfParameterNamed("annualFeeTransactionDate");

        // return
        // this.writePlatformService.applyAnnualFee(command.getSavingsId(),
        // annualFeeTransactionDate);
        return null;
    }
}