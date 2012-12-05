package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.service.ChangeDetectionService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.charge.serialization.ChargeCommandFromCommandJsonDeserializer;
import org.mifosplatform.portfolio.charge.service.ChargeWritePlatformService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChargeDefinitionCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final ChargeCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer;
    private final ChargeWritePlatformService writePlatformService;

    @Autowired
    public ChargeDefinitionCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final ChargeCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer,
            final ChargeWritePlatformService writePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.commandFromCommandJsonDeserializer = commandFromCommandJsonDeserializer;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        CommandSource commandSourceResult = commandSource.copy();

        final Long resourceId = commandSource.resourceId();
        final ChargeDefinitionCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId,
                commandSourceResult.json());

        if (commandSource.isCreate()) {
            try {
                Long newResourceId = this.writePlatformService.createCharge(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                final String jsonOfChangesOnly = this.changeDetectionService.detectChangesOnUpdate(commandSource.resourceName(),
                        commandSource.resourceId(), commandSource.json());
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                final ChargeDefinitionCommand changesOnly = this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId,
                        jsonOfChangesOnly);
                this.writePlatformService.updateCharge(changesOnly);

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isDelete()) {
            try {
                this.writePlatformService.deleteCharge(command);
                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        }

        return commandSourceResult;
    }

    @Override
    public CommandSource handleCommandForCheckerApproval(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();

        Long resourceId = commandSourceResult.resourceId();
        final ChargeDefinitionCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId,
                commandSourceResult.json(), true);

        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "CREATE_CHARGE_MAKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_CHARGE_MAKER", allowedPermissions);

            resourceId = this.writePlatformService.createCharge(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "UPDATE_CHARGE_MAKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_CHARGE_MAKER", allowedPermissions);

            this.writePlatformService.updateCharge(command);

            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "DELETE_CHARGE_MAKER");
            context.authenticatedUser().validateHasPermissionTo("DELETE_CHARGE_MAKER", allowedPermissions);

            this.writePlatformService.deleteCharge(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        }

        return commandSourceResult;
    }
}