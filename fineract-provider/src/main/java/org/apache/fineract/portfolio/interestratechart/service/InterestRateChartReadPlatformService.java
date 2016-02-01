/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.interestratechart.service;

import java.util.Collection;

import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartData;

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