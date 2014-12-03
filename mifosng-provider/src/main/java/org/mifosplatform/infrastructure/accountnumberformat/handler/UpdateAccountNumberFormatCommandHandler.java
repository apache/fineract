/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.handler;

import javax.transaction.Transactional;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.accountnumberformat.service.AccountNumberFormatWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateAccountNumberFormatCommandHandler implements NewCommandSourceHandler {

    private final AccountNumberFormatWritePlatformService accountNumberFormatWritePlatformService;

    @Autowired
    public UpdateAccountNumberFormatCommandHandler(final AccountNumberFormatWritePlatformService accountNumberFormatWritePlatformService) {
        this.accountNumberFormatWritePlatformService = accountNumberFormatWritePlatformService;
    }

    @Override
    @Transactional
    public CommandProcessingResult processCommand(JsonCommand command) {
        return this.accountNumberFormatWritePlatformService.updateAccountNumberFormat(command.entityId(), command);
    }

}
