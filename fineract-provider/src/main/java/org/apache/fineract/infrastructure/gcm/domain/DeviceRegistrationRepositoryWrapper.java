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
package org.apache.fineract.infrastructure.gcm.domain;

import org.apache.fineract.infrastructure.gcm.exception.DeviceRegistrationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceRegistrationRepositoryWrapper {

	private final DeviceRegistrationRepository repository;

	@Autowired
	public DeviceRegistrationRepositoryWrapper(
			DeviceRegistrationRepository repository) {
		this.repository = repository;
	}

	public DeviceRegistration findOneWithNotFoundDetection(
			final Long deviceRegistrationId) {
		final DeviceRegistration deviceRegistration = this.repository
				.findOne(deviceRegistrationId);
		if (deviceRegistration == null) {
			throw new DeviceRegistrationNotFoundException(deviceRegistrationId);
		}
		return deviceRegistration;
	}

	public void save(final DeviceRegistration deviceRegistration) {
		this.repository.save(deviceRegistration);
	}

	public void delete(final DeviceRegistration deviceRegistration) {
		this.repository.delete(deviceRegistration);
	}

	public void saveAndFlush(final DeviceRegistration deviceRegistration) {
		this.repository.saveAndFlush(deviceRegistration);
	}

	public DeviceRegistration findDeviceRegistrationByClientId(Long clientId) {
		return this.repository.findDeviceRegistrationByClientId(clientId);
	}
}
