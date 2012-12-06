package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.portfolio.fund.command.FundCommand;
import org.mifosplatform.portfolio.fund.serialization.FundCommandFromCommandJsonDeserializer;
import org.mifosplatform.portfolio.fund.service.FundWritePlatformService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FundCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final FundCommandFromCommandJsonDeserializer fromCommandJsonDeserializer;
    private final FundWritePlatformService writePlatformService;

    @Autowired
    public FundCommandHandler(final PlatformSecurityContext context,
            final FundCommandFromCommandJsonDeserializer fromCommandJsonDeserializer, final FundWritePlatformService writePlatformService) {
        this.context = context;
        this.fromCommandJsonDeserializer = fromCommandJsonDeserializer;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final Long resourceId = commandSource.resourceId();
        final FundCommand command = this.fromCommandJsonDeserializer.commandFromCommandJson(resourceId, commandSource.json());

        CommandSource commandSourceResult = commandSource.copy();

        Long newResourceId = null;

        if (commandSource.isCreate()) {
            try {
                newResourceId = this.writePlatformService.createFund(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                this.writePlatformService.updateFund(command);

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
        final FundCommand command = this.fromCommandJsonDeserializer.commandFromCommandJson(resourceId, commandSourceResult.json(), true);

        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "CREATE_FUND_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_FUND_CHECKER", allowedPermissions);

            resourceId = this.writePlatformService.createFund(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "UPDATE_FUND_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_FUND_CHECKER", allowedPermissions);

            resourceId = this.writePlatformService.updateFund(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }

        return commandSourceResult;
    }
}