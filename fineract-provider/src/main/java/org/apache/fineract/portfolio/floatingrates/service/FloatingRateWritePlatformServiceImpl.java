/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.floatingrates.domain.FloatingRate;
import org.mifosplatform.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import org.mifosplatform.portfolio.floatingrates.serialization.FloatingRateDataValidator;
import org.mifosplatform.useradministration.domain.AppUser;
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
			handleDataIntegrityIssues(command, dve);
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
			handleDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

	private void handleDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause();

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
			DataIntegrityViolationException dve) {
		logger.error(dve.getMessage(), dve);

	}

}
