package org.mifosng.platform.savingproduct.service;

import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DepositProductWritePlatformService {
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_DEPOSITPRODUCT')")
	EntityIdentifier createDepositProduct(DepositProductCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_DEPOSITPRODUCT')")
	EntityIdentifier updateDepositProduct(DepositProductCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'DELETE_DEPOSITPRODUCT')")
	EntityIdentifier deleteDepositProduct(Long productId);

}
