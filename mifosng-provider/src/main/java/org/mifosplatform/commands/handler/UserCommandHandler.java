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
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.service.AppUserWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class UserCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson toApiJsonSerializer;
    private final AppUserWritePlatformService writePlatformService;

    @Autowired
    public UserCommandHandler(final PlatformSecurityContext context, final FromJsonHelper fromApiJsonHelper,
            final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson toApiJsonSerializer,
            final AppUserWritePlatformService writePlatformService) {
        this.context = context;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final Long resourceId = commandSource.resourceId();
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSource.json());
        final JsonCommand command = JsonCommand.from(commandSource.json(), parsedCommand, this.fromApiJsonHelper);

        CommandSource commandSourceResult = commandSource.copy();
        if (commandSource.isCreate()) {
            try {
                final Long newResourceId = this.writePlatformService.createUser(command);

                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                EntityIdentifier result = null;
                if (maker.hasIdOf(resourceId)) {
                    result = this.writePlatformService.updateUsersOwnAccountDetails(resourceId, command);
                } else {
                    result = this.writePlatformService.updateUser(resourceId, command);
                }

                final String jsonOfChangesOnly = toApiJsonSerializer.serialize(result.getChanges());
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);
                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isDelete()) {
            try {
                this.writePlatformService.deleteUser(resourceId, command);
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

        final Long resourceId = commandSourceResult.resourceId();
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSourceResult.json());
        final JsonCommand command = JsonCommand.withMakerCheckerApproval(commandSourceResult.json(), parsedCommand, this.fromApiJsonHelper);

        if (commandSourceResult.isUserResource()) {
            if (commandSourceResult.isCreate()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                        "CREATE_USER_MAKER");
                context.authenticatedUser().validateHasPermissionTo("CREATE_USER_MAKER", allowedPermissions);

                final Long newResourceId = this.writePlatformService.createUser(command);
                commandSourceResult.updateResourceId(newResourceId);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isUpdate()) {
                EntityIdentifier result = null;
                if (checker.hasIdOf(resourceId)) {
                    result = this.writePlatformService.updateUsersOwnAccountDetails(resourceId, command);
                } else {
                    final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                            "UPDATE_USER_MAKER");
                    context.authenticatedUser().validateHasPermissionTo("UPDATE_USER_MAKER", allowedPermissions);

                    result = this.writePlatformService.updateUser(resourceId, command);
                }
                
                final String jsonOfChangesOnly = toApiJsonSerializer.serialize(result.getChanges());
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isDelete()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                        "DELETE_USER_MAKER");
                context.authenticatedUser().validateHasPermissionTo("DELETE_USER_MAKER", allowedPermissions);

                this.writePlatformService.deleteUser(resourceId, command);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            }
        }

        return commandSourceResult;
    }
}