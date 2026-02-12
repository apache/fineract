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
package org.apache.fineract.portfolio.workingcapitalloanproduct.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductDuplicateExternalIdException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductDuplicateNameException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductDuplicateShortNameException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductRepository;
import org.springframework.stereotype.Component;

/**
 * Validator for Working Capital Loan Product data.
 */
@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanProductDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final WorkingCapitalLoanProductRepository repository;
    private final WorkingCapitalAdvancedPaymentAllocationsJsonParser advancedPaymentAllocationsJsonParser;

    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanProductConstants.nameParamName, WorkingCapitalLoanProductConstants.shortNameParamName,
            WorkingCapitalLoanProductConstants.descriptionParamName, WorkingCapitalLoanProductConstants.fundIdParamName,
            WorkingCapitalLoanProductConstants.startDateParamName, WorkingCapitalLoanProductConstants.closeDateParamName,
            WorkingCapitalLoanProductConstants.externalIdParamName, WorkingCapitalLoanProductConstants.currencyCodeParamName,
            WorkingCapitalLoanProductConstants.digitsAfterDecimalParamName, WorkingCapitalLoanProductConstants.inMultiplesOfParamName,
            WorkingCapitalLoanProductConstants.amortizationTypeParamName, WorkingCapitalLoanProductConstants.flatPercentageAmountParamName,
            WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, WorkingCapitalLoanProductConstants.npvDayCountParamName,
            WorkingCapitalLoanProductConstants.paymentAllocationParamName, WorkingCapitalLoanProductConstants.minPrincipalParamName,
            WorkingCapitalLoanProductConstants.principalParamName, WorkingCapitalLoanProductConstants.maxPrincipalParamName,
            WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName, WorkingCapitalLoanProductConstants.periodPaymentRateParamName,
            WorkingCapitalLoanProductConstants.maxPeriodPaymentRateParamName, WorkingCapitalLoanProductConstants.discountParamName,
            WorkingCapitalLoanProductConstants.repaymentEveryParamName, WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName,
            WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName));

    private static final Set<String> SUPPORTED_PAYMENT_ALLOCATION_RULE_PARAMS = new HashSet<>(
            Arrays.asList("transactionType", "paymentAllocationOrder"));
    private static final Set<String> SUPPORTED_PAYMENT_ALLOCATION_ORDER_PARAMS = new HashSet<>(
            Arrays.asList("paymentAllocationRule", "order"));

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanProductConstants.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        // Validate dates
        validateInputDates(element, baseDataValidator);

        // Validate Details category
        final String name = validateDetailsFields(element, baseDataValidator, true);
        final String shortName = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.shortNameParamName, element);

        // Validate Currency category
        validateCurrencyFields(element, baseDataValidator, true);

        // Validate Settings category
        validateSettingsFields(element, baseDataValidator, true);

        // Validate payment allocation (required on create – must be provided and non-empty)
        if (!this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.paymentAllocationParamName, element)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.paymentAllocationParamName).value(null).notNull();
        } else {
            validatePaymentAllocationParameters(element, baseDataValidator);
            final JsonCommand command = JsonCommand.fromJsonElement(null, element, this.fromApiJsonHelper);
            this.advancedPaymentAllocationsJsonParser.assembleWCPaymentAllocationRules(command);
        }

        // Validate Term category
        final BigDecimal principal = validateTermFields(element, baseDataValidator, true);

        // Validate min/max ranges
        validateMinMaxRanges(element, baseDataValidator, principal);

        // Validate configurable attributes if present
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName, element)) {
            validateConfigurableAttributes(element, baseDataValidator);
        }

        // Throw validation errors if any exist
        throwExceptionIfValidationWarningsExist(dataValidationErrors);

        // Check for duplicates
        final String externalIdValue = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.externalIdParamName,
                element);
        if (externalIdValue != null && !externalIdValue.isBlank()) {
            final ExternalId externalId = ExternalIdFactory.produce(externalIdValue);
            if (this.repository.existsByExternalId(externalId)) {
                throw new WorkingCapitalLoanProductDuplicateExternalIdException(externalIdValue);
            }
        }

        if (this.repository.existsByName(name)) {
            throw new WorkingCapitalLoanProductDuplicateNameException(name);
        }

        if (this.repository.existsByShortName(shortName)) {
            throw new WorkingCapitalLoanProductDuplicateShortNameException(shortName);
        }
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanProductConstants.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        // Validate dates
        validateInputDates(element, baseDataValidator);

        // Validate only fields that are present in the request (partial update support)
        validateDetailsFields(element, baseDataValidator, false);

        validateCurrencyFields(element, baseDataValidator, false);

        validateSettingsFields(element, baseDataValidator, false);

        // Validate payment allocation if present
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.paymentAllocationParamName, element)) {
            validatePaymentAllocationParameters(element, baseDataValidator);
            final JsonCommand command = JsonCommand.fromJsonElement(null, element, this.fromApiJsonHelper);
            this.advancedPaymentAllocationsJsonParser.assembleWCPaymentAllocationRules(command);
        }

        // Validate Term fields if present
        final BigDecimal principal = validateTermFields(element, baseDataValidator, false);

        // Validate configurable attributes if present
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName, element)) {
            validateConfigurableAttributes(element, baseDataValidator);
        }

        // Validate min/max constraints if present
        validateMinMaxRanges(element, baseDataValidator, principal);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateConfigurableAttributes(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName, element)) {
            final JsonObject allowOverrides = element.getAsJsonObject()
                    .getAsJsonObject(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName);
            if (allowOverrides != null && !allowOverrides.isJsonNull()) {
                final Set<String> supportedAttributes = getSupportedConfigurableAttributes();

                // Check for unsupported parameters
                this.fromApiJsonHelper.checkForUnsupportedNestedParameters(
                        WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName, allowOverrides, supportedAttributes);

                // Validate boolean values
                for (final String attribute : supportedAttributes) {
                    if (this.fromApiJsonHelper.parameterExists(attribute, allowOverrides)) {
                        final Boolean attributeValue = this.fromApiJsonHelper.extractBooleanNamed(attribute, allowOverrides);
                        baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName)
                                .value(attributeValue).notNull().validateForBooleanValue();
                    }
                }
            }
        }
    }

    private void validatePaymentAllocationParameters(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        if (!element.isJsonObject()) {
            return;
        }
        final JsonElement paymentAllocationElement = element.getAsJsonObject()
                .get(WorkingCapitalLoanProductConstants.paymentAllocationParamName);
        if (paymentAllocationElement == null || !paymentAllocationElement.isJsonArray()) {
            return;
        }
        final String orderParamPath = WorkingCapitalLoanProductConstants.paymentAllocationParamName + ".paymentAllocationOrder";
        for (final JsonElement ruleEl : paymentAllocationElement.getAsJsonArray()) {
            if (ruleEl == null || !ruleEl.isJsonObject()) {
                continue;
            }
            final JsonObject rule = ruleEl.getAsJsonObject();
            this.fromApiJsonHelper.checkForUnsupportedNestedParameters(WorkingCapitalLoanProductConstants.paymentAllocationParamName, rule,
                    SUPPORTED_PAYMENT_ALLOCATION_RULE_PARAMS);
            // Mandatory: transactionType
            final String transactionType = this.fromApiJsonHelper.extractStringNamed("transactionType", rule);
            baseDataValidator.reset().parameter("paymentAllocation.transactionType").value(transactionType).notBlank();
            // Mandatory: paymentAllocationOrder (must be present and array)
            final JsonElement orderEl = rule.get("paymentAllocationOrder");
            if (orderEl == null) {
                baseDataValidator.reset().parameter("paymentAllocation.paymentAllocationOrder").value(null).notNull();
            } else if (!orderEl.isJsonArray()) {
                baseDataValidator.reset().parameter("paymentAllocation.paymentAllocationOrder").failWithCode("must.be.array",
                        "paymentAllocationOrder must be an array");
            } else {
                for (final JsonElement orderItemEl : orderEl.getAsJsonArray()) {
                    if (orderItemEl == null || !orderItemEl.isJsonObject()) {
                        continue;
                    }
                    final JsonObject orderItem = orderItemEl.getAsJsonObject();
                    this.fromApiJsonHelper.checkForUnsupportedNestedParameters(orderParamPath, orderItem,
                            SUPPORTED_PAYMENT_ALLOCATION_ORDER_PARAMS);
                    // Mandatory: paymentAllocationRule, order
                    final String paymentAllocationRule = this.fromApiJsonHelper.extractStringNamed("paymentAllocationRule", orderItem);
                    baseDataValidator.reset().parameter(orderParamPath + ".paymentAllocationRule").value(paymentAllocationRule).notBlank();
                    final Integer order = this.fromApiJsonHelper.extractIntegerNamed("order", orderItem, Locale.getDefault());
                    baseDataValidator.reset().parameter(orderParamPath + ".order").value(order).notNull();
                }
            }
        }
    }

    private Set<String> getSupportedConfigurableAttributes() {
        final Set<String> supportedAttributes = new HashSet<>();
        supportedAttributes.add(WorkingCapitalLoanProductConstants.flatPercentageAmountOverridableParamName);
        supportedAttributes.add(WorkingCapitalLoanProductConstants.delinquencyBucketClassificationOverridableParamName);
        supportedAttributes.add(WorkingCapitalLoanProductConstants.discountDefaultOverridableParamName);
        supportedAttributes.add(WorkingCapitalLoanProductConstants.periodPaymentFrequencyOverridableParamName);
        supportedAttributes.add(WorkingCapitalLoanProductConstants.periodPaymentFrequencyTypeOverridableParamName);
        return supportedAttributes;
    }

    private String validateDetailsFields(final JsonElement element, final DataValidatorBuilder baseDataValidator, final boolean required) {
        final String name;
        if (required || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.nameParamName, element)) {
            name = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.nameParamName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.nameParamName).value(name).notBlank()
                    .notExceedingLengthOf(100);
        } else {
            name = null;
        }

        if (required || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.shortNameParamName, element)) {
            final String shortName = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.shortNameParamName,
                    element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.shortNameParamName).value(shortName).notBlank()
                    .notExceedingLengthOf(4);
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.descriptionParamName,
                    element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.descriptionParamName).value(description)
                    .notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.fundIdParamName, element)) {
            final Long fundId = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.fundIdParamName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.fundIdParamName).value(fundId).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        return name;
    }

    private void validateCurrencyFields(final JsonElement element, final DataValidatorBuilder baseDataValidator, final boolean required) {
        if (required || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.currencyCodeParamName, element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.currencyCodeParamName,
                    element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.currencyCodeParamName).value(currencyCode).notBlank()
                    .notExceedingLengthOf(3);
        }

        if (required || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.digitsAfterDecimalParamName, element)) {
            final Integer decimalPlace = this.fromApiJsonHelper
                    .extractIntegerNamed(WorkingCapitalLoanProductConstants.digitsAfterDecimalParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.digitsAfterDecimalParamName).value(decimalPlace)
                    .notNull().inMinMaxRange(0, 6);
        }

        if (required || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.inMultiplesOfParamName, element)) {
            final Integer currencyInMultiplesOf = this.fromApiJsonHelper
                    .extractIntegerNamed(WorkingCapitalLoanProductConstants.inMultiplesOfParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.inMultiplesOfParamName).value(currencyInMultiplesOf)
                    .notNull().integerZeroOrGreater();
        }
    }

    private void validateSettingsFields(final JsonElement element, final DataValidatorBuilder baseDataValidator, final boolean required) {
        final String amortizationTypeValue;
        if (required || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.amortizationTypeParamName, element)) {
            amortizationTypeValue = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.amortizationTypeParamName,
                    element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.amortizationTypeParamName).value(amortizationTypeValue)
                    .notBlank();
            if (amortizationTypeValue != null && !amortizationTypeValue.isBlank()) {
                final WorkingCapitalAmortizationType amortizationType = WorkingCapitalAmortizationType.fromString(amortizationTypeValue);
                if (amortizationType == null) {
                    baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.amortizationTypeParamName)
                            .failWithCode("invalid.amortization.type");
                } else {
                    // If FLAT is selected, flatPercentageAmount is mandatory
                    if (amortizationType.isFLAT()) {
                        final BigDecimal flatPercentageAmount = this.fromApiJsonHelper.extractBigDecimalNamed(
                                WorkingCapitalLoanProductConstants.flatPercentageAmountParamName, element, new HashSet<>());
                        baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.flatPercentageAmountParamName)
                                .value(flatPercentageAmount).notNull().zeroOrPositiveAmount();
                    }
                }
            }
        }

        if (required || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.npvDayCountParamName, element)) {
            final Integer npvDayCount = this.fromApiJsonHelper.extractIntegerNamed(WorkingCapitalLoanProductConstants.npvDayCountParamName,
                    element, Locale.getDefault());
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.npvDayCountParamName).value(npvDayCount).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, element)) {
            final Long delinquencyBucketClassificationId = this.fromApiJsonHelper
                    .extractLongNamed(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName)
                    .value(delinquencyBucketClassificationId).ignoreIfNull().integerGreaterThanZero();
        }

    }

    private BigDecimal validateTermFields(final JsonElement element, final DataValidatorBuilder baseDataValidator, final boolean required) {
        final BigDecimal principal;
        if (required || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.principalParamName, element)) {
            principal = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.principalParamName, element,
                    new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.principalParamName).value(principal).notNull()
                    .positiveAmount();
        } else {
            principal = null;
        }

        if (required || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element)) {
            final BigDecimal periodPaymentRateParamName = this.fromApiJsonHelper
                    .extractBigDecimalNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element, new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.periodPaymentRateParamName)
                    .value(periodPaymentRateParamName).notNull().zeroOrPositiveAmount();
        }

        if (required || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element)) {
            final Integer periodPaymentFrequency = this.fromApiJsonHelper
                    .extractIntegerNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.repaymentEveryParamName).value(periodPaymentFrequency)
                    .notNull().integerGreaterThanZero();
        }

        if (required
                || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element)) {
            final String repaymentFrequencyTypeValue = this.fromApiJsonHelper
                    .extractStringNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName)
                    .value(repaymentFrequencyTypeValue).notBlank();
            if (repaymentFrequencyTypeValue != null && !repaymentFrequencyTypeValue.isBlank()) {
                final WorkingCapitalLoanPeriodFrequencyType repaymentFrequencyType = WorkingCapitalLoanPeriodFrequencyType
                        .fromString(repaymentFrequencyTypeValue);
                if (repaymentFrequencyType == null) {
                    baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName)
                            .failWithCode("invalid.period.frequency.type");
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.discountParamName, element)) {
            final BigDecimal discount = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.discountParamName,
                    element, new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.discountParamName).value(discount).ignoreIfNull()
                    .zeroOrPositiveAmount();
        }

        return principal;
    }

    private void validateMinMaxRanges(final JsonElement element, final DataValidatorBuilder baseDataValidator, final BigDecimal principal) {
        final BigDecimal minPrincipal = this.fromApiJsonHelper
                .parameterExists(WorkingCapitalLoanProductConstants.minPrincipalParamName, element)
                        ? this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.minPrincipalParamName, element,
                                new HashSet<>())
                        : null;
        final BigDecimal maxPrincipal = this.fromApiJsonHelper
                .parameterExists(WorkingCapitalLoanProductConstants.maxPrincipalParamName, element)
                        ? this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.maxPrincipalParamName, element,
                                new HashSet<>())
                        : null;

        // Validate min/max values if provided (as per LoanProduct logic)
        if (minPrincipal != null) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.minPrincipalParamName).value(minPrincipal).ignoreIfNull()
                    .positiveAmount();
        }
        if (maxPrincipal != null) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.maxPrincipalParamName).value(maxPrincipal).ignoreIfNull()
                    .positiveAmount();
        }

        if (minPrincipal != null && maxPrincipal != null) {
            if (MathUtil.isGreaterThan(minPrincipal, maxPrincipal)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.minPrincipalParamName)
                        .failWithCode("must.be.less.than.or.equal.to.max");
            }
        }
        if (principal != null && minPrincipal != null) {
            if (MathUtil.isLessThan(principal, minPrincipal)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.principalParamName)
                        .failWithCode("must.be.greater.than.or.equal.to.min");
            }
        }
        if (principal != null && maxPrincipal != null) {
            if (MathUtil.isGreaterThan(principal, maxPrincipal)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.principalParamName)
                        .failWithCode("must.be.less.than.or.equal.to.max");
            }
        }

        final BigDecimal periodPaymentRateMin = this.fromApiJsonHelper
                .parameterExists(WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName, element)
                        ? this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName,
                                element, new HashSet<>())
                        : null;

        final BigDecimal periodPaymentRateMax = this.fromApiJsonHelper
                .parameterExists(WorkingCapitalLoanProductConstants.maxPeriodPaymentRateParamName, element)
                        ? this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.maxPeriodPaymentRateParamName,
                                element, new HashSet<>())
                        : null;
        final BigDecimal periodPaymentRate = this.fromApiJsonHelper
                .parameterExists(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element)
                        ? this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName,
                                element, new HashSet<>())
                        : null;

        // Validate min/max values if provided (as per LoanProduct logic for interest rates)
        if (periodPaymentRateMin != null) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName)
                    .value(periodPaymentRateMin).ignoreIfNull().zeroOrPositiveAmount();
        }
        if (periodPaymentRateMax != null) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.maxPeriodPaymentRateParamName)
                    .value(periodPaymentRateMax).ignoreIfNull().zeroOrPositiveAmount();
        }

        if (periodPaymentRateMin != null && periodPaymentRateMax != null) {
            if (MathUtil.isGreaterThan(periodPaymentRateMin, periodPaymentRateMax)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName)
                        .failWithCode("must.be.less.than.or.equal.to.max");
            }
        }
        if (periodPaymentRate != null && periodPaymentRateMin != null) {
            if (MathUtil.isLessThan(periodPaymentRate, periodPaymentRateMin)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.periodPaymentRateParamName)
                        .failWithCode("must.be.greater.than.or.equal.to.min");
            }
        }
        if (periodPaymentRate != null && periodPaymentRateMax != null) {
            if (MathUtil.isGreaterThan(periodPaymentRate, periodPaymentRateMax)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.periodPaymentRateParamName)
                        .failWithCode("must.be.less.than.or.equal.to.max");
            }
        }
    }

    private void validateInputDates(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.startDateParamName, element)
                && this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.closeDateParamName, element)) {
            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanProductConstants.startDateParamName,
                    element);
            final LocalDate closeDate = this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanProductConstants.closeDateParamName,
                    element);
            if (closeDate != null && DateUtils.isBefore(closeDate, startDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.closeDateParamName)
                        .failWithCode("must.be.after.startDate", closeDate.toString(), startDate.toString());
            }
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
