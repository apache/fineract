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
package org.apache.fineract.portfolio.workingcapitalloannearbreach.service;

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
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachRequest;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.domain.WorkingCapitalNearBreach;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.exception.WorkingCapitalNearBreachNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.repository.WorkingCapitalNearBreachRepository;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.validator.WorkingCapitalNearBreachParseAndValidator;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalNearBreachWritePlatformServiceImpl implements WorkingCapitalNearBreachWritePlatformService {

    private final WorkingCapitalNearBreachRepository repository;
    private final WorkingCapitalNearBreachParseAndValidator dataValidator;

    @Override
    @Transactional
    public CommandProcessingResult create(final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();
        final WorkingCapitalNearBreachRequest data = dataValidator.validateAndParse(command);
        final WorkingCapitalNearBreach created = createAndPersistNearBreach(data, changes);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(created.getId()).with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult update(final Long nearBreachId, final JsonCommand command) {
        final WorkingCapitalNearBreach existing = repository.findById(nearBreachId)
                .orElseThrow(() -> new WorkingCapitalNearBreachNotFoundException(nearBreachId));

        final WorkingCapitalNearBreachRequest data = dataValidator.validateAndParse(command);
        final Map<String, Object> changes = new HashMap<>();
        final WorkingCapitalNearBreach updated = updateAndPersistNearBreach(existing, data, changes);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(updated.getId()).with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult delete(final JsonCommand command) {
        final Long nearBreachId = command.entityId();
        if (!repository.existsById(nearBreachId)) {
            throw new WorkingCapitalNearBreachNotFoundException(nearBreachId);
        }
        repository.deleteById(nearBreachId);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(nearBreachId).build();
    }

    private WorkingCapitalNearBreach createAndPersistNearBreach(final WorkingCapitalNearBreachRequest request,
            final Map<String, Object> changes) {
        validateDuplicateName(request.nearBreachName(), null);
        final WorkingCapitalLoanPeriodFrequencyType nearBreachFrequencyType = request.nearBreachFrequencyType() != null
                ? WorkingCapitalLoanPeriodFrequencyType.fromString(request.nearBreachFrequencyType())
                : null;

        final WorkingCapitalNearBreach created = new WorkingCapitalNearBreach(request.nearBreachName(), request.nearBreachFrequency(),
                nearBreachFrequencyType, request.nearBreachThreshold());
        repository.saveAndFlush(created);

        changes.put(WorkingCapitalLoanProductConstants.nearBreachNameParamName, created.getName());
        changes.put(WorkingCapitalLoanProductConstants.nearBreachFrequencyParamName, created.getFrequency());
        changes.put(WorkingCapitalLoanProductConstants.nearBreachFrequencyTypeParamName,
                created.getFrequencyType() != null ? created.getFrequencyType().name() : null);
        changes.put(WorkingCapitalLoanProductConstants.nearBreachThresholdParamName, created.getThreshold());
        return created;
    }

    private WorkingCapitalNearBreach updateAndPersistNearBreach(final WorkingCapitalNearBreach item,
            final WorkingCapitalNearBreachRequest request, final Map<String, Object> changes) {
        final String nearBreachName = request.nearBreachName();
        final Integer nearBreachFrequency = request.nearBreachFrequency();
        final WorkingCapitalLoanPeriodFrequencyType nearBreachFrequencyType = request.nearBreachFrequencyType() != null
                ? WorkingCapitalLoanPeriodFrequencyType.fromString(request.nearBreachFrequencyType())
                : null;
        final BigDecimal nearBreachThreshold = request.nearBreachThreshold();

        if (Validator.isChanged(nearBreachName, item.getName())) {
            validateDuplicateName(nearBreachName, item.getId());
            item.setName(nearBreachName);
            changes.put(WorkingCapitalLoanProductConstants.nearBreachNameParamName, nearBreachName);
        }
        if (Validator.isChanged(nearBreachFrequency, item.getFrequency())) {
            item.setFrequency(nearBreachFrequency);
            changes.put(WorkingCapitalLoanProductConstants.nearBreachFrequencyParamName, nearBreachFrequency);
        }
        if (Validator.isChanged(nearBreachFrequencyType, item.getFrequencyType())) {
            item.setFrequencyType(nearBreachFrequencyType);
            changes.put(WorkingCapitalLoanProductConstants.nearBreachFrequencyTypeParamName,
                    nearBreachFrequencyType != null ? nearBreachFrequencyType.name() : null);
        }
        if (Validator.isBigDecimalChanged(nearBreachThreshold, item.getThreshold())) {
            item.setThreshold(nearBreachThreshold);
            changes.put(WorkingCapitalLoanProductConstants.nearBreachThresholdParamName, nearBreachThreshold);
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
