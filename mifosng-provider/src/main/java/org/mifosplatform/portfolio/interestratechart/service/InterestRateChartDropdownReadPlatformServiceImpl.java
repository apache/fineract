/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.interestratechart.InterestRateChartPeriodType;
import org.springframework.stereotype.Service;

@Service
public class InterestRateChartDropdownReadPlatformServiceImpl implements InterestRateChartDropdownReadPlatformService {

    @Override
    public Collection<EnumOptionData> retrievePeriodTypeOptions() {
        return InterestRateChartEnumerations.periodType(InterestRateChartPeriodType.values());
    }

}