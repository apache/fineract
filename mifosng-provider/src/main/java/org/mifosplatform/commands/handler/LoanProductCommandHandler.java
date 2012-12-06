package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;
import org.mifosplatform.portfolio.loanproduct.serialization.LoanProductCommandFromCommandJsonDeserializer;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductWritePlatformService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanProductCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final LoanProductCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer;
    private final LoanProductWritePlatformService writePlatformService;

    @Autowired
    public LoanProductCommandHandler(final PlatformSecurityContext context,
            final LoanProductCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer,
            final LoanProductWritePlatformService writePlatformService) {
        this.context = context;
        this.commandFromCommandJsonDeserializer = commandFromCommandJsonDeserializer;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final Long resourceId = commandSource.resourceId();
        final LoanProductCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId, commandSource.json());
        CommandSource commandSourceResult = commandSource.copy();

        if (commandSource.isCreate()) {
            try {
                EntityIdentifier result = this.writePlatformService.createLoanProduct(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(result.getEntityId());
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                this.writePlatformService.updateLoanProduct(command);

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
        final LoanProductCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId,
                commandSourceResult.json(), true);

        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "CREATE_LOANPRODUCT_MAKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_LOANPRODUCT_MAKER", allowedPermissions);

            EntityIdentifier result = this.writePlatformService.createLoanProduct(command);
            commandSourceResult.updateResourceId(result.getEntityId());
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "UPDATE_LOANPRODUCT_MAKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_LOANPRODUCT_MAKER", allowedPermissions);

            this.writePlatformService.updateLoanProduct(command);

            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }

        return commandSourceResult;
    }
}