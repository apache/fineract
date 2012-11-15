package org.mifosng.platform.loanproduct.service;

import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LoanProductWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_LOANPRODUCT')")
	EntityIdentifier createLoanProduct(LoanProductCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_LOANPRODUCT')")
	EntityIdentifier updateLoanProduct(LoanProductCommand command);
}