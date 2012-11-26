package org.mifosng.platform.makerchecker.service;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.client.service.ClientWritePlatformService;
import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosng.platform.makerchecker.domain.CommandSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final ClientWritePlatformService clientWritePlatformService;

    @Autowired
    public ClientCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService, final ClientWritePlatformService clientWritePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.apiDataConversionService = apiDataConversionService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.clientWritePlatformService = clientWritePlatformService;
    }

    public CommandSource handle(final CommandSource commandSource, final String apiRequestBodyInJson) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        CommandSource commandSourceResult = commandSource.copy();

        final Long resourceId = commandSource.resourceId();
        Long newResourceId = null;

        final ClientCommand command = this.apiDataConversionService.convertApiRequestJsonToClientCommand(resourceId, apiRequestBodyInJson);
        final String commandSerializedAsJson = this.apiJsonSerializerService.serializeClientCommandToJson(command);
        commandSourceResult.updateJsonTo(commandSerializedAsJson);

        if (commandSource.isCreate()) {
            try {
                newResourceId = this.clientWritePlatformService.createClient(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                final String jsonOfChangesOnly = this.changeDetectionService.detectChangesOnUpdate(commandSource.resourceName(), commandSource.resourceId(), commandSerializedAsJson);
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                final ClientCommand changesOnly = this.apiDataConversionService.convertInternalJsonFormatToClientCommand(
                        commandSource.resourceId(), jsonOfChangesOnly, false);

                EntityIdentifier result = this.clientWritePlatformService.updateClientDetails(changesOnly);
                newResourceId = result.getEntityId();

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isDelete()) {
            try {
                EntityIdentifier result = this.clientWritePlatformService.deleteClient(command);
                newResourceId = result.getEntityId();
                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        }

        return commandSourceResult;
    }

    public CommandSource handle(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();
        
        Long resourceId = null;
        if (commandSourceResult.isClientResource()) {
            final ClientCommand command = this.apiDataConversionService.convertInternalJsonFormatToClientCommand(
                    commandSourceResult.resourceId(), commandSourceResult.json(), true);

            if (commandSourceResult.isCreate()) {
                resourceId = this.clientWritePlatformService.createClient(command);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isUpdate()) {
                EntityIdentifier result = this.clientWritePlatformService.updateClientDetails(command);
                resourceId = result.getEntityId();
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isDelete()) {
                EntityIdentifier result = this.clientWritePlatformService.deleteClient(command);
                resourceId = result.getEntityId();
                commandSourceResult.markAsChecked(checker, new LocalDate());
            }
        }

        commandSourceResult.updateResourceId(resourceId);

        return commandSourceResult;
    }
}