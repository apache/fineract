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

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
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

    private static final String CONFIG_KEY_PARAM = "configkey";
    private static final String VALUE_PARAM = "value";
    private static final String DESCRIPTION_PARAM = "description";

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

        final OrganisationCreditBureau orgcb = this.organisationCreditBureauRepository.getReferenceById(creditBureauId);

        final CreditBureauConfiguration cb_config = CreditBureauConfiguration.createNew(
                command.stringValueOfParameterNamed(CONFIG_KEY_PARAM), command.stringValueOfParameterNamed(VALUE_PARAM),
                command.stringValueOfParameterNamed(DESCRIPTION_PARAM), orgcb);

        this.creditBureauConfigurationRepository.saveAndFlush(cb_config);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(cb_config.getId()) //
                .build();

    }

    @Transactional
    @Override
    public CommandProcessingResult updateCreditBureauConfiguration(Long configurationId, JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final CreditBureauConfiguration config = retrieveConfigBy(configurationId);
            final Map<String, Object> changes = updateConfiguration(config, command);

            if (!changes.isEmpty()) {
                this.creditBureauConfigurationRepository.save(config);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(configurationId) //
                    .with(changes) //
                    .build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw ErrorHandler.getMappable(dve, "error.msg.cund.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + dve.getMostSpecificCause().getMessage());
        }

    }

    private Map<String, Object> updateConfiguration(final CreditBureauConfiguration config, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(1);

        if (command.isChangeInStringParameterNamed(CONFIG_KEY_PARAM, config.getConfigurationKey())) {
            final String newValue = command.stringValueOfParameterNamed(CONFIG_KEY_PARAM);
            actualChanges.put(CONFIG_KEY_PARAM, newValue);
            config.updateConfigurationKey(newValue);
        }

        if (command.isChangeInStringParameterNamed(VALUE_PARAM, config.getValue())) {
            final String newValue = command.stringValueOfParameterNamed(VALUE_PARAM);
            actualChanges.put(VALUE_PARAM, newValue);
            config.updateValue(newValue);
        }

        // Existing behaviour kept as is: the description is compared with the configuration key, not with the
        // description.
        if (command.isChangeInStringParameterNamed(DESCRIPTION_PARAM, config.getConfigurationKey())) {
            final String newValue = command.stringValueOfParameterNamed(DESCRIPTION_PARAM);
            actualChanges.put(DESCRIPTION_PARAM, newValue);
            config.updateDescription(newValue);
        }

        return actualChanges;
    }

    private CreditBureauConfiguration retrieveConfigBy(final Long creditBureauId) {
        return this.creditBureauConfigurationRepository.findById(creditBureauId)
                .orElseThrow(() -> new CreditReportNotFoundException(creditBureauId));
    }
}
