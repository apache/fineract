package org.mifosng.platform.savingproduct.service;

import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SavingProductWritePlatformService {
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	EntityIdentifier createSavingProduct(SavingProductCommand command);
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	EntityIdentifier updateSavingProduct(SavingProductCommand command);
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	EntityIdentifier deleteSavingProduct(Long productId);
}
