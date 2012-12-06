package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.command.ClientNoteCommand;
import org.mifosplatform.portfolio.client.serialization.ClientNoteCommandFromCommandJsonDeserializer;
import org.mifosplatform.portfolio.client.service.ClientWritePlatformService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientNoteCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ClientWritePlatformService clientWritePlatformService;
    private final ClientNoteCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer;

    @Autowired
    public ClientNoteCommandHandler(final PlatformSecurityContext context,
            final ClientNoteCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer,
            final ClientWritePlatformService clientWritePlatformService) {
        this.context = context;
        this.commandFromCommandJsonDeserializer = commandFromCommandJsonDeserializer;
        this.clientWritePlatformService = clientWritePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final ClientNoteCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(commandSource.resourceId(),
                commandSource.json());
        CommandSource commandSourceResult = commandSource.copy();

        if (commandSource.isCreate()) {
            try {
                EntityIdentifier result = this.clientWritePlatformService.addClientNote(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(result.getEntityId());
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                this.clientWritePlatformService.updateNote(command);
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

        final ClientNoteCommand command = this.commandFromCommandJsonDeserializer.commandFromCommandJson(commandSourceResult.resourceId(),
                commandSourceResult.json(), true);
        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "CREATE_CLIENTNOTE_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_CLIENTNOTE_CHECKER", allowedPermissions);

            EntityIdentifier result = this.clientWritePlatformService.addClientNote(command);
            commandSourceResult.updateResourceId(result.getEntityId());
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "UPDATE_CLIENTNOTE_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_CLIENTNOTE_CHECKER", allowedPermissions);

            this.clientWritePlatformService.updateNote(command);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }

        return commandSourceResult;
    }
}