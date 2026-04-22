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
package org.apache.fineract.portfolio.loanorigination.service;

import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants.ENABLE_ORIGINATOR_CREATION_DURING_LOAN_APPLICATION;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.CHANNEL_TYPE_CODE_NAME;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.ORIGINATOR_TYPE_CODE_NAME;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyNotFoundException;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanorigination.data.LoanApplicationOriginatorData;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorStatus;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorCreationNotAllowedException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotActiveException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanOriginatorHelper {

    private final LoanOriginatorRepository loanOriginatorRepository;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    // REQUIRES_NEW isolates the INSERT into a separate transaction and persistence context,
    // so a constraint violation does not corrupt the caller's session or mark the
    // outer transaction as rollback-only, allowing a safe retry.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long findOrCreateOriginatorId(final LoanApplicationOriginatorData data) {
        final ExternalId externalId = new ExternalId(data.getExternalId());
        return loanOriginatorRepository.findByExternalId(externalId).map(existing -> {
            validateActive(existing);
            return existing.getId();
        }).orElseGet(() -> {
            if (!isOriginatorCreationDuringLoanApplicationEnabled()) {
                throw new LoanOriginatorCreationNotAllowedException(data.getExternalId());
            }
            return createNewOriginator(data, externalId).getId();
        });
    }

    private void validateActive(final LoanOriginator originator) {
        if (originator.getStatus() != LoanOriginatorStatus.ACTIVE) {
            throw new LoanOriginatorNotActiveException(originator.getId(), originator.getStatus().getValue());
        }
    }

    private boolean isOriginatorCreationDuringLoanApplicationEnabled() {
        try {
            final GlobalConfigurationProperty config = globalConfigurationRepository
                    .findOneByNameWithNotFoundDetection(ENABLE_ORIGINATOR_CREATION_DURING_LOAN_APPLICATION);
            return config.isEnabled();
        } catch (final GlobalConfigurationPropertyNotFoundException e) {
            log.warn("Global configuration '{}' not found, defaulting to disabled", ENABLE_ORIGINATOR_CREATION_DURING_LOAN_APPLICATION);
            return false;
        }
    }

    private LoanOriginator createNewOriginator(final LoanApplicationOriginatorData data, final ExternalId externalId) {
        log.info("Creating new originator with externalId: {} during loan application", data.getExternalId());

        final CodeValue originatorType = resolveCodeValue(data.getTypeId(), ORIGINATOR_TYPE_CODE_NAME);
        final CodeValue channelType = resolveCodeValue(data.getChannelTypeId(), CHANNEL_TYPE_CODE_NAME);

        final LoanOriginator originator = LoanOriginator.create(externalId, data.getName(), LoanOriginatorStatus.ACTIVE, originatorType,
                channelType);

        return loanOriginatorRepository.saveAndFlush(originator);
    }

    private CodeValue resolveCodeValue(final Long codeValueId, final String codeName) {
        if (codeValueId == null) {
            return null;
        }
        return codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(codeName, codeValueId);
    }
}
