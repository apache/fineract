package org.mifosplatform.portfolio.loanproduct.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface LoanProductWritePlatformService {

    EntityIdentifier createLoanProduct(JsonCommand command);

    EntityIdentifier updateLoanProduct(Long loanProductId, JsonCommand command);
}