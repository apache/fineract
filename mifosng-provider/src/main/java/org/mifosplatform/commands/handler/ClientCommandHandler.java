package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioCommandDeserializerService;
import org.mifosng.platform.client.service.ClientWritePlatformService;
import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.service.ChangeDetectionService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final ClientWritePlatformService clientWritePlatformService;
    private final PortfolioCommandDeserializerService commandDeserializerService;

    @Autowired
    public ClientCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final PortfolioCommandDeserializerService commandDeserializerService,
            final ClientWritePlatformService clientWritePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.commandDeserializerService = commandDeserializerService;
        this.clientWritePlatformService = clientWritePlatformService;
    }

    public CommandSource handle(final CommandSource commandSource, final String commandSerializedAsJson) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final ClientCommand command = this.commandDeserializerService.deserializeClientCommand(commandSource.resourceId(),
                commandSource.json(), false);

        CommandSource commandSourceResult = commandSource.copy();
        Long newResourceId = null;

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
                final String jsonOfChangesOnly = this.changeDetectionService.detectChangesOnUpdate(commandSource.resourceName(),
                        commandSource.resourceId(), commandSource.json());
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                final ClientCommand changesOnly = this.commandDeserializerService.deserializeClientCommand(commandSource.resourceId(),
                        jsonOfChangesOnly, false);

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
        final ClientCommand command = this.commandDeserializerService.deserializeClientCommand(commandSourceResult.resourceId(),
                commandSourceResult.json(), true);

        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "CREATE_CLIENT_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_CLIENT_CHECKER", allowedPermissions);

            resourceId = this.clientWritePlatformService.createClient(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "UPDATE_CLIENT_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_CLIENT_CHECKER", allowedPermissions);

            EntityIdentifier result = this.clientWritePlatformService.updateClientDetails(command);
            resourceId = result.getEntityId();
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "DELETE_CLIENT_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("DELETE_CLIENT_CHECKER", allowedPermissions);

            EntityIdentifier result = this.clientWritePlatformService.deleteClient(command);
            resourceId = result.getEntityId();
            commandSourceResult.markAsChecked(checker, new LocalDate());
        }

        return commandSourceResult;
    }
}