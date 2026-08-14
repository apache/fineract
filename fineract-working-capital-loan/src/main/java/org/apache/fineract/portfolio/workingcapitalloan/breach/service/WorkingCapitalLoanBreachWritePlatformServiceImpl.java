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
package org.apache.fineract.portfolio.workingcapitalloan.breach.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.common.service.Validator;
import org.apache.fineract.portfolio.workingcapitalloan.breach.data.WorkingCapitalLoanBreachRequest;
import org.apache.fineract.portfolio.workingcapitalloan.breach.domain.WorkingCapitalLoanBreach;
import org.apache.fineract.portfolio.workingcapitalloan.breach.exception.WorkingCapitalLoanBreachNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.breach.repository.WorkingCapitalLoanBreachRepository;
import org.apache.fineract.portfolio.workingcapitalloan.breach.validator.WorkingCapitalLoanBreachParseAndValidator;
import org.apache.fineract.portfolio.workingcapitalloan.loan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.product.domain.WorkingCapitalLoanBreachAmountCalculationType;
import org.apache.fineract.portfolio.workingcapitalloan.product.repository.WorkingCapitalLoanProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanBreachWritePlatformServiceImpl implements WorkingCapitalLoanBreachWritePlatformService {

    private final WorkingCapitalLoanBreachRepository workingCapitalBreachRepository;
    private final WorkingCapitalLoanBreachParseAndValidator dataValidator;
    private final WorkingCapitalLoanProductRepository workingCapitalLoanProductRepository;

    private static final String BREACH_FREQUENCY_PARAM = "breachFrequency";
    private static final String BREACH_FREQUENCY_TYPE_PARAM = "breachFrequencyType";
    private static final String BREACH_AMOUNT_CALCULATION_TYPE_PARAM = "breachAmountCalculationType";
    private static final String BREACH_AMOUNT_PARAM = "breachAmount";
    private static final String NAME_PARAM = "name";

    @Override
    @Transactional
    public CommandProcessingResult create(final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();
        final WorkingCapitalLoanBreachRequest data = dataValidator.validateAndParse(command);
        final WorkingCapitalLoanBreach created = createAndPersistBreach(data, changes);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(created.getId()).with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult update(final Long breachId, final JsonCommand command) {
        final WorkingCapitalLoanBreach existing = workingCapitalBreachRepository.findById(breachId)
                .orElseThrow(() -> new WorkingCapitalLoanBreachNotFoundException(breachId));

        final WorkingCapitalLoanBreachRequest data = dataValidator.validateAndParse(command);
        final Map<String, Object> changes = new HashMap<>();
        final WorkingCapitalLoanBreach updated = updateAndPersistBreach(existing, data, changes);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(updated.getId()).with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult delete(final JsonCommand command) {
        final Long breachId = command.entityId();
        final WorkingCapitalLoanBreach breach = workingCapitalBreachRepository.findById(breachId)
                .orElseThrow(() -> new WorkingCapitalLoanBreachNotFoundException(breachId));
        if (workingCapitalLoanProductRepository.existsByBreach(breach)) {
            throw new PlatformDataIntegrityException("error.msg.data.integrity.issue.entity.linked",
                    String.format("Data integrity issue with resource: %d", breachId));
        }
        workingCapitalBreachRepository.delete(breach);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(breachId).build();
    }

    private WorkingCapitalLoanBreach createAndPersistBreach(final String name, final Integer breachFrequency,
            final WorkingCapitalLoanPeriodFrequencyType breachFrequencyType,
            final WorkingCapitalLoanBreachAmountCalculationType breachAmountCalculationType, final BigDecimal breachAmount) {
        final WorkingCapitalLoanBreach breach = new WorkingCapitalLoanBreach();
        breach.setName(name);
        breach.setBreachFrequency(breachFrequency);
        breach.setBreachFrequencyType(breachFrequencyType);
        breach.setBreachAmountCalculationType(breachAmountCalculationType);
        breach.setBreachAmount(breachAmount);
        return workingCapitalBreachRepository.saveAndFlush(breach);
    }

    private WorkingCapitalLoanBreach createAndPersistBreach(final WorkingCapitalLoanBreachRequest request,
            final Map<String, Object> changes) {
        validateDuplicateName(request.name(), null);
        final WorkingCapitalLoanPeriodFrequencyType breachFrequencyType = request.breachFrequencyType() != null
                ? WorkingCapitalLoanPeriodFrequencyType.fromString(request.breachFrequencyType())
                : null;
        final WorkingCapitalLoanBreachAmountCalculationType breachAmountCalculationType = request.breachAmountCalculationType() != null
                ? WorkingCapitalLoanBreachAmountCalculationType.valueOf(request.breachAmountCalculationType())
                : null;

        final WorkingCapitalLoanBreach created = createAndPersistBreach(request.name(), request.breachFrequency(), breachFrequencyType,
                breachAmountCalculationType, request.breachAmount());

        changes.put(NAME_PARAM, created.getName());
        changes.put(BREACH_FREQUENCY_PARAM, created.getBreachFrequency());
        changes.put(BREACH_FREQUENCY_TYPE_PARAM, created.getBreachFrequencyType() != null ? created.getBreachFrequencyType().name() : null);
        changes.put(BREACH_AMOUNT_CALCULATION_TYPE_PARAM,
                created.getBreachAmountCalculationType() != null ? created.getBreachAmountCalculationType().name() : null);
        changes.put(BREACH_AMOUNT_PARAM, created.getBreachAmount());
        return created;
    }

    private WorkingCapitalLoanBreach updateAndPersistBreach(final WorkingCapitalLoanBreach item,
            final WorkingCapitalLoanBreachRequest request, final Map<String, Object> changes) {
        final Integer breachFrequency = request.breachFrequency();
        final String name = request.name();
        final WorkingCapitalLoanPeriodFrequencyType breachFrequencyType = request.breachFrequencyType() != null
                ? WorkingCapitalLoanPeriodFrequencyType.fromString(request.breachFrequencyType())
                : null;
        final WorkingCapitalLoanBreachAmountCalculationType breachAmountCalculationType = request.breachAmountCalculationType() != null
                ? WorkingCapitalLoanBreachAmountCalculationType.valueOf(request.breachAmountCalculationType())
                : null;
        final BigDecimal breachAmount = request.breachAmount();

        if (Validator.isChanged(name, item.getName())) {
            validateDuplicateName(name, item.getId());
            item.setName(name);
            changes.put(NAME_PARAM, name);
        }
        if (Validator.isChanged(breachFrequency, item.getBreachFrequency())) {
            item.setBreachFrequency(breachFrequency);
            changes.put(BREACH_FREQUENCY_PARAM, breachFrequency);
        }
        if (Validator.isChanged(breachFrequencyType, item.getBreachFrequencyType())) {
            item.setBreachFrequencyType(breachFrequencyType);
            changes.put(BREACH_FREQUENCY_TYPE_PARAM, breachFrequencyType != null ? breachFrequencyType.name() : null);
        }
        if (Validator.isChanged(breachAmountCalculationType, item.getBreachAmountCalculationType())) {
            item.setBreachAmountCalculationType(breachAmountCalculationType);
            changes.put(BREACH_AMOUNT_CALCULATION_TYPE_PARAM,
                    breachAmountCalculationType != null ? breachAmountCalculationType.name() : null);
        }
        if (Validator.isBigDecimalChanged(breachAmount, item.getBreachAmount())) {
            item.setBreachAmount(breachAmount);
            changes.put(BREACH_AMOUNT_PARAM, breachAmount);
        }

        return changes.isEmpty() ? item : workingCapitalBreachRepository.save(item);
    }

    private void validateDuplicateName(final String name, final Long currentId) {
        workingCapitalBreachRepository.findByName(name).ifPresent(existing -> {
            final boolean sameEntity = currentId != null && Objects.equals(existing.getId(), currentId);
            if (!sameEntity) {
                throw new PlatformDataIntegrityException("error.msg.data.integrity.issue.entity.duplicated",
                        "Data integrity issue with resource: " + existing.getId());
            }
        });
    }
}
