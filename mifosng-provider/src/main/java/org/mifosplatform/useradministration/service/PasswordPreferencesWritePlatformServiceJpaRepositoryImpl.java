/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.useradministration.api.PasswordPreferencesApiConstants;
import org.mifosplatform.useradministration.data.PasswordPreferencesDataValidator;
import org.mifosplatform.useradministration.domain.PasswordValidationPolicy;
import org.mifosplatform.useradministration.domain.PasswordValidationPolicyRepository;
import org.mifosplatform.useradministration.exception.PasswordValidationPolicyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordPreferencesWritePlatformServiceJpaRepositoryImpl implements PasswordPreferencesWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(PasswordPreferencesWritePlatformServiceJpaRepositoryImpl.class);
    private final PasswordValidationPolicyRepository validationRepository;
    private final PasswordPreferencesDataValidator dataValidator;

    @Autowired
    public PasswordPreferencesWritePlatformServiceJpaRepositoryImpl(final PasswordValidationPolicyRepository validationPolicyRepository,
            final PasswordPreferencesDataValidator dataValidator) {
        this.validationRepository = validationPolicyRepository;
        this.dataValidator = dataValidator;

    }

    @Transactional
    @Override
    public CommandProcessingResult updatePreferences(final JsonCommand command) {

        this.dataValidator.validateForUpdate(command.json());
        Long validationPolicyId = command.longValueOfParameterNamed(PasswordPreferencesApiConstants.VALIDATION_POLICY_ID);
        try {
            final List<PasswordValidationPolicy> validationPolicies = this.validationRepository.findAll();

            Map<String, Object> changes = new HashMap<>(1);

            boolean found = false;

            for (PasswordValidationPolicy policy : validationPolicies) {
                if (policy.getId().equals(validationPolicyId)) {
                    found = true;
                    if (!policy.isActive()) {
                        changes = policy.activate();
                    }
                } else if (policy.isActive() && !policy.getId().equals(validationPolicyId)) {
                    policy.deActivate();
                }
            }

            if (!found) { throw new PasswordValidationPolicyNotFoundException(validationPolicyId); }

            if (!changes.isEmpty()) {
                this.validationRepository.save(validationPolicies);
                this.validationRepository.flush();
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            logger.error(dve.getMessage(), dve);
            throw new PlatformDataIntegrityException("error.msg.password.validation.policy.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }
}