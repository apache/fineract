/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.handler;

import org.mifosplatform.commands.annotation.CommandType;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.shares.service.ShareProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "SHAREPRODUCT", action = "UPDATE")
public class UpdateShareProductCommandHandler implements NewCommandSourceHandler {

    private final ShareProductWritePlatformService shareProductWritePlatformService ;
    
    @Autowired
    public UpdateShareProductCommandHandler(final ShareProductWritePlatformService shareProductWritePlatformService) {
        this.shareProductWritePlatformService = shareProductWritePlatformService ;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(JsonCommand jsonCommand) {
        return this.shareProductWritePlatformService.updateProduct(jsonCommand.entityId(), jsonCommand);
    }

}
