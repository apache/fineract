/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.domain;

import org.mifosplatform.portfolio.floatingrates.exception.FloatingRateNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FloatingRateRepositoryWrapper {

	private final FloatingRateRepository floatingRateRepository;

	@Autowired
	public FloatingRateRepositoryWrapper(
			final FloatingRateRepository floatingRateRepository) {
		this.floatingRateRepository = floatingRateRepository;
	}

	public FloatingRate retrieveBaseLendingRate() {
		return this.floatingRateRepository.retrieveBaseLendingRate();
	}

	public FloatingRate findOneWithNotFoundDetection(final Long id) {
		final FloatingRate floatingRate = this.floatingRateRepository
				.findOne(id);
		if (floatingRate == null) {
			throw new FloatingRateNotFoundException(id);
		}
		return floatingRate;
	}

	public void save(FloatingRate newFloatingRate) {
		this.floatingRateRepository.save(newFloatingRate);
	}

	public void saveAndFlush(FloatingRate floatingRateForUpdate) {
		this.floatingRateRepository.saveAndFlush(floatingRateForUpdate);
	}
}
