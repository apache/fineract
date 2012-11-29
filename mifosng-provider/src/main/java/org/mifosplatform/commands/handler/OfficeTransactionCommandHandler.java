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
import org.mifosplatform.infrastructure.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.infrastructure.office.service.OfficeWritePlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OfficeTransactionCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioCommandDeserializerService commandDeserializerService;
    private final OfficeWritePlatformService writePlatformService;

    @Autowired
    public OfficeTransactionCommandHandler(final PlatformSecurityContext context,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioCommandDeserializerService commandDeserializerService, final OfficeWritePlatformService writePlatformService) {
        this.context = context;
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

        final BranchMoneyTransferCommand command = this.apiDataConversionService
                .convertApiRequestJsonToBranchMoneyTransferCommand(apiRequestBodyInJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeOfficeTransactionCommandToJson(command);
        commandSourceResult.updateJsonTo(commandSerializedAsJson);

        Long newResourceId = null;

        if (commandSource.isCreate()) {
            try {
                newResourceId = this.writePlatformService.externalBranchMoneyTransfer(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            throw new UnsupportedCommandException(commandSource.commandName());
        } else if (commandSource.isDelete()) { throw new UnsupportedCommandException(commandSource.commandName()); }

        return commandSourceResult;
    }

    /*
     * Used when users with 'checker' capability approve a command.
     */
    public CommandSource handle(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();

        Long resourceId = commandSourceResult.resourceId();
        final BranchMoneyTransferCommand command = this.commandDeserializerService.deserializeOfficeTransactionCommand(
                commandSourceResult.json(), true);

        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "CREATE_OFFICETRANSACTION_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_OFFICETRANSACTION_CHECKER", allowedPermissions);

            resourceId = this.writePlatformService.externalBranchMoneyTransfer(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            throw new UnsupportedCommandException(commandSourceResult.commandName());
        } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }

        return commandSourceResult;
    }
}