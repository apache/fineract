/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.collectionsheet.service.CollectionSheetWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCollectionSheetCommandHandler implements NewCommandSourceHandler {

    private final CollectionSheetWritePlatformService collectionSheetWritePlatformService;

    @Autowired
    public UpdateCollectionSheetCommandHandler(final CollectionSheetWritePlatformService collectionSheetWritePlatformService) {
        this.collectionSheetWritePlatformService = collectionSheetWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.collectionSheetWritePlatformService.updateCollectionSheet(command);
    }

}
