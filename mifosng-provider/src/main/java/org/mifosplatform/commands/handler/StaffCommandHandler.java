package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.staff.command.StaffCommand;
import org.mifosplatform.organisation.staff.serialization.StaffCommandFromCommandJsonDeserializer;
import org.mifosplatform.organisation.staff.service.StaffWritePlatformService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StaffCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final StaffCommandFromCommandJsonDeserializer fromCommandJsonDeserializer;
    private final StaffWritePlatformService writePlatformService;

    @Autowired
    public StaffCommandHandler(final PlatformSecurityContext context,
            final StaffCommandFromCommandJsonDeserializer fromCommandJsonDeserializer, final StaffWritePlatformService writePlatformService) {
        this.context = context;
        this.fromCommandJsonDeserializer = fromCommandJsonDeserializer;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final Long resourceId = commandSource.resourceId();
        final StaffCommand command = this.fromCommandJsonDeserializer.commandFromCommandJson(resourceId, commandSource.json());
        CommandSource commandSourceResult = commandSource.copy();

        Long newResourceId = null;
        if (commandSource.isCreate()) {
            try {
                newResourceId = this.writePlatformService.createStaff(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                this.writePlatformService.updateStaff(command);

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
        final StaffCommand command = this.fromCommandJsonDeserializer.commandFromCommandJson(resourceId, commandSourceResult.json(), true);

        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "CREATE_STAFF_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_STAFF_CHECKER", allowedPermissions);

            resourceId = this.writePlatformService.createStaff(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "UPDATE_STAFF_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_STAFF_CHECKER", allowedPermissions);

            resourceId = this.writePlatformService.updateStaff(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }

        return commandSourceResult;
    }
}