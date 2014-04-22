package org.mifosplatform.portfolio.loanaccount.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.loanaccount.service.LoanWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 2-4-14
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
@Service
public class LoanRecoveryPaymentCommandHandler implements NewCommandSourceHandler {

    private final LoanWritePlatformService writePlatformService;

    @Autowired
    public LoanRecoveryPaymentCommandHandler(LoanWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return writePlatformService.makeLoanRecoveryPayment(command.getLoanId(), command);
    }
}
