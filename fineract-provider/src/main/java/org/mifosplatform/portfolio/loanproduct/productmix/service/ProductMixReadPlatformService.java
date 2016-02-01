/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.productmix.service;

import java.util.Collection;

import org.mifosplatform.portfolio.loanproduct.productmix.data.ProductMixData;

public interface ProductMixReadPlatformService {

    ProductMixData retrieveLoanProductMixDetails(Long productId);

    Collection<ProductMixData> retrieveAllProductMixes();
}
