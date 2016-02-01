/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.util.Collection;

import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartSlabData;

public interface InterestRateChartSlabReadPlatformService {

    Collection<InterestRateChartSlabData> retrieveAll(Long chartId);

    InterestRateChartSlabData retrieveOne(Long chartId, Long chartSlabId);

    InterestRateChartSlabData retrieveWithTemplate(InterestRateChartSlabData chartSlab);

    InterestRateChartSlabData retrieveTemplate();

}