package org.mifosplatform.commands.handler;

import org.mifosng.platform.infrastructure.errorhandling.UnsupportedCommandException;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
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
    private final PermissionsCommandHandler permissionCommandHandler;
    private final UserCommandHandler userCommandHandler;
    private final CodeCommandHandler codeCommandHandler;
    private final StaffCommandHandler staffCommandHandler;
    private final FundCommandHandler fundCommandHandler;
    private final OfficeCommandHandler officeCommandHandler;
    private final OfficeTransactionCommandHandler officeTransactionCommandHandler;
    private final CurrencyCommandHandler currencyCommandHandler;

    @Autowired
    public CommandSourceHandlerDelegator(final PlatformSecurityContext context, 
            final ClientCommandHandler clientCommandHandler,
            final RoleCommandHandler roleCommandHandler,
            final PermissionsCommandHandler permissionCommandHandler,
            final UserCommandHandler userCommandHandler,
            final CodeCommandHandler codeCommandHandler,
            final StaffCommandHandler staffCommandHandler,
            final FundCommandHandler fundCommandHandler,
            final OfficeCommandHandler officeCommandHandler,
            final OfficeTransactionCommandHandler officeTransactionCommandHandler,
            final CurrencyCommandHandler currencyCommandHandler) {
        this.context = context;
        this.clientCommandHandler = clientCommandHandler;
        this.roleCommandHandler = roleCommandHandler;
        this.permissionCommandHandler = permissionCommandHandler;
        this.userCommandHandler = userCommandHandler;
        this.codeCommandHandler = codeCommandHandler;
        this.staffCommandHandler = staffCommandHandler;
        this.fundCommandHandler = fundCommandHandler;
        this.officeCommandHandler = officeCommandHandler;
        this.officeTransactionCommandHandler = officeTransactionCommandHandler;
        this.currencyCommandHandler = currencyCommandHandler;
    }

    public CommandSource handle(final CommandSource commandSource, final String apiRequestBodyInJson) {

        context.authenticatedUser();
        CommandSource commandSourceResult = null;
        if (commandSource.isClientResource()) {
            commandSourceResult = clientCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else if (commandSource.isRoleResource()) {
            commandSourceResult = roleCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else if (commandSource.isPermissionResource()) {
            commandSourceResult = permissionCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else if (commandSource.isUserResource()) {
            commandSourceResult = userCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else if (commandSource.isCodeResource()) {
            commandSourceResult = codeCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else if (commandSource.isStaffResource()) {
            commandSourceResult = staffCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else if (commandSource.isFundResource()) {
            commandSourceResult = fundCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else if (commandSource.isOfficeResource()) {
            commandSourceResult = officeCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else if (commandSource.isOfficeTransactionResource()) {
            commandSourceResult = officeTransactionCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else if (commandSource.isCurrencyResource()) {
            commandSourceResult = currencyCommandHandler.handle(commandSource, apiRequestBodyInJson);
        } else {
            throw new UnsupportedCommandException(commandSource.commandName());
        }

        return commandSourceResult;
    }
    
    public CommandSource handleExistingCommand(final CommandSource existingCommandSource) {
        
        CommandSource commandSourceResult = null;
        if (existingCommandSource.isClientResource()) {
            commandSourceResult = clientCommandHandler.handle(existingCommandSource);
        } else if (existingCommandSource.isRoleResource()) {
            commandSourceResult = roleCommandHandler.handle(existingCommandSource);
        } else if (existingCommandSource.isPermissionResource()) {
            commandSourceResult = permissionCommandHandler.handle(existingCommandSource);
        } else if (existingCommandSource.isUserResource()) {
            commandSourceResult = userCommandHandler.handle(existingCommandSource);
        } else if (existingCommandSource.isCodeResource()) {
            commandSourceResult = codeCommandHandler.handle(existingCommandSource);
        } else if (existingCommandSource.isStaffResource()) {
            commandSourceResult = staffCommandHandler.handle(existingCommandSource);
        } else if (existingCommandSource.isFundResource()) {
            commandSourceResult = fundCommandHandler.handle(existingCommandSource);
        } else if (existingCommandSource.isOfficeResource()) {
            commandSourceResult = officeCommandHandler.handle(existingCommandSource);
        } else if (existingCommandSource.isOfficeTransactionResource()) {
            commandSourceResult = officeTransactionCommandHandler.handle(existingCommandSource);
        } else if (existingCommandSource.isCurrencyResource()) {
            commandSourceResult = currencyCommandHandler.handle(existingCommandSource);
        } else {
            throw new UnsupportedCommandException(existingCommandSource.commandName());
        }

        return commandSourceResult;
    }
}