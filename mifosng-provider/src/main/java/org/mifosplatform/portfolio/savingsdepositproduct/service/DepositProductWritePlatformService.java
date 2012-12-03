package org.mifosplatform.portfolio.savingsdepositproduct.service;

import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.portfolio.savingsdepositproduct.command.DepositProductCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DepositProductWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_DEPOSITPRODUCT')")
    EntityIdentifier createDepositProduct(DepositProductCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_DEPOSITPRODUCT')")
    EntityIdentifier updateDepositProduct(DepositProductCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'DELETE_DEPOSITPRODUCT')")
    EntityIdentifier deleteDepositProduct(Long productId);
}