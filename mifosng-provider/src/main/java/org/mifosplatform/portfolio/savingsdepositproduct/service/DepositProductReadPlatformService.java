/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositproduct.service;

import java.util.Collection;

import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductData;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductLookup;

public interface DepositProductReadPlatformService {

    Collection<DepositProductData> retrieveAllDepositProducts();

    Collection<DepositProductLookup> retrieveAllDepositProductsForLookup();

    DepositProductData retrieveDepositProductData(Long productId);

    DepositProductData retrieveNewDepositProductDetails();

}
