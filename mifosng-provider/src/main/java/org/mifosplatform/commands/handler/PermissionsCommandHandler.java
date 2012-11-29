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
import org.mifosplatform.infrastructure.user.command.PermissionsCommand;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.mifosplatform.infrastructure.user.service.PermissionWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionsCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioCommandDeserializerService commandDeserializerService;
    private final PermissionWritePlatformService permissionWritePlatformService;

    @Autowired
    public PermissionsCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioCommandDeserializerService commandDeserializerService,
            final PermissionWritePlatformService permissionWritePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.commandDeserializerService = commandDeserializerService;
        this.permissionWritePlatformService = permissionWritePlatformService;
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

        if (commandSource.isUpdate()) {
            try {
                final PermissionsCommand command = this.apiDataConversionService.convertApiRequestJsonToPermissionsCommand(apiRequestBodyInJson);
                final String commandSerializedAsJson = this.commandSerializerService.serializePermissionsCommandToJson(command);
                commandSourceResult.updateJsonTo(commandSerializedAsJson);

                final String jsonOfChangesOnly = this.changeDetectionService.detectChangesOnUpdate(commandSource.resourceName(), commandSource.resourceId(), commandSerializedAsJson);
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                final PermissionsCommand changesOnly = this.commandDeserializerService.deserializePermissionsCommand(jsonOfChangesOnly, false);

                this.permissionWritePlatformService.updateMakerCheckerPermissions(changesOnly);

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else {
            throw new UnsupportedCommandException(commandSource.commandName());
        }

        return commandSourceResult;
    }

    /*
     * Used when users with 'checker' capability approve a command.
     */
    public CommandSource handle(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();

        if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER", "PERMISSIONS_ROLE");
            context.authenticatedUser().validateHasPermissionTo("PERMISSIONS_ROLE", allowedPermissions);
            
            final PermissionsCommand command = this.commandDeserializerService.deserializePermissionsCommand(commandSourceResult.json(), true);
            this.permissionWritePlatformService.updateMakerCheckerPermissions(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else { 
            throw new UnsupportedCommandException(commandSourceResult.commandName());
        }

        return commandSourceResult;
    }
}