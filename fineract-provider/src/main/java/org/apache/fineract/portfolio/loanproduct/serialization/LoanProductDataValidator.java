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
package org.apache.fineract.portfolio.loanproduct.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.accounting.common.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductValueConditionType;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.loanproduct.exception.EqualAmortizationUnsupportedFeatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public final class LoanProductDataValidator {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("locale", "dateFormat", "name", "description", "fundId",
            "currencyCode", "digitsAfterDecimal", "inMultiplesOf", "principal", "minPrincipal", "maxPrincipal", "repaymentEvery",
            "numberOfRepayments", "minNumberOfRepayments", "maxNumberOfRepayments", "repaymentFrequencyType", "interestRatePerPeriod",
            "minInterestRatePerPeriod", "maxInterestRatePerPeriod", "interestRateFrequencyType", "amortizationType", "interestType",
            "interestCalculationPeriodType", LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName, "inArrearsTolerance",
            "transactionProcessingStrategyId", "graceOnPrincipalPayment", "recurringMoratoriumOnPrincipalPeriods", "graceOnInterestPayment", "graceOnInterestCharged", "charges",
            "accountingRule", "includeInBorrowerCycle", "startDate", "closeDate", "externalId", "isLinkedToFloatingInterestRates",
            "floatingRatesId", "interestRateDifferential", "minDifferentialLendingRate", "defaultDifferentialLendingRate",
            "maxDifferentialLendingRate", "isFloatingInterestRateCalculationAllowed", "syncExpectedWithDisbursementDate",
            LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), LOAN_PRODUCT_ACCOUNTING_PARAMS.OVERPAYMENT.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.FEE_INCOME_ACCOUNT_MAPPING.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_RECOVERY.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTY_INCOME_ACCOUNT_MAPPING.getValue(), LoanProductConstants.useBorrowerCycleParameterName,
            LoanProductConstants.principalVariationsForBorrowerCycleParameterName,
            LoanProductConstants.interestRateVariationsForBorrowerCycleParameterName,
            LoanProductConstants.numberOfRepaymentVariationsForBorrowerCycleParameterName, LoanProductConstants.shortName,
            LoanProductConstants.multiDisburseLoanParameterName, LoanProductConstants.outstandingLoanBalanceParameterName,
            LoanProductConstants.maxTrancheCountParameterName, LoanProductConstants.graceOnArrearsAgeingParameterName,
            LoanProductConstants.overdueDaysForNPAParameterName, LoanProductConstants.isInterestRecalculationEnabledParameterName,
            LoanProductConstants.daysInYearTypeParameterName, LoanProductConstants.daysInMonthTypeParameterName,
            LoanProductConstants.rescheduleStrategyMethodParameterName,
            LoanProductConstants.interestRecalculationCompoundingMethodParameterName,
            LoanProductConstants.recalculationRestFrequencyIntervalParameterName,
            LoanProductConstants.recalculationRestFrequencyTypeParameterName,
            LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName,
            LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName,
            LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName,
            LoanProductConstants.minimumDaysBetweenDisbursalAndFirstRepayment, LoanProductConstants.mandatoryGuaranteeParamName,
            LoanProductConstants.holdGuaranteeFundsParamName, LoanProductConstants.minimumGuaranteeFromGuarantorParamName,
            LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, LoanProductConstants.principalThresholdForLastInstallmentParamName,
            LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName, LoanProductConstants.canDefineEmiAmountParamName,
            LoanProductConstants.installmentAmountInMultiplesOfParamName,
            LoanProductConstants.preClosureInterestCalculationStrategyParamName, LoanProductConstants.allowAttributeOverridesParamName,
            LoanProductConstants.allowVariableInstallmentsParamName, LoanProductConstants.minimumGapBetweenInstallments,
            LoanProductConstants.maximumGapBetweenInstallments, LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName,
            LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName,
            LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName,
            LoanProductConstants.recalculationRestFrequencyWeekdayParamName,
            LoanProductConstants.recalculationRestFrequencyNthDayParamName, LoanProductConstants.recalculationRestFrequencyOnDayParamName,
            LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, LoanProductConstants.allowCompoundingOnEodParamName,
            LoanProductConstants.canUseForTopup, LoanProductConstants.isEqualAmortizationParam));

    private static final String[] supportedloanConfigurableAttributes = {LoanProductConstants.amortizationTypeParamName,
            LoanProductConstants.interestTypeParamName, LoanProductConstants.transactionProcessingStrategyIdParamName,
            LoanProductConstants.interestCalculationPeriodTypeParamName,
            LoanProductConstants.inArrearsToleranceParamName, LoanProductConstants.repaymentEveryParamName,
            LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName,
            LoanProductConstants.graceOnArrearsAgeingParameterName};

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanProductDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanproduct");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final String shortName = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.shortName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.shortName).value(shortName).notBlank().notExceedingLengthOf(4);

        final String description = this.fromApiJsonHelper.extractStringNamed("description", element);
        baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(500);

        if (this.fromApiJsonHelper.parameterExists("fundId", element)) {
            final Long fundId = this.fromApiJsonHelper.extractLongNamed("fundId", element);
            baseDataValidator.reset().parameter("fundId").value(fundId).ignoreIfNull().integerGreaterThanZero();
        }
        
        boolean isEqualAmortization = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isEqualAmortizationParam, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.isEqualAmortizationParam, element);
            baseDataValidator.reset().parameter(LoanProductConstants.isEqualAmortizationParam).value(isEqualAmortization).ignoreIfNull()
                    .validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumDaysBetweenDisbursalAndFirstRepayment, element)) {
            final Long minimumDaysBetweenDisbursalAndFirstRepayment = this.fromApiJsonHelper.extractLongNamed(
                    LoanProductConstants.minimumDaysBetweenDisbursalAndFirstRepayment, element);
            baseDataValidator.reset().parameter(LoanProductConstants.minimumDaysBetweenDisbursalAndFirstRepayment)
                    .value(minimumDaysBetweenDisbursalAndFirstRepayment).ignoreIfNull().integerGreaterThanZero();
        }

        final Boolean includeInBorrowerCycle = this.fromApiJsonHelper.extractBooleanNamed("includeInBorrowerCycle", element);
        baseDataValidator.reset().parameter("includeInBorrowerCycle").value(includeInBorrowerCycle).ignoreIfNull()
                .validateForBooleanValue();

        // terms
        final String currencyCode = this.fromApiJsonHelper.extractStringNamed("currencyCode", element);
        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank().notExceedingLengthOf(3);

        final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerNamed("digitsAfterDecimal", element, Locale.getDefault());
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);

        final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed("inMultiplesOf", element, Locale.getDefault());
        baseDataValidator.reset().parameter("inMultiplesOf").value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        baseDataValidator.reset().parameter("principal").value(principal).positiveAmount();

        final String minPrincipalParameterName = "minPrincipal";
        BigDecimal minPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(minPrincipalParameterName, element)) {
            minPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minPrincipalParameterName, element);
            baseDataValidator.reset().parameter(minPrincipalParameterName).value(minPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        final String maxPrincipalParameterName = "maxPrincipal";
        BigDecimal maxPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(maxPrincipalParameterName, element)) {
            maxPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxPrincipalParameterName, element);
            baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        if (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) != -1) {

            if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) != -1) {
                baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).notLessThanMin(minPrincipalAmount);
                if (minPrincipalAmount.compareTo(maxPrincipalAmount) <= 0 && principal != null) {
                    baseDataValidator.reset().parameter("principal").value(principal)
                            .inMinAndMaxAmountRange(minPrincipalAmount, maxPrincipalAmount);
                }
            } else if (principal != null) {
                baseDataValidator.reset().parameter("principal").value(principal).notGreaterThanMax(maxPrincipalAmount);
            }
        } else if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) != -1 && principal != null) {
            baseDataValidator.reset().parameter("principal").value(principal).notLessThanMin(minPrincipalAmount);
        }

        final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        baseDataValidator.reset().parameter("numberOfRepayments").value(numberOfRepayments).notNull().integerGreaterThanZero();

        final String minNumberOfRepaymentsParameterName = "minNumberOfRepayments";
        Integer minNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(minNumberOfRepaymentsParameterName, element)) {
            minNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(minNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(minNumberOfRepaymentsParameterName).value(minNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        final String maxNumberOfRepaymentsParameterName = "maxNumberOfRepayments";
        Integer maxNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(maxNumberOfRepaymentsParameterName, element)) {
            maxNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(maxNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        if (maxNumberOfRepayments != null && maxNumberOfRepayments.compareTo(0) == 1) {
            if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) == 1) {
                baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments)
                        .notLessThanMin(minNumberOfRepayments);
                if (minNumberOfRepayments.compareTo(maxNumberOfRepayments) <= 0) {
                    baseDataValidator.reset().parameter("numberOfRepayments").value(numberOfRepayments)
                            .inMinMaxRange(minNumberOfRepayments, maxNumberOfRepayments);
                }
            } else {
                baseDataValidator.reset().parameter("numberOfRepayments").value(numberOfRepayments)
                        .notGreaterThanMax(maxNumberOfRepayments);
            }
        } else if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) == 1) {
            baseDataValidator.reset().parameter("numberOfRepayments").value(numberOfRepayments).notLessThanMin(minNumberOfRepayments);
        }

        final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
        baseDataValidator.reset().parameter("repaymentEvery").value(repaymentEvery).notNull().integerGreaterThanZero();

        final Integer repaymentFrequencyType = this.fromApiJsonHelper.extractIntegerNamed("repaymentFrequencyType", element,
                Locale.getDefault());
        baseDataValidator.reset().parameter("repaymentFrequencyType").value(repaymentFrequencyType).notNull().inMinMaxRange(0, 3);

        // settings
        final Integer amortizationType = this.fromApiJsonHelper.extractIntegerNamed("amortizationType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("amortizationType").value(amortizationType).notNull().inMinMaxRange(0, 1);

        final Integer interestType = this.fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestType").value(interestType).notNull().inMinMaxRange(0, 1);

        final Integer interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerNamed("interestCalculationPeriodType", element,
                Locale.getDefault());
        baseDataValidator.reset().parameter("interestCalculationPeriodType").value(interestCalculationPeriodType).notNull()
                .inMinMaxRange(0, 1);

        final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
        baseDataValidator.reset().parameter("inArrearsTolerance").value(inArrearsTolerance).ignoreIfNull().zeroOrPositiveAmount();

        final Long transactionProcessingStrategyId = this.fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId", element);
        baseDataValidator.reset().parameter("transactionProcessingStrategyId").value(transactionProcessingStrategyId).notNull()
                .integerGreaterThanZero();

        // grace validation
        final Integer graceOnPrincipalPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
        baseDataValidator.reset().parameter("graceOnPrincipalPayment").value(graceOnPrincipalPayment).zeroOrPositiveAmount();

        final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
        baseDataValidator.reset().parameter("graceOnInterestPayment").value(graceOnInterestPayment).zeroOrPositiveAmount();

        final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
        baseDataValidator.reset().parameter("graceOnInterestCharged").value(graceOnInterestCharged).zeroOrPositiveAmount();

        final Integer graceOnArrearsAgeing = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                LoanProductConstants.graceOnArrearsAgeingParameterName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.graceOnArrearsAgeingParameterName).value(graceOnArrearsAgeing)
                .integerZeroOrGreater();

        final Integer overdueDaysForNPA = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                LoanProductConstants.overdueDaysForNPAParameterName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.overdueDaysForNPAParameterName).value(overdueDaysForNPA)
                .integerZeroOrGreater();

        /**
         * { @link DaysInYearType }
         */
        final Integer daysInYearType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.daysInYearTypeParameterName,
                element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.daysInYearTypeParameterName).value(daysInYearType).notNull()
                .isOneOfTheseValues(1, 360, 364, 365);

        /**
         * { @link DaysInMonthType }
         */
        final Integer daysInMonthType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.daysInMonthTypeParameterName,
                element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.daysInMonthTypeParameterName).value(daysInMonthType).notNull()
                .isOneOfTheseValues(1, 30);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName, element)) {
            Boolean npaChangeConfig = this.fromApiJsonHelper.extractBooleanNamed(
                    LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName)
                    .value(npaChangeConfig).notNull().isOneOfTheseValues(true, false);
        }

        // Interest recalculation settings
        final Boolean isInterestRecalculationEnabled = this.fromApiJsonHelper.extractBooleanNamed(
                LoanProductConstants.isInterestRecalculationEnabledParameterName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.isInterestRecalculationEnabledParameterName)
                .value(isInterestRecalculationEnabled).notNull().isOneOfTheseValues(true, false);

        if (isInterestRecalculationEnabled != null) {
            if (isInterestRecalculationEnabled.booleanValue()) {
                if (isEqualAmortization) { throw new EqualAmortizationUnsupportedFeatureException("interest.recalculation",
                        "interest recalculation"); }
                validateInterestRecalculationParams(element, baseDataValidator, null);
            }
        }

        // interest rates
        if (this.fromApiJsonHelper.parameterExists("isLinkedToFloatingInterestRates", element)
                && this.fromApiJsonHelper.extractBooleanNamed("isLinkedToFloatingInterestRates", element) == true) {
            if (isEqualAmortization) { throw new EqualAmortizationUnsupportedFeatureException("floating.interest.rate",
                    "floating interest rate"); }      	
        	if (this.fromApiJsonHelper.parameterExists("interestRatePerPeriod", element)) {
                baseDataValidator
                        .reset()
                        .parameter("interestRatePerPeriod")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.true",
                                "interestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("minInterestRatePerPeriod", element)) {
                baseDataValidator
                        .reset()
                        .parameter("minInterestRatePerPeriod")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.true",
                                "minInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("maxInterestRatePerPeriod", element)) {
                baseDataValidator
                        .reset()
                        .parameter("maxInterestRatePerPeriod")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.true",
                                "maxInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("interestRateFrequencyType", element)) {
                baseDataValidator
                        .reset()
                        .parameter("interestRateFrequencyType")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.true",
                                "interestRateFrequencyType param is not supported when isLinkedToFloatingInterestRates is true");
            }
            if ((interestType == null || interestType != InterestMethod.DECLINING_BALANCE.getValue())
                    || (isInterestRecalculationEnabled == null || isInterestRecalculationEnabled == false)) {
                baseDataValidator
                        .reset()
                        .parameter("isLinkedToFloatingInterestRates")
                        .failWithCode("supported.only.for.declining.balance.interest.recalculation.enabled",
                                "Floating interest rates are supported only for declining balance and interest recalculation enabled loan products");
            }

            final Integer floatingRatesId = this.fromApiJsonHelper.extractIntegerNamed("floatingRatesId", element, Locale.getDefault());
            baseDataValidator.reset().parameter("floatingRatesId").value(floatingRatesId).notNull();

            final BigDecimal interestRateDifferential = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRateDifferential",
                    element);
            baseDataValidator.reset().parameter("interestRateDifferential").value(interestRateDifferential).notNull()
                    .zeroOrPositiveAmount();

            final String minDifferentialLendingRateParameterName = "minDifferentialLendingRate";
            BigDecimal minDifferentialLendingRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    minDifferentialLendingRateParameterName, element);
            baseDataValidator.reset().parameter(minDifferentialLendingRateParameterName).value(minDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            final String defaultDifferentialLendingRateParameterName = "defaultDifferentialLendingRate";
            BigDecimal defaultDifferentialLendingRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    defaultDifferentialLendingRateParameterName, element);
            baseDataValidator.reset().parameter(defaultDifferentialLendingRateParameterName).value(defaultDifferentialLendingRate)
                    .notNull().zeroOrPositiveAmount();

            final String maxDifferentialLendingRateParameterName = "maxDifferentialLendingRate";
            BigDecimal maxDifferentialLendingRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    maxDifferentialLendingRateParameterName, element);
            baseDataValidator.reset().parameter(maxDifferentialLendingRateParameterName).value(maxDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            if (defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                if (minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                    baseDataValidator.reset().parameter("defaultDifferentialLendingRate").value(defaultDifferentialLendingRate)
                            .notLessThanMin(minDifferentialLendingRate);
                }
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                if (minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                    baseDataValidator.reset().parameter("maxDifferentialLendingRate").value(maxDifferentialLendingRate)
                            .notLessThanMin(minDifferentialLendingRate);
                }
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                if (defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                    baseDataValidator.reset().parameter("maxDifferentialLendingRate").value(maxDifferentialLendingRate)
                            .notLessThanMin(defaultDifferentialLendingRate);
                }
            }

            final Boolean isFloatingInterestRateCalculationAllowed = this.fromApiJsonHelper.extractBooleanNamed(
                    "isFloatingInterestRateCalculationAllowed", element);
            baseDataValidator.reset().parameter("isFloatingInterestRateCalculationAllowed").value(isFloatingInterestRateCalculationAllowed)
                    .notNull().isOneOfTheseValues(true, false);
        } else {
            if (this.fromApiJsonHelper.parameterExists("floatingRatesId", element)) {
                baseDataValidator
                        .reset()
                        .parameter("floatingRatesId")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "floatingRatesId param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("interestRateDifferential", element)) {
                baseDataValidator
                        .reset()
                        .parameter("interestRateDifferential")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "interestRateDifferential param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("minDifferentialLendingRate", element)) {
                baseDataValidator
                        .reset()
                        .parameter("minDifferentialLendingRate")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "minDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("defaultDifferentialLendingRate", element)) {
                baseDataValidator
                        .reset()
                        .parameter("defaultDifferentialLendingRate")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "defaultDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("maxDifferentialLendingRate", element)) {
                baseDataValidator
                        .reset()
                        .parameter("maxDifferentialLendingRate")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "maxDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("isFloatingInterestRateCalculationAllowed", element)) {
                baseDataValidator
                        .reset()
                        .parameter("isFloatingInterestRateCalculationAllowed")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "isFloatingInterestRateCalculationAllowed param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            final BigDecimal interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod",
                    element);
            baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod).notNull().zeroOrPositiveAmount();

            final String minInterestRatePerPeriodParameterName = "minInterestRatePerPeriod";
            BigDecimal minInterestRatePerPeriod = null;
            if (this.fromApiJsonHelper.parameterExists(minInterestRatePerPeriodParameterName, element)) {
                minInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minInterestRatePerPeriodParameterName,
                        element);
                baseDataValidator.reset().parameter(minInterestRatePerPeriodParameterName).value(minInterestRatePerPeriod).ignoreIfNull()
                        .zeroOrPositiveAmount();
            }

            final String maxInterestRatePerPeriodParameterName = "maxInterestRatePerPeriod";
            BigDecimal maxInterestRatePerPeriod = null;
            if (this.fromApiJsonHelper.parameterExists(maxInterestRatePerPeriodParameterName, element)) {
                maxInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxInterestRatePerPeriodParameterName,
                        element);
                baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod).ignoreIfNull()
                        .zeroOrPositiveAmount();
            }

            if (maxInterestRatePerPeriod != null && maxInterestRatePerPeriod.compareTo(BigDecimal.ZERO) != -1) {
                if (minInterestRatePerPeriod != null && minInterestRatePerPeriod.compareTo(BigDecimal.ZERO) != -1) {
                    baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod)
                            .notLessThanMin(minInterestRatePerPeriod);
                    if (minInterestRatePerPeriod.compareTo(maxInterestRatePerPeriod) <= 0) {
                        baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod)
                                .inMinAndMaxAmountRange(minInterestRatePerPeriod, maxInterestRatePerPeriod);
                    }
                } else {
                    baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod)
                            .notGreaterThanMax(maxInterestRatePerPeriod);
                }
            } else if (minInterestRatePerPeriod != null && minInterestRatePerPeriod.compareTo(BigDecimal.ZERO) != -1) {
                baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod)
                        .notLessThanMin(minInterestRatePerPeriod);
            }

            final Integer interestRateFrequencyType = this.fromApiJsonHelper.extractIntegerNamed("interestRateFrequencyType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("interestRateFrequencyType").value(interestRateFrequencyType).notNull().inMinMaxRange(0, 3);
        }

        // Guarantee Funds
        Boolean holdGuaranteeFunds = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.holdGuaranteeFundsParamName, element)) {
            holdGuaranteeFunds = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.holdGuaranteeFundsParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.holdGuaranteeFundsParamName).value(holdGuaranteeFunds).notNull()
                    .isOneOfTheseValues(true, false);
        }

        if (holdGuaranteeFunds != null) {
            if (holdGuaranteeFunds) {
                validateGuaranteeParams(element, baseDataValidator, null);
            }
        }

        BigDecimal principalThresholdForLastInstallment = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                LoanProductConstants.principalThresholdForLastInstallmentParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.principalThresholdForLastInstallmentParamName)
                .value(principalThresholdForLastInstallment).notLessThanMin(BigDecimal.ZERO).notGreaterThanMax(BigDecimal.valueOf(100));
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.canDefineEmiAmountParamName, element)) {
            final Boolean canDefineInstallmentAmount = this.fromApiJsonHelper.extractBooleanNamed(
                    LoanProductConstants.canDefineEmiAmountParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.canDefineEmiAmountParamName).value(canDefineInstallmentAmount)
                    .isOneOfTheseValues(true, false);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.installmentAmountInMultiplesOfParamName, element)) {
            final Integer installmentAmountInMultiplesOf = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    LoanProductConstants.installmentAmountInMultiplesOfParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.installmentAmountInMultiplesOfParamName)
                    .value(installmentAmountInMultiplesOf).ignoreIfNull().integerGreaterThanZero();
        }

        // accounting related data validation
        final Integer accountingRuleType = this.fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).notNull().inMinMaxRange(1, 4);

        if (isCashBasedAccounting(accountingRuleType) || isAccrualBasedAccounting(accountingRuleType)) {

            final Long fundAccountId = this.fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(),
                    element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).value(fundAccountId).notNull()
                    .integerGreaterThanZero();

            final Long loanPortfolioAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue()).value(loanPortfolioAccountId)
                    .notNull().integerGreaterThanZero();

            final Long transfersInSuspenseAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue())
                    .value(transfersInSuspenseAccountId).notNull().integerGreaterThanZero();

            final Long incomeFromInterestId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue()).value(incomeFromInterestId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromPenaltyId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromRecoveryAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_RECOVERY.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_RECOVERY.getValue())
                    .value(incomeFromRecoveryAccountId).notNull().integerGreaterThanZero();

            final Long writeOffAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue()).value(writeOffAccountId)
                    .notNull().integerGreaterThanZero();

            final Long overpaymentAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.OVERPAYMENT.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.OVERPAYMENT.getValue()).value(overpaymentAccountId)
                    .notNull().integerGreaterThanZero();

            validatePaymentChannelFundSourceMappings(baseDataValidator, element);
            validateChargeToIncomeAccountMappings(baseDataValidator, element);

        }

        if (isAccrualBasedAccounting(accountingRuleType)) {

            final Long receivableInterestAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue())
                    .value(receivableInterestAccountId).notNull().integerGreaterThanZero();

            final Long receivableFeeAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue()).value(receivableFeeAccountId)
                    .notNull().integerGreaterThanZero();

            final Long receivablePenaltyAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue())
                    .value(receivablePenaltyAccountId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.useBorrowerCycleParameterName, element)) {
            final Boolean useBorrowerCycle = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.useBorrowerCycleParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.useBorrowerCycleParameterName).value(useBorrowerCycle).ignoreIfNull()
                    .validateForBooleanValue();
            if (useBorrowerCycle) {
                validateBorrowerCycleVariations(element, baseDataValidator);
            }
        }

        validateMultiDisburseLoanData(baseDataValidator, element);

        validateLoanConfigurableAttributes(baseDataValidator, element);

        validateVariableInstallmentSettings(baseDataValidator, element);

        validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, null);

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.canUseForTopup, element)){
            final Boolean canUseForTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.canUseForTopup,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.canUseForTopup).value(canUseForTopup).validateForBooleanValue();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateVariableInstallmentSettings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowVariableInstallmentsParamName, element)
                && this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.allowVariableInstallmentsParamName, element)) {
        	
            boolean isEqualAmortization = false;
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isEqualAmortizationParam, element)) {
                isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.isEqualAmortizationParam, element);
            }
            if (isEqualAmortization) { throw new EqualAmortizationUnsupportedFeatureException("variable.installment",
                    "variable installment"); }

            Long minimumGapBetweenInstallments = null;
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGapBetweenInstallments, element)) {
                minimumGapBetweenInstallments = this.fromApiJsonHelper.extractLongNamed(LoanProductConstants.minimumGapBetweenInstallments,
                        element);
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments)
                        .value(minimumGapBetweenInstallments).notNull();
            } else {
                baseDataValidator
                        .reset()
                        .parameter(LoanProductConstants.minimumGapBetweenInstallments)
                        .failWithCode("is.mandatory.when.allowVariableInstallments.is.true",
                                "minimumGap param is mandatory when allowVariableInstallments is true");
            }

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.maximumGapBetweenInstallments, element)) {
                final Long maximumGapBetweenInstallments = this.fromApiJsonHelper.extractLongNamed(
                        LoanProductConstants.maximumGapBetweenInstallments, element);
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments)
                        .value(maximumGapBetweenInstallments).notNull();
                baseDataValidator.reset().parameter(LoanProductConstants.maximumGapBetweenInstallments)
                        .value(maximumGapBetweenInstallments).notNull().longGreaterThanNumber(minimumGapBetweenInstallments);
            }

        } else {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGapBetweenInstallments, element)) {
                baseDataValidator
                        .reset()
                        .parameter(LoanProductConstants.minimumGapBetweenInstallments)
                        .failWithCode("not.supported.when.allowVariableInstallments.is.false",
                                "minimumGap param is not supported when allowVariableInstallments is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.maximumGapBetweenInstallments, element)) {
                baseDataValidator
                        .reset()
                        .parameter(LoanProductConstants.maximumGapBetweenInstallments)
                        .failWithCode("not.supported.when.allowVariableInstallments.is.false",
                                "maximumGap param is not supported when allowVariableInstallments is not supplied or false");
            }

        }
    }

    private void validateLoanConfigurableAttributes(final DataValidatorBuilder baseDataValidator, final JsonElement element) {

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowAttributeOverridesParamName, element)) {

            final JsonObject object = element.getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName);

            // Validate that parameter names are allowed
            Set<String> supportedConfigurableAttributes = new HashSet<>();
            Collections.addAll(supportedConfigurableAttributes, supportedloanConfigurableAttributes);
            this.fromApiJsonHelper.checkForUnsupportedNestedParameters(LoanProductConstants.allowAttributeOverridesParamName, object,
                    supportedConfigurableAttributes);

            Integer length = supportedloanConfigurableAttributes.length;

            for (int i = 0; i < length; i++) {
                /* Validate the attribute names */
                if (this.fromApiJsonHelper
                        .parameterExists(supportedloanConfigurableAttributes[i], object)) {
                    Boolean loanConfigurationAttributeValue = this.fromApiJsonHelper.extractBooleanNamed(
                            supportedloanConfigurableAttributes[i], object);
                    /* Validate the boolean value */
                    baseDataValidator.reset().parameter(LoanProductConstants.allowAttributeOverridesParamName)
                            .value(loanConfigurationAttributeValue).notNull().validateForBooleanValue();
                }

            }
        }
    }

    private void validateMultiDisburseLoanData(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        Boolean multiDisburseLoan = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.multiDisburseLoanParameterName, element)) {
            multiDisburseLoan = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.multiDisburseLoanParameterName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.multiDisburseLoanParameterName).value(multiDisburseLoan)
                    .ignoreIfNull().validateForBooleanValue();
        }
        
        boolean isEqualAmortization = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isEqualAmortizationParam, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.isEqualAmortizationParam, element);
        }
        if (isEqualAmortization && multiDisburseLoan) { throw new EqualAmortizationUnsupportedFeatureException("tranche.disbursal",
                "tranche disbursal"); }

        if (multiDisburseLoan) {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.outstandingLoanBalanceParameterName, element)) {
                final BigDecimal outstandingLoanBalance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        LoanProductConstants.outstandingLoanBalanceParameterName, element);
                baseDataValidator.reset().parameter(LoanProductConstants.outstandingLoanBalanceParameterName).value(outstandingLoanBalance)
                        .ignoreIfNull().zeroOrPositiveAmount();
            }

            final Integer maxTrancheCount = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.maxTrancheCountParameterName,
                    element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.maxTrancheCountParameterName).value(maxTrancheCount).notNull()
                    .integerGreaterThanZero();

            final Integer interestType = this.fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
            baseDataValidator.reset().parameter("interestType").value(interestType).ignoreIfNull()
                    .integerSameAsNumber(InterestMethod.DECLINING_BALANCE.getValue());
        }
    }

    private void validateInterestRecalculationParams(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            final LoanProduct loanProduct) {

        /**
         * { @link InterestRecalculationCompoundingMethod }
         */
        InterestRecalculationCompoundingMethod compoundingMethod = null;

        if (loanProduct == null
                || this.fromApiJsonHelper
                        .parameterExists(LoanProductConstants.interestRecalculationCompoundingMethodParameterName, element)) {
            final Integer interestRecalculationCompoundingMethod = this.fromApiJsonHelper.extractIntegerNamed(
                    LoanProductConstants.interestRecalculationCompoundingMethodParameterName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.interestRecalculationCompoundingMethodParameterName)
                    .value(interestRecalculationCompoundingMethod).notNull().inMinMaxRange(0, 3);
            if (interestRecalculationCompoundingMethod != null) {
                compoundingMethod = InterestRecalculationCompoundingMethod.fromInt(interestRecalculationCompoundingMethod);
            }
        }

        if (compoundingMethod == null) {
            if (loanProduct == null) {
                compoundingMethod = InterestRecalculationCompoundingMethod.NONE;
            } else {
                compoundingMethod = InterestRecalculationCompoundingMethod.fromInt(loanProduct.getProductInterestRecalculationDetails()
                        .getInterestRecalculationCompoundingMethod());
            }
        }

        /**
         * { @link LoanRescheduleStrategyMethod }
         */
        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.rescheduleStrategyMethodParameterName, element)) {
            final Integer rescheduleStrategyMethod = this.fromApiJsonHelper.extractIntegerNamed(
                    LoanProductConstants.rescheduleStrategyMethodParameterName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.rescheduleStrategyMethodParameterName).value(rescheduleStrategyMethod)
                    .notNull().inMinMaxRange(1, 3);
        }

        RecalculationFrequencyType frequencyType = null;

        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationRestFrequencyTypeParameterName, element)) {
            final Integer recalculationRestFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(
                    LoanProductConstants.recalculationRestFrequencyTypeParameterName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.recalculationRestFrequencyTypeParameterName)
                    .value(recalculationRestFrequencyType).notNull().inMinMaxRange(1, 4);
            if (recalculationRestFrequencyType != null) {
                frequencyType = RecalculationFrequencyType.fromInt(recalculationRestFrequencyType);
            }
        }

        if (frequencyType == null) {
            if (loanProduct == null) {
                frequencyType = RecalculationFrequencyType.INVALID;
            } else {
                frequencyType = loanProduct.getProductInterestRecalculationDetails().getRestFrequencyType();
            }
        }

        if (!frequencyType.isSameAsRepayment()) {
            if (loanProduct == null
                    || this.fromApiJsonHelper
                            .parameterExists(LoanProductConstants.recalculationRestFrequencyIntervalParameterName, element)) {
                final Integer recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(
                        LoanProductConstants.recalculationRestFrequencyIntervalParameterName, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.recalculationRestFrequencyIntervalParameterName)
                        .value(recurrenceInterval).notNull();
            }
            if (loanProduct == null
                    || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationRestFrequencyNthDayParamName, element)
                    || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationRestFrequencyWeekdayParamName, element)) {
                CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator,
                        LoanProductConstants.recalculationRestFrequencyNthDayParamName,
                        LoanProductConstants.recalculationRestFrequencyWeekdayParamName, element, this.fromApiJsonHelper);
            }
            if (loanProduct == null
                    || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationRestFrequencyOnDayParamName, element)) {
                final Integer recalculationRestFrequencyOnDay = this.fromApiJsonHelper.extractIntegerNamed(
                        LoanProductConstants.recalculationRestFrequencyOnDayParamName, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.recalculationRestFrequencyOnDayParamName)
                        .value(recalculationRestFrequencyOnDay).ignoreIfNull().inMinMaxRange(1, 28);
            }
        }

        if (compoundingMethod.isCompoundingEnabled()) {
            RecalculationFrequencyType compoundingfrequencyType = null;

            if (loanProduct == null
                    || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName,
                            element)) {
                final Integer recalculationCompoundingFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(
                        LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName)
                        .value(recalculationCompoundingFrequencyType).notNull().inMinMaxRange(1, 4);
                if (recalculationCompoundingFrequencyType != null) {
                    compoundingfrequencyType = RecalculationFrequencyType.fromInt(recalculationCompoundingFrequencyType);
                    if (!compoundingfrequencyType.isSameAsRepayment()) {
                        PeriodFrequencyType repaymentFrequencyType = null;
                        if (this.fromApiJsonHelper.parameterExists("repaymentFrequencyType", element)) {
                            Integer repaymentFrequencyTypeVal = this.fromApiJsonHelper.extractIntegerNamed("repaymentFrequencyType",
                                    element, Locale.getDefault());
                            repaymentFrequencyType = PeriodFrequencyType.fromInt(repaymentFrequencyTypeVal);
                        } else if (loanProduct != null) {
                            repaymentFrequencyType = loanProduct.getLoanProductRelatedDetail().getRepaymentPeriodFrequencyType();
                        }
                        if (!compoundingfrequencyType.isSameFrequency(repaymentFrequencyType)) {
                            baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName)
                                    .value(recalculationCompoundingFrequencyType).failWithCode("must.be.same.as.repayment.frequency");
                        }
                    }
                }
            }

            if (compoundingfrequencyType == null) {
                if (loanProduct == null) {
                    compoundingfrequencyType = RecalculationFrequencyType.INVALID;
                } else {
                    compoundingfrequencyType = loanProduct.getProductInterestRecalculationDetails().getCompoundingFrequencyType();
                }
            }

            if (!compoundingfrequencyType.isSameAsRepayment()) {
                if (loanProduct == null
                        || this.fromApiJsonHelper.parameterExists(
                                LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName, element)) {
                    final Integer recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(
                            LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName, element, Locale.getDefault());
                    Integer repaymentEvery = null;
                    if (loanProduct == null) {
                        repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
                    } else {
                        repaymentEvery = loanProduct.getLoanProductRelatedDetail().getRepayEvery();
                    }

                    baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName)
                            .value(recurrenceInterval).notNull().integerInMultiplesOfNumber(repaymentEvery);
                }
                if (loanProduct == null
                        || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName,
                                element)
                        || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName,
                                element)) {
                    CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator,
                            LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName,
                            LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName, element, this.fromApiJsonHelper);
                }
                if (loanProduct == null
                        || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName,
                                element)) {
                    final Integer recalculationRestFrequencyOnDay = this.fromApiJsonHelper.extractIntegerNamed(
                            LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName, element, Locale.getDefault());
                    baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName)
                            .value(recalculationRestFrequencyOnDay).ignoreIfNull().inMinMaxRange(1, 28);
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName, element)) {
            final Boolean isArrearsBasedOnOriginalSchedule = this.fromApiJsonHelper.extractBooleanNamed(
                    LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName)
                    .value(isArrearsBasedOnOriginalSchedule).notNull().isOneOfTheseValues(true, false);
        }
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, element)) {
            final Boolean isCompoundingToBePostedAsTransactions = this.fromApiJsonHelper.extractBooleanNamed(
                    LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName)
                    .value(isCompoundingToBePostedAsTransactions).notNull().isOneOfTheseValues(true, false);
        }

        final Integer preCloseInterestCalculationStrategy = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                LoanProductConstants.preClosureInterestCalculationStrategyParamName, element);
        baseDataValidator
                .reset()
                .parameter(LoanProductConstants.preClosureInterestCalculationStrategyParamName)
                .value(preCloseInterestCalculationStrategy)
                .ignoreIfNull()
                .inMinMaxRange(LoanPreClosureInterestCalculationStrategy.getMinValue(),
                        LoanPreClosureInterestCalculationStrategy.getMaxValue());
    }

    public void validateForUpdate(final String json, final LoanProduct loanProduct) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanproduct");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        if (this.fromApiJsonHelper.parameterExists("name", element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
            baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.shortName, element)) {
            final String shortName = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.shortName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.shortName).value(shortName).notBlank().notExceedingLengthOf(4);
        }

        if (this.fromApiJsonHelper.parameterExists("description", element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed("description", element);
            baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists("fundId", element)) {
            final Long fundId = this.fromApiJsonHelper.extractLongNamed("fundId", element);
            baseDataValidator.reset().parameter("fundId").value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists("includeInBorrowerCycle", element)) {
            final Boolean includeInBorrowerCycle = this.fromApiJsonHelper.extractBooleanNamed("includeInBorrowerCycle", element);
            baseDataValidator.reset().parameter("includeInBorrowerCycle").value(includeInBorrowerCycle).ignoreIfNull()
                    .validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists("currencyCode", element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed("currencyCode", element);
            baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank().notExceedingLengthOf(3);
        }

        if (this.fromApiJsonHelper.parameterExists("digitsAfterDecimal", element)) {
            final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerNamed("digitsAfterDecimal", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("digitsAfterDecimal").value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);
        }

        if (this.fromApiJsonHelper.parameterExists("inMultiplesOf", element)) {
            final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed("inMultiplesOf", element, Locale.getDefault());
            baseDataValidator.reset().parameter("inMultiplesOf").value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();
        }

        final String minPrincipalParameterName = "minPrincipal";
        BigDecimal minPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(minPrincipalParameterName, element)) {
            minPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minPrincipalParameterName, element);
            baseDataValidator.reset().parameter(minPrincipalParameterName).value(minPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        final String maxPrincipalParameterName = "maxPrincipal";
        BigDecimal maxPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(maxPrincipalParameterName, element)) {
            maxPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxPrincipalParameterName, element);
            baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("principal", element)) {
            final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
            baseDataValidator.reset().parameter("principal").value(principal).positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("inArrearsTolerance", element)) {
            final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
            baseDataValidator.reset().parameter("inArrearsTolerance").value(inArrearsTolerance).ignoreIfNull().zeroOrPositiveAmount();
        }

        final String minNumberOfRepaymentsParameterName = "minNumberOfRepayments";
        Integer minNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(minNumberOfRepaymentsParameterName, element)) {
            minNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(minNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(minNumberOfRepaymentsParameterName).value(minNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        final String maxNumberOfRepaymentsParameterName = "maxNumberOfRepayments";
        Integer maxNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(maxNumberOfRepaymentsParameterName, element)) {
            maxNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(maxNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists("numberOfRepayments", element)) {
            final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
            baseDataValidator.reset().parameter("numberOfRepayments").value(numberOfRepayments).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists("repaymentEvery", element)) {
            final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
            baseDataValidator.reset().parameter("repaymentEvery").value(repaymentEvery).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists("repaymentFrequencyType", element)) {
            final Integer repaymentFrequencyType = this.fromApiJsonHelper.extractIntegerNamed("repaymentFrequencyType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("repaymentFrequencyType").value(repaymentFrequencyType).notNull().inMinMaxRange(0, 3);
        }

        if (this.fromApiJsonHelper.parameterExists("transactionProcessingStrategyId", element)) {
            final Long transactionProcessingStrategyId = this.fromApiJsonHelper
                    .extractLongNamed("transactionProcessingStrategyId", element);
            baseDataValidator.reset().parameter("transactionProcessingStrategyId").value(transactionProcessingStrategyId).notNull()
                    .integerGreaterThanZero();
        }

        // grace validation
        if (this.fromApiJsonHelper.parameterExists("graceOnPrincipalPayment", element)) {
            final Integer graceOnPrincipalPayment = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
            baseDataValidator.reset().parameter("graceOnPrincipalPayment").value(graceOnPrincipalPayment).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("graceOnInterestPayment", element)) {
            final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
            baseDataValidator.reset().parameter("graceOnInterestPayment").value(graceOnInterestPayment).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("graceOnInterestCharged", element)) {
            final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
            baseDataValidator.reset().parameter("graceOnInterestCharged").value(graceOnInterestCharged).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.graceOnArrearsAgeingParameterName, element)) {
            final Integer graceOnArrearsAgeing = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    LoanProductConstants.graceOnArrearsAgeingParameterName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.graceOnArrearsAgeingParameterName).value(graceOnArrearsAgeing)
                    .integerZeroOrGreater();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.overdueDaysForNPAParameterName, element)) {
            final Integer overdueDaysForNPA = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    LoanProductConstants.overdueDaysForNPAParameterName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.overdueDaysForNPAParameterName).value(overdueDaysForNPA)
                    .integerZeroOrGreater();
        }

        //
        if (this.fromApiJsonHelper.parameterExists("amortizationType", element)) {
            final Integer amortizationType = this.fromApiJsonHelper.extractIntegerNamed("amortizationType", element, Locale.getDefault());
            baseDataValidator.reset().parameter("amortizationType").value(amortizationType).notNull().inMinMaxRange(0, 1);
        }

        if (this.fromApiJsonHelper.parameterExists("interestType", element)) {
            final Integer interestType = this.fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
            baseDataValidator.reset().parameter("interestType").value(interestType).notNull().inMinMaxRange(0, 1);
        }
        Integer interestCalculationPeriodType = loanProduct.getLoanProductRelatedDetail().getInterestCalculationPeriodMethod().getValue();
        if (this.fromApiJsonHelper.parameterExists("interestCalculationPeriodType", element)) {
            interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerNamed("interestCalculationPeriodType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("interestCalculationPeriodType").value(interestCalculationPeriodType).notNull()
                    .inMinMaxRange(0, 1);
        }

        /**
         * { @link DaysInYearType }
         */
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.daysInYearTypeParameterName, element)) {
            final Integer daysInYearType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.daysInYearTypeParameterName,
                    element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.daysInYearTypeParameterName).value(daysInYearType).notNull()
                    .isOneOfTheseValues(1, 360, 364, 365);
        }

        /**
         * { @link DaysInMonthType }
         */
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.daysInMonthTypeParameterName, element)) {
            final Integer daysInMonthType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.daysInMonthTypeParameterName,
                    element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.daysInMonthTypeParameterName).value(daysInMonthType).notNull()
                    .isOneOfTheseValues(1, 30);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName, element)) {
            Boolean npaChangeConfig = this.fromApiJsonHelper.extractBooleanNamed(
                    LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName)
                    .value(npaChangeConfig).notNull().isOneOfTheseValues(true, false);
        }
        
        boolean isEqualAmortization = loanProduct.isEqualAmortization();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isEqualAmortizationParam, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.isEqualAmortizationParam, element);
            baseDataValidator.reset().parameter(LoanProductConstants.isEqualAmortizationParam).value(isEqualAmortization).ignoreIfNull()
                    .validateForBooleanValue();
        }

        // Interest recalculation settings
        Boolean isInterestRecalculationEnabled = loanProduct.isInterestRecalculationEnabled();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isInterestRecalculationEnabledParameterName, element)) {
            isInterestRecalculationEnabled = this.fromApiJsonHelper.extractBooleanNamed(
                    LoanProductConstants.isInterestRecalculationEnabledParameterName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.isInterestRecalculationEnabledParameterName)
                    .value(isInterestRecalculationEnabled).notNull().isOneOfTheseValues(true, false);
        }

        if (isInterestRecalculationEnabled != null) {
            if (isInterestRecalculationEnabled) {
                if (isEqualAmortization) { throw new EqualAmortizationUnsupportedFeatureException("interest.recalculation",
                        "interest recalculation"); }
                validateInterestRecalculationParams(element, baseDataValidator, loanProduct);
            }
        }

        // interest rates
        boolean isLinkedToFloatingInterestRates = loanProduct.isLinkedToFloatingInterestRate();
        if (this.fromApiJsonHelper.parameterExists("isLinkedToFloatingInterestRates", element)) {
            isLinkedToFloatingInterestRates = this.fromApiJsonHelper.extractBooleanNamed("isLinkedToFloatingInterestRates", element);
        }
        if (isLinkedToFloatingInterestRates) {
        	if(isEqualAmortization){
            	throw new EqualAmortizationUnsupportedFeatureException("floating.interest.rate", "floating interest rate");
            }
            if (this.fromApiJsonHelper.parameterExists("interestRatePerPeriod", element)) {
                baseDataValidator
                        .reset()
                        .parameter("interestRatePerPeriod")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.true",
                                "interestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("minInterestRatePerPeriod", element)) {
                baseDataValidator
                        .reset()
                        .parameter("minInterestRatePerPeriod")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.true",
                                "minInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("maxInterestRatePerPeriod", element)) {
                baseDataValidator
                        .reset()
                        .parameter("maxInterestRatePerPeriod")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.true",
                                "maxInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("interestRateFrequencyType", element)) {
                baseDataValidator
                        .reset()
                        .parameter("interestRateFrequencyType")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.true",
                                "interestRateFrequencyType param is not supported when isLinkedToFloatingInterestRates is true");
            }

            Integer interestType = this.fromApiJsonHelper.parameterExists("interestType", element) ? this.fromApiJsonHelper
                    .extractIntegerNamed("interestType", element, Locale.getDefault()) : loanProduct.getLoanProductRelatedDetail()
                    .getInterestMethod().getValue();
            if ((interestType == null || interestType != InterestMethod.DECLINING_BALANCE.getValue())
                    || (isInterestRecalculationEnabled == null || isInterestRecalculationEnabled == false)) {
                baseDataValidator
                        .reset()
                        .parameter("isLinkedToFloatingInterestRates")
                        .failWithCode("supported.only.for.declining.balance.interest.recalculation.enabled",
                                "Floating interest rates are supported only for declining balance and interest recalculation enabled loan products");
            }

            Long floatingRatesId = loanProduct.getFloatingRates() == null ? null : loanProduct.getFloatingRates().getFloatingRate().getId();
            if (this.fromApiJsonHelper.parameterExists("floatingRatesId", element)) {
                floatingRatesId = this.fromApiJsonHelper.extractLongNamed("floatingRatesId", element);
            }
            baseDataValidator.reset().parameter("floatingRatesId").value(floatingRatesId).notNull();

            BigDecimal interestRateDifferential = loanProduct.getFloatingRates() == null ? null : loanProduct.getFloatingRates()
                    .getInterestRateDifferential();
            if (this.fromApiJsonHelper.parameterExists("interestRateDifferential", element)) {
                interestRateDifferential = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRateDifferential", element);
            }
            baseDataValidator.reset().parameter("interestRateDifferential").value(interestRateDifferential).notNull()
                    .zeroOrPositiveAmount();

            final String minDifferentialLendingRateParameterName = "minDifferentialLendingRate";
            BigDecimal minDifferentialLendingRate = loanProduct.getFloatingRates() == null ? null : loanProduct.getFloatingRates()
                    .getMinDifferentialLendingRate();
            if (this.fromApiJsonHelper.parameterExists(minDifferentialLendingRateParameterName, element)) {
                minDifferentialLendingRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        minDifferentialLendingRateParameterName, element);
            }
            baseDataValidator.reset().parameter(minDifferentialLendingRateParameterName).value(minDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            final String defaultDifferentialLendingRateParameterName = "defaultDifferentialLendingRate";
            BigDecimal defaultDifferentialLendingRate = loanProduct.getFloatingRates() == null ? null : loanProduct.getFloatingRates()
                    .getDefaultDifferentialLendingRate();
            if (this.fromApiJsonHelper.parameterExists(defaultDifferentialLendingRateParameterName, element)) {
                defaultDifferentialLendingRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        defaultDifferentialLendingRateParameterName, element);
            }
            baseDataValidator.reset().parameter(defaultDifferentialLendingRateParameterName).value(defaultDifferentialLendingRate)
                    .notNull().zeroOrPositiveAmount();

            final String maxDifferentialLendingRateParameterName = "maxDifferentialLendingRate";
            BigDecimal maxDifferentialLendingRate = loanProduct.getFloatingRates() == null ? null : loanProduct.getFloatingRates()
                    .getMaxDifferentialLendingRate();
            if (this.fromApiJsonHelper.parameterExists(maxDifferentialLendingRateParameterName, element)) {
                maxDifferentialLendingRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        maxDifferentialLendingRateParameterName, element);
            }
            baseDataValidator.reset().parameter(maxDifferentialLendingRateParameterName).value(maxDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            if (defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                if (minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                    baseDataValidator.reset().parameter("defaultDifferentialLendingRate").value(defaultDifferentialLendingRate)
                            .notLessThanMin(minDifferentialLendingRate);
                }
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                if (minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                    baseDataValidator.reset().parameter("maxDifferentialLendingRate").value(maxDifferentialLendingRate)
                            .notLessThanMin(minDifferentialLendingRate);
                }
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                if (defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) != -1) {
                    baseDataValidator.reset().parameter("maxDifferentialLendingRate").value(maxDifferentialLendingRate)
                            .notLessThanMin(defaultDifferentialLendingRate);
                }
            }

            Boolean isFloatingInterestRateCalculationAllowed = loanProduct.getFloatingRates() == null ? null : loanProduct
                    .getFloatingRates().isFloatingInterestRateCalculationAllowed();
            if (this.fromApiJsonHelper.parameterExists("isFloatingInterestRateCalculationAllowed", element)) {
                isFloatingInterestRateCalculationAllowed = this.fromApiJsonHelper.extractBooleanNamed(
                        "isFloatingInterestRateCalculationAllowed", element);
            }
            baseDataValidator.reset().parameter("isFloatingInterestRateCalculationAllowed").value(isFloatingInterestRateCalculationAllowed)
                    .notNull().isOneOfTheseValues(true, false);
        } else {
            if (this.fromApiJsonHelper.parameterExists("floatingRatesId", element)) {
                baseDataValidator
                        .reset()
                        .parameter("floatingRatesId")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "floatingRatesId param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("interestRateDifferential", element)) {
                baseDataValidator
                        .reset()
                        .parameter("interestRateDifferential")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "interestRateDifferential param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("minDifferentialLendingRate", element)) {
                baseDataValidator
                        .reset()
                        .parameter("minDifferentialLendingRate")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "minDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("defaultDifferentialLendingRate", element)) {
                baseDataValidator
                        .reset()
                        .parameter("defaultDifferentialLendingRate")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "defaultDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("maxDifferentialLendingRate", element)) {
                baseDataValidator
                        .reset()
                        .parameter("maxDifferentialLendingRate")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "maxDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("isFloatingInterestRateCalculationAllowed", element)) {
                baseDataValidator
                        .reset()
                        .parameter("isFloatingInterestRateCalculationAllowed")
                        .failWithCode("not.supported.when.isLinkedToFloatingInterestRates.is.false",
                                "isFloatingInterestRateCalculationAllowed param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            final String minInterestRatePerPeriodParameterName = "minInterestRatePerPeriod";
            BigDecimal minInterestRatePerPeriod = loanProduct.getMinNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists(minInterestRatePerPeriodParameterName, element)) {
                minInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minInterestRatePerPeriodParameterName,
                        element);
            }
            baseDataValidator.reset().parameter(minInterestRatePerPeriodParameterName).value(minInterestRatePerPeriod).ignoreIfNull()
                    .zeroOrPositiveAmount();

            final String maxInterestRatePerPeriodParameterName = "maxInterestRatePerPeriod";
            BigDecimal maxInterestRatePerPeriod = loanProduct.getMaxNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists(maxInterestRatePerPeriodParameterName, element)) {
                maxInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxInterestRatePerPeriodParameterName,
                        element);
            }
            baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod).ignoreIfNull()
                    .zeroOrPositiveAmount();

            BigDecimal interestRatePerPeriod = loanProduct.getLoanProductRelatedDetail().getNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists("interestRatePerPeriod", element)) {
                interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
            }
            baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod).notNull().zeroOrPositiveAmount();

            Integer interestRateFrequencyType = loanProduct.getLoanProductRelatedDetail().getInterestPeriodFrequencyType().getValue();
            if (this.fromApiJsonHelper.parameterExists("interestRateFrequencyType", element)) {
                interestRateFrequencyType = this.fromApiJsonHelper.extractIntegerNamed("interestRateFrequencyType", element,
                        Locale.getDefault());
            }
            baseDataValidator.reset().parameter("interestRateFrequencyType").value(interestRateFrequencyType).notNull().inMinMaxRange(0, 3);
        }

        // Guarantee Funds
        Boolean holdGuaranteeFunds = loanProduct.isHoldGuaranteeFundsEnabled();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.holdGuaranteeFundsParamName, element)) {
            holdGuaranteeFunds = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.holdGuaranteeFundsParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.holdGuaranteeFundsParamName).value(holdGuaranteeFunds).notNull()
                    .isOneOfTheseValues(true, false);
        }

        if (holdGuaranteeFunds != null) {
            if (holdGuaranteeFunds) {
                validateGuaranteeParams(element, baseDataValidator, null);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.principalThresholdForLastInstallmentParamName, element)) {
            BigDecimal principalThresholdForLastInstallment = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    LoanProductConstants.principalThresholdForLastInstallmentParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.principalThresholdForLastInstallmentParamName)
                    .value(principalThresholdForLastInstallment).notNull().notLessThanMin(BigDecimal.ZERO)
                    .notGreaterThanMax(BigDecimal.valueOf(100));
        }
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.canDefineEmiAmountParamName, element)) {
            final Boolean canDefineInstallmentAmount = this.fromApiJsonHelper.extractBooleanNamed(
                    LoanProductConstants.canDefineEmiAmountParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.canDefineEmiAmountParamName).value(canDefineInstallmentAmount)
                    .isOneOfTheseValues(true, false);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.installmentAmountInMultiplesOfParamName, element)) {
            final Integer installmentAmountInMultiplesOf = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    LoanProductConstants.installmentAmountInMultiplesOfParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.installmentAmountInMultiplesOfParamName)
                    .value(installmentAmountInMultiplesOf).ignoreIfNull().integerGreaterThanZero();
        }

        final Integer accountingRuleType = this.fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).ignoreIfNull().inMinMaxRange(1, 4);

        final Long fundAccountId = this.fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).value(fundAccountId).ignoreIfNull()
                .integerGreaterThanZero();

        final Long loanPortfolioAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue()).value(loanPortfolioAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long transfersInSuspenseAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue())
                .value(transfersInSuspenseAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromInterestId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue()).value(incomeFromInterestId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
                element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromPenaltyId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromRecoveryAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_RECOVERY.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_RECOVERY.getValue())
                .value(incomeFromRecoveryAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long writeOffAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue()).value(writeOffAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long overpaymentAccountId = this.fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.OVERPAYMENT.getValue(),
                element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.OVERPAYMENT.getValue()).value(overpaymentAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long receivableInterestAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue())
                .value(receivableInterestAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long receivableFeeAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue()).value(receivableFeeAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long receivablePenaltyAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue())
                .value(receivablePenaltyAccountId).ignoreIfNull().integerGreaterThanZero();

        validatePaymentChannelFundSourceMappings(baseDataValidator, element);
        validateChargeToIncomeAccountMappings(baseDataValidator, element);

        validateMinMaxConstraints(element, baseDataValidator, loanProduct);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.useBorrowerCycleParameterName, element)) {
            final Boolean useBorrowerCycle = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.useBorrowerCycleParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.useBorrowerCycleParameterName).value(useBorrowerCycle).ignoreIfNull()
                    .validateForBooleanValue();
            if (useBorrowerCycle) {
                validateBorrowerCycleVariations(element, baseDataValidator);
            }
        }

        validateMultiDisburseLoanData(baseDataValidator, element);

        // validateLoanConfigurableAttributes(baseDataValidator,element);

        validateVariableInstallmentSettings(baseDataValidator, element);

        validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, loanProduct);

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.canUseForTopup, element)){
            final Boolean canUseForTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.canUseForTopup,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.canUseForTopup).value(canUseForTopup).validateForBooleanValue();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    /*
     * Validation for advanced accounting options
     */
    private void validatePaymentChannelFundSourceMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (this.fromApiJsonHelper.parameterExists(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element)) {
            final JsonArray paymentChannelMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
            if (paymentChannelMappingArray != null && paymentChannelMappingArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                    final Long paymentTypeId = this.fromApiJsonHelper.extractLongNamed(
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_TYPE.getValue(), jsonObject);
                    final Long paymentSpecificFundAccountId = this.fromApiJsonHelper.extractLongNamed(
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), jsonObject);

                    baseDataValidator
                            .reset()
                            .parameter(
                                    LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue() + "[" + i + "]."
                                            + LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_TYPE.getValue()).value(paymentTypeId).notNull()
                            .integerGreaterThanZero();
                    baseDataValidator
                            .reset()
                            .parameter(
                                    LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue() + "[" + i + "]."
                                            + LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).value(paymentSpecificFundAccountId)
                            .notNull().integerGreaterThanZero();
                    i++;
                } while (i < paymentChannelMappingArray.size());
            }
        }
    }

    private void validateChargeToIncomeAccountMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        // validate for both fee and penalty charges
        validateChargeToIncomeAccountMappings(baseDataValidator, element, true);
        validateChargeToIncomeAccountMappings(baseDataValidator, element, true);
    }

    private void validateChargeToIncomeAccountMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element,
            final boolean isPenalty) {
        String parameterName;
        if (isPenalty) {
            parameterName = LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTY_INCOME_ACCOUNT_MAPPING.getValue();
        } else {
            parameterName = LOAN_PRODUCT_ACCOUNTING_PARAMS.FEE_INCOME_ACCOUNT_MAPPING.getValue();
        }

        if (this.fromApiJsonHelper.parameterExists(parameterName, element)) {
            final JsonArray chargeToIncomeAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(parameterName, element);
            if (chargeToIncomeAccountMappingArray != null && chargeToIncomeAccountMappingArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = chargeToIncomeAccountMappingArray.get(i).getAsJsonObject();
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.CHARGE_ID.getValue(),
                            jsonObject);
                    final Long incomeAccountId = this.fromApiJsonHelper.extractLongNamed(
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_ACCOUNT_ID.getValue(), jsonObject);
                    baseDataValidator.reset()
                            .parameter(parameterName + "[" + i + "]." + LOAN_PRODUCT_ACCOUNTING_PARAMS.CHARGE_ID.getValue())
                            .value(chargeId).notNull().integerGreaterThanZero();
                    baseDataValidator.reset()
                            .parameter(parameterName + "[" + i + "]." + LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_ACCOUNT_ID.getValue())
                            .value(incomeAccountId).notNull().integerGreaterThanZero();
                    i++;
                } while (i < chargeToIncomeAccountMappingArray.size());
            }
        }
    }

    public void validateMinMaxConstraints(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            final LoanProduct loanProduct) {

        validatePrincipalMinMaxConstraint(element, loanProduct, baseDataValidator);

        validateNumberOfRepaymentsMinMaxConstraint(element, loanProduct, baseDataValidator);

        validateNominalInterestRatePerPeriodMinMaxConstraint(element, loanProduct, baseDataValidator);
    }

    public void validateMinMaxConstraints(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            final LoanProduct loanProduct, Integer cycleNumber) {

        final Map<String, BigDecimal> minmaxValues = loanProduct.fetchBorrowerCycleVariationsForCycleNumber(cycleNumber);
        final String principalParameterName = "principal";
        BigDecimal principalAmount = null;
        BigDecimal minPrincipalAmount = null;
        BigDecimal maxPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            principalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            minPrincipalAmount = minmaxValues.get(LoanProductConstants.minPrincipal);
            maxPrincipalAmount = minmaxValues.get(LoanProductConstants.maxPrincipal);
        }

        if ((minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) == 1)
                && (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) == 1)) {
            baseDataValidator.reset().parameter(principalParameterName).value(principalAmount)
                    .inMinAndMaxAmountRange(minPrincipalAmount, maxPrincipalAmount);
        } else {
            if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) == 1) {
                baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).notLessThanMin(minPrincipalAmount);
            } else if (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) == 1) {
                baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).notGreaterThanMax(maxPrincipalAmount);
            }
        }

        final String numberOfRepaymentsParameterName = "numberOfRepayments";
        Integer maxNumberOfRepayments = null;
        Integer minNumberOfRepayments = null;
        Integer numberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(numberOfRepaymentsParameterName, element)) {
            numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
            if (minmaxValues.get(LoanProductConstants.minNumberOfRepayments) != null) {
                minNumberOfRepayments = minmaxValues.get(LoanProductConstants.minNumberOfRepayments).intValueExact();
            }
            if (minmaxValues.get(LoanProductConstants.maxNumberOfRepayments) != null) {
                maxNumberOfRepayments = minmaxValues.get(LoanProductConstants.maxNumberOfRepayments).intValueExact();
            }
        }

        if (maxNumberOfRepayments != null && maxNumberOfRepayments.compareTo(0) == 1) {
            if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) == 1) {
                baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                        .inMinMaxRange(minNumberOfRepayments, maxNumberOfRepayments);
            } else {
                baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                        .notGreaterThanMax(maxNumberOfRepayments);
            }
        } else if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) == 1) {
            baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                    .notLessThanMin(minNumberOfRepayments);
        }

        final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
        BigDecimal interestRatePerPeriod = null;
        BigDecimal minInterestRatePerPeriod = null;
        BigDecimal maxInterestRatePerPeriod = null;
        if (this.fromApiJsonHelper.parameterExists(interestRatePerPeriodParameterName, element)) {
            interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName, element);
            minInterestRatePerPeriod = minmaxValues.get(LoanProductConstants.minInterestRatePerPeriod);
            maxInterestRatePerPeriod = minmaxValues.get(LoanProductConstants.maxInterestRatePerPeriod);
        }
        if (maxInterestRatePerPeriod != null) {
            if (minInterestRatePerPeriod != null) {
                baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod)
                        .inMinAndMaxAmountRange(minInterestRatePerPeriod, maxInterestRatePerPeriod);
            } else {
                baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod)
                        .notGreaterThanMax(maxInterestRatePerPeriod);
            }
        } else if (minInterestRatePerPeriod != null) {
            baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod)
                    .notLessThanMin(minInterestRatePerPeriod);
        }

    }

    private void validatePrincipalMinMaxConstraint(final JsonElement element, final LoanProduct loanProduct,
            final DataValidatorBuilder baseDataValidator) {

        boolean principalUpdated = false;
        boolean minPrincipalUpdated = false;
        boolean maxPrincipalUpdated = false;
        final String principalParameterName = "principal";
        BigDecimal principalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            principalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            principalUpdated = true;
        } else {
            principalAmount = loanProduct.getPrincipalAmount().getAmount();
        }

        final String minPrincipalParameterName = "minPrincipal";
        BigDecimal minPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(minPrincipalParameterName, element)) {
            minPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minPrincipalParameterName, element);
            minPrincipalUpdated = true;
        } else {
            minPrincipalAmount = loanProduct.getMinPrincipalAmount().getAmount();
        }

        final String maxPrincipalParameterName = "maxPrincipal";
        BigDecimal maxPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(maxPrincipalParameterName, element)) {
            maxPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxPrincipalParameterName, element);
            maxPrincipalUpdated = true;
        } else {
            maxPrincipalAmount = loanProduct.getMaxPrincipalAmount().getAmount();
        }

        if (minPrincipalUpdated) {
            baseDataValidator.reset().parameter(minPrincipalParameterName).value(minPrincipalAmount).notGreaterThanMax(maxPrincipalAmount);
        }

        if (maxPrincipalUpdated) {
            baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).notLessThanMin(minPrincipalAmount);
        }

        if ((principalUpdated || minPrincipalUpdated || maxPrincipalUpdated)) {

            if ((minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) == 1)
                    && (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) == 1)) {
                baseDataValidator.reset().parameter(principalParameterName).value(principalAmount)
                        .inMinAndMaxAmountRange(minPrincipalAmount, maxPrincipalAmount);
            } else {
                if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) == 1) {
                    baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).notLessThanMin(minPrincipalAmount);
                } else if (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) == 1) {
                    baseDataValidator.reset().parameter(principalParameterName).value(principalAmount)
                            .notGreaterThanMax(maxPrincipalAmount);
                }
            }
        }
    }

    private void validateNumberOfRepaymentsMinMaxConstraint(final JsonElement element, final LoanProduct loanProduct,
            final DataValidatorBuilder baseDataValidator) {
        boolean numberOfRepaymentsUpdated = false;
        boolean minNumberOfRepaymentsUpdated = false;
        boolean maxNumberOfRepaymentsUpdated = false;

        final String numberOfRepaymentsParameterName = "numberOfRepayments";
        Integer numberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(numberOfRepaymentsParameterName, element)) {
            numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
            numberOfRepaymentsUpdated = true;
        } else {
            numberOfRepayments = loanProduct.getNumberOfRepayments();
        }

        final String minNumberOfRepaymentsParameterName = "minNumberOfRepayments";
        Integer minNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(minNumberOfRepaymentsParameterName, element)) {
            minNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(minNumberOfRepaymentsParameterName, element);
            minNumberOfRepaymentsUpdated = true;
        } else {
            minNumberOfRepayments = loanProduct.getMinNumberOfRepayments();
        }

        final String maxNumberOfRepaymentsParameterName = "maxNumberOfRepayments";
        Integer maxNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(maxNumberOfRepaymentsParameterName, element)) {
            maxNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(maxNumberOfRepaymentsParameterName, element);
            maxNumberOfRepaymentsUpdated = true;
        } else {
            maxNumberOfRepayments = loanProduct.getMaxNumberOfRepayments();
        }

        if (minNumberOfRepaymentsUpdated) {
            baseDataValidator.reset().parameter(minNumberOfRepaymentsParameterName).value(minNumberOfRepayments).ignoreIfNull()
                    .notGreaterThanMax(maxNumberOfRepayments);
        }

        if (maxNumberOfRepaymentsUpdated) {
            baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments)
                    .notLessThanMin(minNumberOfRepayments);
        }

        if (numberOfRepaymentsUpdated || minNumberOfRepaymentsUpdated || maxNumberOfRepaymentsUpdated) {
            if (maxNumberOfRepayments != null && maxNumberOfRepayments.compareTo(0) == 1) {
                if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) == 1) {
                    baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                            .inMinMaxRange(minNumberOfRepayments, maxNumberOfRepayments);
                } else {
                    baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                            .notGreaterThanMax(maxNumberOfRepayments);
                }
            } else if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) == 1) {
                baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                        .notLessThanMin(minNumberOfRepayments);
            }
        }
    }

    private void validateNominalInterestRatePerPeriodMinMaxConstraint(final JsonElement element, final LoanProduct loanProduct,
            final DataValidatorBuilder baseDataValidator) {

        if ((this.fromApiJsonHelper.parameterExists("isLinkedToFloatingInterestRates", element) && this.fromApiJsonHelper
                .extractBooleanNamed("isLinkedToFloatingInterestRates", element) == true) || loanProduct.isLinkedToFloatingInterestRate()) { return; }
        boolean iRPUpdated = false;
        boolean minIRPUpdated = false;
        boolean maxIRPUpdated = false;
        final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
        BigDecimal interestRatePerPeriod = null;

        if (this.fromApiJsonHelper.parameterExists(interestRatePerPeriodParameterName, element)) {
            interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName, element);
            iRPUpdated = true;
        } else {
            interestRatePerPeriod = loanProduct.getNominalInterestRatePerPeriod();
        }

        final String minInterestRatePerPeriodParameterName = "minInterestRatePerPeriod";
        BigDecimal minInterestRatePerPeriod = null;
        if (this.fromApiJsonHelper.parameterExists(minInterestRatePerPeriodParameterName, element)) {
            minInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minInterestRatePerPeriodParameterName,
                    element);
            minIRPUpdated = true;
        } else {
            minInterestRatePerPeriod = loanProduct.getMinNominalInterestRatePerPeriod();
        }

        final String maxInterestRatePerPeriodParameterName = "maxInterestRatePerPeriod";
        BigDecimal maxInterestRatePerPeriod = null;
        if (this.fromApiJsonHelper.parameterExists(maxInterestRatePerPeriodParameterName, element)) {
            maxInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxInterestRatePerPeriodParameterName,
                    element);
            maxIRPUpdated = true;
        } else {
            maxInterestRatePerPeriod = loanProduct.getMaxNominalInterestRatePerPeriod();
        }

        if (minIRPUpdated) {
            baseDataValidator.reset().parameter(minInterestRatePerPeriodParameterName).value(minInterestRatePerPeriod).ignoreIfNull()
                    .notGreaterThanMax(maxInterestRatePerPeriod);
        }

        if (maxIRPUpdated) {
            baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod).ignoreIfNull()
                    .notLessThanMin(minInterestRatePerPeriod);
        }

        if (iRPUpdated || minIRPUpdated || maxIRPUpdated) {
            if (maxInterestRatePerPeriod != null) {
                if (minInterestRatePerPeriod != null) {
                    baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod)
                            .inMinAndMaxAmountRange(minInterestRatePerPeriod, maxInterestRatePerPeriod);
                } else {
                    baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod)
                            .notGreaterThanMax(maxInterestRatePerPeriod);
                }
            } else if (minInterestRatePerPeriod != null) {
                baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod)
                        .notLessThanMin(minInterestRatePerPeriod);
            }
        }
    }

    private boolean isCashBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.CASH_BASED.getValue().equals(accountingRuleType);
    }

    private boolean isAccrualBasedAccounting(final Integer accountingRuleType) {
        return isUpfrontAccrualAccounting(accountingRuleType) || isPeriodicAccounting(accountingRuleType);
    }

    private boolean isUpfrontAccrualAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.ACCRUAL_UPFRONT.getValue().equals(accountingRuleType);
    }

    private boolean isPeriodicAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.ACCRUAL_PERIODIC.getValue().equals(accountingRuleType);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    private void validateBorrowerCycleVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        validateBorrowerCyclePrincipalVariations(element, baseDataValidator);
        validateBorrowerCycleRepaymentVariations(element, baseDataValidator);
        validateBorrowerCycleInterestVariations(element, baseDataValidator);
    }

    private void validateBorrowerCyclePrincipalVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {

        validateBorrowerCycleVariations(element, baseDataValidator, LoanProductConstants.principalVariationsForBorrowerCycleParameterName,
                LoanProductConstants.principalPerCycleParameterName, LoanProductConstants.minPrincipalPerCycleParameterName,
                LoanProductConstants.maxPrincipalPerCycleParameterName, LoanProductConstants.principalValueUsageConditionParamName,
                LoanProductConstants.principalCycleNumbersParamName);
    }

    private void validateBorrowerCycleRepaymentVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        validateBorrowerCycleVariations(element, baseDataValidator,
                LoanProductConstants.numberOfRepaymentVariationsForBorrowerCycleParameterName,
                LoanProductConstants.numberOfRepaymentsPerCycleParameterName,
                LoanProductConstants.minNumberOfRepaymentsPerCycleParameterName,
                LoanProductConstants.maxNumberOfRepaymentsPerCycleParameterName,
                LoanProductConstants.repaymentValueUsageConditionParamName, LoanProductConstants.repaymentCycleNumberParamName);
    }

    private void validateBorrowerCycleInterestVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        validateBorrowerCycleVariations(element, baseDataValidator,
                LoanProductConstants.interestRateVariationsForBorrowerCycleParameterName,
                LoanProductConstants.interestRatePerPeriodPerCycleParameterName,
                LoanProductConstants.minInterestRatePerPeriodPerCycleParameterName,
                LoanProductConstants.maxInterestRatePerPeriodPerCycleParameterName,
                LoanProductConstants.interestRateValueUsageConditionParamName, LoanProductConstants.interestRateCycleNumberParamName);
    }

    private void validateBorrowerCycleVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            final String variationParameterName, final String defaultParameterName, final String minParameterName,
            final String maxParameterName, final String valueUsageConditionParamName, final String cycleNumbersParamName) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        Integer lastCycleNumber = 0;
        LoanProductValueConditionType lastConditionType = LoanProductValueConditionType.EQUAL;
        if (this.fromApiJsonHelper.parameterExists(variationParameterName, element)) {
            final JsonArray variationArray = this.fromApiJsonHelper.extractJsonArrayNamed(variationParameterName, element);
            if (variationArray != null && variationArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = variationArray.get(i).getAsJsonObject();

                    BigDecimal defaultValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.defaultValueParameterName,
                            jsonObject, locale);
                    BigDecimal minValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.minValueParameterName,
                            jsonObject, locale);
                    BigDecimal maxValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.maxValueParameterName,
                            jsonObject, locale);
                    Integer cycleNumber = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.borrowerCycleNumberParamName,
                            jsonObject, locale);
                    Integer valueUsageCondition = this.fromApiJsonHelper.extractIntegerNamed(
                            LoanProductConstants.valueConditionTypeParamName, jsonObject, locale);

                    baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).notBlank();
                    if (minValue != null) {
                        baseDataValidator.reset().parameter(minParameterName).value(minValue).notGreaterThanMax(maxValue);
                    }

                    if (maxValue != null) {
                        baseDataValidator.reset().parameter(maxParameterName).value(maxValue).notLessThanMin(minValue);
                    }
                    if ((minValue != null && minValue.compareTo(BigDecimal.ZERO) == 1)
                            && (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) == 1)) {
                        baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue)
                                .inMinAndMaxAmountRange(minValue, maxValue);
                    } else {
                        if (minValue != null && minValue.compareTo(BigDecimal.ZERO) == 1) {
                            baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).notLessThanMin(minValue);
                        } else if (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) == 1) {
                            baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).notGreaterThanMax(maxValue);
                        }
                    }

                    LoanProductValueConditionType conditionType = LoanProductValueConditionType.INVALID;
                    if (valueUsageCondition != null) {
                        conditionType = LoanProductValueConditionType.fromInt(valueUsageCondition);
                    }
                    baseDataValidator
                            .reset()
                            .parameter(valueUsageConditionParamName)
                            .value(valueUsageCondition)
                            .notNull()
                            .inMinMaxRange(LoanProductValueConditionType.EQUAL.getValue(),
                                    LoanProductValueConditionType.GREATERTHAN.getValue());
                    if (lastConditionType.equals(LoanProductValueConditionType.EQUAL)
                            && conditionType.equals(LoanProductValueConditionType.GREATERTHAN)) {
                        if (lastCycleNumber == 0) {
                            baseDataValidator.reset().parameter(cycleNumbersParamName)
                                    .failWithCode(LoanProductConstants.VALUE_CONDITION_START_WITH_ERROR);
                            lastCycleNumber = 1;
                        }
                        baseDataValidator.reset().parameter(cycleNumbersParamName).value(cycleNumber).notNull()
                                .integerSameAsNumber(lastCycleNumber);
                    } else if (lastConditionType.equals(LoanProductValueConditionType.EQUAL)) {
                        baseDataValidator.reset().parameter(cycleNumbersParamName).value(cycleNumber).notNull()
                                .integerSameAsNumber(lastCycleNumber + 1);
                    } else if (lastConditionType.equals(LoanProductValueConditionType.GREATERTHAN)) {
                        baseDataValidator.reset().parameter(cycleNumbersParamName).value(cycleNumber).notNull()
                                .integerGreaterThanNumber(lastCycleNumber);
                    }
                    if (conditionType != null) {
                        lastConditionType = conditionType;
                    }
                    if (cycleNumber != null) {
                        lastCycleNumber = cycleNumber;
                    }
                    i++;
                } while (i < variationArray.size());
                if (!lastConditionType.equals(LoanProductValueConditionType.GREATERTHAN)) {
                    baseDataValidator.reset().parameter(cycleNumbersParamName)
                            .failWithCode(LoanProductConstants.VALUE_CONDITION_END_WITH_ERROR);
                }
            }

        }

    }

    private void validateGuaranteeParams(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            final LoanProduct loanProduct) {
        BigDecimal mandatoryGuarantee = BigDecimal.ZERO;
        BigDecimal minimumGuaranteeFromOwnFunds = BigDecimal.ZERO;
        BigDecimal minimumGuaranteeFromGuarantor = BigDecimal.ZERO;
        if (loanProduct != null) {
            mandatoryGuarantee = loanProduct.getLoanProductGuaranteeDetails().getMandatoryGuarantee();
            minimumGuaranteeFromOwnFunds = loanProduct.getLoanProductGuaranteeDetails().getMinimumGuaranteeFromOwnFunds();
            minimumGuaranteeFromGuarantor = loanProduct.getLoanProductGuaranteeDetails().getMinimumGuaranteeFromGuarantor();
        }

        if (loanProduct == null || this.fromApiJsonHelper.parameterExists(LoanProductConstants.mandatoryGuaranteeParamName, element)) {
            mandatoryGuarantee = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanProductConstants.mandatoryGuaranteeParamName,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.mandatoryGuaranteeParamName).value(mandatoryGuarantee).notNull();
            if (mandatoryGuarantee == null) {
                mandatoryGuarantee = BigDecimal.ZERO;
            }
        }

        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGuaranteeFromGuarantorParamName, element)) {
            minimumGuaranteeFromGuarantor = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    LoanProductConstants.minimumGuaranteeFromGuarantorParamName, element);
            if (minimumGuaranteeFromGuarantor == null) {
                minimumGuaranteeFromGuarantor = BigDecimal.ZERO;
            }
        }

        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, element)) {
            minimumGuaranteeFromOwnFunds = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, element);
            if (minimumGuaranteeFromOwnFunds == null) {
                minimumGuaranteeFromOwnFunds = BigDecimal.ZERO;
            }
        }

        if (mandatoryGuarantee.compareTo(minimumGuaranteeFromOwnFunds.add(minimumGuaranteeFromGuarantor)) == -1) {
            baseDataValidator.parameter(LoanProductConstants.mandatoryGuaranteeParamName).failWithCode(
                    "must.be.greter.than.sum.of.min.funds");
        }

    }

    private void validatePartialPeriodSupport(final Integer interestCalculationPeriodType, final DataValidatorBuilder baseDataValidator,
            final JsonElement element, final LoanProduct loanProduct) {
        if (interestCalculationPeriodType != null) {
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                    .fromInt(interestCalculationPeriodType);
            boolean considerPartialPeriodUpdates = interestCalculationPeriodMethod.isDaily();

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName, element)) {
                final Boolean considerPartialInterestEnabled = this.fromApiJsonHelper.extractBooleanNamed(
                        LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName, element);
                baseDataValidator.reset().parameter(LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName)
                        .value(considerPartialInterestEnabled).notNull().isOneOfTheseValues(true, false);
                final boolean considerPartialPeriods = considerPartialInterestEnabled == null ? false : considerPartialInterestEnabled;
                if (interestCalculationPeriodMethod.isDaily()) {
                    if (considerPartialPeriods) {
                        baseDataValidator.reset().parameter(LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName)
                                .failWithCode("not.supported.for.daily.calcualtions");
                    }
                } else {
                    considerPartialPeriodUpdates = considerPartialPeriods;
                }
            }

            if (!considerPartialPeriodUpdates) {
                Boolean isInterestRecalculationEnabled = null;
                if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isInterestRecalculationEnabledParameterName, element)) {
                    isInterestRecalculationEnabled = this.fromApiJsonHelper.extractBooleanNamed(
                            LoanProductConstants.isInterestRecalculationEnabledParameterName, element);
                } else if (loanProduct != null) {
                    isInterestRecalculationEnabled = loanProduct.isInterestRecalculationEnabled();
                }
                if (isInterestRecalculationEnabled != null && isInterestRecalculationEnabled) {
                    baseDataValidator.reset().parameter(LoanProductConstants.isInterestRecalculationEnabledParameterName)
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }

                Boolean multiDisburseLoan = null;
                if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.multiDisburseLoanParameterName, element)) {
                    multiDisburseLoan = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.multiDisburseLoanParameterName,
                            element);
                } else if (loanProduct != null) {
                    isInterestRecalculationEnabled = loanProduct.isMultiDisburseLoan();
                }
                if (multiDisburseLoan != null && multiDisburseLoan) {
                    baseDataValidator.reset().parameter(LoanProductConstants.multiDisburseLoanParameterName)
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }

                Boolean variableInstallments = null;
                if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowVariableInstallmentsParamName, element)) {
                    variableInstallments = this.fromApiJsonHelper.extractBooleanNamed(
                            LoanProductConstants.allowVariableInstallmentsParamName, element);
                } else if (loanProduct != null) {
                    isInterestRecalculationEnabled = loanProduct.allowVariabeInstallments();
                }
                if (variableInstallments != null && variableInstallments) {
                    baseDataValidator.reset().parameter(LoanProductConstants.allowVariableInstallmentsParamName)
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }

                Boolean floatingInterestRates = null;
                if (this.fromApiJsonHelper.parameterExists("isLinkedToFloatingInterestRates", element)) {
                    floatingInterestRates = this.fromApiJsonHelper.extractBooleanNamed("isLinkedToFloatingInterestRates", element);
                } else if (loanProduct != null) {
                    isInterestRecalculationEnabled = loanProduct.isLinkedToFloatingInterestRate();
                }
                if (floatingInterestRates != null && floatingInterestRates) {
                    baseDataValidator.reset().parameter("isLinkedToFloatingInterestRates")
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }
            }

        }
    }
}