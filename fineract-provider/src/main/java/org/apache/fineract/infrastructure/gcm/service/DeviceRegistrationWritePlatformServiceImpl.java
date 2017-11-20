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
package org.apache.fineract.infrastructure.gcm.service;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.gcm.domain.DeviceRegistration;
import org.apache.fineract.infrastructure.gcm.domain.DeviceRegistrationRepositoryWrapper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.openjpa.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceRegistrationWritePlatformServiceImpl implements
		DeviceRegistrationWritePlatformService {

	private final DeviceRegistrationRepositoryWrapper deviceRegistrationRepository;
	private final ClientRepositoryWrapper clientRepositoryWrapper;
	private final PlatformSecurityContext context;

	@Autowired
	public DeviceRegistrationWritePlatformServiceImpl(
			final DeviceRegistrationRepositoryWrapper deviceRegistrationRepository,
			final ClientRepositoryWrapper clientRepositoryWrapper,
			final PlatformSecurityContext context) {
		this.deviceRegistrationRepository = deviceRegistrationRepository;
		this.clientRepositoryWrapper = clientRepositoryWrapper;
		this.context = context;
	}

	@Transactional
	@Override
	public DeviceRegistration registerDevice(Long clientId,
			String registrationId) {
		this.context.authenticatedUser();
		Client client = this.clientRepositoryWrapper
				.findOneWithNotFoundDetection(clientId);
		try {
			DeviceRegistration deviceRegistration = DeviceRegistration
					.instance(client, registrationId);
			this.deviceRegistrationRepository.save(deviceRegistration);
			return deviceRegistration;
		} catch (final EntityExistsException dve) {
			handleDataIntegrityIssues(registrationId, dve, dve);
			return null;
		} catch (final DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(registrationId,
					dve.getMostSpecificCause(), dve);
			return null;
		} catch (final PersistenceException dve) {
			Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
			handleDataIntegrityIssues(registrationId, throwable, dve);
			return null;
		} catch (final Exception dve) {
			Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
			handleDataIntegrityIssues(registrationId, throwable, dve);
			return null;
		}

	}

	private void handleDataIntegrityIssues(final String registrationId,
			final Throwable realCause,
			@SuppressWarnings("unused") final Exception dve) {

		if (realCause.getMessage().contains("registration_key")) {
			throw new PlatformDataIntegrityException(
					"error.msg.duplicate.device.registration.id",
					"Registration id : " + registrationId + " already exist.",
					"name", registrationId);
		}

		throw new PlatformDataIntegrityException(
				"error.msg.charge.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "
						+ realCause.getMessage());
	}

	@Override
	public DeviceRegistration updateDeviceRegistration(Long id, Long clientId,
			String registrationId) {
		DeviceRegistration deviceRegistration = this.deviceRegistrationRepository
				.findOneWithNotFoundDetection(id);
		Client client = this.clientRepositoryWrapper
				.findOneWithNotFoundDetection(clientId);
		deviceRegistration.setClient(client);
		deviceRegistration.setRegistrationId(registrationId);
		deviceRegistration.setUpdatedOnDate(DateUtils
				.getLocalDateTimeOfTenant().toDate());
		return deviceRegistration;
	}

    @Override
    public void deleteDeviceRegistration(Long id) {
        DeviceRegistration deviceRegistration = this.deviceRegistrationRepository.findOneWithNotFoundDetection(id);
        this.deviceRegistrationRepository.delete(deviceRegistration);
    }

}
