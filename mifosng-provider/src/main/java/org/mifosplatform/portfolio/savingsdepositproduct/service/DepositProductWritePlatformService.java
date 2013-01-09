package org.mifosplatform.portfolio.savingsdepositproduct.service;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savingsdepositproduct.command.DepositProductCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DepositProductWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_DEPOSITPRODUCT')")
    CommandProcessingResult createDepositProduct(DepositProductCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'UPDATE_DEPOSITPRODUCT')")
    CommandProcessingResult updateDepositProduct(DepositProductCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_DEPOSITPRODUCT')")
    CommandProcessingResult deleteDepositProduct(Long productId);
}