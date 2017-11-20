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
package org.apache.fineract.infrastructure.creditbureau.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureau;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauRepository;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureau;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureauRepository;
import org.apache.fineract.infrastructure.creditbureau.serialization.CreditBureauCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganisationCreditBureauWritePlatflormServiceImpl
		implements OrganisationCreditBureauWritePlatflormService {

	private final PlatformSecurityContext context;

	private final OrganisationCreditBureauRepository organisationCreditBureauRepository;

	private final CreditBureauRepository creditBureauRepository;

	private final CreditBureauCommandFromApiJsonDeserializer fromApiJsonDeserializer;

	@Autowired
	public OrganisationCreditBureauWritePlatflormServiceImpl(final PlatformSecurityContext context,
			final OrganisationCreditBureauRepository organisationCreditBureauRepository, final CreditBureauRepository creditBureauRepository,
			final CreditBureauCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
		this.context = context;
		this.organisationCreditBureauRepository = organisationCreditBureauRepository;
		this.creditBureauRepository = creditBureauRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;

	}

	@Override
	public CommandProcessingResult addOrganisationCreditBureau(Long creditBureauId, JsonCommand command) {
		this.context.authenticatedUser();
		this.fromApiJsonDeserializer.validateForCreate(command.json(), creditBureauId);

		final CreditBureau creditBureau = this.creditBureauRepository.getOne(creditBureauId);

		final OrganisationCreditBureau organisationCreditBureau = OrganisationCreditBureau.fromJson(command, creditBureau);

		this.organisationCreditBureauRepository.save(organisationCreditBureau);

		return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(organisationCreditBureau.getId())
				.build();
	}

	@Transactional
	@Override
	public CommandProcessingResult updateCreditBureau(JsonCommand command) {
		 this.context.authenticatedUser();
		this.fromApiJsonDeserializer.validateForUpdate(command.json());

		final long creditbureauID = command.longValueOfParameterNamed("creditBureauId");
		
		final boolean is_active = command.booleanPrimitiveValueOfParameterNamed("is_active");

		final OrganisationCreditBureau orgcb = organisationCreditBureauRepository.getOne(creditbureauID);

		orgcb.setIsActive(is_active);

		organisationCreditBureauRepository.saveAndFlush(orgcb);

		return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(orgcb.getId())
				.build();

	}

}
