package org.mifosplatform.portfolio.loanproduct.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface LoanProductWritePlatformService {

    CommandProcessingResult createLoanProduct(JsonCommand command);

    CommandProcessingResult updateLoanProduct(Long loanProductId, JsonCommand command);
}