package org.mifosplatform.commands.service;

import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataServiceImpl;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChangeDetectionServiceJdbc implements ChangeDetectionService {

    private final static Logger logger = LoggerFactory.getLogger(ChangeDetectionServiceJdbc.class);
    
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final CommandSerializer commandSerializerService;
    private final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer;

    @Autowired
    public ChangeDetectionServiceJdbc(final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer,
            final PortfolioApiDataConversionService apiDataConversionService, final CommandSerializer commandSerializerService,
            final ClientReadPlatformService clientReadPlatformService) {
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.clientReadPlatformService = clientReadPlatformService;
    }

    @Override
    public String detectChangesOnUpdate(final String resourceName, final Long resourceId, final String commandSerializedAsJson) {
logger.info(resourceName + ": ID: " + resourceId + "  JSON: " + commandSerializedAsJson);
        String changesOnlyJson = "";

        if ("clients".equalsIgnoreCase(resourceName)) {
            // FIXME - KW/JW - this change detection code for update of clients
            // using ClientCommand can re placed when more suitable sql version
            // is
            // implemented.
            final ClientData originalClient = this.clientReadPlatformService.retrieveIndividualClient(resourceId);
            final ClientData changedClient = this.apiDataConversionService.convertInternalJsonFormatToClientDataChange(resourceId,
                    commandSerializedAsJson);

            final String baseJson = this.toApiJsonSerializer.serialize(originalClient);
            final String workingJson = this.toApiJsonSerializer.serialize(changedClient);
            final ClientCommand changesOnly = this.apiDataConversionService.detectChanges(resourceId, baseJson, workingJson);

            changesOnlyJson = this.commandSerializerService.serializeCommandToJson(changesOnly);
        } else {
            changesOnlyJson = commandSerializedAsJson;
        }

        return changesOnlyJson;
    }

}