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

import jakarta.persistence.PersistenceException;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepository;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.apache.fineract.portfolio.tax.serialization.TaxValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

@Slf4j
@RequiredArgsConstructor
public class TaxWritePlatformServiceImpl implements TaxWritePlatformService {

    private static final String TAX_COMPONENT_NAME_CONSTRAINT = "uq_m_tax_component_name";
    private static final String TAX_GROUP_NAME_CONSTRAINT = "uq_m_tax_group_name";

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
        saveAndFlushTaxComponent(taxComponent, command);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(taxComponent.getId()) //
                .build();
    }

    @Override
    public CommandProcessingResult updateTaxComponent(final Long id, final JsonCommand command) {
        this.validator.validateForTaxComponentUpdate(command.json());
        final TaxComponent taxComponent = this.taxComponentRepositoryWrapper.findOneWithNotFoundDetection(id);
        this.validator.validateStartDate(taxComponent.startDate(), command);
        Map<String, Object> changes = taxComponent.update(command);
        this.validator.validateTaxComponentForUpdate(taxComponent);
        saveAndFlushTaxComponent(taxComponent, command);
        return new CommandProcessingResultBuilder() //
                .withEntityId(id) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult createTaxGroup(final JsonCommand command) {
        this.validator.validateForTaxGroupCreate(command.json());
        final TaxGroup taxGroup = this.taxAssembler.assembleTaxGroupFrom(command);
        this.validator.validateTaxGroup(taxGroup);
        saveAndFlushTaxGroup(taxGroup, command);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(taxGroup.getId()) //
                .build();
    }

    @Override
    public CommandProcessingResult updateTaxGroup(final Long id, final JsonCommand command) {
        this.validator.validateForTaxGroupUpdate(command.json());
        final TaxGroup taxGroup = this.taxGroupRepositoryWrapper.findOneWithNotFoundDetection(id);
        final boolean isUpdate = true;
        Set<TaxGroupMappings> groupMappings = this.taxAssembler.assembleTaxGroupMappingsFrom(command, isUpdate);
        this.validator.validateTaxGroupEndDateAndTaxComponent(taxGroup, groupMappings);
        Map<String, Object> changes = taxGroup.update(command, groupMappings);
        this.validator.validateTaxGroup(taxGroup);
        saveAndFlushTaxGroup(taxGroup, command);
        return new CommandProcessingResultBuilder() //
                .withEntityId(id) //
                .with(changes) //
                .build();
    }

    private void saveAndFlushTaxComponent(final TaxComponent taxComponent, final JsonCommand command) {
        try {
            this.taxComponentRepository.saveAndFlush(taxComponent);
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve, TAX_COMPONENT_NAME_CONSTRAINT, "tax.component",
                    "Tax component");
        } catch (final PersistenceException dve) {
            handleDataIntegrityIssues(command, ExceptionUtils.getRootCause(dve.getCause()), dve, TAX_COMPONENT_NAME_CONSTRAINT,
                    "tax.component", "Tax component");
        }
    }

    private void saveAndFlushTaxGroup(final TaxGroup taxGroup, final JsonCommand command) {
        try {
            this.taxGroupRepository.saveAndFlush(taxGroup);
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve, TAX_GROUP_NAME_CONSTRAINT, "tax.group", "Tax group");
        } catch (final PersistenceException dve) {
            handleDataIntegrityIssues(command, ExceptionUtils.getRootCause(dve.getCause()), dve, TAX_GROUP_NAME_CONSTRAINT, "tax.group",
                    "Tax group");
        }
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve,
            final String nameConstraint, final String errorCodeEntity, final String entityLabel) {
        final String message = realCause == null ? null : realCause.getMessage();
        if (message != null && message.toLowerCase().contains(nameConstraint)) {
            final String name = command.stringValueOfParameterNamed(TaxApiConstants.nameParamName);
            throw new PlatformDataIntegrityException("error.msg." + errorCodeEntity + ".duplicate.name",
                    entityLabel + " with name `" + name + "` already exists", TaxApiConstants.nameParamName, name);
        }
        log.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg." + errorCodeEntity + ".unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + message);
    }
}
