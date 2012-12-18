package org.mifosplatform.portfolio.loanaccount.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.portfolio.loanaccount.service.LoanWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UndoApprovalLoanApplicationCommandHandler implements NewCommandSourceHandler {

    private final LoanWritePlatformService writePlatformService;

    @Autowired
    public UndoApprovalLoanApplicationCommandHandler(final LoanWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public EntityIdentifier processCommand(final JsonCommand command) {

        return this.writePlatformService.undoLoanApplicationApproval(command.resourceId(), command);
    }
}