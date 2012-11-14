package org.mifosng.platform.savingproduct.service;

import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DepositProductWritePlatformService {
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER')")
	EntityIdentifier createDepositProduct(DepositProductCommand command);
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER')")
	EntityIdentifier updateDepositProduct(DepositProductCommand command);
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER')")
	EntityIdentifier deleteDepositProduct(Long productId);

}
