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
package org.apache.fineract.portfolio.interestratechart.data;

import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.apache.fineract.portfolio.interestratechart.exception.InterestRateChartSlabNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link InterestRateChartSlabRepository} that is responsible for checking
 * if {@link InterestRateChartSlab} is returned when using <code>findOne</code>
 * repository method and throwing an appropriate not found exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link InterestRateChartSlabRepository} is required.
 * </p>
 */
@Service
public class InterestRateChartSlabRepositoryWrapper {

    private final InterestRateChartSlabRepository repository;

    @Autowired
    public InterestRateChartSlabRepositoryWrapper(final InterestRateChartSlabRepository repository) {
        this.repository = repository;
    }

    public InterestRateChartSlab findOneWithNotFoundDetection(final Long chartSlabId) {
        final InterestRateChartSlab chartSlab = this.repository.findOne(chartSlabId);
        if (chartSlab == null) { throw new InterestRateChartSlabNotFoundException(chartSlabId); }
        return chartSlab;
    }

    public void save(final InterestRateChartSlab chartSlab) {
        this.repository.save(chartSlab);
    }

    public void delete(final InterestRateChartSlab chartSlab) {
        this.repository.delete(chartSlab);
    }

    public void saveAndFlush(final InterestRateChartSlab chartSlab) {
        this.repository.saveAndFlush(chartSlab);
    }
    
}