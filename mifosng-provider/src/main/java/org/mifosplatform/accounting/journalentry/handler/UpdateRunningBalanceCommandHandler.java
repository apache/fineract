/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.handler;

import org.mifosplatform.accounting.journalentry.service.JournalEntryRunningBalanceUpdateService;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateRunningBalanceCommandHandler implements NewCommandSourceHandler {

    private final JournalEntryRunningBalanceUpdateService journalEntryRunningBalanceUpdateService;

    @Autowired
    public UpdateRunningBalanceCommandHandler(final JournalEntryRunningBalanceUpdateService journalEntryRunningBalanceUpdateService) {
        this.journalEntryRunningBalanceUpdateService = journalEntryRunningBalanceUpdateService;
    }

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return journalEntryRunningBalanceUpdateService.updateOfficeRunningBalance(command);
    }

}
