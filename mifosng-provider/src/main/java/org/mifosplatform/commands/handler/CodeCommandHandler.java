package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.infrastructure.codes.service.CodeWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class CodeCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson toApiJsonSerializer;
    private final CodeWritePlatformService writePlatformService;

    @Autowired
    public CodeCommandHandler(final PlatformSecurityContext context,
            final FromJsonHelper fromApiJsonHelper,
            final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson toApiJsonSerializer,
            final CodeWritePlatformService writePlatformService) {
        this.context = context;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final Long resourceId = commandSource.resourceId();
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSource.json());
        final JsonCommand command = JsonCommand.from(commandSource.json(), parsedCommand, this.fromApiJsonHelper);
        
        CommandSource commandSourceResult = commandSource.copy();

        if (commandSource.isCreate()) {
            try {
                Long newResourceId = this.writePlatformService.createCode(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                EntityIdentifier result = this.writePlatformService.updateCode(resourceId, command);
                
                final String jsonOfChangesOnly = toApiJsonSerializer.serialize(result.getChanges());
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isDelete()) {
            try {
                this.writePlatformService.deleteCode(resourceId, command);
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
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSourceResult.json());
        final JsonCommand command = JsonCommand.withMakerCheckerApproval(commandSourceResult.json(), parsedCommand, this.fromApiJsonHelper);

        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "CREATE_CODE_MAKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_CODE_MAKER", allowedPermissions);

            resourceId = this.writePlatformService.createCode(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "UPDATE_CODE_MAKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_CODE_MAKER", allowedPermissions);

            EntityIdentifier result = this.writePlatformService.updateCode(resourceId, command);
            
            final String jsonOfChangesOnly = toApiJsonSerializer.serialize(result.getChanges());
            commandSourceResult.updateJsonTo(jsonOfChangesOnly);
            
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "DELETE_CODE_MAKER");
            context.authenticatedUser().validateHasPermissionTo("DELETE_CODE_MAKER", allowedPermissions);

            this.writePlatformService.deleteCode(resourceId, command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        }

        return commandSourceResult;
    }
}