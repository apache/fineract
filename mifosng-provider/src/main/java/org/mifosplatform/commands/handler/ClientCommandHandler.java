package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.service.ClientWritePlatformService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class ClientCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ClientWritePlatformService clientWritePlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson toApiJsonSerializer;

    @Autowired
    public ClientCommandHandler(final PlatformSecurityContext context, final FromJsonHelper fromApiJsonHelper,
            final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson toApiJsonSerializer,
            final ClientWritePlatformService clientWritePlatformService) {
        this.context = context;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.clientWritePlatformService = clientWritePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSource.json());
        final JsonCommand command = JsonCommand.from(commandSource.json(), parsedCommand, this.fromApiJsonHelper);

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
                EntityIdentifier result = this.clientWritePlatformService.updateClientDetails(commandSource.resourceId(), command);
                newResourceId = result.getEntityId();
                
                final String jsonOfChangesOnly = toApiJsonSerializer.serialize(result.getChanges());
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isDelete()) {
            try {
                EntityIdentifier result = this.clientWritePlatformService.deleteClient(commandSource.resourceId(), command);
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

        final JsonElement parsedCommand = fromApiJsonHelper.parse(commandSourceResult.json());
        final JsonCommand command = JsonCommand.withMakerCheckerApproval(commandSourceResult.json(), parsedCommand, fromApiJsonHelper);

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

            this.clientWritePlatformService.updateClientDetails(commandSourceResult.resourceId(), command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "DELETE_CLIENT_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("DELETE_CLIENT_CHECKER", allowedPermissions);

            this.clientWritePlatformService.deleteClient(commandSourceResult.resourceId(), command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        }

        return commandSourceResult;
    }
}