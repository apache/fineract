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
package org.apache.fineract.portfolio.tax.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepository;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.apache.fineract.portfolio.tax.serialization.TaxValidator;

@RequiredArgsConstructor
public class TaxWritePlatformServiceImpl implements TaxWritePlatformService {

    private final TaxValidator validator;
    private final TaxAssembler taxAssembler;
    private final TaxComponentRepository taxComponentRepository;
    private final TaxComponentRepositoryWrapper taxComponentRepositoryWrapper;
    private final TaxGroupRepository taxGroupRepository;
    private final TaxGroupRepositoryWrapper taxGroupRepositoryWrapper;

    @Override
    public CommandProcessingResult createTaxComponent(final JsonCommand command) {
        this.validator.validateForTaxComponentCreate(command.json());
        TaxComponent taxComponent = this.taxAssembler.assembleTaxComponentFrom(command);

        // Enforce unique tax component name (case-sensitive, relying on DB collation for case handling)
        if (taxComponent.getName() != null) {
            Optional<TaxComponent> existing = this.taxComponentRepository.findByName(taxComponent.getName());
            if (existing.isPresent()) {
                final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                String errorCode = "tax.component.name.must.be.unique";
                String userMessage = "Tax component with name '" + taxComponent.getName() + "' already exists";
                ApiParameterError error = ApiParameterError.parameterError(errorCode, userMessage, TaxApiConstants.nameParamName,
                        taxComponent.getName());
                dataValidationErrors.add(error);
                throw new PlatformApiDataValidationException(errorCode, userMessage, dataValidationErrors);
            }
        }

        this.taxComponentRepository.saveAndFlush(taxComponent);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(taxComponent.getId()) //
                .build();
    }

    @Override
    public CommandProcessingResult updateTaxComponent(final Long id, final JsonCommand command) {
        this.validator.validateForTaxComponentUpdate(command.json(), id);
        final TaxComponent taxComponent = this.taxComponentRepositoryWrapper.findOneWithNotFoundDetection(id);
        this.validator.validateStartDate(taxComponent.startDate(), command);

        // Enforce unique tax component name when name is being changed
        if (command.parameterExists(TaxApiConstants.nameParamName)) {
            final String newName = command.stringValueOfParameterNamed(TaxApiConstants.nameParamName);
            if (newName != null && !newName.equalsIgnoreCase(taxComponent.getName())) {
                Optional<TaxComponent> existing = this.taxComponentRepository.findByName(newName);
                if (existing.isPresent() && !existing.get().getId().equals(taxComponent.getId())) {
                    final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                    String errorCode = "tax.component.name.must.be.unique";
                    String userMessage = "Tax component with name '" + newName + "' already exists";
                    ApiParameterError error = ApiParameterError.parameterError(errorCode, userMessage, TaxApiConstants.nameParamName,
                            newName);
                    dataValidationErrors.add(error);
                    throw new PlatformApiDataValidationException(errorCode, userMessage, dataValidationErrors);
                }
            }
        }

        Map<String, Object> changes = taxComponent.update(command);
        this.validator.validateTaxComponentForUpdate(taxComponent);
        this.taxComponentRepository.saveAndFlush(taxComponent);
        return new CommandProcessingResultBuilder() //
                .withEntityId(id) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult createTaxGroup(final JsonCommand command) {
        this.validator.validateForTaxGroupCreate(command.json());
        final TaxGroup taxGroup = this.taxAssembler.assembleTaxGroupFrom(command);

        // Enforce unique tax group name (case-insensitive)
        if (taxGroup.getName() != null && this.taxGroupRepository.existsByNameIgnoreCase(taxGroup.getName())) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            String errorCode = "tax.group.name.must.be.unique";
            String userMessage = "Tax group with name '" + taxGroup.getName() + "' already exists";
            ApiParameterError error = ApiParameterError.parameterError(errorCode, userMessage, TaxApiConstants.nameParamName,
                    taxGroup.getName());
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException(errorCode, userMessage, dataValidationErrors);
        }

        this.validator.validateTaxGroup(taxGroup);
        this.taxGroupRepository.saveAndFlush(taxGroup);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(taxGroup.getId()) //
                .build();
    }

    @Override
    public CommandProcessingResult updateTaxGroup(final Long id, final JsonCommand command) {
        this.validator.validateForTaxGroupUpdate(command.json());
        final TaxGroup taxGroup = this.taxGroupRepositoryWrapper.findOneWithNotFoundDetection(id);

        // Validate unique tax group name (case-insensitive) BEFORE updating if name is being changed
        if (command.parameterExists(TaxApiConstants.nameParamName)) {
            final String newName = command.stringValueOfParameterNamed(TaxApiConstants.nameParamName);
            if (newName != null && !newName.equalsIgnoreCase(taxGroup.getName())
                    && this.taxGroupRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
                final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                String errorCode = "tax.group.name.must.be.unique";
                String userMessage = "Tax group with name '" + newName + "' already exists";
                ApiParameterError error = ApiParameterError.parameterError(errorCode, userMessage, TaxApiConstants.nameParamName, newName);
                dataValidationErrors.add(error);
                throw new PlatformApiDataValidationException(errorCode, userMessage, dataValidationErrors);
            }
        }

        final boolean isUpdate = true;
        Set<TaxGroupMappings> groupMappings = this.taxAssembler.assembleTaxGroupMappingsFrom(command, isUpdate);
        this.validator.validateTaxGroupEndDateAndTaxComponent(taxGroup, groupMappings);
        Map<String, Object> changes = taxGroup.update(command, groupMappings);
        this.validator.validateTaxGroup(taxGroup);
        this.taxGroupRepository.saveAndFlush(taxGroup);
        return new CommandProcessingResultBuilder() //
                .withEntityId(id) //
                .with(changes) //
                .build();
    }

}
