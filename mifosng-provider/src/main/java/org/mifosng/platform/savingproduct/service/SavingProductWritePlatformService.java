package org.mifosng.platform.savingproduct.service;

import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SavingProductWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_SAVINGSPRODUCT')")
	EntityIdentifier createSavingProduct(SavingProductCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_SAVINGSPRODUCT')")
	EntityIdentifier updateSavingProduct(SavingProductCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'DELETE_SAVINGSPRODUCT')")
	EntityIdentifier deleteSavingProduct(Long productId);
}
