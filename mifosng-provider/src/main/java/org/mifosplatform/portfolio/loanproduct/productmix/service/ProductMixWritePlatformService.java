package org.mifosplatform.portfolio.loanproduct.productmix.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface ProductMixWritePlatformService {

    CommandProcessingResult createProductMix(Long productId, JsonCommand command);

    CommandProcessingResult updateProductMix(Long productId, JsonCommand command);

    CommandProcessingResult deleteProductMix(Long productId);

}
