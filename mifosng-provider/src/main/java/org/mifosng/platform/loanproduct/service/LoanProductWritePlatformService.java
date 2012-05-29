package org.mifosng.platform.loanproduct.service;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.platform.api.commands.LoanProductCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LoanProductWritePlatformService {

	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	EntityIdentifier createLoanProduct(LoanProductCommand command);
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	EntityIdentifier updateLoanProduct(LoanProductCommand command);
}