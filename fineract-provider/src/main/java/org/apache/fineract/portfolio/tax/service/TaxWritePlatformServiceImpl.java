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

import java.util.Map;
import java.util.Set;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepository;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.apache.fineract.portfolio.tax.serialization.TaxValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaxWritePlatformServiceImpl implements TaxWritePlatformService {

    private final TaxValidator validator;
    private final TaxAssembler taxAssembler;
    private final TaxComponentRepository taxComponentRepository;
    private final TaxComponentRepositoryWrapper taxComponentRepositoryWrapper;
    private final TaxGroupRepository taxGroupRepository;
    private final TaxGroupRepositoryWrapper taxGroupRepositoryWrapper;

    @Autowired
    public TaxWritePlatformServiceImpl(final TaxValidator validator, final TaxAssembler taxAssembler,
            final TaxComponentRepository taxComponentRepository, final TaxGroupRepository taxGroupRepository,
            final TaxComponentRepositoryWrapper taxComponentRepositoryWrapper, final TaxGroupRepositoryWrapper taxGroupRepositoryWrapper) {
        this.validator = validator;
        this.taxAssembler = taxAssembler;
        this.taxComponentRepository = taxComponentRepository;
        this.taxGroupRepository = taxGroupRepository;
        this.taxComponentRepositoryWrapper = taxComponentRepositoryWrapper;
        this.taxGroupRepositoryWrapper = taxGroupRepositoryWrapper;
    }

    @Override
    public CommandProcessingResult createTaxComponent(final JsonCommand command) {
        this.validator.validateForTaxComponentCreate(command.json());
        TaxComponent taxComponent = this.taxAssembler.assembleTaxComponentFrom(command);
        this.taxComponentRepository.save(taxComponent);
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
        this.taxComponentRepository.save(taxComponent);
        return new CommandProcessingResultBuilder() //
                .withEntityId(id) //
                .with(changes).build();
    }

    @Override
    public CommandProcessingResult createTaxGroup(final JsonCommand command) {
        this.validator.validateForTaxGroupCreate(command.json());
        final TaxGroup taxGroup = this.taxAssembler.assembleTaxGroupFrom(command);
        this.validator.validateTaxGroup(taxGroup);
        this.taxGroupRepository.save(taxGroup);
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
        this.taxGroupRepository.save(taxGroup);
        return new CommandProcessingResultBuilder() //
                .withEntityId(id) //
                .with(changes).build();
    }

}
