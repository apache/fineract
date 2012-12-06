package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.command.OfficeCommand;
import org.mifosplatform.organisation.office.serialization.OfficeCommandFromCommandJsonDeserializer;
import org.mifosplatform.organisation.office.service.OfficeWritePlatformService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OfficeCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final OfficeCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer;
    private final OfficeWritePlatformService writePlatformService;

    @Autowired
    public OfficeCommandHandler(final PlatformSecurityContext context,
            final OfficeCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer,
            final OfficeWritePlatformService writePlatformService) {
        this.context = context;
        this.commandFromCommandJsonDeserializer = commandFromCommandJsonDeserializer;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final Long resourceId = commandSource.resourceId();
        final OfficeCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId, commandSource.json());

        CommandSource commandSourceResult = commandSource.copy();

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
                this.writePlatformService.updateOffice(command);

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
        final OfficeCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId,
                commandSourceResult.json(), true);
        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "CREATE_OFFICE_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_OFFICE_CHECKER", allowedPermissions);

            resourceId = this.writePlatformService.createOffice(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "UPDATE_OFFICE_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_OFFICE_CHECKER", allowedPermissions);

            resourceId = this.writePlatformService.updateOffice(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }

        return commandSourceResult;
    }
}