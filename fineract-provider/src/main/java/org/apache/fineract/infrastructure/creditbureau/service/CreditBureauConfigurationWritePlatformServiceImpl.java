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

import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauConfiguration;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauConfigurationRepository;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureau;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureauRepository;
import org.apache.fineract.infrastructure.creditbureau.exception.CreditReportNotFoundException;
import org.apache.fineract.infrastructure.creditbureau.serialization.CreditBureauConfigurationCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditBureauConfigurationWritePlatformServiceImpl implements CreditBureauConfigurationWritePlatformService {

    private final PlatformSecurityContext context;

    private final CreditBureauConfigurationCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    private final CreditBureauConfigurationRepository creditBureauConfigurationRepository;

    private final OrganisationCreditBureauRepository organisationCreditBureauRepository;

    @Autowired
    public CreditBureauConfigurationWritePlatformServiceImpl(final PlatformSecurityContext context,
            final CreditBureauConfigurationCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final CreditBureauConfigurationRepository creditBureauConfigurationRepository,
            OrganisationCreditBureauRepository organisationCreditBureauRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.creditBureauConfigurationRepository = creditBureauConfigurationRepository;
        this.organisationCreditBureauRepository = organisationCreditBureauRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult addCreditBureauConfiguration(Long creditBureauId, JsonCommand command) {
        this.context.authenticatedUser();

        this.fromApiJsonDeserializer.validateForCreate(command.json(), creditBureauId);

        final OrganisationCreditBureau orgcb = this.organisationCreditBureauRepository.getById(creditBureauId);

        final CreditBureauConfiguration cb_config = CreditBureauConfiguration.fromJson(command, orgcb);

        this.creditBureauConfigurationRepository.saveAndFlush(cb_config);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(cb_config.getId()).build();

    }

    @Transactional
    @Override
    public CommandProcessingResult updateCreditBureauConfiguration(Long configurationId, JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final CreditBureauConfiguration config = retrieveConfigBy(configurationId);
            final Map<String, Object> changes = config.update(command);

            if (!changes.isEmpty()) {
                this.creditBureauConfigurationRepository.save(config);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(configurationId) //
                    .with(changes) //
                    .build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + dve.getMostSpecificCause(), dve);
        }

    }

    private CreditBureauConfiguration retrieveConfigBy(final Long creditBureauId) {
        return this.creditBureauConfigurationRepository.findById(creditBureauId)
                .orElseThrow(() -> new CreditReportNotFoundException(creditBureauId));
    }
}
