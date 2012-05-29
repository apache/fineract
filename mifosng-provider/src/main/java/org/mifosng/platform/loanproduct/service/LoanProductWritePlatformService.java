package org.mifosng.platform.loanproduct.service;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.platform.api.commands.LoanProductCommand;

public interface LoanProductWritePlatformService {

	EntityIdentifier createLoanProduct(LoanProductCommand command);
	
	EntityIdentifier updateLoanProduct(LoanProductCommand command);
}