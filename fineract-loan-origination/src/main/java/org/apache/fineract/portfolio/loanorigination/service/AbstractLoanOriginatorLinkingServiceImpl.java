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
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.loanaccount.service.LoanOriginatorLinkingService;
import org.apache.fineract.portfolio.loanorigination.data.LoanApplicationOriginatorData;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorStatus;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotActiveException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotFoundException;
import org.apache.fineract.portfolio.loanorigination.serialization.LoanApplicationOriginatorDataValidator;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class AbstractLoanOriginatorLinkingServiceImpl implements LoanOriginatorLinkingService {

    private static final String SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION = "23";

    protected final LoanOriginatorRepository loanOriginatorRepository;
    protected final LoanApplicationOriginatorDataValidator validator;
    protected final LoanOriginatorHelper loanOriginatorHelper;

    public AbstractLoanOriginatorLinkingServiceImpl(LoanOriginatorRepository loanOriginatorRepository,
            LoanApplicationOriginatorDataValidator validator, LoanOriginatorHelper loanOriginatorHelper) {
        this.loanOriginatorRepository = loanOriginatorRepository;
        this.validator = validator;
        this.loanOriginatorHelper = loanOriginatorHelper;
    }

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
            final Long originatorId = resolveOrCreateOriginatorId(originatorData, loanOriginatorHelper::findOrCreateOriginatorId);

            if (attachedOriginatorIds.contains(originatorId)) {
                log.debug("Originator {} already attached to loan {}, skipping duplicate", originatorId, loanId);
                continue;
            }

            createAndSaveOriginatorMapping(loanId, originatorId);

            attachedOriginatorIds.add(originatorId);
        }
    }

    /**
     * Default no-op for modules that do not reconcile originators on disbursement. Loan-specific linking overrides
     * this.
     */
    @Override
    public void processOriginatorsForLoanDisbursement(final Long loanId, final JsonArray originatorsArray) {
        // no-op by default
    }

    protected abstract void createAndSaveOriginatorMapping(Long loanId, Long originatorId);

    /**
     * Resolves an originator by internal ID when provided, otherwise finds or creates it by external ID using the
     * supplied strategy (application-time vs disbursement-time creation rules).
     */
    protected Long resolveOrCreateOriginatorId(final LoanApplicationOriginatorData originatorData,
            final Function<LoanApplicationOriginatorData, Long> findOrCreateByExternalId) {
        if (originatorData.getId() != null) {
            final LoanOriginator originator = loanOriginatorRepository.findById(originatorData.getId())
                    .orElseThrow(() -> new LoanOriginatorNotFoundException(originatorData.getId()));
            if (originator.getStatus() != LoanOriginatorStatus.ACTIVE) {
                throw new LoanOriginatorNotActiveException(originator.getId(), originator.getStatus().getValue());
            }
            return originator.getId();
        }
        return findOrCreateOriginatorIdByExternalId(originatorData, findOrCreateByExternalId);
    }

    protected boolean isConstraintViolation(final DataAccessException e) {
        return e.getMostSpecificCause() instanceof SQLException sqlEx && sqlEx.getSQLState() != null
                && sqlEx.getSQLState().startsWith(SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
    }

    private Long findOrCreateOriginatorIdByExternalId(final LoanApplicationOriginatorData originatorData,
            final Function<LoanApplicationOriginatorData, Long> findOrCreateByExternalId) {
        try {
            return findOrCreateByExternalId.apply(originatorData);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            if (!isConstraintViolation(e)) {
                throw e;
            }
            // Another thread created the originator concurrently - retry
            return findOrCreateByExternalId.apply(originatorData);
        }
    }
}
