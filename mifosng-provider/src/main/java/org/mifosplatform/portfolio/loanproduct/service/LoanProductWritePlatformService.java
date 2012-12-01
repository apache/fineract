package org.mifosplatform.portfolio.loanproduct.service;

import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;

public interface LoanProductWritePlatformService {

    EntityIdentifier createLoanProduct(LoanProductCommand command);

    EntityIdentifier updateLoanProduct(LoanProductCommand command);
}