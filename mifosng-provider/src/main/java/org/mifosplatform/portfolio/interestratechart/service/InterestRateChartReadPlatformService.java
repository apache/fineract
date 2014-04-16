/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.util.Collection;

import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartData;

public interface InterestRateChartReadPlatformService {
    
    //Collection<InterestRateChartData> retrieveAll(Long savingsProductId);

    InterestRateChartData retrieveOne(Long interestChartId);
    
    //Collection<InterestRateChartData> retrieveAllWithSlabs();
    
    Collection<InterestRateChartData> retrieveAllWithSlabs(Long savingsProductId);
    
    Collection<InterestRateChartData> retrieveAllWithSlabsWithTemplate(Long savingsProductId);

    InterestRateChartData retrieveOneWithSlabs(Long interestChartId);
    
    InterestRateChartData retrieveWithTemplate(InterestRateChartData interestRateChartData);
    
    InterestRateChartData retrieveOneWithSlabsOnProductId(Long productId);
    
    InterestRateChartData template();
    
    InterestRateChartData retrieveActiveChartWithTemplate(Long productId);
}