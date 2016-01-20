/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.products.service;

import java.util.Collection;
import java.util.Set;

import org.mifosplatform.portfolio.products.data.ProductData;

public interface ProductReadPlatformService {

    public Collection<ProductData> retrieveAllProducts();

    public ProductData retrieveOne(final Long productId);

    public ProductData retrieveTemplate();

    public Set<String> getResponseDataParams();
}
