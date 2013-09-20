package org.mifosplatform.portfolio.loanproduct.productmix.service;

import java.util.Collection;

import org.mifosplatform.portfolio.loanproduct.productmix.data.ProductMixData;

public interface ProductMixReadPlatformService {

    ProductMixData retrieveLoanProductMixDetails(Long productId);

    Collection<ProductMixData> retrieveAllProductMixes();
}
