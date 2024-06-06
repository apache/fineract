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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
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
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.common.AccountingConstants.LoanProductAccountingParams;
import org.apache.fineract.accounting.common.AccountingValidations;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsValidator;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductPaymentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductValueConditionType;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.loanproduct.exception.EqualAmortizationUnsupportedFeatureException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public final class LoanProductDataValidator {

    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String FUND_ID = "fundId";
    public static final String INCLUDE_IN_BORROWER_CYCLE = "includeInBorrowerCycle";
    public static final String CURRENCY_CODE = "currencyCode";
    public static final String DIGITS_AFTER_DECIMAL = "digitsAfterDecimal";
    public static final String IN_MULTIPLES_OF = "inMultiplesOf";
    public static final String PRINCIPAL = "principal";
    public static final String MIN_PRINCIPAL = "minPrincipal";
    public static final String MAX_PRINCIPAL = "maxPrincipal";
    public static final String NUMBER_OF_REPAYMENTS = "numberOfRepayments";
    public static final String MIN_NUMBER_OF_REPAYMENTS = "minNumberOfRepayments";
    public static final String MAX_NUMBER_OF_REPAYMENTS = "maxNumberOfRepayments";
    public static final String REPAYMENT_EVERY = "repaymentEvery";
    public static final String REPAYMENT_FREQUENCY_TYPE = "repaymentFrequencyType";
    public static final String AMORTIZATION_TYPE = "amortizationType";
    public static final String INTEREST_TYPE = "interestType";
    public static final String INTEREST_CALCULATION_PERIOD_TYPE = "interestCalculationPeriodType";
    public static final String IN_ARREARS_TOLERANCE = "inArrearsTolerance";
    public static final String TRANSACTION_PROCESSING_STRATEGY_CODE = "transactionProcessingStrategyCode";
    public static final String ADVANCED_PAYMENT_ALLOCATIONS = "paymentAllocation";
    public static final String CREDIT_ALLOCATIONS = "creditAllocation";
    public static final String GRACE_ON_PRINCIPAL_PAYMENT = "graceOnPrincipalPayment";
    public static final String GRACE_ON_INTEREST_PAYMENT = "graceOnInterestPayment";
    public static final String GRACE_ON_INTEREST_CHARGED = "graceOnInterestCharged";
    public static final String IS_LINKED_TO_FLOATING_INTEREST_RATES = "isLinkedToFloatingInterestRates";
    public static final String INTEREST_RATE_PER_PERIOD = "interestRatePerPeriod";
    public static final String MIN_INTEREST_RATE_PER_PERIOD = "minInterestRatePerPeriod";
    public static final String MAX_INTEREST_RATE_PER_PERIOD = "maxInterestRatePerPeriod";
    public static final String INTEREST_RATE_FREQUENCY_TYPE = "interestRateFrequencyType";
    public static final String FLOATING_RATES_ID = "floatingRatesId";
    public static final String INTEREST_RATE_DIFFERENTIAL = "interestRateDifferential";
    public static final String MIN_DIFFERENTIAL_LENDING_RATE = "minDifferentialLendingRate";
    public static final String DEFAULT_DIFFERENTIAL_LENDING_RATE = "defaultDifferentialLendingRate";
    public static final String MAX_DIFFERENTIAL_LENDING_RATE = "maxDifferentialLendingRate";
    public static final String IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED = "isFloatingInterestRateCalculationAllowed";
    public static final String ACCOUNTING_RULE = "accountingRule";
    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat", NAME, DESCRIPTION, FUND_ID,
            CURRENCY_CODE, DIGITS_AFTER_DECIMAL, IN_MULTIPLES_OF, PRINCIPAL, MIN_PRINCIPAL, MAX_PRINCIPAL, REPAYMENT_EVERY,
            NUMBER_OF_REPAYMENTS, MIN_NUMBER_OF_REPAYMENTS, MAX_NUMBER_OF_REPAYMENTS, REPAYMENT_FREQUENCY_TYPE, INTEREST_RATE_PER_PERIOD,
            MIN_INTEREST_RATE_PER_PERIOD, MAX_INTEREST_RATE_PER_PERIOD, INTEREST_RATE_FREQUENCY_TYPE, AMORTIZATION_TYPE, INTEREST_TYPE,
            INTEREST_CALCULATION_PERIOD_TYPE, LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
            IN_ARREARS_TOLERANCE, TRANSACTION_PROCESSING_STRATEGY_CODE, ADVANCED_PAYMENT_ALLOCATIONS, CREDIT_ALLOCATIONS,
            GRACE_ON_PRINCIPAL_PAYMENT, "recurringMoratoriumOnPrincipalPeriods", GRACE_ON_INTEREST_PAYMENT, GRACE_ON_INTEREST_CHARGED,
            "charges", ACCOUNTING_RULE, INCLUDE_IN_BORROWER_CYCLE, "startDate", "closeDate", "externalId",
            IS_LINKED_TO_FLOATING_INTEREST_RATES, FLOATING_RATES_ID, INTEREST_RATE_DIFFERENTIAL, MIN_DIFFERENTIAL_LENDING_RATE,
            DEFAULT_DIFFERENTIAL_LENDING_RATE, MAX_DIFFERENTIAL_LENDING_RATE, IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED,
            "syncExpectedWithDisbursementDate", LoanProductAccountingParams.FEES_RECEIVABLE.getValue(),
            LoanProductAccountingParams.FUND_SOURCE.getValue(), LoanProductAccountingParams.INCOME_FROM_FEES.getValue(),
            LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(),
            LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue(), LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(),
            LoanProductAccountingParams.OVERPAYMENT.getValue(), LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(),
            LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), LoanProductAccountingParams.GOODWILL_CREDIT.getValue(),
            LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue(),
            LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
            LoanProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue(), LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(),
            LoanProductAccountingParams.PENALTY_INCOME_ACCOUNT_MAPPING.getValue(),
            LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE.getValue(), LoanProductAccountingParams.CHARGE_OFF_EXPENSE.getValue(),
            LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES.getValue(),
            LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(),
            LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(),
            LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(),
            LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(),
            LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(),
            LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME,
            LoanProductConstants.PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME,
            LoanProductConstants.INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME,
            LoanProductConstants.NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, LoanProductConstants.SHORT_NAME,
            LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME, LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME,
            LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME, LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME,
            LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME,
            LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME, LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME,
            LoanProductConstants.rescheduleStrategyMethodParameterName,
            LoanProductConstants.interestRecalculationCompoundingMethodParameterName,
            LoanProductConstants.recalculationRestFrequencyIntervalParameterName,
            LoanProductConstants.recalculationRestFrequencyTypeParameterName,
            LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName,
            LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName,
            LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName,
            LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT, LoanProductConstants.mandatoryGuaranteeParamName,
            LoanProductConstants.holdGuaranteeFundsParamName, LoanProductConstants.minimumGuaranteeFromGuarantorParamName,
            LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, LoanProductConstants.principalThresholdForLastInstallmentParamName,
            LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME,
            LoanProductConstants.canDefineEmiAmountParamName, LoanProductConstants.installmentAmountInMultiplesOfParamName,
            LoanProductConstants.preClosureInterestCalculationStrategyParamName, LoanProductConstants.allowAttributeOverridesParamName,
            LoanProductConstants.allowVariableInstallmentsParamName, LoanProductConstants.minimumGapBetweenInstallments,
            LoanProductConstants.maximumGapBetweenInstallments, LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName,
            LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName,
            LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName,
            LoanProductConstants.recalculationRestFrequencyWeekdayParamName, LoanProductConstants.recalculationRestFrequencyNthDayParamName,
            LoanProductConstants.recalculationRestFrequencyOnDayParamName,
            LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, LoanProductConstants.allowCompoundingOnEodParamName,
            LoanProductConstants.CAN_USE_FOR_TOPUP, LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, LoanProductConstants.RATES_PARAM_NAME,
            LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName, LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS,
            LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED, LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE,
            LoanProductConstants.OVER_APPLIED_NUMBER, LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME,
            LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT, LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT,
            LoanProductConstants.ENABLE_DOWN_PAYMENT, LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT,
            LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, LoanProductConstants.REPAYMENT_START_DATE_TYPE,
            LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, LoanProductConstants.LOAN_SCHEDULE_TYPE,
            LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE, LoanProductConstants.FIXED_LENGTH));

    private static final String[] SUPPORTED_LOAN_CONFIGURABLE_ATTRIBUTES = { LoanProductConstants.amortizationTypeParamName,
            LoanProductConstants.interestTypeParamName, LoanProductConstants.transactionProcessingStrategyCodeParamName,
            LoanProductConstants.interestCalculationPeriodTypeParamName, LoanProductConstants.inArrearsToleranceParamName,
            LoanProductConstants.repaymentEveryParamName, LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName,
            LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME };
    public static final String LOANPRODUCT = "loanproduct";
    public static final String OVER_APPLIED_CALCULATION_TYPE = "overAppliedCalculationType";
    public static final String OPENING_SQUARE_BRACKET = "[";
    public static final String CLOSING_SQUARE_BRACKET = "]";
    public static final String DOT = ".";

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final AdvancedPaymentAllocationsJsonParser advancedPaymentAllocationsJsonParser;
    private final AdvancedPaymentAllocationsValidator advancedPaymentAllocationsValidator;

    public void validateForCreate(final JsonCommand command) {
        String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(LOANPRODUCT);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
        baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(100);

        final String shortName = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.SHORT_NAME, element);
        baseDataValidator.reset().parameter(LoanProductConstants.SHORT_NAME).value(shortName).notBlank().notExceedingLengthOf(4);

        final String description = this.fromApiJsonHelper.extractStringNamed(DESCRIPTION, element);
        baseDataValidator.reset().parameter(DESCRIPTION).value(description).notExceedingLengthOf(500);

        if (this.fromApiJsonHelper.parameterExists(FUND_ID, element)) {
            final Long fundId = this.fromApiJsonHelper.extractLongNamed(FUND_ID, element);
            baseDataValidator.reset().parameter(FUND_ID).value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        boolean isEqualAmortization = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element);
            baseDataValidator.reset().parameter(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM).value(isEqualAmortization).ignoreIfNull()
                    .validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT, element)) {
            final Long minimumDaysBetweenDisbursalAndFirstRepayment = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT, element);
            baseDataValidator.reset().parameter(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT)
                    .value(minimumDaysBetweenDisbursalAndFirstRepayment).ignoreIfNull().integerGreaterThanZero();
        }

        final Boolean includeInBorrowerCycle = this.fromApiJsonHelper.extractBooleanNamed(INCLUDE_IN_BORROWER_CYCLE, element);
        baseDataValidator.reset().parameter(INCLUDE_IN_BORROWER_CYCLE).value(includeInBorrowerCycle).ignoreIfNull()
                .validateForBooleanValue();

        // terms
        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(CURRENCY_CODE, element);
        baseDataValidator.reset().parameter(CURRENCY_CODE).value(currencyCode).notBlank().notExceedingLengthOf(3);

        final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerNamed(DIGITS_AFTER_DECIMAL, element, Locale.getDefault());
        baseDataValidator.reset().parameter(DIGITS_AFTER_DECIMAL).value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);

        final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed(IN_MULTIPLES_OF, element, Locale.getDefault());
        baseDataValidator.reset().parameter(IN_MULTIPLES_OF).value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(PRINCIPAL, element);
        baseDataValidator.reset().parameter(PRINCIPAL).value(principal).positiveAmount();

        final String minPrincipalParameterName = MIN_PRINCIPAL;
        BigDecimal minPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(minPrincipalParameterName, element)) {
            minPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minPrincipalParameterName, element);
            baseDataValidator.reset().parameter(minPrincipalParameterName).value(minPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        final String maxPrincipalParameterName = MAX_PRINCIPAL;
        BigDecimal maxPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(maxPrincipalParameterName, element)) {
            maxPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxPrincipalParameterName, element);
            baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        if (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) >= 0) {

            if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).notLessThanMin(minPrincipalAmount);
                if (minPrincipalAmount.compareTo(maxPrincipalAmount) <= 0 && principal != null) {
                    baseDataValidator.reset().parameter(PRINCIPAL).value(principal).inMinAndMaxAmountRange(minPrincipalAmount,
                            maxPrincipalAmount);
                }
            } else if (principal != null) {
                baseDataValidator.reset().parameter(PRINCIPAL).value(principal).notGreaterThanMax(maxPrincipalAmount);
            }
        } else if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) >= 0 && principal != null) {
            baseDataValidator.reset().parameter(PRINCIPAL).value(principal).notLessThanMin(minPrincipalAmount);
        }

        final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(NUMBER_OF_REPAYMENTS, element);
        baseDataValidator.reset().parameter(NUMBER_OF_REPAYMENTS).value(numberOfRepayments).notNull().integerGreaterThanZero();

        final String minNumberOfRepaymentsParameterName = MIN_NUMBER_OF_REPAYMENTS;
        Integer minNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(minNumberOfRepaymentsParameterName, element)) {
            minNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(minNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(minNumberOfRepaymentsParameterName).value(minNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        final String maxNumberOfRepaymentsParameterName = MAX_NUMBER_OF_REPAYMENTS;
        Integer maxNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(maxNumberOfRepaymentsParameterName, element)) {
            maxNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(maxNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        if (maxNumberOfRepayments != null && maxNumberOfRepayments.compareTo(0) > 0) {
            if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
                baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments)
                        .notLessThanMin(minNumberOfRepayments);
                if (minNumberOfRepayments.compareTo(maxNumberOfRepayments) <= 0) {
                    baseDataValidator.reset().parameter(NUMBER_OF_REPAYMENTS).value(numberOfRepayments).inMinMaxRange(minNumberOfRepayments,
                            maxNumberOfRepayments);
                }
            } else {
                baseDataValidator.reset().parameter(NUMBER_OF_REPAYMENTS).value(numberOfRepayments)
                        .notGreaterThanMax(maxNumberOfRepayments);
            }
        } else if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
            baseDataValidator.reset().parameter(NUMBER_OF_REPAYMENTS).value(numberOfRepayments).notLessThanMin(minNumberOfRepayments);
        }

        final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(REPAYMENT_EVERY, element);
        baseDataValidator.reset().parameter(REPAYMENT_EVERY).value(repaymentEvery).notNull().integerGreaterThanZero();

        final Integer repaymentFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(REPAYMENT_FREQUENCY_TYPE, element,
                Locale.getDefault());
        baseDataValidator.reset().parameter(REPAYMENT_FREQUENCY_TYPE).value(repaymentFrequencyType).notNull().inMinMaxRange(0, 3);

        // settings
        final Integer amortizationType = this.fromApiJsonHelper.extractIntegerNamed(AMORTIZATION_TYPE, element, Locale.getDefault());
        baseDataValidator.reset().parameter(AMORTIZATION_TYPE).value(amortizationType).notNull().inMinMaxRange(0, 1);

        final Integer interestType = this.fromApiJsonHelper.extractIntegerNamed(INTEREST_TYPE, element, Locale.getDefault());
        baseDataValidator.reset().parameter(INTEREST_TYPE).value(interestType).notNull().inMinMaxRange(0, 1);

        final Integer interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerNamed(INTEREST_CALCULATION_PERIOD_TYPE, element,
                Locale.getDefault());
        baseDataValidator.reset().parameter(INTEREST_CALCULATION_PERIOD_TYPE).value(interestCalculationPeriodType).notNull()
                .inMinMaxRange(0, 1);

        final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(IN_ARREARS_TOLERANCE, element);
        baseDataValidator.reset().parameter(IN_ARREARS_TOLERANCE).value(inArrearsTolerance).ignoreIfNull().zeroOrPositiveAmount();

        final String transactionProcessingStrategyCode = this.fromApiJsonHelper.extractStringNamed(TRANSACTION_PROCESSING_STRATEGY_CODE,
                element);
        baseDataValidator.reset().parameter(TRANSACTION_PROCESSING_STRATEGY_CODE).value(transactionProcessingStrategyCode).notBlank();
        // Validating whether the processor is existing
        loanRepaymentScheduleTransactionProcessorFactory.determineProcessor(transactionProcessingStrategyCode);

        Long delinquencyBucketId = null;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME, element)) {
            delinquencyBucketId = this.fromApiJsonHelper.extractLongNamed(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME).value(delinquencyBucketId)
                    .ignoreIfNull().integerGreaterThanZero();
        }

        // grace validation
        final Integer graceOnPrincipalPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(GRACE_ON_PRINCIPAL_PAYMENT, element);
        baseDataValidator.reset().parameter(GRACE_ON_PRINCIPAL_PAYMENT).value(graceOnPrincipalPayment).zeroOrPositiveAmount();

        final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(GRACE_ON_INTEREST_PAYMENT, element);
        baseDataValidator.reset().parameter(GRACE_ON_INTEREST_PAYMENT).value(graceOnInterestPayment).zeroOrPositiveAmount();

        final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(GRACE_ON_INTEREST_CHARGED, element);
        baseDataValidator.reset().parameter(GRACE_ON_INTEREST_CHARGED).value(graceOnInterestCharged).zeroOrPositiveAmount();

        final Integer graceOnArrearsAgeing = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, element);
        baseDataValidator.reset().parameter(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME).value(graceOnArrearsAgeing)
                .integerZeroOrGreater();

        final Integer overdueDaysForNPA = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, element);
        baseDataValidator.reset().parameter(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME).value(overdueDaysForNPA)
                .integerZeroOrGreater();

        final Integer daysInYearType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME,
                element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME).value(daysInYearType).notNull()
                .isOneOfTheseValues(1, 360, 364, 365);

        final Integer daysInMonthType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME,
                element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME).value(daysInMonthType).notNull()
                .isOneOfTheseValues(1, 30);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME,
                element)) {
            Boolean npaChangeConfig = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME)
                    .value(npaChangeConfig).notNull().isOneOfTheseValues(true, false);
        }

        // Interest recalculation settings
        final Boolean isInterestRecalculationEnabled = this.fromApiJsonHelper
                .extractBooleanNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, element);
        baseDataValidator.reset().parameter(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME)
                .value(isInterestRecalculationEnabled).notNull().isOneOfTheseValues(true, false);

        if (isInterestRecalculationEnabled != null && isInterestRecalculationEnabled) {
            if (isEqualAmortization) {
                throw new EqualAmortizationUnsupportedFeatureException("interest.recalculation", "interest recalculation");
            }
            validateInterestRecalculationParams(element, baseDataValidator, null);
        }

        // interest rates
        boolean isInterestBearing = false;
        if (this.fromApiJsonHelper.parameterExists(IS_LINKED_TO_FLOATING_INTEREST_RATES, element)
                && this.fromApiJsonHelper.extractBooleanNamed(IS_LINKED_TO_FLOATING_INTEREST_RATES, element)) {
            if (isEqualAmortization) {
                throw new EqualAmortizationUnsupportedFeatureException("floating.interest.rate", "floating interest rate");
            }
            if (this.fromApiJsonHelper.parameterExists(INTEREST_RATE_PER_PERIOD, element)) {
                baseDataValidator.reset().parameter(INTEREST_RATE_PER_PERIOD).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "interestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists(MIN_INTEREST_RATE_PER_PERIOD, element)) {
                baseDataValidator.reset().parameter(MIN_INTEREST_RATE_PER_PERIOD).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "minInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists(MAX_INTEREST_RATE_PER_PERIOD, element)) {
                baseDataValidator.reset().parameter(MAX_INTEREST_RATE_PER_PERIOD).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "maxInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists(INTEREST_RATE_FREQUENCY_TYPE, element)) {
                baseDataValidator.reset().parameter(INTEREST_RATE_FREQUENCY_TYPE).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "interestRateFrequencyType param is not supported when isLinkedToFloatingInterestRates is true");
            }
            if ((interestType == null || !interestType.equals(InterestMethod.DECLINING_BALANCE.getValue()))
                    || (isInterestRecalculationEnabled == null || !isInterestRecalculationEnabled)) {
                baseDataValidator.reset().parameter(IS_LINKED_TO_FLOATING_INTEREST_RATES).failWithCode(
                        "supported.only.for.declining.balance.interest.recalculation.enabled",
                        "Floating interest rates are supported only for declining balance and interest recalculation enabled loan products");
            }

            final Integer floatingRatesId = this.fromApiJsonHelper.extractIntegerNamed(FLOATING_RATES_ID, element, Locale.getDefault());
            baseDataValidator.reset().parameter(FLOATING_RATES_ID).value(floatingRatesId).notNull();

            final BigDecimal interestRateDifferential = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(INTEREST_RATE_DIFFERENTIAL,
                    element);
            baseDataValidator.reset().parameter(INTEREST_RATE_DIFFERENTIAL).value(interestRateDifferential).notNull()
                    .zeroOrPositiveAmount();

            final String minDifferentialLendingRateParameterName = MIN_DIFFERENTIAL_LENDING_RATE;
            BigDecimal minDifferentialLendingRate = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(minDifferentialLendingRateParameterName, element);
            baseDataValidator.reset().parameter(minDifferentialLendingRateParameterName).value(minDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            final String defaultDifferentialLendingRateParameterName = DEFAULT_DIFFERENTIAL_LENDING_RATE;
            BigDecimal defaultDifferentialLendingRate = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(defaultDifferentialLendingRateParameterName, element);
            baseDataValidator.reset().parameter(defaultDifferentialLendingRateParameterName).value(defaultDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            final String maxDifferentialLendingRateParameterName = MAX_DIFFERENTIAL_LENDING_RATE;
            BigDecimal maxDifferentialLendingRate = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(maxDifferentialLendingRateParameterName, element);
            baseDataValidator.reset().parameter(maxDifferentialLendingRateParameterName).value(maxDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            if (defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0
                    && minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(DEFAULT_DIFFERENTIAL_LENDING_RATE).value(defaultDifferentialLendingRate)
                        .notLessThanMin(minDifferentialLendingRate);
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0
                    && minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(MAX_DIFFERENTIAL_LENDING_RATE).value(maxDifferentialLendingRate)
                        .notLessThanMin(minDifferentialLendingRate);
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0
                    && defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(MAX_DIFFERENTIAL_LENDING_RATE).value(maxDifferentialLendingRate)
                        .notLessThanMin(defaultDifferentialLendingRate);
            }

            final Boolean isFloatingInterestRateCalculationAllowed = this.fromApiJsonHelper
                    .extractBooleanNamed(IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED, element);
            baseDataValidator.reset().parameter(IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED)
                    .value(isFloatingInterestRateCalculationAllowed).notNull().isOneOfTheseValues(true, false);
            isInterestBearing = true;
        } else {
            if (this.fromApiJsonHelper.parameterExists(FLOATING_RATES_ID, element)) {
                baseDataValidator.reset().parameter(FLOATING_RATES_ID).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "floatingRatesId param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(INTEREST_RATE_DIFFERENTIAL, element)) {
                baseDataValidator.reset().parameter(INTEREST_RATE_DIFFERENTIAL).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "interestRateDifferential param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(MIN_DIFFERENTIAL_LENDING_RATE, element)) {
                baseDataValidator.reset().parameter(MIN_DIFFERENTIAL_LENDING_RATE).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "minDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(DEFAULT_DIFFERENTIAL_LENDING_RATE, element)) {
                baseDataValidator.reset().parameter(DEFAULT_DIFFERENTIAL_LENDING_RATE).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "defaultDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(MAX_DIFFERENTIAL_LENDING_RATE, element)) {
                baseDataValidator.reset().parameter(MAX_DIFFERENTIAL_LENDING_RATE).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "maxDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED, element)) {
                baseDataValidator.reset().parameter(IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "isFloatingInterestRateCalculationAllowed param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            BigDecimal interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(INTEREST_RATE_PER_PERIOD, element);
            baseDataValidator.reset().parameter(INTEREST_RATE_PER_PERIOD).value(interestRatePerPeriod).notNull().zeroOrPositiveAmount();

            final String minInterestRatePerPeriodParameterName = MIN_INTEREST_RATE_PER_PERIOD;
            BigDecimal minInterestRatePerPeriod = null;
            if (this.fromApiJsonHelper.parameterExists(minInterestRatePerPeriodParameterName, element)) {
                minInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minInterestRatePerPeriodParameterName,
                        element);
                baseDataValidator.reset().parameter(minInterestRatePerPeriodParameterName).value(minInterestRatePerPeriod).ignoreIfNull()
                        .zeroOrPositiveAmount();
            }

            final String maxInterestRatePerPeriodParameterName = MAX_INTEREST_RATE_PER_PERIOD;
            BigDecimal maxInterestRatePerPeriod = null;
            if (this.fromApiJsonHelper.parameterExists(maxInterestRatePerPeriodParameterName, element)) {
                maxInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxInterestRatePerPeriodParameterName,
                        element);
                baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod).ignoreIfNull()
                        .zeroOrPositiveAmount();
            }

            if (maxInterestRatePerPeriod != null && maxInterestRatePerPeriod.compareTo(BigDecimal.ZERO) >= 0) {
                if (minInterestRatePerPeriod != null && minInterestRatePerPeriod.compareTo(BigDecimal.ZERO) >= 0) {
                    baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod)
                            .notLessThanMin(minInterestRatePerPeriod);
                    if (minInterestRatePerPeriod.compareTo(maxInterestRatePerPeriod) <= 0) {
                        baseDataValidator.reset().parameter(INTEREST_RATE_PER_PERIOD).value(interestRatePerPeriod)
                                .inMinAndMaxAmountRange(minInterestRatePerPeriod, maxInterestRatePerPeriod);
                    }
                } else {
                    baseDataValidator.reset().parameter(INTEREST_RATE_PER_PERIOD).value(interestRatePerPeriod)
                            .notGreaterThanMax(maxInterestRatePerPeriod);
                }
            } else if (minInterestRatePerPeriod != null && minInterestRatePerPeriod.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(INTEREST_RATE_PER_PERIOD).value(interestRatePerPeriod)
                        .notLessThanMin(minInterestRatePerPeriod);
            }

            final Integer interestRateFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(INTEREST_RATE_FREQUENCY_TYPE, element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter(INTEREST_RATE_FREQUENCY_TYPE).value(interestRateFrequencyType).notNull().inMinMaxRange(0,
                    4);
            isInterestBearing = interestRatePerPeriod.compareTo(BigDecimal.ZERO) > 0;
        }

        // Fixed Length validation

        fixedLengthValidations(transactionProcessingStrategyCode, isInterestBearing, numberOfRepayments, repaymentEvery, element,
                baseDataValidator);

        // Guarantee Funds
        Boolean holdGuaranteeFunds = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.holdGuaranteeFundsParamName, element)) {
            holdGuaranteeFunds = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.holdGuaranteeFundsParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.holdGuaranteeFundsParamName).value(holdGuaranteeFunds).notNull()
                    .isOneOfTheseValues(true, false);
        }

        if (holdGuaranteeFunds != null && holdGuaranteeFunds) {
            validateGuaranteeParams(element, baseDataValidator, null);
        }

        BigDecimal principalThresholdForLastInstallment = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.principalThresholdForLastInstallmentParamName)
                .value(principalThresholdForLastInstallment).notLessThanMin(BigDecimal.ZERO).notGreaterThanMax(BigDecimal.valueOf(100));

        BigDecimal fixedPrincipalPercentagePerInstallment = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName)
                .value(fixedPrincipalPercentagePerInstallment).notLessThanMin(BigDecimal.ONE).notGreaterThanMax(BigDecimal.valueOf(100));

        if (amortizationType != null && !amortizationType.equals(AmortizationMethod.EQUAL_PRINCIPAL.getValue())
                && fixedPrincipalPercentagePerInstallment != null) {
            baseDataValidator.reset().parameter(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName).failWithCode(
                    "not.supported.principal.fixing.not.allowed.with.equal.installments",
                    "Principal fixing cannot be done with equal installment amortization");
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.canDefineEmiAmountParamName, element)) {
            final Boolean canDefineInstallmentAmount = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.canDefineEmiAmountParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.canDefineEmiAmountParamName).value(canDefineInstallmentAmount)
                    .isOneOfTheseValues(true, false);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.installmentAmountInMultiplesOfParamName, element)) {
            final Integer installmentAmountInMultiplesOf = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanProductConstants.installmentAmountInMultiplesOfParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.installmentAmountInMultiplesOfParamName)
                    .value(installmentAmountInMultiplesOf).ignoreIfNull().integerGreaterThanZero();
        }

        // accounting related data validation
        final Integer accountingRuleType = this.fromApiJsonHelper.extractIntegerNamed(ACCOUNTING_RULE, element, Locale.getDefault());
        baseDataValidator.reset().parameter(ACCOUNTING_RULE).value(accountingRuleType).notNull().inMinMaxRange(1, 4);

        if (AccountingValidations.isCashBasedAccounting(accountingRuleType)
                || AccountingValidations.isAccrualBasedAccounting(accountingRuleType)) {

            final Long fundAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.FUND_SOURCE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.FUND_SOURCE.getValue()).value(fundAccountId).notNull()
                    .integerGreaterThanZero();

            final Long loanPortfolioAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.LOAN_PORTFOLIO.getValue()).value(loanPortfolioAccountId)
                    .notNull().integerGreaterThanZero();

            final Long transfersInSuspenseAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue())
                    .value(transfersInSuspenseAccountId).notNull().integerGreaterThanZero();

            final Long incomeFromInterestId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INTEREST_ON_LOANS.getValue()).value(incomeFromInterestId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.INCOME_FROM_FEES.getValue(),
                    element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId).notNull()
                    .integerGreaterThanZero();

            final Long incomeFromPenaltyId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromRecoveryAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue())
                    .value(incomeFromRecoveryAccountId).notNull().integerGreaterThanZero();

            final Long writeOffAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue()).value(writeOffAccountId)
                    .notNull().integerGreaterThanZero();

            final Long goodwillCreditAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.GOODWILL_CREDIT.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.GOODWILL_CREDIT.getValue()).value(goodwillCreditAccountId)
                    .ignoreIfNull().integerGreaterThanZero();

            final Long overpaymentAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.OVERPAYMENT.getValue(),
                    element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.OVERPAYMENT.getValue()).value(overpaymentAccountId).notNull()
                    .integerGreaterThanZero();

            final Long incomeFromChargeOffInterestAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue())
                    .value(incomeFromChargeOffInterestAccountId).ignoreIfNull().integerGreaterThanZero();

            final Long incomeFromChargeOffFeesAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES.getValue())
                    .value(incomeFromChargeOffFeesAccountId).ignoreIfNull().integerGreaterThanZero();

            final Long incomeFromChargeOffPenaltyAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue())
                    .value(incomeFromChargeOffPenaltyAccountId).ignoreIfNull().integerGreaterThanZero();

            final Long incomeFromGoodwillCreditInterestAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue())
                    .value(incomeFromGoodwillCreditInterestAccountId).ignoreIfNull().integerGreaterThanZero();

            final Long incomeFromGoodwillCreditFeesAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue())
                    .value(incomeFromGoodwillCreditFeesAccountId).ignoreIfNull().integerGreaterThanZero();

            final Long incomeFromGoodwillCreditPenaltyAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue())
                    .value(incomeFromGoodwillCreditPenaltyAccountId).ignoreIfNull().integerGreaterThanZero();

            final Long chargeOffExpenseAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.CHARGE_OFF_EXPENSE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.CHARGE_OFF_EXPENSE.getValue()).value(chargeOffExpenseAccountId)
                    .ignoreIfNull().integerGreaterThanZero();

            final Long chargeOffFraudExpenseAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE.getValue())
                    .value(chargeOffFraudExpenseAccountId).ignoreIfNull().integerGreaterThanZero();

            validatePaymentChannelFundSourceMappings(baseDataValidator, element);
            validateChargeToIncomeAccountMappings(baseDataValidator, element);

        }

        if (AccountingValidations.isAccrualBasedAccounting(accountingRuleType)) {

            final Long receivableInterestAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue())
                    .value(receivableInterestAccountId).notNull().integerGreaterThanZero();

            final Long receivableFeeAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.FEES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.FEES_RECEIVABLE.getValue()).value(receivableFeeAccountId)
                    .notNull().integerGreaterThanZero();

            final Long receivablePenaltyAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue())
                    .value(receivablePenaltyAccountId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, element)) {
            final Boolean useBorrowerCycle = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME).value(useBorrowerCycle)
                    .ignoreIfNull().validateForBooleanValue();
            if (useBorrowerCycle) {
                validateBorrowerCycleVariations(element, baseDataValidator);
            }
        }

        validateMultiDisburseLoanData(baseDataValidator, element);

        validateLoanConfigurableAttributes(baseDataValidator, element);

        validateVariableInstallmentSettings(baseDataValidator, element);

        validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, null);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.CAN_USE_FOR_TOPUP, element)) {
            final Boolean canUseForTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.CAN_USE_FOR_TOPUP, element);
            baseDataValidator.reset().parameter(LoanProductConstants.CAN_USE_FOR_TOPUP).value(canUseForTopup).validateForBooleanValue();
        }

        final Integer dueDaysForRepaymentEvent = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT, element);
        baseDataValidator.reset().parameter(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT).value(dueDaysForRepaymentEvent)
                .integerZeroOrGreater();

        final Integer overDueDaysForRepaymentEvent = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT, element);
        baseDataValidator.reset().parameter(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT).value(overDueDaysForRepaymentEvent)
                .integerZeroOrGreater();

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_DOWN_PAYMENT, element)) {
            final Boolean enableDownPayment = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_DOWN_PAYMENT).value(enableDownPayment).ignoreIfNull()
                    .validateForBooleanValue();
            validateDownPaymentPercentage(enableDownPayment, baseDataValidator, element);
            validateAutoRepaymentForDownPayment(enableDownPayment, baseDataValidator, element);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.REPAYMENT_START_DATE_TYPE, element)) {
            final Integer repaymentStartDateType = this.fromApiJsonHelper
                    .extractIntegerNamed(LoanProductConstants.REPAYMENT_START_DATE_TYPE, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.REPAYMENT_START_DATE_TYPE).value(repaymentStartDateType).notNull()
                    .isOneOfTheseValues(1, 2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, element)) {
            final Boolean enableInstallmentLevelDelinquency = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY)
                    .value(enableInstallmentLevelDelinquency).ignoreIfNull().validateForBooleanValue();
            if (delinquencyBucketId == null) {
                if (enableInstallmentLevelDelinquency) {
                    baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY).failWithCode(
                            "can.be.enabled.for.loan.product.having.valid.delinquency.bucket",
                            "Installment level delinquency cannot be enabled if Delinquency bucket is not configured for loan product");
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_SCHEDULE_TYPE, element)) {
            validateLoanScheduleType(transactionProcessingStrategyCode, baseDataValidator, element);
        }

        String loanScheduleProcessingType = LoanScheduleProcessingType.HORIZONTAL.name();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE, element)) {
            loanScheduleProcessingType = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE).value(loanScheduleProcessingType)
                    .isOneOfEnumValues(LoanScheduleProcessingType.class);

            if (LoanScheduleProcessingType.VERTICAL.equals(LoanScheduleProcessingType.valueOf(loanScheduleProcessingType))
                    && !AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                            .equals(transactionProcessingStrategyCode)) {
                baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE).failWithCode(
                        "supported.only.for.progressive.loan.schedule.handling",
                        "Vertical repayment schedule processing is only available with `Advanced payment allocation` strategy");
            }
        }

        if (AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(transactionProcessingStrategyCode)
                && LoanScheduleProcessingType.HORIZONTAL.name().equals(loanScheduleProcessingType)) {
            advancedPaymentAllocationsValidator.checkGroupingOfAllocationRules(advancedPaymentAllocationsJsonParser
                    .assembleLoanProductPaymentAllocationRules(command, transactionProcessingStrategyCode));
        }
        final Integer recurringMoratoriumOnPrincipalPeriods = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed("recurringMoratoriumOnPrincipalPeriods", element);
        validateRepaymentPeriodWithGraceSettings(numberOfRepayments, graceOnPrincipalPayment, graceOnInterestPayment,
                graceOnInterestCharged, recurringMoratoriumOnPrincipalPeriods, baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateAutoRepaymentForDownPayment(Boolean enableDownPayment, DataValidatorBuilder baseDataValidator,
            JsonElement element) {
        if (enableDownPayment) {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, element)) {
                final Boolean enableAutoRepaymentForDownPayment = this.fromApiJsonHelper
                        .extractBooleanNamed(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, element);
                baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT)
                        .value(enableAutoRepaymentForDownPayment).ignoreIfNull().validateForBooleanValue();
            }
        } else {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, element)) {
                baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT).failWithCode(
                        "supported.only.for.enable.down.payment.true",
                        "Auto repayment for down-payment is supported only if enable down-payment is true");
            }
        }
    }

    private void validateDownPaymentPercentage(Boolean enableDownPayment, DataValidatorBuilder baseDataValidator, JsonElement element) {
        if (enableDownPayment) {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, element)) {
                BigDecimal disbursedAmountPercentageDownPayment = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, element);
                baseDataValidator.reset().parameter(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT)
                        .value(disbursedAmountPercentageDownPayment).notLessThanMin(BigDecimal.ONE)
                        .notGreaterThanMax(BigDecimal.valueOf(100)).scaleNotGreaterThan(6);
            } else {
                baseDataValidator.reset().parameter(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT).failWithCode(
                        "required.for.enable.down.payment.true",
                        "Disbursed amount percentage for down-payment is required if enable down-payment is true");
            }
        } else {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, element)) {
                baseDataValidator.reset().parameter(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT).failWithCode(
                        "supported.only.for.enable.down.payment.true",
                        "Disbursed amount percentage for down-payment is supported only if enable down-payment is true");
            }
        }
    }

    private void validateVariableInstallmentSettings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowVariableInstallmentsParamName, element)
                && this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.allowVariableInstallmentsParamName, element)) {

            boolean isEqualAmortization = false;
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element)) {
                isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element);
            }
            if (isEqualAmortization) {
                throw new EqualAmortizationUnsupportedFeatureException("variable.installment", "variable installment");
            }

            Long minimumGapBetweenInstallments = null;
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGapBetweenInstallments, element)) {
                minimumGapBetweenInstallments = this.fromApiJsonHelper.extractLongNamed(LoanProductConstants.minimumGapBetweenInstallments,
                        element);
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).value(minimumGapBetweenInstallments)
                        .notNull();
            } else {
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
                        "is.mandatory.when.allowVariableInstallments.is.true",
                        "minimumGap param is mandatory when allowVariableInstallments is true");
            }

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.maximumGapBetweenInstallments, element)) {
                final Long maximumGapBetweenInstallments = this.fromApiJsonHelper
                        .extractLongNamed(LoanProductConstants.maximumGapBetweenInstallments, element);
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).value(maximumGapBetweenInstallments)
                        .notNull();
                baseDataValidator.reset().parameter(LoanProductConstants.maximumGapBetweenInstallments).value(maximumGapBetweenInstallments)
                        .notNull().longGreaterThanNumber(minimumGapBetweenInstallments);
            }

        } else {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGapBetweenInstallments, element)) {
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
                        "not.supported.when.allowVariableInstallments.is.false",
                        "minimumGap param is not supported when allowVariableInstallments is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.maximumGapBetweenInstallments, element)) {
                baseDataValidator.reset().parameter(LoanProductConstants.maximumGapBetweenInstallments).failWithCode(
                        "not.supported.when.allowVariableInstallments.is.false",
                        "maximumGap param is not supported when allowVariableInstallments is not supplied or false");
            }

        }
    }

    private void validateLoanConfigurableAttributes(final DataValidatorBuilder baseDataValidator, final JsonElement element) {

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowAttributeOverridesParamName, element)) {

            final JsonObject object = element.getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName);

            // Validate that parameter names are allowed
            Set<String> supportedConfigurableAttributes = new HashSet<>();
            Collections.addAll(supportedConfigurableAttributes, SUPPORTED_LOAN_CONFIGURABLE_ATTRIBUTES);
            this.fromApiJsonHelper.checkForUnsupportedNestedParameters(LoanProductConstants.allowAttributeOverridesParamName, object,
                    supportedConfigurableAttributes);

            for (String supportedLoanConfigurableAttribute : SUPPORTED_LOAN_CONFIGURABLE_ATTRIBUTES) {
                /* Validate the attribute names */
                if (this.fromApiJsonHelper.parameterExists(supportedLoanConfigurableAttribute, object)) {
                    Boolean loanConfigurationAttributeValue = this.fromApiJsonHelper.extractBooleanNamed(supportedLoanConfigurableAttribute,
                            object);
                    /* Validate the boolean value */
                    baseDataValidator.reset().parameter(LoanProductConstants.allowAttributeOverridesParamName)
                            .value(loanConfigurationAttributeValue).notNull().validateForBooleanValue();
                }

            }
        }
    }

    private void validateMultiDisburseLoanData(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        Boolean multiDisburseLoan = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME, element)) {
            multiDisburseLoan = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME).value(multiDisburseLoan)
                    .ignoreIfNull().validateForBooleanValue();
        }

        boolean isEqualAmortization = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element);
        }
        if (isEqualAmortization && multiDisburseLoan) {
            throw new EqualAmortizationUnsupportedFeatureException("tranche.disbursal", "tranche disbursal");
        }

        if (multiDisburseLoan) {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME, element)) {
                final BigDecimal outstandingLoanBalance = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME, element);
                baseDataValidator.reset().parameter(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME)
                        .value(outstandingLoanBalance).ignoreIfNull().zeroOrPositiveAmount();
            }

            final Integer maxTrancheCount = this.fromApiJsonHelper
                    .extractIntegerNamed(LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME).value(maxTrancheCount).notNull()
                    .integerGreaterThanZero();

            final Integer interestType = this.fromApiJsonHelper.extractIntegerNamed(INTEREST_TYPE, element, Locale.getDefault());
            baseDataValidator.reset().parameter(INTEREST_TYPE).value(interestType).ignoreIfNull()
                    .integerSameAsNumber(InterestMethod.DECLINING_BALANCE.getValue());
        }

        final String overAppliedCalculationType = this.fromApiJsonHelper.extractStringNamed(OVER_APPLIED_CALCULATION_TYPE, element);
        baseDataValidator.reset().parameter(OVER_APPLIED_CALCULATION_TYPE).value(overAppliedCalculationType).notExceedingLengthOf(10);
    }

    private void validateInterestRecalculationParams(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            final LoanProduct loanProduct) {

        InterestRecalculationCompoundingMethod compoundingMethod = null;

        if (loanProduct == null || this.fromApiJsonHelper
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
                compoundingMethod = InterestRecalculationCompoundingMethod
                        .fromInt(loanProduct.getProductInterestRecalculationDetails().getInterestRecalculationCompoundingMethod());
            }
        }

        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.rescheduleStrategyMethodParameterName, element)) {
            final Integer rescheduleStrategyMethod = this.fromApiJsonHelper
                    .extractIntegerNamed(LoanProductConstants.rescheduleStrategyMethodParameterName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.rescheduleStrategyMethodParameterName).value(rescheduleStrategyMethod)
                    .notNull().inMinMaxRange(1, 3);
        }

        RecalculationFrequencyType frequencyType = null;

        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationRestFrequencyTypeParameterName, element)) {
            final Integer recalculationRestFrequencyType = this.fromApiJsonHelper
                    .extractIntegerNamed(LoanProductConstants.recalculationRestFrequencyTypeParameterName, element, Locale.getDefault());
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
            if (loanProduct == null || this.fromApiJsonHelper
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
                final Integer recalculationRestFrequencyOnDay = this.fromApiJsonHelper
                        .extractIntegerNamed(LoanProductConstants.recalculationRestFrequencyOnDayParamName, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.recalculationRestFrequencyOnDayParamName)
                        .value(recalculationRestFrequencyOnDay).ignoreIfNull().inMinMaxRange(1, 28);
            }
        }

        if (compoundingMethod.isCompoundingEnabled()) {
            RecalculationFrequencyType compoundingFrequencyType = null;

            if (loanProduct == null || this.fromApiJsonHelper
                    .parameterExists(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName, element)) {
                final Integer recalculationCompoundingFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(
                        LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName)
                        .value(recalculationCompoundingFrequencyType).notNull().inMinMaxRange(1, 4);
                if (recalculationCompoundingFrequencyType != null) {
                    compoundingFrequencyType = RecalculationFrequencyType.fromInt(recalculationCompoundingFrequencyType);
                    if (!compoundingFrequencyType.isSameAsRepayment()) {
                        PeriodFrequencyType repaymentFrequencyType = null;
                        if (this.fromApiJsonHelper.parameterExists(REPAYMENT_FREQUENCY_TYPE, element)) {
                            Integer repaymentFrequencyTypeVal = this.fromApiJsonHelper.extractIntegerNamed(REPAYMENT_FREQUENCY_TYPE,
                                    element, Locale.getDefault());
                            repaymentFrequencyType = PeriodFrequencyType.fromInt(repaymentFrequencyTypeVal);
                        } else if (loanProduct != null) {
                            repaymentFrequencyType = loanProduct.getLoanProductRelatedDetail().getRepaymentPeriodFrequencyType();
                        }
                        if (!compoundingFrequencyType.isSameFrequency(repaymentFrequencyType)) {
                            baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName)
                                    .value(recalculationCompoundingFrequencyType).failWithCode("must.be.same.as.repayment.frequency");
                        }
                    }
                }
            }

            if (compoundingFrequencyType == null) {
                if (loanProduct == null) {
                    compoundingFrequencyType = RecalculationFrequencyType.INVALID;
                } else {
                    compoundingFrequencyType = loanProduct.getProductInterestRecalculationDetails().getCompoundingFrequencyType();
                }
            }

            if (!compoundingFrequencyType.isSameAsRepayment()) {
                if (loanProduct == null || this.fromApiJsonHelper
                        .parameterExists(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName, element)) {
                    final Integer recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(
                            LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName, element, Locale.getDefault());
                    Integer repaymentEvery = null;
                    if (loanProduct == null) {
                        repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(REPAYMENT_EVERY, element);
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
                if (loanProduct == null || this.fromApiJsonHelper
                        .parameterExists(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName, element)) {
                    final Integer recalculationRestFrequencyOnDay = this.fromApiJsonHelper.extractIntegerNamed(
                            LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName, element, Locale.getDefault());
                    baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName)
                            .value(recalculationRestFrequencyOnDay).ignoreIfNull().inMinMaxRange(1, 28);
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName, element)) {
            final Boolean isArrearsBasedOnOriginalSchedule = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName)
                    .value(isArrearsBasedOnOriginalSchedule).notNull().isOneOfTheseValues(true, false);
        }
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, element)) {
            final Boolean isCompoundingToBePostedAsTransactions = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName)
                    .value(isCompoundingToBePostedAsTransactions).notNull().isOneOfTheseValues(true, false);
        }

        final Integer preCloseInterestCalculationStrategy = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(LoanProductConstants.preClosureInterestCalculationStrategyParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.preClosureInterestCalculationStrategyParamName)
                .value(preCloseInterestCalculationStrategy).ignoreIfNull().inMinMaxRange(
                        LoanPreClosureInterestCalculationStrategy.getMinValue(), LoanPreClosureInterestCalculationStrategy.getMaxValue());
    }

    public void validateForUpdate(final JsonCommand command, final LoanProduct loanProduct) {
        String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(LOANPRODUCT);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        if (this.fromApiJsonHelper.parameterExists(NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
            baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.SHORT_NAME, element)) {
            final String shortName = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.SHORT_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.SHORT_NAME).value(shortName).notBlank().notExceedingLengthOf(4);
        }

        if (this.fromApiJsonHelper.parameterExists(DESCRIPTION, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(DESCRIPTION, element);
            baseDataValidator.reset().parameter(DESCRIPTION).value(description).notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(FUND_ID, element)) {
            final Long fundId = this.fromApiJsonHelper.extractLongNamed(FUND_ID, element);
            baseDataValidator.reset().parameter(FUND_ID).value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(INCLUDE_IN_BORROWER_CYCLE, element)) {
            final Boolean includeInBorrowerCycle = this.fromApiJsonHelper.extractBooleanNamed(INCLUDE_IN_BORROWER_CYCLE, element);
            baseDataValidator.reset().parameter(INCLUDE_IN_BORROWER_CYCLE).value(includeInBorrowerCycle).ignoreIfNull()
                    .validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(CURRENCY_CODE, element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed(CURRENCY_CODE, element);
            baseDataValidator.reset().parameter(CURRENCY_CODE).value(currencyCode).notBlank().notExceedingLengthOf(3);
        }

        if (this.fromApiJsonHelper.parameterExists(DIGITS_AFTER_DECIMAL, element)) {
            final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerNamed(DIGITS_AFTER_DECIMAL, element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter(DIGITS_AFTER_DECIMAL).value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);
        }

        if (this.fromApiJsonHelper.parameterExists(IN_MULTIPLES_OF, element)) {
            final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed(IN_MULTIPLES_OF, element, Locale.getDefault());
            baseDataValidator.reset().parameter(IN_MULTIPLES_OF).value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();
        }

        final String minPrincipalParameterName = MIN_PRINCIPAL;
        BigDecimal minPrincipalAmount;
        if (this.fromApiJsonHelper.parameterExists(minPrincipalParameterName, element)) {
            minPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minPrincipalParameterName, element);
            baseDataValidator.reset().parameter(minPrincipalParameterName).value(minPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        final String maxPrincipalParameterName = MAX_PRINCIPAL;
        BigDecimal maxPrincipalAmount;
        if (this.fromApiJsonHelper.parameterExists(maxPrincipalParameterName, element)) {
            maxPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxPrincipalParameterName, element);
            baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(PRINCIPAL, element)) {
            final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(PRINCIPAL, element);
            baseDataValidator.reset().parameter(PRINCIPAL).value(principal).positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(IN_ARREARS_TOLERANCE, element)) {
            final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(IN_ARREARS_TOLERANCE, element);
            baseDataValidator.reset().parameter(IN_ARREARS_TOLERANCE).value(inArrearsTolerance).ignoreIfNull().zeroOrPositiveAmount();
        }

        final String minNumberOfRepaymentsParameterName = MIN_NUMBER_OF_REPAYMENTS;
        Integer minNumberOfRepayments;
        if (this.fromApiJsonHelper.parameterExists(minNumberOfRepaymentsParameterName, element)) {
            minNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(minNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(minNumberOfRepaymentsParameterName).value(minNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        final String maxNumberOfRepaymentsParameterName = MAX_NUMBER_OF_REPAYMENTS;
        Integer maxNumberOfRepayments;
        if (this.fromApiJsonHelper.parameterExists(maxNumberOfRepaymentsParameterName, element)) {
            maxNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(maxNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        Integer numberOfRepayments = loanProduct.getNumberOfRepayments();
        if (this.fromApiJsonHelper.parameterExists(NUMBER_OF_REPAYMENTS, element)) {
            numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(NUMBER_OF_REPAYMENTS, element);
            baseDataValidator.reset().parameter(NUMBER_OF_REPAYMENTS).value(numberOfRepayments).notNull().integerGreaterThanZero();
        }

        Integer repaymentEvery = loanProduct.getLoanProductRelatedDetail().getRepayEvery();
        if (this.fromApiJsonHelper.parameterExists(REPAYMENT_EVERY, element)) {
            repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(REPAYMENT_EVERY, element);
            baseDataValidator.reset().parameter(REPAYMENT_EVERY).value(repaymentEvery).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(REPAYMENT_FREQUENCY_TYPE, element)) {
            final Integer repaymentFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(REPAYMENT_FREQUENCY_TYPE, element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter(REPAYMENT_FREQUENCY_TYPE).value(repaymentFrequencyType).notNull().inMinMaxRange(0, 3);
        }

        String transactionProcessingStrategyCode = loanProduct.getTransactionProcessingStrategyCode();
        if (this.fromApiJsonHelper.parameterExists(TRANSACTION_PROCESSING_STRATEGY_CODE, element)) {
            transactionProcessingStrategyCode = this.fromApiJsonHelper.extractStringNamed(TRANSACTION_PROCESSING_STRATEGY_CODE, element);
            baseDataValidator.reset().parameter(TRANSACTION_PROCESSING_STRATEGY_CODE).value(transactionProcessingStrategyCode).notBlank();
            // Validating whether the processor is existing
            loanRepaymentScheduleTransactionProcessorFactory.determineProcessor(transactionProcessingStrategyCode);
        }

        // grace validation
        Integer graceOnPrincipalPayment = loanProduct.getLoanProductRelatedDetail().getGraceOnPrincipalPayment();
        if (this.fromApiJsonHelper.parameterExists(GRACE_ON_PRINCIPAL_PAYMENT, element)) {
            graceOnPrincipalPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(GRACE_ON_PRINCIPAL_PAYMENT, element);
            baseDataValidator.reset().parameter(GRACE_ON_PRINCIPAL_PAYMENT).value(graceOnPrincipalPayment).zeroOrPositiveAmount();
        }

        Integer graceOnInterestPayment = loanProduct.getLoanProductRelatedDetail().getGraceOnInterestPayment();
        if (this.fromApiJsonHelper.parameterExists(GRACE_ON_INTEREST_PAYMENT, element)) {
            graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(GRACE_ON_INTEREST_PAYMENT, element);
            baseDataValidator.reset().parameter(GRACE_ON_INTEREST_PAYMENT).value(graceOnInterestPayment).zeroOrPositiveAmount();
        }

        Integer graceOnInterestCharged = loanProduct.getLoanProductRelatedDetail().getGraceOnInterestCharged();
        if (this.fromApiJsonHelper.parameterExists(GRACE_ON_INTEREST_CHARGED, element)) {
            graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(GRACE_ON_INTEREST_CHARGED, element);
            baseDataValidator.reset().parameter(GRACE_ON_INTEREST_CHARGED).value(graceOnInterestCharged).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, element)) {
            final Integer graceOnArrearsAgeing = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME).value(graceOnArrearsAgeing)
                    .integerZeroOrGreater();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, element)) {
            final Integer overdueDaysForNPA = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME).value(overdueDaysForNPA)
                    .integerZeroOrGreater();
        }

        Long delinquencyBucketId = null;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME, element)) {
            delinquencyBucketId = this.fromApiJsonHelper.extractLongNamed(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME).value(delinquencyBucketId)
                    .ignoreIfNull().integerGreaterThanZero();
        }

        Integer amortizationType = null;
        if (this.fromApiJsonHelper.parameterExists(AMORTIZATION_TYPE, element)) {
            amortizationType = this.fromApiJsonHelper.extractIntegerNamed(AMORTIZATION_TYPE, element, Locale.getDefault());
            baseDataValidator.reset().parameter(AMORTIZATION_TYPE).value(amortizationType).notNull().inMinMaxRange(0, 1);
        }

        if (this.fromApiJsonHelper.parameterExists(INTEREST_TYPE, element)) {
            final Integer interestType = this.fromApiJsonHelper.extractIntegerNamed(INTEREST_TYPE, element, Locale.getDefault());
            baseDataValidator.reset().parameter(INTEREST_TYPE).value(interestType).notNull().inMinMaxRange(0, 1);
        }
        Integer interestCalculationPeriodType = loanProduct.getLoanProductRelatedDetail().getInterestCalculationPeriodMethod().getValue();
        if (this.fromApiJsonHelper.parameterExists(INTEREST_CALCULATION_PERIOD_TYPE, element)) {
            interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerNamed(INTEREST_CALCULATION_PERIOD_TYPE, element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter(INTEREST_CALCULATION_PERIOD_TYPE).value(interestCalculationPeriodType).notNull()
                    .inMinMaxRange(0, 1);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME, element)) {
            final Integer daysInYearType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME,
                    element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME).value(daysInYearType).notNull()
                    .isOneOfTheseValues(1, 360, 364, 365);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME, element)) {
            final Integer daysInMonthType = this.fromApiJsonHelper
                    .extractIntegerNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME).value(daysInMonthType).notNull()
                    .isOneOfTheseValues(1, 30);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME,
                element)) {
            Boolean npaChangeConfig = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME)
                    .value(npaChangeConfig).notNull().isOneOfTheseValues(true, false);
        }

        boolean isEqualAmortization = loanProduct.isEqualAmortization();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element);
            baseDataValidator.reset().parameter(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM).value(isEqualAmortization).ignoreIfNull()
                    .validateForBooleanValue();
        }

        // Interest recalculation settings
        Boolean isInterestRecalculationEnabled = loanProduct.isInterestRecalculationEnabled();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, element)) {
            isInterestRecalculationEnabled = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME)
                    .value(isInterestRecalculationEnabled).notNull().isOneOfTheseValues(true, false);
        }

        if (isInterestRecalculationEnabled != null && isInterestRecalculationEnabled) {
            if (isEqualAmortization) {
                throw new EqualAmortizationUnsupportedFeatureException("interest.recalculation", "interest recalculation");
            }
            validateInterestRecalculationParams(element, baseDataValidator, loanProduct);
        }

        // interest rates
        boolean isLinkedToFloatingInterestRates = loanProduct.isLinkedToFloatingInterestRate();
        if (this.fromApiJsonHelper.parameterExists(IS_LINKED_TO_FLOATING_INTEREST_RATES, element)) {
            isLinkedToFloatingInterestRates = this.fromApiJsonHelper.extractBooleanNamed(IS_LINKED_TO_FLOATING_INTEREST_RATES, element);
        }
        boolean isInterestBearing = false;
        if (isLinkedToFloatingInterestRates) {
            if (isEqualAmortization) {
                throw new EqualAmortizationUnsupportedFeatureException("floating.interest.rate", "floating interest rate");
            }
            if (this.fromApiJsonHelper.parameterExists(INTEREST_RATE_PER_PERIOD, element)) {
                baseDataValidator.reset().parameter(INTEREST_RATE_PER_PERIOD).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "interestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists(MIN_INTEREST_RATE_PER_PERIOD, element)) {
                baseDataValidator.reset().parameter(MIN_INTEREST_RATE_PER_PERIOD).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "minInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists(MAX_INTEREST_RATE_PER_PERIOD, element)) {
                baseDataValidator.reset().parameter(MAX_INTEREST_RATE_PER_PERIOD).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "maxInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists(INTEREST_RATE_FREQUENCY_TYPE, element)) {
                baseDataValidator.reset().parameter(INTEREST_RATE_FREQUENCY_TYPE).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "interestRateFrequencyType param is not supported when isLinkedToFloatingInterestRates is true");
            }

            Integer interestType = this.fromApiJsonHelper.parameterExists(INTEREST_TYPE, element)
                    ? this.fromApiJsonHelper.extractIntegerNamed(INTEREST_TYPE, element, Locale.getDefault())
                    : loanProduct.getLoanProductRelatedDetail().getInterestMethod().getValue();
            if ((interestType == null || !interestType.equals(InterestMethod.DECLINING_BALANCE.getValue()))
                    || (isInterestRecalculationEnabled == null || !isInterestRecalculationEnabled)) {
                baseDataValidator.reset().parameter(IS_LINKED_TO_FLOATING_INTEREST_RATES).failWithCode(
                        "supported.only.for.declining.balance.interest.recalculation.enabled",
                        "Floating interest rates are supported only for declining balance and interest recalculation enabled loan products");
            }

            Long floatingRatesId = loanProduct.getFloatingRates() == null ? null : loanProduct.getFloatingRates().getFloatingRate().getId();
            if (this.fromApiJsonHelper.parameterExists(FLOATING_RATES_ID, element)) {
                floatingRatesId = this.fromApiJsonHelper.extractLongNamed(FLOATING_RATES_ID, element);
            }
            baseDataValidator.reset().parameter(FLOATING_RATES_ID).value(floatingRatesId).notNull();

            BigDecimal interestRateDifferential = loanProduct.getFloatingRates() == null ? null
                    : loanProduct.getFloatingRates().getInterestRateDifferential();
            if (this.fromApiJsonHelper.parameterExists(INTEREST_RATE_DIFFERENTIAL, element)) {
                interestRateDifferential = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(INTEREST_RATE_DIFFERENTIAL, element);
            }
            baseDataValidator.reset().parameter(INTEREST_RATE_DIFFERENTIAL).value(interestRateDifferential).notNull()
                    .zeroOrPositiveAmount();

            final String minDifferentialLendingRateParameterName = MIN_DIFFERENTIAL_LENDING_RATE;
            BigDecimal minDifferentialLendingRate = loanProduct.getFloatingRates() == null ? null
                    : loanProduct.getFloatingRates().getMinDifferentialLendingRate();
            if (this.fromApiJsonHelper.parameterExists(minDifferentialLendingRateParameterName, element)) {
                minDifferentialLendingRate = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(minDifferentialLendingRateParameterName, element);
            }
            baseDataValidator.reset().parameter(minDifferentialLendingRateParameterName).value(minDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            final String defaultDifferentialLendingRateParameterName = DEFAULT_DIFFERENTIAL_LENDING_RATE;
            BigDecimal defaultDifferentialLendingRate = loanProduct.getFloatingRates() == null ? null
                    : loanProduct.getFloatingRates().getDefaultDifferentialLendingRate();
            if (this.fromApiJsonHelper.parameterExists(defaultDifferentialLendingRateParameterName, element)) {
                defaultDifferentialLendingRate = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(defaultDifferentialLendingRateParameterName, element);
            }
            baseDataValidator.reset().parameter(defaultDifferentialLendingRateParameterName).value(defaultDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            final String maxDifferentialLendingRateParameterName = MAX_DIFFERENTIAL_LENDING_RATE;
            BigDecimal maxDifferentialLendingRate = loanProduct.getFloatingRates() == null ? null
                    : loanProduct.getFloatingRates().getMaxDifferentialLendingRate();
            if (this.fromApiJsonHelper.parameterExists(maxDifferentialLendingRateParameterName, element)) {
                maxDifferentialLendingRate = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(maxDifferentialLendingRateParameterName, element);
            }
            baseDataValidator.reset().parameter(maxDifferentialLendingRateParameterName).value(maxDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            if (defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0
                    && minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(DEFAULT_DIFFERENTIAL_LENDING_RATE).value(defaultDifferentialLendingRate)
                        .notLessThanMin(minDifferentialLendingRate);
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0
                    && minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(MAX_DIFFERENTIAL_LENDING_RATE).value(maxDifferentialLendingRate)
                        .notLessThanMin(minDifferentialLendingRate);
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0
                    && defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(MAX_DIFFERENTIAL_LENDING_RATE).value(maxDifferentialLendingRate)
                        .notLessThanMin(defaultDifferentialLendingRate);
            }

            Boolean isFloatingInterestRateCalculationAllowed = loanProduct.getFloatingRates() == null ? null
                    : loanProduct.getFloatingRates().isFloatingInterestRateCalculationAllowed();
            if (this.fromApiJsonHelper.parameterExists(IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED, element)) {
                isFloatingInterestRateCalculationAllowed = this.fromApiJsonHelper
                        .extractBooleanNamed(IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED, element);
            }
            baseDataValidator.reset().parameter(IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED)
                    .value(isFloatingInterestRateCalculationAllowed).notNull().isOneOfTheseValues(true, false);
            isInterestBearing = true;
        } else {
            if (this.fromApiJsonHelper.parameterExists(FLOATING_RATES_ID, element)) {
                baseDataValidator.reset().parameter(FLOATING_RATES_ID).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "floatingRatesId param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(INTEREST_RATE_DIFFERENTIAL, element)) {
                baseDataValidator.reset().parameter(INTEREST_RATE_DIFFERENTIAL).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "interestRateDifferential param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(MIN_DIFFERENTIAL_LENDING_RATE, element)) {
                baseDataValidator.reset().parameter(MIN_DIFFERENTIAL_LENDING_RATE).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "minDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(DEFAULT_DIFFERENTIAL_LENDING_RATE, element)) {
                baseDataValidator.reset().parameter(DEFAULT_DIFFERENTIAL_LENDING_RATE).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "defaultDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(MAX_DIFFERENTIAL_LENDING_RATE, element)) {
                baseDataValidator.reset().parameter(MAX_DIFFERENTIAL_LENDING_RATE).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "maxDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED, element)) {
                baseDataValidator.reset().parameter(IS_FLOATING_INTEREST_RATE_CALCULATION_ALLOWED).failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "isFloatingInterestRateCalculationAllowed param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            final String minInterestRatePerPeriodParameterName = MIN_INTEREST_RATE_PER_PERIOD;
            BigDecimal minInterestRatePerPeriod = loanProduct.getMinNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists(minInterestRatePerPeriodParameterName, element)) {
                minInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minInterestRatePerPeriodParameterName,
                        element);
            }
            baseDataValidator.reset().parameter(minInterestRatePerPeriodParameterName).value(minInterestRatePerPeriod).ignoreIfNull()
                    .zeroOrPositiveAmount();

            final String maxInterestRatePerPeriodParameterName = MAX_INTEREST_RATE_PER_PERIOD;
            BigDecimal maxInterestRatePerPeriod = loanProduct.getMaxNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists(maxInterestRatePerPeriodParameterName, element)) {
                maxInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxInterestRatePerPeriodParameterName,
                        element);
            }
            baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod).ignoreIfNull()
                    .zeroOrPositiveAmount();

            BigDecimal interestRatePerPeriod = loanProduct.getLoanProductRelatedDetail().getNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists(INTEREST_RATE_PER_PERIOD, element)) {
                interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(INTEREST_RATE_PER_PERIOD, element);
            }
            baseDataValidator.reset().parameter(INTEREST_RATE_PER_PERIOD).value(interestRatePerPeriod).notNull().zeroOrPositiveAmount();

            Integer interestRateFrequencyType = loanProduct.getLoanProductRelatedDetail().getInterestPeriodFrequencyType().getValue();
            if (this.fromApiJsonHelper.parameterExists(INTEREST_RATE_FREQUENCY_TYPE, element)) {
                interestRateFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(INTEREST_RATE_FREQUENCY_TYPE, element,
                        Locale.getDefault());
            }
            baseDataValidator.reset().parameter(INTEREST_RATE_FREQUENCY_TYPE).value(interestRateFrequencyType).notNull().inMinMaxRange(0,
                    4);
            isInterestBearing = interestRatePerPeriod.compareTo(BigDecimal.ZERO) > 0;
        }

        // Fixed Length validation
        fixedLengthValidations(transactionProcessingStrategyCode, isInterestBearing, numberOfRepayments, repaymentEvery, element,
                baseDataValidator);

        // Guarantee Funds
        Boolean holdGuaranteeFunds = loanProduct.isHoldGuaranteeFunds();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.holdGuaranteeFundsParamName, element)) {
            holdGuaranteeFunds = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.holdGuaranteeFundsParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.holdGuaranteeFundsParamName).value(holdGuaranteeFunds).notNull()
                    .isOneOfTheseValues(true, false);
        }

        if (holdGuaranteeFunds != null && holdGuaranteeFunds) {
            validateGuaranteeParams(element, baseDataValidator, null);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.principalThresholdForLastInstallmentParamName, element)) {
            BigDecimal principalThresholdForLastInstallment = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.principalThresholdForLastInstallmentParamName)
                    .value(principalThresholdForLastInstallment).notNull().notLessThanMin(BigDecimal.ZERO)
                    .notGreaterThanMax(BigDecimal.valueOf(100));
        }

        BigDecimal fixedPrincipalPercentagePerInstallment = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName)
                .value(fixedPrincipalPercentagePerInstallment).notLessThanMin(BigDecimal.ONE).notGreaterThanMax(BigDecimal.valueOf(100));

        if (!AmortizationMethod.EQUAL_PRINCIPAL.getValue().equals(amortizationType) && fixedPrincipalPercentagePerInstallment != null) {
            baseDataValidator.reset().parameter(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName).failWithCode(
                    "not.supported.principal.fixing.not.allowed.with.equal.installments",
                    "Principal fixing cannot be done with equal installment amortization");
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.canDefineEmiAmountParamName, element)) {
            final Boolean canDefineInstallmentAmount = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.canDefineEmiAmountParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.canDefineEmiAmountParamName).value(canDefineInstallmentAmount)
                    .isOneOfTheseValues(true, false);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.installmentAmountInMultiplesOfParamName, element)) {
            final Integer installmentAmountInMultiplesOf = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanProductConstants.installmentAmountInMultiplesOfParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.installmentAmountInMultiplesOfParamName)
                    .value(installmentAmountInMultiplesOf).ignoreIfNull().integerGreaterThanZero();
        }

        final Integer accountingRuleType = this.fromApiJsonHelper.extractIntegerNamed(ACCOUNTING_RULE, element, Locale.getDefault());
        baseDataValidator.reset().parameter(ACCOUNTING_RULE).value(accountingRuleType).ignoreIfNull().inMinMaxRange(1, 4);

        final Long fundAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.FUND_SOURCE.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.FUND_SOURCE.getValue()).value(fundAccountId).ignoreIfNull()
                .integerGreaterThanZero();

        final Long loanPortfolioAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.LOAN_PORTFOLIO.getValue()).value(loanPortfolioAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long transfersInSuspenseAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue()).value(transfersInSuspenseAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromInterestId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INTEREST_ON_LOANS.getValue()).value(incomeFromInterestId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.INCOME_FROM_FEES.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId).ignoreIfNull()
                .integerGreaterThanZero();

        final Long incomeFromPenaltyId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromRecoveryAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue()).value(incomeFromRecoveryAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long writeOffAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue()).value(writeOffAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long goodwillCreditAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.GOODWILL_CREDIT.getValue()).value(goodwillCreditAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long overpaymentAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.OVERPAYMENT.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.OVERPAYMENT.getValue()).value(overpaymentAccountId).ignoreIfNull()
                .integerGreaterThanZero();

        final Long receivableInterestAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue()).value(receivableInterestAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long receivableFeeAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.FEES_RECEIVABLE.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.FEES_RECEIVABLE.getValue()).value(receivableFeeAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long receivablePenaltyAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue()).value(receivablePenaltyAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromChargeOffInterestAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue())
                .value(incomeFromChargeOffInterestAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromChargeOffFeesAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES.getValue())
                .value(incomeFromChargeOffFeesAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromChargeOffPenaltyAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue())
                .value(incomeFromChargeOffPenaltyAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromGoodwillCreditInterestAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue())
                .value(incomeFromGoodwillCreditInterestAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromGoodwillCreditFeesAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue())
                .value(incomeFromGoodwillCreditFeesAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromGoodwillCreditPenaltyAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue())
                .value(incomeFromGoodwillCreditPenaltyAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long chargeOffExpenseAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.CHARGE_OFF_EXPENSE.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.CHARGE_OFF_EXPENSE.getValue()).value(chargeOffExpenseAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long chargeOffFraudExpenseAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE.getValue())
                .value(chargeOffFraudExpenseAccountId).ignoreIfNull().integerGreaterThanZero();

        validatePaymentChannelFundSourceMappings(baseDataValidator, element);
        validateChargeToIncomeAccountMappings(baseDataValidator, element);

        validateMinMaxConstraints(element, baseDataValidator, loanProduct);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, element)) {
            final Boolean useBorrowerCycle = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME).value(useBorrowerCycle)
                    .ignoreIfNull().validateForBooleanValue();
            if (useBorrowerCycle) {
                validateBorrowerCycleVariations(element, baseDataValidator);
            }
        }

        validateMultiDisburseLoanData(baseDataValidator, element);

        // validateLoanConfigurableAttributes(baseDataValidator,element);

        validateVariableInstallmentSettings(baseDataValidator, element);

        validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, loanProduct);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.CAN_USE_FOR_TOPUP, element)) {
            final Boolean canUseForTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.CAN_USE_FOR_TOPUP, element);
            baseDataValidator.reset().parameter(LoanProductConstants.CAN_USE_FOR_TOPUP).value(canUseForTopup).validateForBooleanValue();
        }

        final Integer dueDaysForRepaymentEvent = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT, element);
        baseDataValidator.reset().parameter(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT).value(dueDaysForRepaymentEvent)
                .integerZeroOrGreater();

        final Integer overDueDaysForRepaymentEvent = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT, element);
        baseDataValidator.reset().parameter(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT).value(overDueDaysForRepaymentEvent)
                .integerZeroOrGreater();

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_DOWN_PAYMENT, element)) {
            final Boolean enableDownPayment = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_DOWN_PAYMENT).value(enableDownPayment).ignoreIfNull()
                    .validateForBooleanValue();
            validateDownPaymentPercentage(enableDownPayment, baseDataValidator, element);
            validateAutoRepaymentForDownPayment(enableDownPayment, baseDataValidator, element);
        }

        Integer repaymentStartDateType = loanProduct.getRepaymentStartDateType().getValue();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.REPAYMENT_START_DATE_TYPE, element)) {
            repaymentStartDateType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.REPAYMENT_START_DATE_TYPE, element,
                    Locale.getDefault());
        }
        baseDataValidator.reset().parameter(LoanProductConstants.REPAYMENT_START_DATE_TYPE).value(repaymentStartDateType).notNull()
                .isOneOfTheseValues(1, 2);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, element)) {
            final Boolean enableInstallmentLevelDelinquency = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY)
                    .value(enableInstallmentLevelDelinquency).ignoreIfNull().validateForBooleanValue();
            if (delinquencyBucketId == null) {
                if (enableInstallmentLevelDelinquency) {
                    baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY).failWithCode(
                            "can.be.enabled.for.loan.product.having.valid.delinquency.bucket",
                            "Installment level delinquency cannot be enabled if Delinquency bucket is not configured for loan product");
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_SCHEDULE_TYPE, element)) {
            validateLoanScheduleType(transactionProcessingStrategyCode, baseDataValidator, element);
        }

        String loanScheduleProcessingType = loanProduct.getLoanProductRelatedDetail().getLoanScheduleProcessingType().name();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE, element)) {
            loanScheduleProcessingType = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE).value(loanScheduleProcessingType)
                    .isOneOfEnumValues(LoanScheduleProcessingType.class);
        }

        List<LoanProductPaymentAllocationRule> allocationRules = loanProduct.getPaymentAllocationRules();
        if (this.fromApiJsonHelper.parameterExists(ADVANCED_PAYMENT_ALLOCATIONS, element)
                && LoanScheduleProcessingType.HORIZONTAL.name().equals(loanScheduleProcessingType)) {
            allocationRules = advancedPaymentAllocationsJsonParser.assembleLoanProductPaymentAllocationRules(command,
                    transactionProcessingStrategyCode);
        }

        if (LoanScheduleProcessingType.HORIZONTAL.name().equals(loanScheduleProcessingType)
                && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                        .equals(transactionProcessingStrategyCode)) {
            advancedPaymentAllocationsValidator.checkGroupingOfAllocationRules(allocationRules);
        }
        if (LoanScheduleProcessingType.VERTICAL.equals(LoanScheduleProcessingType.valueOf(loanScheduleProcessingType))
                && !AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                        .equals(transactionProcessingStrategyCode)) {
            baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE).failWithCode(
                    "supported.only.for.progressive.loan.schedule.handling",
                    "Vertical repayment schedule processing is only available with `Advanced payment allocation` strategy");
        }
        final Integer recurringMoratoriumOnPrincipalPeriods = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed("recurringMoratoriumOnPrincipalPeriods", element);

        validateRepaymentPeriodWithGraceSettings(numberOfRepayments, graceOnPrincipalPayment, graceOnInterestPayment,
                graceOnInterestCharged, recurringMoratoriumOnPrincipalPeriods, baseDataValidator);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    /*
     * Validation for advanced accounting options
     */
    private void validatePaymentChannelFundSourceMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (this.fromApiJsonHelper.parameterExists(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element)) {
            final JsonArray paymentChannelMappingArray = this.fromApiJsonHelper
                    .extractJsonArrayNamed(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
            if (paymentChannelMappingArray != null && paymentChannelMappingArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                    final Long paymentTypeId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.PAYMENT_TYPE.getValue(),
                            jsonObject);
                    final Long paymentSpecificFundAccountId = this.fromApiJsonHelper
                            .extractLongNamed(LoanProductAccountingParams.FUND_SOURCE.getValue(), jsonObject);

                    baseDataValidator.reset()
                            .parameter(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue() + OPENING_SQUARE_BRACKET
                                    + i + CLOSING_SQUARE_BRACKET + DOT + LoanProductAccountingParams.PAYMENT_TYPE.getValue())
                            .value(paymentTypeId).notNull().integerGreaterThanZero();
                    baseDataValidator.reset()
                            .parameter(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue() + OPENING_SQUARE_BRACKET
                                    + i + CLOSING_SQUARE_BRACKET + DOT + LoanProductAccountingParams.FUND_SOURCE.getValue())
                            .value(paymentSpecificFundAccountId).notNull().integerGreaterThanZero();
                    i++;
                } while (i < paymentChannelMappingArray.size());
            }
        }
    }

    private void validateChargeToIncomeAccountMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        // validate for both fee and penalty charges
        // TODO: This look faulty. Need to analyze this behaviour before changing
        validateChargeToIncomeAccountMappings(baseDataValidator, element, true);
        validateChargeToIncomeAccountMappings(baseDataValidator, element, true);
    }

    private void validateChargeToIncomeAccountMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element,
            final boolean isPenalty) {
        String parameterName;
        if (isPenalty) {
            parameterName = LoanProductAccountingParams.PENALTY_INCOME_ACCOUNT_MAPPING.getValue();
        } else {
            parameterName = LoanProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue();
        }

        if (this.fromApiJsonHelper.parameterExists(parameterName, element)) {
            final JsonArray chargeToIncomeAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(parameterName, element);
            if (chargeToIncomeAccountMappingArray != null && chargeToIncomeAccountMappingArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = chargeToIncomeAccountMappingArray.get(i).getAsJsonObject();
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.CHARGE_ID.getValue(),
                            jsonObject);
                    final Long incomeAccountId = this.fromApiJsonHelper
                            .extractLongNamed(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue(), jsonObject);
                    baseDataValidator.reset().parameter(parameterName + OPENING_SQUARE_BRACKET + i + CLOSING_SQUARE_BRACKET + DOT
                            + LoanProductAccountingParams.CHARGE_ID.getValue()).value(chargeId).notNull().integerGreaterThanZero();
                    baseDataValidator.reset()
                            .parameter(parameterName + OPENING_SQUARE_BRACKET + i + CLOSING_SQUARE_BRACKET + DOT
                                    + LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue())
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
        final String principalParameterName = PRINCIPAL;
        BigDecimal principalAmount = null;
        BigDecimal minPrincipalAmount = null;
        BigDecimal maxPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            principalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            minPrincipalAmount = minmaxValues.get(LoanProductConstants.MIN_PRINCIPAL);
            maxPrincipalAmount = minmaxValues.get(LoanProductConstants.MAX_PRINCIPAL);
        }

        if ((minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) > 0)
                && (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) > 0)) {
            baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).inMinAndMaxAmountRange(minPrincipalAmount,
                    maxPrincipalAmount);
        } else {
            if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
                baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).notLessThanMin(minPrincipalAmount);
            } else if (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
                baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).notGreaterThanMax(maxPrincipalAmount);
            }
        }

        final String numberOfRepaymentsParameterName = NUMBER_OF_REPAYMENTS;
        Integer maxNumberOfRepayments = null;
        Integer minNumberOfRepayments = null;
        Integer numberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(numberOfRepaymentsParameterName, element)) {
            numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
            if (minmaxValues.get(LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS) != null) {
                minNumberOfRepayments = minmaxValues.get(LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS).intValueExact();
            }
            if (minmaxValues.get(LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS) != null) {
                maxNumberOfRepayments = minmaxValues.get(LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS).intValueExact();
            }
        }

        if (maxNumberOfRepayments != null && maxNumberOfRepayments.compareTo(0) > 0) {
            if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
                baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                        .inMinMaxRange(minNumberOfRepayments, maxNumberOfRepayments);
            } else {
                baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                        .notGreaterThanMax(maxNumberOfRepayments);
            }
        } else if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
            baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                    .notLessThanMin(minNumberOfRepayments);
        }

        final String interestRatePerPeriodParameterName = INTEREST_RATE_PER_PERIOD;
        BigDecimal interestRatePerPeriod = null;
        BigDecimal minInterestRatePerPeriod = null;
        BigDecimal maxInterestRatePerPeriod = null;
        if (this.fromApiJsonHelper.parameterExists(interestRatePerPeriodParameterName, element)) {
            interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName, element);
            minInterestRatePerPeriod = minmaxValues.get(LoanProductConstants.MIN_INTEREST_RATE_PER_PERIOD);
            maxInterestRatePerPeriod = minmaxValues.get(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD);
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
        final String principalParameterName = PRINCIPAL;
        BigDecimal principalAmount;
        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            principalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            principalUpdated = true;
        } else {
            principalAmount = loanProduct.getPrincipalAmount().getAmount();
        }

        final String minPrincipalParameterName = MIN_PRINCIPAL;
        BigDecimal minPrincipalAmount;
        if (this.fromApiJsonHelper.parameterExists(minPrincipalParameterName, element)) {
            minPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minPrincipalParameterName, element);
            minPrincipalUpdated = true;
        } else {
            minPrincipalAmount = loanProduct.getMinPrincipalAmount().getAmount();
        }

        final String maxPrincipalParameterName = MAX_PRINCIPAL;
        BigDecimal maxPrincipalAmount;
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

            if ((minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) > 0)
                    && (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) > 0)) {
                baseDataValidator.reset().parameter(principalParameterName).value(principalAmount)
                        .inMinAndMaxAmountRange(minPrincipalAmount, maxPrincipalAmount);
            } else {
                if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).notLessThanMin(minPrincipalAmount);
                } else if (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
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

        final String numberOfRepaymentsParameterName = NUMBER_OF_REPAYMENTS;
        Integer numberOfRepayments;
        if (this.fromApiJsonHelper.parameterExists(numberOfRepaymentsParameterName, element)) {
            numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
            numberOfRepaymentsUpdated = true;
        } else {
            numberOfRepayments = loanProduct.getNumberOfRepayments();
        }

        final String minNumberOfRepaymentsParameterName = MIN_NUMBER_OF_REPAYMENTS;
        Integer minNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(minNumberOfRepaymentsParameterName, element)) {
            minNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(minNumberOfRepaymentsParameterName, element);
            minNumberOfRepaymentsUpdated = true;
        } else {
            minNumberOfRepayments = loanProduct.getMinNumberOfRepayments();
        }

        final String maxNumberOfRepaymentsParameterName = MAX_NUMBER_OF_REPAYMENTS;
        Integer maxNumberOfRepayments;
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
            if (maxNumberOfRepayments != null && maxNumberOfRepayments.compareTo(0) > 0) {
                if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
                    baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                            .inMinMaxRange(minNumberOfRepayments, maxNumberOfRepayments);
                } else {
                    baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                            .notGreaterThanMax(maxNumberOfRepayments);
                }
            } else if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
                baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                        .notLessThanMin(minNumberOfRepayments);
            }
        }
    }

    private void validateNominalInterestRatePerPeriodMinMaxConstraint(final JsonElement element, final LoanProduct loanProduct,
            final DataValidatorBuilder baseDataValidator) {

        if ((this.fromApiJsonHelper.parameterExists(IS_LINKED_TO_FLOATING_INTEREST_RATES, element)
                && this.fromApiJsonHelper.extractBooleanNamed(IS_LINKED_TO_FLOATING_INTEREST_RATES, element))
                || loanProduct.isLinkedToFloatingInterestRate()) {
            return;
        }
        boolean iRPUpdated = false;
        boolean minIRPUpdated = false;
        boolean maxIRPUpdated = false;
        final String interestRatePerPeriodParameterName = INTEREST_RATE_PER_PERIOD;
        BigDecimal interestRatePerPeriod;

        if (this.fromApiJsonHelper.parameterExists(interestRatePerPeriodParameterName, element)) {
            interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName, element);
            iRPUpdated = true;
        } else {
            interestRatePerPeriod = loanProduct.getNominalInterestRatePerPeriod();
        }

        final String minInterestRatePerPeriodParameterName = MIN_INTEREST_RATE_PER_PERIOD;
        BigDecimal minInterestRatePerPeriod = null;
        if (this.fromApiJsonHelper.parameterExists(minInterestRatePerPeriodParameterName, element)) {
            minInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minInterestRatePerPeriodParameterName,
                    element);
            minIRPUpdated = true;
        } else {
            minInterestRatePerPeriod = loanProduct.getMinNominalInterestRatePerPeriod();
        }

        final String maxInterestRatePerPeriodParameterName = MAX_INTEREST_RATE_PER_PERIOD;
        BigDecimal maxInterestRatePerPeriod;
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

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private void validateBorrowerCycleVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        validateBorrowerCyclePrincipalVariations(element, baseDataValidator);
        validateBorrowerCycleRepaymentVariations(element, baseDataValidator);
        validateBorrowerCycleInterestVariations(element, baseDataValidator);
    }

    private void validateBorrowerCyclePrincipalVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {

        validateBorrowerCycleVariations(element, baseDataValidator,
                LoanProductConstants.PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.PRINCIPAL_PER_CYCLE_PARAMETER_NAME, LoanProductConstants.MIN_PRINCIPAL_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.MIN_PRINCIPAL_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.PRINCIPAL_VALUE_USAGE_CONDITION_PARAM_NAME, LoanProductConstants.PRINCIPAL_CYCLE_NUMBERS_PARAM_NAME);
    }

    private void validateBorrowerCycleRepaymentVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        validateBorrowerCycleVariations(element, baseDataValidator,
                LoanProductConstants.NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.REPAYMENT_VALUE_USAGE_CONDITION_PARAM_NAME, LoanProductConstants.REPAYMENT_CYCLE_NUMBER_PARAM_NAME);
    }

    private void validateBorrowerCycleInterestVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        validateBorrowerCycleVariations(element, baseDataValidator,
                LoanProductConstants.INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.MIN_INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.INTEREST_RATE_VALUE_USAGE_CONDITION_PARAM_NAME,
                LoanProductConstants.INTEREST_RATE_VALUE_USAGE_CONDITION_PARAM_NAME);
    }

    private void validateBorrowerCycleVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            final String variationParameterName, final String defaultParameterName, final String minParameterName,
            final String maxParameterName, final String valueUsageConditionParamName, final String cycleNumbersParamName) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        int lastCycleNumber = 0;
        LoanProductValueConditionType lastConditionType = LoanProductValueConditionType.EQUAL;
        if (this.fromApiJsonHelper.parameterExists(variationParameterName, element)) {
            final JsonArray variationArray = this.fromApiJsonHelper.extractJsonArrayNamed(variationParameterName, element);
            if (variationArray != null && variationArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = variationArray.get(i).getAsJsonObject();

                    BigDecimal defaultValue = this.fromApiJsonHelper
                            .extractBigDecimalNamed(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME, jsonObject, locale);
                    BigDecimal minValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.MIN_VALUE_PARAMETER_NAME,
                            jsonObject, locale);
                    BigDecimal maxValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.MAX_VALUE_PARAMETER_NAME,
                            jsonObject, locale);
                    Integer cycleNumber = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME,
                            jsonObject, locale);
                    Integer valueUsageCondition = this.fromApiJsonHelper
                            .extractIntegerNamed(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME, jsonObject, locale);

                    baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).notBlank();
                    if (minValue != null) {
                        baseDataValidator.reset().parameter(minParameterName).value(minValue).notGreaterThanMax(maxValue);
                    }

                    if (maxValue != null) {
                        baseDataValidator.reset().parameter(maxParameterName).value(maxValue).notLessThanMin(minValue);
                    }
                    if ((minValue != null && minValue.compareTo(BigDecimal.ZERO) > 0)
                            && (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) > 0)) {
                        baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).inMinAndMaxAmountRange(minValue,
                                maxValue);
                    } else {
                        if (minValue != null && minValue.compareTo(BigDecimal.ZERO) > 0) {
                            baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).notLessThanMin(minValue);
                        } else if (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) > 0) {
                            baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).notGreaterThanMax(maxValue);
                        }
                    }

                    LoanProductValueConditionType conditionType = LoanProductValueConditionType.INVALID;
                    if (valueUsageCondition != null) {
                        conditionType = LoanProductValueConditionType.fromInt(valueUsageCondition);
                    }
                    baseDataValidator.reset().parameter(valueUsageConditionParamName).value(valueUsageCondition).notNull().inMinMaxRange(
                            LoanProductValueConditionType.EQUAL.getValue(), LoanProductValueConditionType.GREATERTHAN.getValue());
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
            minimumGuaranteeFromGuarantor = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanProductConstants.minimumGuaranteeFromGuarantorParamName, element);
            if (minimumGuaranteeFromGuarantor == null) {
                minimumGuaranteeFromGuarantor = BigDecimal.ZERO;
            }
        }

        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, element)) {
            minimumGuaranteeFromOwnFunds = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, element);
            if (minimumGuaranteeFromOwnFunds == null) {
                minimumGuaranteeFromOwnFunds = BigDecimal.ZERO;
            }
        }

        if (mandatoryGuarantee.compareTo(minimumGuaranteeFromOwnFunds.add(minimumGuaranteeFromGuarantor)) < 0) {
            baseDataValidator.parameter(LoanProductConstants.mandatoryGuaranteeParamName)
                    .failWithCode("must.be.greater.than.sum.of.min.funds");
        }

    }

    private void validatePartialPeriodSupport(final Integer interestCalculationPeriodType, final DataValidatorBuilder baseDataValidator,
            final JsonElement element, final LoanProduct loanProduct) {
        if (interestCalculationPeriodType != null) {
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                    .fromInt(interestCalculationPeriodType);
            boolean considerPartialPeriodUpdates = interestCalculationPeriodMethod.isDaily();

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
                    element)) {
                final Boolean considerPartialInterestEnabled = this.fromApiJsonHelper
                        .extractBooleanNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME, element);
                baseDataValidator.reset().parameter(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME)
                        .value(considerPartialInterestEnabled).notNull().isOneOfTheseValues(true, false);
                final boolean considerPartialPeriods = considerPartialInterestEnabled != null && considerPartialInterestEnabled;
                if (interestCalculationPeriodMethod.isDaily()) {
                    if (considerPartialPeriods) {
                        baseDataValidator.reset().parameter(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME)
                                .failWithCode("not.supported.for.daily.calculations");
                    }
                } else {
                    considerPartialPeriodUpdates = considerPartialPeriods;
                }
            } else if (loanProduct != null) {
                if (!interestCalculationPeriodMethod.isDaily()) {
                    considerPartialPeriodUpdates = loanProduct.getLoanProductRelatedDetail().isAllowPartialPeriodInterestCalcualtion();
                }
            }

            if (!considerPartialPeriodUpdates) {
                Boolean isInterestRecalculationEnabled = null;
                if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME,
                        element)) {
                    isInterestRecalculationEnabled = this.fromApiJsonHelper
                            .extractBooleanNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, element);
                } else if (loanProduct != null) {
                    isInterestRecalculationEnabled = loanProduct.isInterestRecalculationEnabled();
                }
                if (isInterestRecalculationEnabled != null && isInterestRecalculationEnabled) {
                    baseDataValidator.reset().parameter(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME)
                            .failWithCode("not.supported.for.selected.interest.calculation.type");
                }

                Boolean multiDisburseLoan = null;
                if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME, element)) {
                    multiDisburseLoan = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME,
                            element);
                } else if (loanProduct != null) {
                    multiDisburseLoan = loanProduct.isMultiDisburseLoan();
                }
                if (multiDisburseLoan != null && multiDisburseLoan) {
                    baseDataValidator.reset().parameter(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME)
                            .failWithCode("not.supported.for.selected.interest.calculation.type");
                }

                Boolean variableInstallments = null;
                if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowVariableInstallmentsParamName, element)) {
                    variableInstallments = this.fromApiJsonHelper
                            .extractBooleanNamed(LoanProductConstants.allowVariableInstallmentsParamName, element);
                } else if (loanProduct != null) {
                    variableInstallments = loanProduct.isAllowVariabeInstallments();
                }
                if (variableInstallments != null && variableInstallments) {
                    baseDataValidator.reset().parameter(LoanProductConstants.allowVariableInstallmentsParamName)
                            .failWithCode("not.supported.for.selected.interest.calculation.type");
                }

                Boolean floatingInterestRates = null;
                if (this.fromApiJsonHelper.parameterExists(IS_LINKED_TO_FLOATING_INTEREST_RATES, element)) {
                    floatingInterestRates = this.fromApiJsonHelper.extractBooleanNamed(IS_LINKED_TO_FLOATING_INTEREST_RATES, element);
                } else if (loanProduct != null) {
                    floatingInterestRates = loanProduct.isLinkedToFloatingInterestRate();
                }
                if (floatingInterestRates != null && floatingInterestRates) {
                    baseDataValidator.reset().parameter(IS_LINKED_TO_FLOATING_INTEREST_RATES)
                            .failWithCode("not.supported.for.selected.interest.calculation.type");
                }
            }

        }
    }

    public void fixedLengthValidations(final String transactionProcessingStrategyCode, final boolean thereIsInterest,
            final Integer numberOfRepayments, final Integer repayEvery, final JsonElement element,
            final DataValidatorBuilder baseDataValidator) {
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FIXED_LENGTH, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            final Integer fixedLength = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FIXED_LENGTH, element, locale);
            baseDataValidator.reset().parameter(LoanProductConstants.FIXED_LENGTH).value(fixedLength).ignoreIfNull()
                    .integerGreaterThanZero();

            if (fixedLength != null) {
                if (!AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                        .equals(transactionProcessingStrategyCode)) {
                    final String errorMsg = "Fixed Length configuration is only allowed with "
                            + AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY + " strategy";
                    throw new GeneralPlatformDomainRuleException("error.msg.fixed.length.only.supported.for.advanced.payment.allocation",
                            errorMsg, AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
                }

                if (thereIsInterest) {
                    final String errorMsg = "Fixed Length configuration is only allowed for zero interest products";
                    throw new GeneralPlatformDomainRuleException("error.msg.fixed.length.only.supported.for.zero.interest", errorMsg);
                }

                final Integer valueToCompare = ((numberOfRepayments - 1) * repayEvery) + 1;
                if (fixedLength.compareTo(valueToCompare) < 0) {
                    final String errorMsg = "Wrong configuration between Number Of Repayments: " + numberOfRepayments + " * " + repayEvery
                            + " and Fixed Length: " + fixedLength + " values";
                    throw new GeneralPlatformDomainRuleException("error.msg.number.repayments.and.fixed.length.configuration.not.valid",
                            errorMsg, numberOfRepayments, repayEvery, valueToCompare, fixedLength);
                }
            }
        }
    }

    private void validateLoanScheduleType(final String transactionProcessingStrategyCode, final DataValidatorBuilder baseDataValidator,
            final JsonElement element) {
        final String loanScheduleType = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_SCHEDULE_TYPE, element);
        baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_TYPE).value(loanScheduleType)
                .isOneOfEnumValues(LoanScheduleType.class);

        if (!LoanScheduleType.PROGRESSIVE.equals(LoanScheduleType.valueOf(loanScheduleType))
                && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                        .equals(transactionProcessingStrategyCode)) {
            baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE).failWithCode(
                    "supported.only.for.progressive.loan.schedule.type",
                    "Progressive repayment schedule processing is only available with `Advanced payment allocation` strategy");
        }

        if (LoanScheduleType.PROGRESSIVE.equals(LoanScheduleType.valueOf(loanScheduleType))
                && !AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                        .equals(transactionProcessingStrategyCode)) {
            baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_TYPE).failWithCode(
                    "supported.only.with.advanced.payment.allocation.strategy",
                    loanScheduleType + " loan schedule type is not available with " + transactionProcessingStrategyCode + " strategy");
        }
    }

    public void validateRepaymentPeriodWithGraceSettings(final Integer numberOfRepayments, final Integer graceOnPrincipalPayment,
            final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final Integer recurringMoratoriumOnPrincipalPeriods,
            DataValidatorBuilder baseDataValidator) {
        if (numberOfRepayments <= defaultToZeroIfNull(graceOnPrincipalPayment)) {
            baseDataValidator.reset().parameter("graceOnPrincipalPayment").value(graceOnPrincipalPayment)
                    .failWithCode(".mustBeLessThan.numberOfRepayments");
        }

        if (numberOfRepayments <= defaultToZeroIfNull(graceOnInterestPayment)) {
            baseDataValidator.reset().parameter("graceOnInterestPayment").value(graceOnInterestPayment)
                    .failWithCode(".mustBeLessThan.numberOfRepayments");
        }

        if (numberOfRepayments < defaultToZeroIfNull(graceOnInterestCharged)) {
            baseDataValidator.reset().parameter("graceOnInterestCharged").value(graceOnInterestCharged)
                    .failWithCode(".mustBeLessThan.numberOfRepayments");
        }

        int graceOnPrincipal = 0;
        if (graceOnPrincipalPayment != null) {
            graceOnPrincipal = graceOnPrincipalPayment;
        }
        int recurMoratoriumOnPrincipal = 0;
        if (recurringMoratoriumOnPrincipalPeriods != null) {
            recurMoratoriumOnPrincipal = recurringMoratoriumOnPrincipalPeriods;
        }

        if ((recurMoratoriumOnPrincipal > 0) && ((numberOfRepayments - graceOnPrincipal) % (recurMoratoriumOnPrincipal + 1) != 1)) {
            baseDataValidator.reset().parameter("graceOnPrincipalPayments.and.recurringMoratoriumOnPrincipalPeriods")
                    .value(graceOnPrincipal).value(recurMoratoriumOnPrincipal)
                    .failWithCode("causes.principal.moratorium.for.last.installment");
        }
    }

    private Integer defaultToZeroIfNull(final Integer value) {
        Integer result = value;
        if (value == null) {
            result = 0;
        }
        return result;
    }
}
