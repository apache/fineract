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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
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
    private final ChargeRepository chargeRepository;
    private final GLAccountRepositoryWrapper glAccountRepositoryWrapper;

    @Override
    public CommandProcessingResult createTaxComponent(final JsonCommand command) {
        this.validator.validateForTaxComponentCreate(command.json());
        TaxComponent taxComponent = this.taxAssembler.assembleTaxComponentFrom(command);
        this.taxComponentRepository.saveAndFlush(taxComponent);
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

        // Enforce edit restrictions based on usage
        // Check if tax component is in use (linked to charges via tax groups)
        final boolean inUse = taxComponent.isInUse(() -> chargeRepository.existsByTaxGroupContainingTaxComponent(taxComponent.getId()));
        if (inUse) {
            validateRestrictedFieldsForInUseComponent(command);
        }

        // Load GLAccounts and convert account types if provided in the command
        GLAccountType debitAccountType = null;
        GLAccount debitAccount = null;
        GLAccountType creditAccountType = null;
        GLAccount creditAccount = null;

        if (command.parameterExists(TaxApiConstants.debitAccountTypeParamName)) {
            final Integer debitAccountTypeValue = command.integerValueSansLocaleOfParameterNamed(TaxApiConstants.debitAccountTypeParamName);
            if (debitAccountTypeValue != null) {
                debitAccountType = GLAccountType.fromInt(debitAccountTypeValue);
            }
        }

        if (command.parameterExists(TaxApiConstants.debitAccountIdParamName)) {
            final Long debitAccountId = command.longValueOfParameterNamed(TaxApiConstants.debitAccountIdParamName);
            if (debitAccountId != null) {
                debitAccount = this.glAccountRepositoryWrapper.findOneWithNotFoundDetection(debitAccountId);
            }
        }

        if (command.parameterExists(TaxApiConstants.creditAccountTypeParamName)) {
            final Integer creditAccountTypeValue = command
                    .integerValueSansLocaleOfParameterNamed(TaxApiConstants.creditAccountTypeParamName);
            if (creditAccountTypeValue != null) {
                creditAccountType = GLAccountType.fromInt(creditAccountTypeValue);
            }
        }

        if (command.parameterExists(TaxApiConstants.creditAccountIdParamName)) {
            final Long creditAccountId = command.longValueOfParameterNamed(TaxApiConstants.creditAccountIdParamName);
            if (creditAccountId != null) {
                creditAccount = this.glAccountRepositoryWrapper.findOneWithNotFoundDetection(creditAccountId);
            }
        }

        Map<String, Object> changes = taxComponent.update(command, debitAccountType, debitAccount, creditAccountType, creditAccount);
        this.validator.validateTaxComponentForUpdate(taxComponent);
        this.taxComponentRepository.saveAndFlush(taxComponent);
        return new CommandProcessingResultBuilder() //
                .withEntityId(id) //
                .with(changes) //
                .build();
    }

    /**
     * Validates that restricted fields are not being modified when tax component is in use. When a tax component is
     * linked to tax groups (and potentially used in transactions), only the name field can be modified. All other
     * fields (percentage, GL accounts, start date) must remain unchanged to maintain accounting integrity.
     *
     * @param command
     *            the JSON command containing update parameters
     * @throws PlatformApiDataValidationException
     *             if any restricted field is present in the update request
     */
    private void validateRestrictedFieldsForInUseComponent(final JsonCommand command) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tax.component");

        // Check if any restricted fields are being modified
        if (command.parameterExists(TaxApiConstants.percentageParamName)) {
            baseDataValidator.reset().parameter(TaxApiConstants.percentageParamName).failWithCode(
                    "only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions",
                    "Only name can be modified once tax component is linked or used in transactions.");
        }

        if (command.parameterExists(TaxApiConstants.startDateParamName)) {
            baseDataValidator.reset().parameter(TaxApiConstants.startDateParamName).failWithCode(
                    "only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions",
                    "Only name can be modified once tax component is linked or used in transactions.");
        }

        if (command.parameterExists(TaxApiConstants.debitAccountTypeParamName)) {
            baseDataValidator.reset().parameter(TaxApiConstants.debitAccountTypeParamName).failWithCode(
                    "only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions",
                    "Only name can be modified once tax component is linked or used in transactions.");
        }

        if (command.parameterExists(TaxApiConstants.debitAccountIdParamName)) {
            baseDataValidator.reset().parameter(TaxApiConstants.debitAccountIdParamName).failWithCode(
                    "only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions",
                    "Only name can be modified once tax component is linked or used in transactions.");
        }

        if (command.parameterExists(TaxApiConstants.creditAccountTypeParamName)) {
            baseDataValidator.reset().parameter(TaxApiConstants.creditAccountTypeParamName).failWithCode(
                    "only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions",
                    "Only name can be modified once tax component is linked or used in transactions.");
        }

        if (command.parameterExists(TaxApiConstants.creditAccountIdParamName)) {
            baseDataValidator.reset().parameter(TaxApiConstants.creditAccountIdParamName).failWithCode(
                    "only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions",
                    "Only name can be modified once tax component is linked or used in transactions.");
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    @Override
    public CommandProcessingResult createTaxGroup(final JsonCommand command) {
        this.validator.validateForTaxGroupCreate(command.json());
        final TaxGroup taxGroup = this.taxAssembler.assembleTaxGroupFrom(command);
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
