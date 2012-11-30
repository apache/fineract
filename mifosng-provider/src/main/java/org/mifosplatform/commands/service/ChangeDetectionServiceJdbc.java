package org.mifosplatform.commands.service;

import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.PortfolioCommandSerializerService;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChangeDetectionServiceJdbc implements ChangeDetectionService {

    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final PortfolioCommandSerializerService commandSerializerService;

    @Autowired
    public ChangeDetectionServiceJdbc(
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService, 
            final ClientReadPlatformService clientReadPlatformService) {
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.clientReadPlatformService = clientReadPlatformService;
    }

    @Override
    public String detectChangesOnUpdate(final String resourceName, final Long resourceId, final String commandSerializedAsJson) {

        String changesOnlyJson = "";

        if ("clients".equalsIgnoreCase(resourceName)) {
            // FIXME - KW/JW - this change detection code for update of clients
            // using ClientCommand can re placed when more suitable sql version is
            // implemented.
            final ClientData originalClient = this.clientReadPlatformService.retrieveIndividualClient(resourceId);
            final ClientData changedClient = this.apiDataConversionService.convertInternalJsonFormatToClientDataChange(resourceId,
                    commandSerializedAsJson);
    
            final String baseJson = this.apiJsonSerializerService.serializeClientDataToJson(originalClient);
            final String workingJson = this.apiJsonSerializerService.serializeClientDataToJson(changedClient);
            final ClientCommand changesOnly = this.apiDataConversionService.detectChanges(resourceId, baseJson, workingJson);
    
            changesOnlyJson = this.commandSerializerService.serializeCommandToJson(changesOnly);
        } else {
            changesOnlyJson = commandSerializedAsJson;
        }

        return changesOnlyJson;
    }

}