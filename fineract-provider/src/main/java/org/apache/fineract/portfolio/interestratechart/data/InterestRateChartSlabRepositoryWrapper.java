/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.data;

import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.mifosplatform.portfolio.interestratechart.exception.InterestRateChartSlabNotFoundException;
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