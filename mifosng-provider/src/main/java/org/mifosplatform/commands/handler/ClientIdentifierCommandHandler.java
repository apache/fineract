package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.service.ChangeDetectionService;
import org.mifosplatform.infrastructure.core.api.PortfolioCommandDeserializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.client.service.ClientWritePlatformService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientIdentifierCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final ClientWritePlatformService clientWritePlatformService;
    private final PortfolioCommandDeserializerService commandDeserializerService;

    @Autowired
    public ClientIdentifierCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final PortfolioCommandDeserializerService commandDeserializerService,
            final ClientWritePlatformService clientWritePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.commandDeserializerService = commandDeserializerService;
        this.clientWritePlatformService = clientWritePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final ClientIdentifierCommand command = this.commandDeserializerService.deserializeClientIdentifierCommand(
                commandSource.resourceId(), null, commandSource.json(), false);

        CommandSource commandSourceResult = commandSource.copy();
        Long newResourceId = null;

        if (commandSource.isCreate()) {
            try {
                newResourceId = this.clientWritePlatformService.addClientIdentifier(command);
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

                final ClientIdentifierCommand changesOnly = this.commandDeserializerService.deserializeClientIdentifierCommand(
                        commandSource.resourceId(), null, jsonOfChangesOnly, false);

                EntityIdentifier result = this.clientWritePlatformService.updateClientIdentifier(changesOnly);
                newResourceId = result.getEntityId();

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isDelete()) {
            try {
                EntityIdentifier result = this.clientWritePlatformService.deleteClientIdentifier(command);
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

        Long resourceId = null;
        final ClientIdentifierCommand command = this.commandDeserializerService.deserializeClientIdentifierCommand(
                commandSourceResult.resourceId(), null, commandSourceResult.json(), true);

        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "CREATE_CLIENTIDENTIFIER_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_CLIENTIDENTIFIER_CHECKER", allowedPermissions);

            resourceId = this.clientWritePlatformService.addClientIdentifier(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "UPDATE_CLIENTIDENTIFIER_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_CLIENTIDENTIFIER_CHECKER", allowedPermissions);

            EntityIdentifier result = this.clientWritePlatformService.updateClientIdentifier(command);
            resourceId = result.getEntityId();
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "DELETE_CLIENTIDENTIFIER_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("DELETE_CLIENTIDENTIFIER_CHECKER", allowedPermissions);

            EntityIdentifier result = this.clientWritePlatformService.deleteClientIdentifier(command);
            resourceId = result.getEntityId();
            commandSourceResult.markAsChecked(checker, new LocalDate());
        }

        return commandSourceResult;
    }
}