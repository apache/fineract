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
package org.apache.fineract.portfolio.floatingrates.service;

import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import org.apache.fineract.portfolio.floatingrates.serialization.FloatingRateDataValidator;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FloatingRateWritePlatformServiceImpl implements
		FloatingRateWritePlatformService {

	private final static Logger logger = LoggerFactory
			.getLogger(FloatingRateWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;
	private final FloatingRateDataValidator fromApiJsonDeserializer;
	private final FloatingRateRepositoryWrapper floatingRateRepository;

	@Autowired
	public FloatingRateWritePlatformServiceImpl(
			final PlatformSecurityContext context,
			final FloatingRateDataValidator fromApiJsonDeserializer,
			final FloatingRateRepositoryWrapper floatingRateRepository) {
		this.context = context;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.floatingRateRepository = floatingRateRepository;
	}

	@Transactional
	@Override
	public CommandProcessingResult createFloatingRate(final JsonCommand command) {
		try {
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final AppUser currentUser = this.context.authenticatedUser();
			final FloatingRate newFloatingRate = FloatingRate.createNew(
					currentUser, command);
			this.floatingRateRepository.save(newFloatingRate);
			return new CommandProcessingResultBuilder() //
					.withCommandId(command.commandId()) //
					.withEntityId(newFloatingRate.getId()) //
					.build();
		} catch (final DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
			return CommandProcessingResult.empty();
		}catch (final PersistenceException dve) {
			Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
         	return CommandProcessingResult.empty();
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult updateFloatingRate(final JsonCommand command) {
		try {
			final FloatingRate floatingRateForUpdate = this.floatingRateRepository
					.findOneWithNotFoundDetection(command.entityId());
			this.fromApiJsonDeserializer.validateForUpdate(command.json(),
					floatingRateForUpdate);
			final AppUser currentUser = this.context.authenticatedUser();
			final Map<String, Object> changes = floatingRateForUpdate.update(
					command, currentUser);

			if (!changes.isEmpty()) {
				this.floatingRateRepository.save(floatingRateForUpdate);
			}

			return new CommandProcessingResultBuilder() //
					.withCommandId(command.commandId()) //
					.withEntityId(command.entityId()) //
					.with(changes) //
					.build();
		} catch (final DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
			return CommandProcessingResult.empty();
		}catch (final PersistenceException dve) {
			Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
         	return CommandProcessingResult.empty();
		}
	}

	private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause,
			final Exception dve) {

		if (realCause.getMessage().contains("unq_name")) {

			final String name = command.stringValueOfParameterNamed("name");
			throw new PlatformDataIntegrityException(
					"error.msg.floatingrates.duplicate.name",
					"Floating Rate with name `" + name + "` already exists",
					"name", name);
		}

		if (realCause.getMessage().contains("unq_rate_period")) {
			throw new PlatformDataIntegrityException(
					"error.msg.floatingrates.duplicate.active.fromdate",
					"Attempt to add multiple floating rate periods with same fromdate",
					"fromdate", "");
		}

		logAsErrorUnexpectedDataIntegrityException(dve);
		throw new PlatformDataIntegrityException(
				"error.msg.floatingrates.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource.");
	}

	private void logAsErrorUnexpectedDataIntegrityException(
			Exception dve) {
		logger.error(dve.getMessage(), dve);

	}

}
