package org.mifosng.platform.makerchecker.service;

import org.mifosng.platform.infrastructure.errorhandling.UnsupportedCommandException;
import org.mifosng.platform.makerchecker.domain.CommandSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A class to delegate handling of commands to appropriate handler.
 */
@Service
public class CommandSourceHandlerDelegator {

    private final PlatformSecurityContext context;
    private final ClientCommandHandler clientCommandHandler;
    private final RoleCommandHandler roleCommandHandler;

    @Autowired
    public CommandSourceHandlerDelegator(final PlatformSecurityContext context, 
            final ClientCommandHandler clientCommandHandler,
            final RoleCommandHandler roleCommandHandler) {
        this.context = context;
        this.clientCommandHandler = clientCommandHandler;
        this.roleCommandHandler = roleCommandHandler;
    }

    public CommandSource handle(final CommandSource commandSource, final String apiRequestBodyInJson) {

        context.authenticatedUser();
        CommandSource commandSourceResult = null;
        if (commandSource.isClientResource()) {
            commandSourceResult = clientCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else if (commandSource.isRoleResource()) {
            commandSourceResult = roleCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else {
            throw new UnsupportedCommandException(commandSource.commandName());
        }

        return commandSourceResult;
    }

    public CommandSource handleExistingCommand(final CommandSource commandSource) {
        
        CommandSource commandSourceResult = null;
        if (commandSource.isClientResource()) {
            commandSourceResult = clientCommandHandler.handle(commandSource);
        } else if (commandSource.isRoleResource()) {
            commandSourceResult = roleCommandHandler.handle(commandSource);
        } else {
            throw new UnsupportedCommandException(commandSource.commandName());
        }

        return commandSourceResult;
    }
}