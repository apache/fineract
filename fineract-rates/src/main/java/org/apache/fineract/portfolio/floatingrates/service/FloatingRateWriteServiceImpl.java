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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateCreateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateCreateResponse;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateResponse;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRatePeriod;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class FloatingRateWriteServiceImpl implements FloatingRateWriteService {

    private final Validator validator;
    private final FloatingRateRepositoryWrapper floatingRateRepository;

    @Transactional
    @Override
    public FloatingRateCreateResponse create(final FloatingRateCreateRequest request) {
        try {
            validate(request);
            final boolean isBaseLendingRate = request.getIsBaseLendingRate() != null && request.getIsBaseLendingRate();
            final boolean isActive = request.getIsActive() == null || request.getIsActive();
            final FloatingRate newFloatingRate = new FloatingRate(request.getName(), isBaseLendingRate, isActive,
                    buildRatePeriods(request.getRatePeriods()));
            this.floatingRateRepository.saveAndFlush(newFloatingRate);
            return FloatingRateCreateResponse.builder().resourceId(newFloatingRate.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(request.getName(), dve.getMostSpecificCause(), dve);
            return FloatingRateCreateResponse.builder().build();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(request.getName(), throwable, dve);
            return FloatingRateCreateResponse.builder().build();
        }
    }

    @Transactional
    @Override
    public FloatingRateUpdateResponse update(final FloatingRateUpdateRequest request) {
        try {
            final FloatingRate floatingRateForUpdate = this.floatingRateRepository.findOneWithNotFoundDetection(request.getId());
            validate(request);
            final Map<String, Object> changes = applyChanges(floatingRateForUpdate, request);

            if (!changes.isEmpty()) {
                this.floatingRateRepository.save(floatingRateForUpdate);
            }

            return FloatingRateUpdateResponse.builder().resourceId(request.getId()).changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(request.getName(), dve.getMostSpecificCause(), dve);
            return FloatingRateUpdateResponse.builder().build();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(request.getName(), throwable, dve);
            return FloatingRateUpdateResponse.builder().build();
        }
    }

    private <T> void validate(final T request) {
        final Set<ConstraintViolation<T>> violations = this.validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private Map<String, Object> applyChanges(final FloatingRate floatingRate, final FloatingRateUpdateRequest request) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (request.getName() != null && !request.getName().equals(floatingRate.getName())) {
            floatingRate.setName(request.getName());
            actualChanges.put("name", request.getName());
        }

        if (request.getIsBaseLendingRate() != null && request.getIsBaseLendingRate() != floatingRate.isBaseLendingRate()) {
            floatingRate.setBaseLendingRate(request.getIsBaseLendingRate());
            actualChanges.put("isBaseLendingRate", request.getIsBaseLendingRate());
        }

        if (request.getIsActive() != null && request.getIsActive() != floatingRate.isActive()) {
            floatingRate.setActive(request.getIsActive());
            actualChanges.put("isActive", request.getIsActive());
        }

        final List<FloatingRatePeriod> newRatePeriods = buildRatePeriods(request.getRatePeriods());
        if (newRatePeriods != null && !newRatePeriods.isEmpty()) {
            floatingRate.replaceRatePeriods(newRatePeriods);
            actualChanges.put("ratePeriods", request.getRatePeriods());
        }

        return actualChanges;
    }

    private List<FloatingRatePeriod> buildRatePeriods(final List<FloatingRatePeriodRequest> ratePeriodRequests) {
        if (ratePeriodRequests == null) {
            return null;
        }
        final List<FloatingRatePeriod> ratePeriods = new ArrayList<>();
        for (final FloatingRatePeriodRequest ratePeriodRequest : ratePeriodRequests) {
            final boolean isDifferentialToBaseLendingRate = ratePeriodRequest.getIsDifferentialToBaseLendingRate() != null
                    && ratePeriodRequest.getIsDifferentialToBaseLendingRate();
            ratePeriods.add(new FloatingRatePeriod(ratePeriodRequest.fromDateAsLocalDate(), ratePeriodRequest.getInterestRate(),
                    isDifferentialToBaseLendingRate, true));
        }
        return ratePeriods;
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
