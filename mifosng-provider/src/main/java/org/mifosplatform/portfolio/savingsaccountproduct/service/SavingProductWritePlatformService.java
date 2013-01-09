package org.mifosplatform.portfolio.savingsaccountproduct.service;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savingsaccountproduct.command.SavingProductCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SavingProductWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_SAVINGSPRODUCT')")
    CommandProcessingResult createSavingProduct(SavingProductCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'UPDATE_SAVINGSPRODUCT')")
    CommandProcessingResult updateSavingProduct(SavingProductCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_SAVINGSPRODUCT')")
    CommandProcessingResult deleteSavingProduct(Long productId);
}
