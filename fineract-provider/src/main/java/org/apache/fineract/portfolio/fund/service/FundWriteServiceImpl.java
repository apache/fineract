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
package org.apache.fineract.portfolio.fund.service;

import jakarta.persistence.PersistenceException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.fund.data.FundCreateRequest;
import org.apache.fineract.portfolio.fund.data.FundCreateResponse;
import org.apache.fineract.portfolio.fund.data.FundUpdateRequest;
import org.apache.fineract.portfolio.fund.data.FundUpdateResponse;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.exception.FundNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

@Slf4j
@RequiredArgsConstructor
public class FundWriteServiceImpl implements FundWriteService {

    private final FundRepository fundRepository;

    @Override
    @CacheEvict(value = "funds", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('fn')")
    public FundCreateResponse createFund(final FundCreateRequest request) {
        try {
            final Fund fund = Fund.createNew(request.getName(), request.getExternalId());

            fundRepository.saveAndFlush(fund);

            return FundCreateResponse.builder().resourceId(fund.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            throw handleDataIntegrityIssues(request.getName(), request.getExternalId(), e.getMostSpecificCause(), e);
        } catch (final PersistenceException e) {
            throw handleDataIntegrityIssues(request.getName(), request.getExternalId(), ExceptionUtils.getRootCause(e.getCause()), e);
        }
    }

    @Override
    @CacheEvict(value = "funds", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('fn')")
    public FundUpdateResponse updateFund(final FundUpdateRequest request) {
        final Fund fund = fundRepository.findById(request.getId()).orElseThrow(() -> new FundNotFoundException(request.getId()));

        try {
            final Map<String, Object> changes = applyChanges(fund, request);
            if (!changes.isEmpty()) {
                fundRepository.saveAndFlush(fund);
            }

            return FundUpdateResponse.builder().resourceId(fund.getId()).changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            throw handleDataIntegrityIssues(request.getName(), request.getExternalId(), e.getMostSpecificCause(), e);
        } catch (final PersistenceException e) {
            throw handleDataIntegrityIssues(request.getName(), request.getExternalId(), ExceptionUtils.getRootCause(e.getCause()), e);
        }
    }

    /**
     * Applies a partial update. A {@code null} value leaves the corresponding field untouched; an empty string clears
     * it.
     *
     * @return the changed parameters, keyed by API parameter name, holding the values as supplied by the caller
     */
    private Map<String, Object> applyChanges(final Fund fund, final FundUpdateRequest request) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(2);

        final String newName = request.getName();
        if (newName != null && !Objects.equals(StringUtils.defaultIfEmpty(newName, null), fund.getName())) {
            actualChanges.put(FundUpdateRequest.Fields.name, newName);
            fund.updateName(newName);
        }

        final String newExternalId = request.getExternalId();
        if (newExternalId != null && !Objects.equals(StringUtils.defaultIfEmpty(newExternalId, null), fund.getExternalId())) {
            actualChanges.put(FundUpdateRequest.Fields.externalId, newExternalId);
            fund.updateExternalId(newExternalId);
        }

        return actualChanges;
    }

    private RuntimeException handleDataIntegrityIssues(final String name, final String externalId, final Throwable realCause,
            final Exception e) {
        final String message = realCause == null ? "" : realCause.getMessage();
        if (message != null && message.contains("m_fund_external_id_key")) {
            return new PlatformDataIntegrityException("error.msg.fund.duplicate.externalId",
                    "A fund with external id '" + externalId + "' already exists", "externalId", externalId);
        } else if (message != null && message.contains("m_fund_external_id_name")) {
            return new PlatformDataIntegrityException("error.msg.fund.duplicate.name", "A fund with name '" + name + "' already exists",
                    "name", name);
        }

        log.error("Error occurred.", e);
        return ErrorHandler.getMappable(e, "error.msg.fund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + message);
    }
}
