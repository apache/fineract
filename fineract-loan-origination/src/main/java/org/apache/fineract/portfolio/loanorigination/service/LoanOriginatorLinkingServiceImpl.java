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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.loanaccount.service.LoanOriginatorLinkingService;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMapping;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMappingRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorRepository;
import org.apache.fineract.portfolio.loanorigination.serialization.LoanApplicationOriginatorDataValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link LoanOriginatorLinkingService} that handles processing of originators during loan application
 * and reconciles mappings during disbursement. This service is active only when the loan-origination module is enabled.
 */
@Slf4j
@Primary
@Service("loanOriginatorLinkingServiceImpl")
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public class LoanOriginatorLinkingServiceImpl extends AbstractLoanOriginatorLinkingServiceImpl {

    private final LoanOriginatorMappingRepository loanOriginatorMappingRepository;

    public LoanOriginatorLinkingServiceImpl(LoanOriginatorRepository loanOriginatorRepository,
            LoanApplicationOriginatorDataValidator validator, LoanOriginatorHelper loanOriginatorHelper,
            LoanOriginatorMappingRepository loanOriginatorMappingRepository) {
        super(loanOriginatorRepository, validator, loanOriginatorHelper);
        this.loanOriginatorMappingRepository = loanOriginatorMappingRepository;
    }

    @Override
    protected void createAndSaveOriginatorMapping(Long loanId, Long originatorId) {
        if (!loanOriginatorMappingRepository.existsByLoanIdAndOriginatorId(loanId, originatorId)) {
            final LoanOriginator originatorRef = loanOriginatorRepository.getReferenceById(originatorId);
            final LoanOriginatorMapping mapping = LoanOriginatorMapping.create(loanId, originatorRef);
            loanOriginatorMappingRepository.save(mapping);
            log.debug("Attached originator {} to loan {}", originatorId, loanId);
        }
    }

    /**
     * Reconciles loan-originator mappings for a disbursement request so the loan's mappings exactly match the supplied
     * originator array. A null array is treated as no-op, while an empty array detaches all current originators.
     *
     * @param loanId
     *            loan whose originator mappings should be reconciled
     * @param originatorsArray
     *            JSON array from the disbursement request, or null when the request omitted originators
     */
    @Transactional
    @Override
    public void processOriginatorsForLoanDisbursement(final Long loanId, final JsonArray originatorsArray) {
        if (originatorsArray == null) {
            return;
        }

        log.debug("Reconciling {} originators for loan disbursement {}", originatorsArray.size(), loanId);

        final Set<Long> requestedOriginatorIds = resolveOriginatorIdsForDisbursement(originatorsArray);
        reconcileOriginatorMappings(loanId, requestedOriginatorIds);
    }

    private Set<Long> resolveOriginatorIdsForDisbursement(final JsonArray originatorsArray) {
        final Set<Long> requestedOriginatorIds = new HashSet<>();

        for (final JsonElement element : originatorsArray) {
            if (!element.isJsonObject()) {
                throw new PlatformApiDataValidationException(List.of(ApiParameterError.parameterError(
                        "validation.msg.loan.originator.invalid.element", "Each originator entry must be a JSON object", "originators")));
            }

            final JsonObject jsonObject = element.getAsJsonObject();
            requestedOriginatorIds.add(resolveOrCreateOriginatorId(validator.validateAndExtract(jsonObject),
                    loanOriginatorHelper::findOrCreateOriginatorIdForLoanDisbursement));
        }

        return requestedOriginatorIds;
    }

    private void reconcileOriginatorMappings(final Long loanId, final Set<Long> requestedOriginatorIds) {
        final List<LoanOriginatorMapping> currentMappings = loanOriginatorMappingRepository.findByLoanId(loanId);

        final Map<Long, LoanOriginatorMapping> currentByOriginatorId = currentMappings.stream()
                .collect(Collectors.toMap(mapping -> mapping.getOriginator().getId(), Function.identity()));

        final List<LoanOriginatorMapping> toRemove = currentMappings.stream()
                .filter(mapping -> !requestedOriginatorIds.contains(mapping.getOriginator().getId())).toList();

        final List<LoanOriginatorMapping> toAdd = requestedOriginatorIds.stream()
                .filter(originatorId -> !currentByOriginatorId.containsKey(originatorId))
                .map(originatorId -> LoanOriginatorMapping.create(loanId, loanOriginatorRepository.getReferenceById(originatorId)))
                .toList();

        if (!toRemove.isEmpty()) {
            loanOriginatorMappingRepository.deleteAll(toRemove);
        }

        if (!toAdd.isEmpty()) {
            loanOriginatorMappingRepository.saveAll(toAdd);
        }
    }
}
