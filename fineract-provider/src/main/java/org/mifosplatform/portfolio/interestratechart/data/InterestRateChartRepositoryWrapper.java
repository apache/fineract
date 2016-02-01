/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.data;

import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.mifosplatform.portfolio.interestratechart.exception.InterestRateChartNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link InterestRateChartRepository} that is responsible for
 * checking if {@link InterestRateChart} is returned when using
 * <code>findOne</code> repository method and throwing an appropriate not found
 * exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link InterestRateChartRepository} is required.
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
        final InterestRateChart interestRateChart = this.repository.findOne(intrestRateChartId);
        if (interestRateChart == null) { throw new InterestRateChartNotFoundException(intrestRateChartId); }
        return interestRateChart;
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