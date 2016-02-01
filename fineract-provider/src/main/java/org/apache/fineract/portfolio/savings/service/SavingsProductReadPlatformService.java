/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.Collection;

import org.mifosplatform.portfolio.savings.data.SavingsProductData;

public interface SavingsProductReadPlatformService {

    Collection<SavingsProductData> retrieveAll();

    Collection<SavingsProductData> retrieveAllForLookup();

    Collection<SavingsProductData> retrieveAllForLookupByType(Boolean isOverdraftType);

    Collection<SavingsProductData> retrieveAllForCurrency(String currencyCode);

    SavingsProductData retrieveOne(Long productId);

}
