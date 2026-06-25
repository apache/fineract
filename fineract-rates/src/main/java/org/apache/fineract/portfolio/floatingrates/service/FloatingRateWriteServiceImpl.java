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
package org.apache.fineract.portfolio.floatingrates.service;

import jakarta.persistence.PersistenceException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateCreateResponse;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateResponse;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import org.apache.fineract.portfolio.floatingrates.serialization.FloatingRateDataValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class FloatingRateWriteServiceImpl implements FloatingRateWriteService {

    private final FloatingRateDataValidator floatingRateDataValidator;
    private final FloatingRateRepositoryWrapper floatingRateRepository;

    @Transactional
    @Override
    public FloatingRateCreateResponse create(final FloatingRateRequest request) {
        try {
            this.floatingRateDataValidator.validateForCreate(request);
            final FloatingRate newFloatingRate = FloatingRate.createNew(request);
            this.floatingRateRepository.saveAndFlush(newFloatingRate);
            return FloatingRateCreateResponse.builder().resourceId(newFloatingRate.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(request.getName(), dve.getMostSpecificCause(), dve);
            return FloatingRateCreateResponse.builder().build();
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(request.getName(), throwable, dve);
            return FloatingRateCreateResponse.builder().build();
        }
    }

    @Transactional
    @Override
    public FloatingRateUpdateResponse update(final FloatingRateUpdateRequest request) {
        try {
            final FloatingRate floatingRateForUpdate = this.floatingRateRepository.findOneWithNotFoundDetection(request.getId());
            this.floatingRateDataValidator.validateForUpdate(request, floatingRateForUpdate);
            final Map<String, Object> changes = floatingRateForUpdate.update(request);

            if (!changes.isEmpty()) {
                this.floatingRateRepository.save(floatingRateForUpdate);
            }

            return FloatingRateUpdateResponse.builder().resourceId(request.getId()).changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(request.getName(), dve.getMostSpecificCause(), dve);
            return FloatingRateUpdateResponse.builder().build();
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(request.getName(), throwable, dve);
            return FloatingRateUpdateResponse.builder().build();
        }
    }

    private void handleDataIntegrityIssues(final String name, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("unq_name")) {
            throw new PlatformDataIntegrityException("error.msg.floatingrates.duplicate.name",
                    "Floating Rate with name `" + name + "` already exists", "name", name);
        }
        if (realCause.getMessage().contains("unq_rate_period")) {
            throw new PlatformDataIntegrityException("error.msg.floatingrates.duplicate.active.fromdate",
                    "Attempt to add multiple floating rate periods with same fromdate", "fromdate", "");
        }
        log.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.floatingrates.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
