/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.service;

import java.util.List;

import org.mifosplatform.portfolio.floatingrates.data.FloatingRateData;
import org.mifosplatform.portfolio.floatingrates.data.InterestRatePeriodData;

public interface FloatingRatesReadPlatformService {

	List<FloatingRateData> retrieveAll();

	List<FloatingRateData> retrieveLookupActive();

	FloatingRateData retrieveOne(Long floatingRateId);

	List<FloatingRateData> retrieveAllActive();

	FloatingRateData retrieveBaseLendingRate();

	List<InterestRatePeriodData> retrieveInterestRatePeriods(Long floatingRateId);

}
