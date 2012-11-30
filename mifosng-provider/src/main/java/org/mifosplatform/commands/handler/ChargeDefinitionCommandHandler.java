package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.infrastructure.PortfolioCommandDeserializerService;
import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.service.ChangeDetectionService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.charge.service.ChargeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChargeDefinitionCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final PortfolioCommandDeserializerService commandDeserializerService;
    private final ChargeWritePlatformService writePlatformService;
    
    @Autowired
    public ChargeDefinitionCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final PortfolioCommandDeserializerService commandDeserializerService,
            final ChargeWritePlatformService writePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.commandDeserializerService = commandDeserializerService;
        this.writePlatformService = writePlatformService;
    }

    /*
     * Used when users with 'create' capability create a command. If 'maker-checker' is not
     * enabled for this specific command then the 'creator' is also marked 'as the checker' and command
     * automatically is processed and changes state of system.
     */
    public CommandSource handle(final CommandSource commandSource, final String commandSerializedAsJson) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        CommandSource commandSourceResult = commandSource.copy();

        final Long resourceId = commandSource.resourceId();
        final ChargeDefinitionCommand command = this.commandDeserializerService.deserializeChargeDefinitionCommand(resourceId, commandSourceResult.json(), true);
        
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
                final String jsonOfChangesOnly = this.changeDetectionService.detectChangesOnUpdate(commandSource.resourceName(), commandSource.resourceId(), commandSource.json());
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                final ChargeDefinitionCommand changesOnly = this.commandDeserializerService.deserializeChargeDefinitionCommand(resourceId, jsonOfChangesOnly, false);

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

    /*
     * Used when users with 'checker' capability approve a command.
     */
    public CommandSource handle(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();
        
        Long resourceId = commandSourceResult.resourceId();
        final ChargeDefinitionCommand command = this.commandDeserializerService.deserializeChargeDefinitionCommand(resourceId, commandSourceResult.json(), true);
        
        if (commandSourceResult.isUserResource()) {
            if (commandSourceResult.isCreate()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "CREATE_CHARGE_MAKER");
                context.authenticatedUser().validateHasPermissionTo("CREATE_CHARGE_MAKER", allowedPermissions);
                
                resourceId = this.writePlatformService.createCharge(command);
                commandSourceResult.updateResourceId(resourceId);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isUpdate()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "UPDATE_CHARGE_MAKER");
                context.authenticatedUser().validateHasPermissionTo("UPDATE_CHARGE_MAKER", allowedPermissions);
                
                this.writePlatformService.updateCharge(command);
                
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isDelete()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "DELETE_CHARGE_MAKER");
                context.authenticatedUser().validateHasPermissionTo("DELETE_CHARGE_MAKER", allowedPermissions);
                
                this.writePlatformService.deleteCharge(command);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            }
        }

        return commandSourceResult;
    }
}