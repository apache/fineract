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
package org.apache.fineract.portfolio.interestratechart.domain;

import org.apache.fineract.portfolio.interestratechart.exception.InterestRateChartNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link InterestRateChartRepository} that is responsible for checking if {@link InterestRateChart} is
 * returned when using <code>findOne</code> repository method and throwing an appropriate not found exception.
 * </p>
 *
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code base where
 * {@link InterestRateChartRepository} is required.
 * </p>
 */
@Service
public class InterestRateChartRepositoryWrapper {

    private final InterestRateChartRepository repository;

    @Autowired
    public InterestRateChartRepositoryWrapper(final InterestRateChartRepository repository) {
        this.repository = repository;
    }

    public InterestRateChart findOneWithNotFoundDetection(final Long intrestRateChartId) {
        return this.repository.findById(intrestRateChartId).orElseThrow(() -> new InterestRateChartNotFoundException(intrestRateChartId));
    }

    public void save(final InterestRateChart interestRateChart) {
        this.repository.save(interestRateChart);
    }

    public void delete(final InterestRateChart interestRateChart) {
        this.repository.delete(interestRateChart);
    }

    public void saveAndFlush(final InterestRateChart interestRateChart) {
        this.repository.saveAndFlush(interestRateChart);
    }
}
