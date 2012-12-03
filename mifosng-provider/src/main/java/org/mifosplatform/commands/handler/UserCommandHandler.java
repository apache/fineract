package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.service.ChangeDetectionService;
import org.mifosplatform.infrastructure.core.api.PortfolioCommandDeserializerService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.command.UserCommand;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.service.AppUserWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final PortfolioCommandDeserializerService commandDeserializerService;
    private final AppUserWritePlatformService writePlatformService;

    @Autowired
    public UserCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final PortfolioCommandDeserializerService commandDeserializerService, final AppUserWritePlatformService writePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.commandDeserializerService = commandDeserializerService;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final Long resourceId = commandSource.resourceId();
        final UserCommand command = this.commandDeserializerService.deserializeUserCommand(resourceId, commandSource.json(), false);

        CommandSource commandSourceResult = commandSource.copy();
        if (commandSource.isCreate()) {
            try {
                Long newResourceId = this.writePlatformService.createUser(command);

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

                final UserCommand changesOnly = this.commandDeserializerService
                        .deserializeUserCommand(resourceId, jsonOfChangesOnly, false);

                if (maker.hasIdOf(command.getId())) {
                    this.writePlatformService.updateUsersOwnAccountDetails(changesOnly);
                } else {
                    this.writePlatformService.updateUser(changesOnly);
                }

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isDelete()) {
            try {
                this.writePlatformService.deleteUser(command);
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
        final UserCommand command = this.commandDeserializerService.deserializeUserCommand(resourceId, commandSourceResult.json(), true);
        if (commandSourceResult.isUserResource()) {
            if (commandSourceResult.isCreate()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                        "CREATE_USER_MAKER");
                context.authenticatedUser().validateHasPermissionTo("CREATE_USER_MAKER", allowedPermissions);

                resourceId = this.writePlatformService.createUser(command);
                commandSourceResult.updateResourceId(resourceId);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isUpdate()) {
                if (checker.hasIdOf(command.getId())) {
                    this.writePlatformService.updateUsersOwnAccountDetails(command);
                } else {
                    final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER","UPDATE_USER_MAKER");
                    context.authenticatedUser().validateHasPermissionTo("UPDATE_USER_MAKER", allowedPermissions);
                    
                    this.writePlatformService.updateUser(command);
                }

                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isDelete()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER",
                        "DELETE_USER_MAKER");
                context.authenticatedUser().validateHasPermissionTo("DELETE_USER_MAKER", allowedPermissions);

                this.writePlatformService.deleteUser(command);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            }
        }

        return commandSourceResult;
    }
}