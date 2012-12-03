package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.commands.service.ChangeDetectionService;
import org.mifosplatform.infrastructure.core.api.PortfolioCommandDeserializerService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.command.CurrencyCommand;
import org.mifosplatform.organisation.monetary.service.CurrencyWritePlatformService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final PortfolioCommandDeserializerService commandDeserializerService;
    private final CurrencyWritePlatformService writePlatformService;

    @Autowired
    public CurrencyCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final PortfolioCommandDeserializerService commandDeserializerService, final CurrencyWritePlatformService writePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.commandDeserializerService = commandDeserializerService;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        CommandSource commandSourceResult = commandSource.copy();

        if (commandSource.isCreate()) {
            throw new UnsupportedCommandException(commandSource.commandName());
        } else if (commandSource.isUpdate()) {
            try {
                final String jsonOfChangesOnly = this.changeDetectionService.detectChangesOnUpdate(commandSource.resourceName(),
                        commandSource.resourceId(), commandSource.json());
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                final CurrencyCommand changesOnly = this.commandDeserializerService.deserializeCurrencyCommand(jsonOfChangesOnly, false);

                this.writePlatformService.updateAllowedCurrencies(changesOnly);
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

        final CurrencyCommand command = this.commandDeserializerService.deserializeCurrencyCommand(commandSourceResult.json(), true);

        if (commandSourceResult.isCreate()) {
            throw new UnsupportedCommandException(commandSourceResult.commandName());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "UPDATE_CURRENCY_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_CURRENCY_CHECKER", allowedPermissions);

            this.writePlatformService.updateAllowedCurrencies(command);

            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }

        return commandSourceResult;
    }
}