/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import java.util.Collection;

import org.mifosplatform.portfolio.account.data.PortfolioAccountDTO;
import org.mifosplatform.portfolio.account.data.PortfolioAccountData;

public interface PortfolioAccountReadPlatformService {

    PortfolioAccountData retrieveOne(Long accountId, Integer accountTypeId);

    PortfolioAccountData retrieveOne(Long accountId, Integer accountTypeId, String currencyCode);

    Collection<PortfolioAccountData> retrieveAllForLookup(final PortfolioAccountDTO portfolioAccountDTO);

    PortfolioAccountData retrieveOneByPaidInAdvance(Long accountId, Integer accountTypeId);
}