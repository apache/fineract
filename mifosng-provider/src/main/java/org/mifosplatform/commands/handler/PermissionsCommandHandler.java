package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.service.PermissionWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class PermissionsCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson toApiJsonSerializer;
    private final PermissionWritePlatformService permissionWritePlatformService;

    @Autowired
    public PermissionsCommandHandler(final PlatformSecurityContext context,
            final FromJsonHelper fromApiJsonHelper,
            final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson toApiJsonSerializer,
            final PermissionWritePlatformService permissionWritePlatformService) {
        this.context = context;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.permissionWritePlatformService = permissionWritePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSource.json());
        final JsonCommand command = JsonCommand.from(commandSource.json(), parsedCommand, this.fromApiJsonHelper);
        
        CommandSource commandSourceResult = commandSource.copy();

        if (commandSource.isUpdate()) {
            try {
                EntityIdentifier result = this.permissionWritePlatformService.updateMakerCheckerPermissions(command);

                final String jsonOfChangesOnly = toApiJsonSerializer.serialize(result.getChanges());
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);
                
                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else {
            throw new UnsupportedCommandException(commandSource.commandName());
        }

        return commandSourceResult;
    }

    @Override
    public CommandSource handleCommandForCheckerApproval(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();

        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSourceResult.json());
        final JsonCommand command = JsonCommand.withMakerCheckerApproval(commandSourceResult.json(), parsedCommand, this.fromApiJsonHelper);
        
        if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                    "UPDATE_PERMISSION_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_PERMISSION_CHECKER", allowedPermissions);

            EntityIdentifier result = this.permissionWritePlatformService.updateMakerCheckerPermissions(command);
            
            final String jsonOfChangesOnly = toApiJsonSerializer.serialize(result.getChanges());
            commandSourceResult.updateJsonTo(jsonOfChangesOnly);
            
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else {
            throw new UnsupportedCommandException(commandSourceResult.commandName());
        }

        return commandSourceResult;
    }
}