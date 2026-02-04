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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyNotFoundException;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.service.LoanOriginatorLinkingService;
import org.apache.fineract.portfolio.loanorigination.data.LoanApplicationOriginatorData;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMapping;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMappingRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorStatus;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorCreationNotAllowedException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotActiveException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotFoundException;
import org.apache.fineract.portfolio.loanorigination.serialization.LoanApplicationOriginatorDataValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link LoanOriginatorLinkingService} that handles processing of originators during loan
 * application. This service is active only when the loan-origination module is enabled.
 */
@Slf4j
@Service("loanOriginatorLinkingServiceImpl")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public class LoanOriginatorLinkingServiceImpl implements LoanOriginatorLinkingService {

    private final LoanOriginatorRepository loanOriginatorRepository;
    private final LoanOriginatorMappingRepository loanOriginatorMappingRepository;
    private final LoanApplicationOriginatorDataValidator validator;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    @Transactional
    @Override
    public void processOriginatorsForLoanApplication(Long loanId, JsonArray originatorsArray) {
        if (originatorsArray == null || originatorsArray.isEmpty()) {
            return;
        }

        log.debug("Processing {} originators for loan application {}", originatorsArray.size(), loanId);

        Set<Long> attachedOriginatorIds = new HashSet<>();

        for (JsonElement element : originatorsArray) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject jsonObject = element.getAsJsonObject();
            LoanApplicationOriginatorData originatorData = validator.validateAndExtract(jsonObject);
            LoanOriginator originator = resolveOrCreateOriginator(originatorData);

            if (attachedOriginatorIds.contains(originator.getId())) {
                log.debug("Originator {} already attached to loan {}, skipping duplicate", originator.getId(), loanId);
                continue;
            }

            if (originator.getStatus() != LoanOriginatorStatus.ACTIVE) {
                throw new LoanOriginatorNotActiveException(originator.getId(), originator.getStatus().getValue());
            }

            if (!loanOriginatorMappingRepository.existsByLoanIdAndOriginatorId(loanId, originator.getId())) {
                LoanOriginatorMapping mapping = LoanOriginatorMapping.create(loanId, originator);
                loanOriginatorMappingRepository.save(mapping);
                log.debug("Attached originator {} to loan {}", originator.getId(), loanId);
            }

            attachedOriginatorIds.add(originator.getId());
        }
    }

    private LoanOriginator resolveOrCreateOriginator(LoanApplicationOriginatorData originatorData) {
        if (originatorData.getId() != null) {
            return loanOriginatorRepository.findById(originatorData.getId())
                    .orElseThrow(() -> new LoanOriginatorNotFoundException(originatorData.getId()));
        }

        String externalId = originatorData.getExternalId();
        Optional<LoanOriginator> existingOriginator = loanOriginatorRepository.findByExternalId(new ExternalId(externalId));

        if (existingOriginator.isPresent()) {
            return existingOriginator.get();
        }

        if (!isOriginatorCreationDuringLoanApplicationEnabled()) {
            throw new LoanOriginatorCreationNotAllowedException(externalId);
        }

        return createNewOriginator(originatorData);
    }

    private boolean isOriginatorCreationDuringLoanApplicationEnabled() {
        try {
            GlobalConfigurationProperty config = globalConfigurationRepository
                    .findOneByNameWithNotFoundDetection(ENABLE_ORIGINATOR_CREATION_DURING_LOAN_APPLICATION);
            return config.isEnabled();
        } catch (GlobalConfigurationPropertyNotFoundException e) {
            log.warn("Global configuration '{}' not found, defaulting to disabled", ENABLE_ORIGINATOR_CREATION_DURING_LOAN_APPLICATION);
            return false;
        }
    }

    private LoanOriginator createNewOriginator(LoanApplicationOriginatorData data) {
        log.info("Creating new originator with externalId: {} during loan application", data.getExternalId());

        CodeValue originatorType = resolveCodeValue(data.getTypeId(), ORIGINATOR_TYPE_CODE_NAME);
        CodeValue channelType = resolveCodeValue(data.getChannelTypeId(), CHANNEL_TYPE_CODE_NAME);

        LoanOriginator originator = LoanOriginator.create(new ExternalId(data.getExternalId()), data.getName(), LoanOriginatorStatus.ACTIVE,
                originatorType, channelType);

        return loanOriginatorRepository.saveAndFlush(originator);
    }

    private CodeValue resolveCodeValue(Long codeValueId, String codeName) {
        if (codeValueId == null) {
            return null;
        }
        return codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(codeName, codeValueId);
    }
}
