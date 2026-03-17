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
package org.apache.fineract.portfolio.workingcapitalloanproduct.service;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.exception.DelinquencyBucketNotFoundException;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.exception.FundNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductConfigurableAttributes;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductMinMaxConstraints;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductPaymentAllocationRule;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductDuplicateExternalIdException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductDuplicateNameException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductDuplicateShortNameException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductPaymentAllocationRuleRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.serialization.WorkingCapitalLoanProductDataValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanProductWritePlatformServiceImpl implements WorkingCapitalLoanProductWritePlatformService {

    private final WorkingCapitalLoanProductDataValidator validator;
    private final WorkingCapitalLoanProductRepository repository;
    private final WorkingCapitalLoanProductPaymentAllocationRuleRepository paymentAllocationRuleRepository;
    private final WorkingCapitalLoanProductUpdateUtil updateUtil;
    private final FundRepository fundRepository;
    private final DelinquencyBucketRepository delinquencyBucketRepository;
    private final WorkingCapitalAdvancedPaymentAllocationsJsonParser advancedPaymentAllocationsJsonParser;

    @Transactional
    @Override
    public CommandProcessingResult createWorkingCapitalLoanProduct(final JsonCommand command) {
        this.validator.validateForCreate(command.json());

        final Fund fund = findFundByIdIfProvided(command.parameterExists(WorkingCapitalLoanProductConstants.fundIdParamName)
                ? command.longValueOfParameterNamed(WorkingCapitalLoanProductConstants.fundIdParamName)
                : null);
        final DelinquencyBucket delinquencyBucket = findDelinquencyBucketByIdIfProvided(
                command.parameterExists(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName)
                        ? command.longValueOfParameterNamed(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName)
                        : null);
        final List<WorkingCapitalLoanProductPaymentAllocationRule> paymentAllocationRules = this.advancedPaymentAllocationsJsonParser
                .assembleWCPaymentAllocationRules(command);
        final WorkingCapitalLoanProduct product = createProductFromCommand(fund, delinquencyBucket, command, paymentAllocationRules);

        this.repository.saveAndFlush(product);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(product.getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateWorkingCapitalLoanProduct(final Long productId, final JsonCommand command) {
        final WorkingCapitalLoanProduct product = this.repository.findById(productId)
                .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(productId));

        this.validator.validateForUpdate(command.json());

        // Check for duplicates before updating (only if values are being changed)
        if (command.parameterExists(WorkingCapitalLoanProductConstants.externalIdParamName)) {
            final String externalIdValue = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.externalIdParamName);
            if (externalIdValue != null && !externalIdValue.isBlank()) {
                final ExternalId externalId = ExternalIdFactory.produce(externalIdValue);
                if (this.repository.existsByExternalId(externalId) && !externalId.equals(product.getExternalId())) {
                    throw new WorkingCapitalLoanProductDuplicateExternalIdException(externalIdValue);
                }
            }
        }

        if (command.parameterExists(WorkingCapitalLoanProductConstants.nameParamName)) {
            final String name = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.nameParamName);
            if (name != null && !name.isBlank() && this.repository.existsByName(name) && !name.equals(product.getName())) {
                throw new WorkingCapitalLoanProductDuplicateNameException(name);
            }
        }

        if (command.parameterExists(WorkingCapitalLoanProductConstants.shortNameParamName)) {
            final String shortName = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.shortNameParamName);
            if (shortName != null && !shortName.isBlank() && this.repository.existsByShortName(shortName)
                    && !shortName.equals(product.getShortName())) {
                throw new WorkingCapitalLoanProductDuplicateShortNameException(shortName);
            }
        }

        final Map<String, Object> changes = updateProductFields(product, command);

        if (!changes.isEmpty()) {
            this.repository.saveAndFlush(product);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(productId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteWorkingCapitalLoanProduct(final Long productId) {
        final WorkingCapitalLoanProduct product = this.repository.findById(productId)
                .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(productId));

        // TODO: Check if product is used in any loans (when Working Capital Loan entity is created)
        // if (isProductInUse(productId)) {
        // throw new WorkingCapitalLoanProductCannotBeDeletedException(productId);
        // }

        this.repository.delete(product);

        return new CommandProcessingResultBuilder() //
                .withEntityId(productId) //
                .build();
    }

    private Map<String, Object> updateProductFields(final WorkingCapitalLoanProduct product, final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();

        // Update Details category
        if (command.isChangeInStringParameterNamed(WorkingCapitalLoanProductConstants.nameParamName, product.getName())) {
            final String newValue = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.nameParamName);
            changes.put(WorkingCapitalLoanProductConstants.nameParamName, newValue);
            product.setName(newValue);
        }

        if (command.isChangeInStringParameterNamed(WorkingCapitalLoanProductConstants.shortNameParamName, product.getShortName())) {
            final String newValue = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.shortNameParamName);
            changes.put(WorkingCapitalLoanProductConstants.shortNameParamName, newValue);
            product.setShortName(newValue);
        }

        if (command.isChangeInStringParameterNamed(WorkingCapitalLoanProductConstants.descriptionParamName, product.getDescription())) {
            final String newValue = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.descriptionParamName);
            changes.put(WorkingCapitalLoanProductConstants.descriptionParamName, newValue);
            product.setDescription(newValue);
        }

        if (command.isChangeInLocalDateParameterNamed(WorkingCapitalLoanProductConstants.startDateParamName, product.getStartDate())) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(WorkingCapitalLoanProductConstants.startDateParamName);
            changes.put(WorkingCapitalLoanProductConstants.startDateParamName, newValue);
            product.setStartDate(newValue);
        }

        if (command.isChangeInLocalDateParameterNamed(WorkingCapitalLoanProductConstants.closeDateParamName, product.getCloseDate())) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(WorkingCapitalLoanProductConstants.closeDateParamName);
            changes.put(WorkingCapitalLoanProductConstants.closeDateParamName, newValue);
            product.setCloseDate(newValue);
        }

        if (command.isChangeInExternalIdParameterNamed(WorkingCapitalLoanProductConstants.externalIdParamName, product.getExternalId())) {
            final String externalIdValue = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.externalIdParamName);
            final ExternalId newValue = ExternalIdFactory.produce(externalIdValue);
            changes.put(WorkingCapitalLoanProductConstants.externalIdParamName, newValue != null ? newValue.getValue() : null);
            product.setExternalId(newValue);
        }

        // Update embedded details (via UpdateUtil; JsonCommand does not belong in entities)
        if (product.getCurrency() != null) {
            changes.putAll(updateUtil.updateCurrency(product, command));
        }
        if (product.getRelatedDetail() != null) {
            changes.putAll(updateUtil.updateRelatedDetail(product.getRelatedDetail(), command));
        }
        if (product.getMinMaxConstraints() != null) {
            changes.putAll(updateUtil.updateMinMaxConstraints(product.getMinMaxConstraints(), command));
        }

        // Update fund if changed
        final Long existingFundId = product.getFund() != null ? product.getFund().getId() : null;
        if (command.isChangeInLongParameterNamed(WorkingCapitalLoanProductConstants.fundIdParamName, existingFundId)) {
            final Long fundId = command.longValueOfParameterNamed(WorkingCapitalLoanProductConstants.fundIdParamName);
            final Fund fund = findFundByIdIfProvided(fundId);
            product.setFund(fund);
            changes.put(WorkingCapitalLoanProductConstants.fundIdParamName, fundId);
        }

        // Update delinquency bucket if changed
        final Long existingDelinquencyBucketId = product.getDelinquencyBucket() != null ? product.getDelinquencyBucket().getId() : null;
        if (command.isChangeInLongParameterNamed(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName,
                existingDelinquencyBucketId)) {
            final Long delinquencyBucketId = command
                    .longValueOfParameterNamed(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName);
            final DelinquencyBucket delinquencyBucket = findDelinquencyBucketByIdIfProvided(delinquencyBucketId);
            product.setDelinquencyBucket(delinquencyBucket);
            changes.put(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, delinquencyBucketId);
        }

        // Update payment allocation rules if changed
        if (command.parameterExists(WorkingCapitalLoanProductConstants.paymentAllocationParamName)) {
            final List<WorkingCapitalLoanProductPaymentAllocationRule> newRules = this.advancedPaymentAllocationsJsonParser
                    .assembleWCPaymentAllocationRules(command);
            if (newRules != null) {
                newRules.forEach(rule -> rule.setWcProduct(product));
                paymentAllocationRuleRepository.deleteAll(product.getPaymentAllocationRules());
                product.updatePaymentAllocationRules(newRules);
                changes.put(WorkingCapitalLoanProductConstants.paymentAllocationParamName,
                        command.jsonFragment(WorkingCapitalLoanProductConstants.paymentAllocationParamName));
            }
        }

        // Update configurable attributes if changed
        if (command.parameterExists(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName)) {
            if (product.getConfigurableAttributes() == null) {
                // Create new configurable attributes if they don't exist
                final WorkingCapitalLoanProductConfigurableAttributes configurableAttributes = createConfigurableAttributesFromCommand(
                        command);
                configurableAttributes.setWcProduct(product);
                product.setConfigurableAttributes(configurableAttributes);
                changes.put(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName,
                        command.jsonFragment(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName));
            } else {
                // Update existing configurable attributes
                final Map<String, Object> configChanges = updateUtil.updateConfigurableAttributes(product.getConfigurableAttributes(),
                        command);
                if (!configChanges.isEmpty()) {
                    changes.put(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName,
                            command.jsonFragment(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName));
                }
            }
        }

        return changes;
    }

    private WorkingCapitalLoanProduct createProductFromCommand(final Fund fund, final DelinquencyBucket delinquencyBucket,
            final JsonCommand command, final List<WorkingCapitalLoanProductPaymentAllocationRule> paymentAllocationRules) {
        // Details category
        final String name = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.nameParamName);
        final String shortName = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.shortNameParamName);
        final String description = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.descriptionParamName);
        final LocalDate startDate = command.localDateValueOfParameterNamed(WorkingCapitalLoanProductConstants.startDateParamName);
        final LocalDate closeDate = command.localDateValueOfParameterNamed(WorkingCapitalLoanProductConstants.closeDateParamName);
        final ExternalId externalId = command.parameterExists(WorkingCapitalLoanProductConstants.externalIdParamName)
                ? ExternalIdFactory.produce(command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.externalIdParamName))
                : null;

        // Currency category
        final String currencyCode = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.currencyCodeParamName);
        final Integer decimalPlace = command.integerValueOfParameterNamed(WorkingCapitalLoanProductConstants.digitsAfterDecimalParamName);
        final Integer currencyInMultiplesOf = command
                .integerValueOfParameterNamed(WorkingCapitalLoanProductConstants.inMultiplesOfParamName);
        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, decimalPlace, currencyInMultiplesOf);

        // Related detail (core product parameters)
        final String amortizationTypeValue = command
                .stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.amortizationTypeParamName);
        final WorkingCapitalAmortizationType amortizationType = WorkingCapitalAmortizationType.fromString(amortizationTypeValue);
        final BigDecimal flatPercentageAmount = command.parameterExists(WorkingCapitalLoanProductConstants.flatPercentageAmountParamName)
                ? command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.flatPercentageAmountParamName)
                : null;
        final Integer npvDayCount = command.integerValueOfParameterNamed(WorkingCapitalLoanProductConstants.npvDayCountParamName);
        final BigDecimal principal = command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.principalParamName);
        final BigDecimal periodPaymentRate = command
                .bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName);
        final Integer repaymentEvery = command.integerValueOfParameterNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName);
        final String repaymentFrequencyTypeValue = command
                .stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName);
        final WorkingCapitalLoanPeriodFrequencyType repaymentFrequencyType = WorkingCapitalLoanPeriodFrequencyType
                .fromString(repaymentFrequencyTypeValue);
        final BigDecimal discount = command.parameterExists(WorkingCapitalLoanProductConstants.discountParamName)
                ? command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.discountParamName)
                : null;
        final WorkingCapitalLoanProductRelatedDetail relatedDetail = new WorkingCapitalLoanProductRelatedDetail(amortizationType,
                flatPercentageAmount, npvDayCount, principal, periodPaymentRate, repaymentEvery, repaymentFrequencyType, discount);

        // Min/max constraints
        final BigDecimal minPrincipal = command.parameterExists(WorkingCapitalLoanProductConstants.minPrincipalParamName)
                ? command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.minPrincipalParamName)
                : null;
        final BigDecimal maxPrincipal = command.parameterExists(WorkingCapitalLoanProductConstants.maxPrincipalParamName)
                ? command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.maxPrincipalParamName)
                : null;
        final BigDecimal minPeriodPaymentRate = command.parameterExists(WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName)
                ? command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName)
                : null;
        final BigDecimal maxPeriodPaymentRate = command.parameterExists(WorkingCapitalLoanProductConstants.maxPeriodPaymentRateParamName)
                ? command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.maxPeriodPaymentRateParamName)
                : null;
        final WorkingCapitalLoanProductMinMaxConstraints minMaxConstraints = new WorkingCapitalLoanProductMinMaxConstraints(minPrincipal,
                maxPrincipal, minPeriodPaymentRate, maxPeriodPaymentRate);

        // Configurable attributes
        final WorkingCapitalLoanProductConfigurableAttributes configurableAttributes = createConfigurableAttributesFromCommand(command);

        return new WorkingCapitalLoanProduct(name, shortName, externalId, fund, delinquencyBucket, startDate, closeDate, description,
                currency, relatedDetail, minMaxConstraints, paymentAllocationRules, configurableAttributes);
    }

    private WorkingCapitalLoanProductConfigurableAttributes createConfigurableAttributesFromCommand(final JsonCommand command) {
        Boolean flatPercentageAmount = null;
        Boolean delinquencyBucketClassification = null;
        Boolean discountDefault = null;
        Boolean periodPaymentFrequency = null;
        Boolean periodPaymentFrequencyType = null;

        if (command.parameterExists(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName)) {
            final JsonObject allowOverrides = command.parsedJson().getAsJsonObject()
                    .getAsJsonObject(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName);
            if (allowOverrides != null && !allowOverrides.isJsonNull()) {
                if (allowOverrides.has(WorkingCapitalLoanProductConstants.flatPercentageAmountOverridableParamName)
                        && !allowOverrides.get(WorkingCapitalLoanProductConstants.flatPercentageAmountOverridableParamName).isJsonNull()) {
                    flatPercentageAmount = allowOverrides.get(WorkingCapitalLoanProductConstants.flatPercentageAmountOverridableParamName)
                            .getAsBoolean();
                }
                if (allowOverrides.has(WorkingCapitalLoanProductConstants.delinquencyBucketClassificationOverridableParamName)
                        && !allowOverrides.get(WorkingCapitalLoanProductConstants.delinquencyBucketClassificationOverridableParamName)
                                .isJsonNull()) {
                    delinquencyBucketClassification = allowOverrides
                            .get(WorkingCapitalLoanProductConstants.delinquencyBucketClassificationOverridableParamName).getAsBoolean();
                }
                if (allowOverrides.has(WorkingCapitalLoanProductConstants.discountDefaultOverridableParamName)
                        && !allowOverrides.get(WorkingCapitalLoanProductConstants.discountDefaultOverridableParamName).isJsonNull()) {
                    discountDefault = allowOverrides.get(WorkingCapitalLoanProductConstants.discountDefaultOverridableParamName)
                            .getAsBoolean();
                }
                if (allowOverrides.has(WorkingCapitalLoanProductConstants.periodPaymentFrequencyOverridableParamName) && !allowOverrides
                        .get(WorkingCapitalLoanProductConstants.periodPaymentFrequencyOverridableParamName).isJsonNull()) {
                    periodPaymentFrequency = allowOverrides
                            .get(WorkingCapitalLoanProductConstants.periodPaymentFrequencyOverridableParamName).getAsBoolean();
                }
                if (allowOverrides.has(WorkingCapitalLoanProductConstants.periodPaymentFrequencyTypeOverridableParamName) && !allowOverrides
                        .get(WorkingCapitalLoanProductConstants.periodPaymentFrequencyTypeOverridableParamName).isJsonNull()) {
                    periodPaymentFrequencyType = allowOverrides
                            .get(WorkingCapitalLoanProductConstants.periodPaymentFrequencyTypeOverridableParamName).getAsBoolean();
                }
            }
        }

        final WorkingCapitalLoanProductConfigurableAttributes configurableAttributes = new WorkingCapitalLoanProductConfigurableAttributes();
        configurableAttributes.setFlatPercentageAmount(flatPercentageAmount);
        configurableAttributes.setDelinquencyBucketClassification(delinquencyBucketClassification);
        configurableAttributes.setDiscountDefault(discountDefault);
        configurableAttributes.setPeriodPaymentFrequency(periodPaymentFrequency);
        configurableAttributes.setPeriodPaymentFrequencyType(periodPaymentFrequencyType);
        return configurableAttributes;
    }

    private Fund findFundByIdIfProvided(final Long fundId) {
        if (fundId == null) {
            return null;
        }
        return this.fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId));
    }

    private DelinquencyBucket findDelinquencyBucketByIdIfProvided(final Long delinquencyBucketId) {
        if (delinquencyBucketId == null) {
            return null;
        }
        return this.delinquencyBucketRepository.findById(delinquencyBucketId)
                .orElseThrow(() -> DelinquencyBucketNotFoundException.notFound(delinquencyBucketId));
    }

}
