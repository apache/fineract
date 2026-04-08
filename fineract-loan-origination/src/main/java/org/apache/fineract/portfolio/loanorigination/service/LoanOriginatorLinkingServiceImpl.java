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
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.loanaccount.service.LoanOriginatorLinkingService;
import org.apache.fineract.portfolio.loanorigination.data.LoanApplicationOriginatorData;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMapping;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMappingRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorStatus;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotActiveException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotFoundException;
import org.apache.fineract.portfolio.loanorigination.serialization.LoanApplicationOriginatorDataValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
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

    private static final String SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION = "23";

    private final LoanOriginatorRepository loanOriginatorRepository;
    private final LoanOriginatorMappingRepository loanOriginatorMappingRepository;
    private final LoanApplicationOriginatorDataValidator validator;
    private final LoanOriginatorHelper loanOriginatorHelper;

    @Transactional
    @Override
    public void processOriginatorsForLoanApplication(final Long loanId, final JsonArray originatorsArray) {
        if (originatorsArray == null || originatorsArray.isEmpty()) {
            return;
        }

        log.debug("Processing {} originators for loan application {}", originatorsArray.size(), loanId);

        final Set<Long> attachedOriginatorIds = new HashSet<>();

        for (final JsonElement element : originatorsArray) {
            if (!element.isJsonObject()) {
                continue;
            }

            final JsonObject jsonObject = element.getAsJsonObject();
            final LoanApplicationOriginatorData originatorData = validator.validateAndExtract(jsonObject);
            final Long originatorId = resolveOrCreateOriginatorId(originatorData);

            if (attachedOriginatorIds.contains(originatorId)) {
                log.debug("Originator {} already attached to loan {}, skipping duplicate", originatorId, loanId);
                continue;
            }

            if (!loanOriginatorMappingRepository.existsByLoanIdAndOriginatorId(loanId, originatorId)) {
                final LoanOriginator originatorRef = loanOriginatorRepository.getReferenceById(originatorId);
                final LoanOriginatorMapping mapping = LoanOriginatorMapping.create(loanId, originatorRef);
                loanOriginatorMappingRepository.save(mapping);
                log.debug("Attached originator {} to loan {}", originatorId, loanId);
            }

            attachedOriginatorIds.add(originatorId);
        }
    }

    private Long resolveOrCreateOriginatorId(final LoanApplicationOriginatorData originatorData) {
        if (originatorData.getId() != null) {
            final LoanOriginator originator = loanOriginatorRepository.findById(originatorData.getId())
                    .orElseThrow(() -> new LoanOriginatorNotFoundException(originatorData.getId()));
            if (originator.getStatus() != LoanOriginatorStatus.ACTIVE) {
                throw new LoanOriginatorNotActiveException(originator.getId(), originator.getStatus().getValue());
            }
            return originator.getId();
        }
        return findOrCreateOriginatorIdByExternalId(originatorData);
    }

    private Long findOrCreateOriginatorIdByExternalId(final LoanApplicationOriginatorData originatorData) {
        try {
            return loanOriginatorHelper.findOrCreateOriginatorId(originatorData);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            if (!isConstraintViolation(e)) {
                throw e;
            }
            // Another thread created the originator concurrently - retry
            return loanOriginatorHelper.findOrCreateOriginatorId(originatorData);
        }
    }

    private boolean isConstraintViolation(final DataAccessException e) {
        return e.getMostSpecificCause() instanceof SQLException sqlEx && sqlEx.getSQLState() != null
                && sqlEx.getSQLState().startsWith(SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
    }
}
