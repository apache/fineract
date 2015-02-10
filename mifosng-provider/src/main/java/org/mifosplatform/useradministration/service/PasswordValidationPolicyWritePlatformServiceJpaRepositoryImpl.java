/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.PasswordValidationPolicy;
import org.mifosplatform.useradministration.domain.PasswordValidationPolicyRepository;
import org.mifosplatform.useradministration.domain.PermissionRepository;
import org.mifosplatform.useradministration.exception.PasswordValidationPolicyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PasswordValidationPolicyWritePlatformServiceJpaRepositoryImpl implements PasswordValidationPolicyWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(PasswordValidationPolicyWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final PasswordValidationPolicyRepository validationRepository;

    @Autowired
    public PasswordValidationPolicyWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final PasswordValidationPolicyRepository validationPolicyRepository,
                                                                         final PermissionRepository permissionRepository) {
        this.context = context;
        this.validationRepository = validationPolicyRepository;

    }


    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.role.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult activate(final Long validationPolicyId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            final List<PasswordValidationPolicy> validationPolicies = this.validationRepository.findAll();

             Map<String, Object>  changes  = new LinkedHashMap<>(1);

            boolean found = false;

            for(PasswordValidationPolicy policy:validationPolicies){

                if(policy.getId().equals(validationPolicyId)){

                    found = true;

                    if(!policy.isActive()){
                         changes =  policy.active();
                    }
                }
                else if(policy.isActive() && !policy.getId().equals(validationPolicyId)){
                    policy.deActive();

                }
            }

            if(!found) { throw new PasswordValidationPolicyNotFoundException(validationPolicyId); }

            if (!changes.isEmpty()) {
                this.validationRepository.save(validationPolicies);
                this.validationRepository.flush();
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(validationPolicyId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }
    }

}