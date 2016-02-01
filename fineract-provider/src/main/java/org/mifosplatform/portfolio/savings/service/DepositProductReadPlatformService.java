/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.Collection;

import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.data.DepositProductData;

public interface DepositProductReadPlatformService {

    Collection<DepositProductData> retrieveAll(final DepositAccountType depositAccountType);

    Collection<DepositProductData> retrieveAllForLookup(final DepositAccountType depositAccountType);

    DepositProductData retrieveOne(final DepositAccountType depositAccountType, Long productId);

    DepositProductData retrieveOneWithChartSlabs(final DepositAccountType depositAccountType, Long productId);

}
