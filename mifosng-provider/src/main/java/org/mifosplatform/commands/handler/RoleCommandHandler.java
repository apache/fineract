package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.command.RoleCommand;
import org.mifosplatform.useradministration.command.RolePermissionCommand;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.serialization.RoleCommandFromCommandJsonDeserializer;
import org.mifosplatform.useradministration.serialization.RolePermissionsCommandFromCommandJsonDeserializer;
import org.mifosplatform.useradministration.service.RoleWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final RoleWritePlatformService writePlatformService;
    private final RoleCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer;
    private final RolePermissionsCommandFromCommandJsonDeserializer permissionsCommandFromCommandJsonDeserializer;

    @Autowired
    public RoleCommandHandler(final PlatformSecurityContext context, 
            final RoleCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer,
            final RolePermissionsCommandFromCommandJsonDeserializer permissionsCommandFromCommandJsonDeserializer,
            final RoleWritePlatformService writePlatformService) {
        this.context = context;
        this.commandFromCommandJsonDeserializer = commandFromCommandJsonDeserializer;
        this.permissionsCommandFromCommandJsonDeserializer = permissionsCommandFromCommandJsonDeserializer;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        CommandSource commandSourceResult = commandSource.copy();

        final Long resourceId = commandSource.resourceId();
        Long newResourceId = null;

        if (commandSource.isCreate()) {
            try {
                final RoleCommand command = this.commandFromCommandJsonDeserializer
                        .commandFromCommandJson(resourceId, commandSource.json());

                newResourceId = this.writePlatformService.createRole(command);

                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                final RoleCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId,
                        commandSource.json());

                newResourceId = this.writePlatformService.updateRole(command);

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdateRolePermissions()) {
            try {
                final RolePermissionCommand command = this.permissionsCommandFromCommandJsonDeserializer.commandFromCommandJson(
                        resourceId, commandSource.json());

                newResourceId = this.writePlatformService.updateRolePermissions(command);

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isDelete()) { throw new UnsupportedCommandException(commandSource.commandName()); }

        return commandSourceResult;
    }

    @Override
    public CommandSource handleCommandForCheckerApproval(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();

        Long resourceId = commandSourceResult.resourceId();
        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER", "CREATE_ROLE_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_ROLE_CHECKER", allowedPermissions);

            final RoleCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId,
                    commandSourceResult.json(), true);

            resourceId = this.writePlatformService.createRole(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());

        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER", "UPDATE_ROLE_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_ROLE_CHECKER", allowedPermissions);

            final RoleCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId,
                    commandSourceResult.json(), true);

            resourceId = this.writePlatformService.updateRole(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdateRolePermissions()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                    "PERMISSIONS_ROLE_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("PERMISSIONS_ROLE_CHECKER", allowedPermissions);

            final RolePermissionCommand command = this.permissionsCommandFromCommandJsonDeserializer.commandFromCommandJson(resourceId,
                    commandSourceResult.json());

            resourceId = this.writePlatformService.updateRolePermissions(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }

        return commandSourceResult;
    }
}