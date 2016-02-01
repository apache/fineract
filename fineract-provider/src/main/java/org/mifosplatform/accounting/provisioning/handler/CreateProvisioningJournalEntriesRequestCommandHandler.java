/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.handler;

import org.mifosplatform.accounting.provisioning.service.ProvisioningEntriesWritePlatformService;
import org.mifosplatform.commands.annotation.CommandType;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "PROVISIONJOURNALENTRIES", action = "CREATE")
public class CreateProvisioningJournalEntriesRequestCommandHandler implements NewCommandSourceHandler {

    private final ProvisioningEntriesWritePlatformService provisioningEntriesWritePlatformService ;
    @Autowired
    public CreateProvisioningJournalEntriesRequestCommandHandler(
            final ProvisioningEntriesWritePlatformService provisioningEntriesWritePlatformService) {
        this.provisioningEntriesWritePlatformService = provisioningEntriesWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(JsonCommand jsonCommand) {
        return this.provisioningEntriesWritePlatformService.createProvisioningJournalEntries(jsonCommand.entityId(), jsonCommand) ;
    }

}
