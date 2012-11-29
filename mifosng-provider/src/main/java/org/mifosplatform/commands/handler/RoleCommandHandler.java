package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioCommandDeserializerService;
import org.mifosng.platform.api.infrastructure.PortfolioCommandSerializerService;
import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosng.platform.infrastructure.errorhandling.UnsupportedCommandException;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.service.ChangeDetectionService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.command.RoleCommand;
import org.mifosplatform.infrastructure.user.command.RolePermissionCommand;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.mifosplatform.infrastructure.user.service.RoleWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioCommandDeserializerService commandDeserializerService;
    private final RoleWritePlatformService writePlatformService;

    @Autowired
    public RoleCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioCommandDeserializerService commandDeserializerService, final RoleWritePlatformService writePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.commandDeserializerService = commandDeserializerService;
        this.writePlatformService = writePlatformService;
    }

    /*
     * Used when users with 'create' capability create a command. If
     * 'maker-checker' is not enabled for this specific command then the
     * 'creator' is also marked 'as the checker' and command automatically is
     * processed and changes state of system.
     */
    public CommandSource handle(final CommandSource commandSource, final String apiRequestBodyInJson) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        CommandSource commandSourceResult = commandSource.copy();

        final Long resourceId = commandSource.resourceId();
        Long newResourceId = null;

        if (commandSource.isCreate()) {
            try {
                final RoleCommand command = this.apiDataConversionService.convertApiRequestJsonToRoleCommand(resourceId,
                        apiRequestBodyInJson);
                final String commandSerializedAsJson = this.commandSerializerService.serializeRoleCommandToJson(command);
                commandSourceResult.updateJsonTo(commandSerializedAsJson);

                newResourceId = this.writePlatformService.createRole(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                final RoleCommand command = this.apiDataConversionService.convertApiRequestJsonToRoleCommand(resourceId,
                        apiRequestBodyInJson);
                final String commandSerializedAsJson = this.commandSerializerService.serializeRoleCommandToJson(command);
                commandSourceResult.updateJsonTo(commandSerializedAsJson);

                final String jsonOfChangesOnly = this.changeDetectionService.detectChangesOnUpdate(commandSource.resourceName(),
                        commandSource.resourceId(), commandSerializedAsJson);
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                final RoleCommand changesOnly = this.commandDeserializerService
                        .deserializeRoleCommand(resourceId, jsonOfChangesOnly, false);

                newResourceId = this.writePlatformService.updateRole(changesOnly);

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdateRolePermissions()) {
            try {
                final RolePermissionCommand command = this.apiDataConversionService.convertApiRequestJsonToRolePermissionCommand(
                        resourceId, apiRequestBodyInJson);
                final String commandSerializedAsJson = this.commandSerializerService.serializeRolePermissionCommandToJson(command);
                commandSourceResult.updateJsonTo(commandSerializedAsJson);

                final String jsonOfChangesOnly = this.changeDetectionService.detectChangesOnUpdate(commandSource.resourceName(),
                        commandSource.resourceId(), commandSerializedAsJson);
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                final RolePermissionCommand changesOnly = this.commandDeserializerService.deserializeRolePermissionCommand(resourceId,
                        jsonOfChangesOnly, false);

                newResourceId = this.writePlatformService.updateRolePermissions(changesOnly);

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isDelete()) { throw new UnsupportedCommandException(commandSource.commandName()); }

        return commandSourceResult;
    }

    /*
     * Used when users with 'checker' capability approve a command.
     */
    public CommandSource handle(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();

        Long resourceId = commandSourceResult.resourceId();
        if (commandSourceResult.isRoleResource()) {
            if (commandSourceResult.isCreate()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                        "CREATE_ROLE_CHECKER");
                context.authenticatedUser().validateHasPermissionTo("CREATE_ROLE_CHECKER", allowedPermissions);

                final RoleCommand command = this.commandDeserializerService.deserializeRoleCommand(resourceId, commandSourceResult.json(),
                        true);
                resourceId = this.writePlatformService.createRole(command);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isUpdate()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                        "UPDATE_ROLE_CHECKER");
                context.authenticatedUser().validateHasPermissionTo("UPDATE_ROLE_CHECKER", allowedPermissions);

                final RoleCommand command = this.commandDeserializerService.deserializeRoleCommand(resourceId, commandSourceResult.json(),
                        true);
                resourceId = this.writePlatformService.updateRole(command);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isUpdateRolePermissions()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                        "PERMISSIONS_ROLE_CHECKER");
                context.authenticatedUser().validateHasPermissionTo("PERMISSIONS_ROLE_CHECKER", allowedPermissions);

                final RolePermissionCommand command = this.commandDeserializerService.deserializeRolePermissionCommand(resourceId,
                        commandSourceResult.json(), true);
                resourceId = this.writePlatformService.updateRolePermissions(command);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }
        }

        commandSourceResult.updateResourceId(resourceId);

        return commandSourceResult;
    }
}