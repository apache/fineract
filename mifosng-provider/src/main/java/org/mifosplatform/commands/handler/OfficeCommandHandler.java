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
import org.mifosplatform.infrastructure.office.command.OfficeCommand;
import org.mifosplatform.infrastructure.office.service.OfficeWritePlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OfficeCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioCommandDeserializerService commandDeserializerService;
    private final OfficeWritePlatformService writePlatformService;

    @Autowired
    public OfficeCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioCommandDeserializerService commandDeserializerService, 
            final OfficeWritePlatformService writePlatformService) {
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

        final OfficeCommand command = this.apiDataConversionService.convertApiRequestJsonToOfficeCommand(resourceId, apiRequestBodyInJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeOfficeCommandToJson(command);
        commandSourceResult.updateJsonTo(commandSerializedAsJson);

        Long newResourceId = null;

        if (commandSource.isCreate()) {
            try {
                newResourceId = this.writePlatformService.createOffice(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                final String jsonOfChangesOnly = this.changeDetectionService.detectChangesOnUpdate(commandSource.resourceName(),
                        commandSource.resourceId(), commandSerializedAsJson);
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                final OfficeCommand changesOnly = this.commandDeserializerService.deserializeOfficeCommand(resourceId, jsonOfChangesOnly, false);

                this.writePlatformService.updateOffice(changesOnly);

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
        final OfficeCommand command = this.commandDeserializerService.deserializeOfficeCommand(resourceId, commandSourceResult.json(), true);
        
        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "CREATE_OFFICE_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_OFFICE_CHECKER", allowedPermissions);

            resourceId = this.writePlatformService.createOffice(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "UPDATE_OFFICE_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_OFFICE_CHECKER", allowedPermissions);

            resourceId = this.writePlatformService.updateOffice(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }

        return commandSourceResult;
    }
}