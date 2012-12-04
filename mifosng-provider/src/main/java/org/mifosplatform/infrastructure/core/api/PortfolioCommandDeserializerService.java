package org.mifosplatform.infrastructure.core.api;

import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;

/**
 * Service for de-serializing JSON for a command into the platforms internal
 * Java object representation of the command.
 * 
 * <p>
 * Known implementations:
 * </p>
 * 
 * @see PortfolioCommandDeerializerServiceGoogleGson
 */
public interface PortfolioCommandDeserializerService {

    ChargeDefinitionCommand deserializeChargeDefinitionCommand(Long chargeDefinitionId, String commandAsJson, boolean makerCheckerApproval);

    ClientCommand deserializeClientCommand(Long clientId, String commandAsJson, boolean makerCheckerApproval);

    LoanProductCommand deserializeLoanProductCommand(Long loanProductId, String commandAsJson, boolean makerCheckerApproval);

    ClientIdentifierCommand deserializeClientIdentifierCommand(Long clientIdentifierId, Long clientId, String commandAsJson,
            boolean makerCheckerApproval);
}