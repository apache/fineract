package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.service.ChangeDetectionService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.serialization.ClientCommandFromCommandJsonDeserializer;
import org.mifosplatform.portfolio.client.service.ClientWritePlatformService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final ClientCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer;
    private final ClientWritePlatformService clientWritePlatformService;

    @Autowired
    public ClientCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final ClientCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer,
            final ClientWritePlatformService clientWritePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.commandFromCommandJsonDeserializer = commandFromCommandJsonDeserializer;
        this.clientWritePlatformService = clientWritePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final ClientCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(commandSource.resourceId(),
                commandSource.json());

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

                final ClientCommand changesOnly = this.commandFromCommandJsonDeserializer.commandFromCommandJson(
                        commandSource.resourceId(), jsonOfChangesOnly);
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

    @Override
    public CommandSource handleCommandForCheckerApproval(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();

        final ClientCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(commandSourceResult.resourceId(),
                commandSourceResult.json(), true);

        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "CREATE_CLIENT_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_CLIENT_CHECKER", allowedPermissions);

            Long resourceId = this.clientWritePlatformService.createClient(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "UPDATE_CLIENT_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_CLIENT_CHECKER", allowedPermissions);

            this.clientWritePlatformService.updateClientDetails(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "DELETE_CLIENT_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("DELETE_CLIENT_CHECKER", allowedPermissions);

            this.clientWritePlatformService.deleteClient(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        }

        return commandSourceResult;
    }
}