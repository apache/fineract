package org.mifosplatform.commands.handler;

import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
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
    private final ChargeDefinitionCommandHandler chargeDefinitionCommandHandler;
    private final LoanProductCommandHandler loanProductCommandHandler;
    private final ClientIdentifierCommandHandler clientIdentifierCommandHandler;

    @Autowired
    public CommandSourceHandlerDelegator(final PlatformSecurityContext context, 
            final ClientCommandHandler clientCommandHandler,
            final ClientIdentifierCommandHandler clientIdentifierCommandHandler,
            final RoleCommandHandler roleCommandHandler,
            final PermissionsCommandHandler permissionCommandHandler,
            final UserCommandHandler userCommandHandler,
            final CodeCommandHandler codeCommandHandler,
            final StaffCommandHandler staffCommandHandler,
            final FundCommandHandler fundCommandHandler,
            final OfficeCommandHandler officeCommandHandler,
            final OfficeTransactionCommandHandler officeTransactionCommandHandler,
            final CurrencyCommandHandler currencyCommandHandler,
            final ChargeDefinitionCommandHandler chargeDefinitionCommandHandler,
            final LoanProductCommandHandler loanProductCommandHandler) {
        this.context = context;
        this.clientCommandHandler = clientCommandHandler;
        this.clientIdentifierCommandHandler = clientIdentifierCommandHandler;
        this.roleCommandHandler = roleCommandHandler;
        this.permissionCommandHandler = permissionCommandHandler;
        this.userCommandHandler = userCommandHandler;
        this.codeCommandHandler = codeCommandHandler;
        this.staffCommandHandler = staffCommandHandler;
        this.fundCommandHandler = fundCommandHandler;
        this.officeCommandHandler = officeCommandHandler;
        this.officeTransactionCommandHandler = officeTransactionCommandHandler;
        this.currencyCommandHandler = currencyCommandHandler;
        this.chargeDefinitionCommandHandler = chargeDefinitionCommandHandler;
        this.loanProductCommandHandler = loanProductCommandHandler;
    }

    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        context.authenticatedUser();
        CommandSource commandSourceResult = null;
        if (commandSource.isClientResource()) {
            commandSourceResult = clientCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isClientIdentifierResource()) {
            commandSourceResult = clientIdentifierCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isRoleResource()) {
            commandSourceResult = roleCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isPermissionResource()) {
            commandSourceResult = permissionCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isUserResource()) {
            commandSourceResult = userCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isCodeResource()) {
            commandSourceResult = codeCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isStaffResource()) {
            commandSourceResult = staffCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isFundResource()) {
            commandSourceResult = fundCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isOfficeResource()) {
            commandSourceResult = officeCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isOfficeTransactionResource()) {
            commandSourceResult = officeTransactionCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isCurrencyResource()) {
            commandSourceResult = currencyCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isChargeDefinitionResource()) {
            commandSourceResult = chargeDefinitionCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else if (commandSource.isLoanProductResource()) {
                commandSourceResult = loanProductCommandHandler.handleCommandWithSupportForRollback(commandSource);
        } else {
            throw new UnsupportedCommandException(commandSource.commandName());
        }

        return commandSourceResult;
    }
    
    public CommandSource handleCommandForCheckerApproval(final CommandSource existingCommandSource) {
        
        CommandSource commandSourceResult = null;
        if (existingCommandSource.isClientResource()) {
            commandSourceResult = clientCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isClientIdentifierResource()) {
            commandSourceResult = clientIdentifierCommandHandler.handleCommandWithSupportForRollback(existingCommandSource);
        } else if (existingCommandSource.isRoleResource()) {
            commandSourceResult = roleCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isPermissionResource()) {
            commandSourceResult = permissionCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isUserResource()) {
            commandSourceResult = userCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isCodeResource()) {
            commandSourceResult = codeCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isStaffResource()) {
            commandSourceResult = staffCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isFundResource()) {
            commandSourceResult = fundCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isOfficeResource()) {
            commandSourceResult = officeCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isOfficeTransactionResource()) {
            commandSourceResult = officeTransactionCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isCurrencyResource()) {
            commandSourceResult = currencyCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isChargeDefinitionResource()) {
            commandSourceResult = chargeDefinitionCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else if (existingCommandSource.isLoanProductResource()) {
            commandSourceResult = loanProductCommandHandler.handleCommandForCheckerApproval(existingCommandSource);
        } else {
            throw new UnsupportedCommandException(existingCommandSource.commandName());
        }

        return commandSourceResult;
    }
}