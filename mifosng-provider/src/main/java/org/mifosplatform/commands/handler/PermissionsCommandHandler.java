package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.serialization.PermissionsCommandFromCommandJsonDeserializer;
import org.mifosplatform.useradministration.service.PermissionWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionsCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final PermissionsCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer;
    private final PermissionWritePlatformService permissionWritePlatformService;

    @Autowired
    public PermissionsCommandHandler(final PlatformSecurityContext context,
            final PermissionsCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer,
            final PermissionWritePlatformService permissionWritePlatformService) {
        this.context = context;
        this.commandFromCommandJsonDeserializer = commandFromCommandJsonDeserializer;
        this.permissionWritePlatformService = permissionWritePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        CommandSource commandSourceResult = commandSource.copy();

        if (commandSource.isUpdate()) {
            try {
                final PermissionsCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(
                        commandSource.resourceId(), commandSource.json());

                this.permissionWritePlatformService.updateMakerCheckerPermissions(command);

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

        if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                    "UPDATE_PERMISSION_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_PERMISSION_CHECKER", allowedPermissions);

            final PermissionsCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(
                    commandSourceResult.resourceId(), commandSourceResult.json(), true);

            this.permissionWritePlatformService.updateMakerCheckerPermissions(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else {
            throw new UnsupportedCommandException(commandSourceResult.commandName());
        }

        return commandSourceResult;
    }
}