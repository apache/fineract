package org.mifosplatform.portfolio.savingsaccountproduct.service;

import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.portfolio.savingsaccountproduct.command.SavingProductCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SavingProductWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_SAVINGSPRODUCT')")
	EntityIdentifier createSavingProduct(SavingProductCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_SAVINGSPRODUCT')")
	EntityIdentifier updateSavingProduct(SavingProductCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'DELETE_SAVINGSPRODUCT')")
	EntityIdentifier deleteSavingProduct(Long productId);
}
