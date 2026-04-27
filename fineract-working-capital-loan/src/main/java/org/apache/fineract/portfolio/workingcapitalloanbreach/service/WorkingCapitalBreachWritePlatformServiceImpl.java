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
package org.apache.fineract.portfolio.workingcapitalloanbreach.service;

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
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachRequest;
import org.apache.fineract.portfolio.workingcapitalloanbreach.domain.WorkingCapitalBreach;
import org.apache.fineract.portfolio.workingcapitalloanbreach.exception.WorkingCapitalBreachNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloanbreach.repository.WorkingCapitalBreachRepository;
import org.apache.fineract.portfolio.workingcapitalloanbreach.validator.WorkingCapitalBreachParseAndValidator;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalBreachAmountCalculationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalBreachWritePlatformServiceImpl implements WorkingCapitalBreachWritePlatformService {

    private final WorkingCapitalBreachRepository repository;
    private final WorkingCapitalBreachParseAndValidator dataValidator;

    private static final String BREACH_FREQUENCY_PARAM = "breachFrequency";
    private static final String BREACH_FREQUENCY_TYPE_PARAM = "breachFrequencyType";
    private static final String BREACH_AMOUNT_CALCULATION_TYPE_PARAM = "breachAmountCalculationType";
    private static final String BREACH_AMOUNT_PARAM = "breachAmount";
    private static final String NAME_PARAM = "name";

    @Override
    @Transactional
    public CommandProcessingResult create(final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();
        final WorkingCapitalBreachRequest data = dataValidator.validateAndParse(command);
        final WorkingCapitalBreach created = createAndPersistBreach(data, changes);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(created.getId()).with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult update(final Long breachId, final JsonCommand command) {
        final WorkingCapitalBreach existing = repository.findById(breachId)
                .orElseThrow(() -> new WorkingCapitalBreachNotFoundException(breachId));

        final WorkingCapitalBreachRequest data = dataValidator.validateAndParse(command);
        final Map<String, Object> changes = new HashMap<>();
        final WorkingCapitalBreach updated = updateAndPersistBreach(existing, data, changes);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(updated.getId()).with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult delete(final JsonCommand command) {
        final Long breachId = command.entityId();
        if (!repository.existsById(breachId)) {
            throw new WorkingCapitalBreachNotFoundException(breachId);
        }
        repository.deleteById(breachId);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(breachId).build();
    }

    private WorkingCapitalBreach createAndPersistBreach(final String name, final Integer breachFrequency,
            final WorkingCapitalLoanPeriodFrequencyType breachFrequencyType,
            final WorkingCapitalBreachAmountCalculationType breachAmountCalculationType, final BigDecimal breachAmount) {
        final WorkingCapitalBreach breach = new WorkingCapitalBreach();
        breach.setName(name);
        breach.setBreachFrequency(breachFrequency);
        breach.setBreachFrequencyType(breachFrequencyType);
        breach.setBreachAmountCalculationType(breachAmountCalculationType);
        breach.setBreachAmount(breachAmount);
        return repository.saveAndFlush(breach);
    }

    private WorkingCapitalBreach createAndPersistBreach(final WorkingCapitalBreachRequest request, final Map<String, Object> changes) {
        validateDuplicateName(request.name(), null);
        final WorkingCapitalLoanPeriodFrequencyType breachFrequencyType = request.breachFrequencyType() != null
                ? WorkingCapitalLoanPeriodFrequencyType.fromString(request.breachFrequencyType())
                : null;
        final WorkingCapitalBreachAmountCalculationType breachAmountCalculationType = request.breachAmountCalculationType() != null
                ? WorkingCapitalBreachAmountCalculationType.valueOf(request.breachAmountCalculationType())
                : null;

        final WorkingCapitalBreach created = createAndPersistBreach(request.name(), request.breachFrequency(), breachFrequencyType,
                breachAmountCalculationType, request.breachAmount());

        changes.put(NAME_PARAM, created.getName());
        changes.put(BREACH_FREQUENCY_PARAM, created.getBreachFrequency());
        changes.put(BREACH_FREQUENCY_TYPE_PARAM, created.getBreachFrequencyType() != null ? created.getBreachFrequencyType().name() : null);
        changes.put(BREACH_AMOUNT_CALCULATION_TYPE_PARAM,
                created.getBreachAmountCalculationType() != null ? created.getBreachAmountCalculationType().name() : null);
        changes.put(BREACH_AMOUNT_PARAM, created.getBreachAmount());
        return created;
    }

    private WorkingCapitalBreach updateAndPersistBreach(final WorkingCapitalBreach item, final WorkingCapitalBreachRequest request,
            final Map<String, Object> changes) {
        final Integer breachFrequency = request.breachFrequency();
        final String name = request.name();
        final WorkingCapitalLoanPeriodFrequencyType breachFrequencyType = request.breachFrequencyType() != null
                ? WorkingCapitalLoanPeriodFrequencyType.fromString(request.breachFrequencyType())
                : null;
        final WorkingCapitalBreachAmountCalculationType breachAmountCalculationType = request.breachAmountCalculationType() != null
                ? WorkingCapitalBreachAmountCalculationType.valueOf(request.breachAmountCalculationType())
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

        return changes.isEmpty() ? item : repository.save(item);
    }

    private void validateDuplicateName(final String name, final Long currentId) {
        repository.findByName(name).ifPresent(existing -> {
            final boolean sameEntity = currentId != null && Objects.equals(existing.getId(), currentId);
            if (!sameEntity) {
                throw new PlatformDataIntegrityException("error.msg.data.integrity.issue.entity.duplicated",
                        "Data integrity issue with resource: " + existing.getId());
            }
        });
    }
}
