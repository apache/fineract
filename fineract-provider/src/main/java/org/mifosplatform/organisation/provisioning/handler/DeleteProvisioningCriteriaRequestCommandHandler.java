/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.handler;

import org.mifosplatform.commands.annotation.CommandType;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.organisation.provisioning.service.ProvisioningCriteriaWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "PROVISIONCRITERIA", action = "DELETE")
public class DeleteProvisioningCriteriaRequestCommandHandler implements NewCommandSourceHandler {

    private final ProvisioningCriteriaWritePlatformService provisioningCriteriaWritePlatformService;

    @Autowired
    public DeleteProvisioningCriteriaRequestCommandHandler(final ProvisioningCriteriaWritePlatformService provisioningCriteriaWritePlatformService) {
        this.provisioningCriteriaWritePlatformService = provisioningCriteriaWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(JsonCommand jsonCommand) {
        return this.provisioningCriteriaWritePlatformService.deleteProvisioningCriteria(jsonCommand.entityId()) ;
    }

}
