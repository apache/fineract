/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class InterestRateChartSlabNotFoundException extends AbstractPlatformResourceNotFoundException {

    public InterestRateChartSlabNotFoundException(final Long id) {
        super("error.msg.interest.rate.chart.slab.id.invalid", "Interest rate chart slab with identifier " + id + " does not exist", id);
    }
    
    public InterestRateChartSlabNotFoundException(final Long id, final Long chartId) {
        super("error.msg.interest.rate.chart.slab.id.invalid", "Interest rate chart slab with identifier " + id + " does not exist in interest chart with identifier " + chartId , id, chartId);
    }
}