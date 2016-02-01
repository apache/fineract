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
package org.apache.fineract.portfolio.floatingrates.domain;

import org.apache.fineract.portfolio.floatingrates.exception.FloatingRateNotFoundException;
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
