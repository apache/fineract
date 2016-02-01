/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import org.mifosplatform.portfolio.savings.data.DepositAccountInterestRateChartData;

public interface DepositAccountInterestRateChartReadPlatformService {

    DepositAccountInterestRateChartData retrieveOne(Long interestChartId);

    DepositAccountInterestRateChartData retrieveOneWithSlabs(Long interestChartId);

    DepositAccountInterestRateChartData retrieveWithTemplate(DepositAccountInterestRateChartData DepositAccountInterestRateChartData);

    DepositAccountInterestRateChartData retrieveOneWithSlabsOnAccountId(Long accountId);

    DepositAccountInterestRateChartData template();
}