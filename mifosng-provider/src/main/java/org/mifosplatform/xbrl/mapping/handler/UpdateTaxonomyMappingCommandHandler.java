package org.mifosplatform.xbrl.mapping.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.xbrl.mapping.service.WriteTaxonomyMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateTaxonomyMappingCommandHandler implements NewCommandSourceHandler {

    private final WriteTaxonomyMappingService writeTaxonomyService;

    @Autowired
    public UpdateTaxonomyMappingCommandHandler(WriteTaxonomyMappingService writeTaxonomyService) {
        this.writeTaxonomyService = writeTaxonomyService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return this.writeTaxonomyService.updateMapping(command.entityId(), command);
    }

}
