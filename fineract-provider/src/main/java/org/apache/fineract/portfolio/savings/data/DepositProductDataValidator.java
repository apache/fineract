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
package org.apache.fineract.portfolio.savings.data;

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.idParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.FIXED_DEPOSIT_PRODUCT_REQUEST_DATA_PARAMETERS;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.FIXED_DEPOSIT_PRODUCT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.RECURRING_DEPOSIT_PRODUCT_REQUEST_DATA_PARAMETERS;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.allowWithdrawalParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.chartsParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositAmountParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositMaxAmountParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositMinAmountParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.isMandatoryDepositParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.maxDepositTermParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.maxDepositTermTypeIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.minDepositTermParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.minDepositTermTypeIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosurePenalApplicableParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosurePenalInterestParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.currencyCodeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.feeAmountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.feeOnMonthDayParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.inMultiplesOfParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minBalanceForInterestCalculationParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nameParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.shortNameParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.taxGroupIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withHoldTaxParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.common.AccountingConstants.SavingProductAccountingParams;
import org.apache.fineract.accounting.common.AccountingValidations;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartDataValidator;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.PreClosurePenalInterestOnType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositProductDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final InterestRateChartDataValidator chartDataValidator;
    private final SavingsProductAccountingDataValidator savingsProductAccountingDataValidator;

    public void validateForFixedDepositCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, FIXED_DEPOSIT_PRODUCT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FIXED_DEPOSIT_PRODUCT_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        validateDepositDetailForCreate(element, this.fromApiJsonHelper, baseDataValidator, DepositAccountType.FIXED_DEPOSIT);

        validatePreClosureDetailForCreate(element, baseDataValidator);

        validateDepositTermDeatilForCreate(element, baseDataValidator);

        validateChartsData(element, baseDataValidator);

        validateDepositAmountForCreate(element, baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForFixedDepositUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, FIXED_DEPOSIT_PRODUCT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FIXED_DEPOSIT_PRODUCT_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        validateDepositDetailForUpdate(element, fromApiJsonHelper, baseDataValidator);

        validatePreClosureDetailForUpdate(element, baseDataValidator);

        validateDepositTermDetailForUpdate(element, baseDataValidator);

        validateChartsData(element, baseDataValidator);

        validateDepositAmountForUpdate(element, baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForRecurringDepositCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, RECURRING_DEPOSIT_PRODUCT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        validateDepositDetailForCreate(element, this.fromApiJsonHelper, baseDataValidator, DepositAccountType.RECURRING_DEPOSIT);

        validatePreClosureDetailForCreate(element, baseDataValidator);

        validateDepositTermDeatilForCreate(element, baseDataValidator);

        validateRecurringDetailForCreate(element, baseDataValidator);

        validateChartsData(element, baseDataValidator);

        validateDepositAmountForCreate(element, baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForRecurringDepositUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, RECURRING_DEPOSIT_PRODUCT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        validateDepositDetailForUpdate(element, fromApiJsonHelper, baseDataValidator);

        validatePreClosureDetailForUpdate(element, baseDataValidator);

        validateDepositTermDetailForUpdate(element, baseDataValidator);

        validateRecurringDepositUpdate(element, baseDataValidator);

        validateChartsData(element, baseDataValidator);

        validateDepositAmountForUpdate(element, baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateDepositDetailForCreate(final JsonElement element, final FromJsonHelper fromApiJsonHelper,
            final DataValidatorBuilder baseDataValidator, final DepositAccountType accountType) {
        final String name = fromApiJsonHelper.extractStringNamed(nameParamName, element);
        baseDataValidator.reset().parameter(nameParamName).value(name).notBlank().notExceedingLengthOf(100);

        final String shortName = fromApiJsonHelper.extractStringNamed(shortNameParamName, element);
        baseDataValidator.reset().parameter(shortNameParamName).value(shortName).notBlank().notExceedingLengthOf(4);

        final String description = fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
        baseDataValidator.reset().parameter(descriptionParamName).value(description).notBlank().notExceedingLengthOf(500);

        final String currencyCode = fromApiJsonHelper.extractStringNamed(currencyCodeParamName, element);
        baseDataValidator.reset().parameter(currencyCodeParamName).value(currencyCode).notBlank();

        final Integer digitsAfterDecimal = fromApiJsonHelper.extractIntegerSansLocaleNamed(digitsAfterDecimalParamName, element);
        baseDataValidator.reset().parameter(digitsAfterDecimalParamName).value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);

        if (fromApiJsonHelper.parameterExists(inMultiplesOfParamName, element)) {
            final Integer inMultiplesOf = fromApiJsonHelper.extractIntegerNamed(inMultiplesOfParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(inMultiplesOfParamName).value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();
        }

        if (fromApiJsonHelper.parameterExists(nominalAnnualInterestRateParamName, element)) {
            final BigDecimal nominalAnnualInterestRate = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(nominalAnnualInterestRateParamName, element);
            baseDataValidator.reset().parameter(nominalAnnualInterestRateParamName).value(nominalAnnualInterestRate).notNull()
                    .zeroOrPositiveAmount();
        }
        final Integer interestCompoundingPeriodType = fromApiJsonHelper
                .extractIntegerSansLocaleNamed(interestCompoundingPeriodTypeParamName, element);
        baseDataValidator.reset().parameter(interestCompoundingPeriodTypeParamName).value(interestCompoundingPeriodType).notNull()
                .isOneOfTheseValues(SavingsCompoundingInterestPeriodType.integerValues());

        final Integer interestPostingPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(interestPostingPeriodTypeParamName,
                element);
        baseDataValidator.reset().parameter(interestPostingPeriodTypeParamName).value(interestPostingPeriodType).notNull()
                .isOneOfTheseValues(SavingsPostingInterestPeriodType.integerValues());

        final Integer interestCalculationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(interestCalculationTypeParamName, element);
        baseDataValidator.reset().parameter(interestCalculationTypeParamName).value(interestCalculationType).notNull()
                .isOneOfTheseValues(SavingsInterestCalculationType.integerValues());

        final Integer interestCalculationDaysInYearType = fromApiJsonHelper
                .extractIntegerSansLocaleNamed(interestCalculationDaysInYearTypeParamName, element);
        baseDataValidator.reset().parameter(interestCalculationDaysInYearTypeParamName).value(interestCalculationDaysInYearType).notNull()
                .isOneOfTheseValues(SavingsInterestCalculationDaysInYearType.integerValues());

        if (fromApiJsonHelper.parameterExists(lockinPeriodFrequencyParamName, element)) {

            final Integer lockinPeriodFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName, element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).integerZeroOrGreater();

            if (lockinPeriodFrequency != null) {
                final Integer lockinPeriodFrequencyType = fromApiJsonHelper
                        .extractIntegerSansLocaleNamed(lockinPeriodFrequencyTypeParamName, element);
                baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).notNull()
                        .inMinMaxRange(0, 3);
            }
        }

        if (fromApiJsonHelper.parameterExists(lockinPeriodFrequencyTypeParamName, element)) {
            final Integer lockinPeriodFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(lockinPeriodFrequencyTypeParamName,
                    element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).inMinMaxRange(0, 3);

            if (lockinPeriodFrequencyType != null) {
                final Integer lockinPeriodFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName,
                        element);
                baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).notNull()
                        .integerZeroOrGreater();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(minBalanceForInterestCalculationParamName, element)) {
            final BigDecimal minBalanceForInterestCalculation = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(minBalanceForInterestCalculationParamName, element);
            baseDataValidator.reset().parameter(minBalanceForInterestCalculationParamName).value(minBalanceForInterestCalculation)
                    .ignoreIfNull().zeroOrPositiveAmount();
        }

        // accounting related data validation
        final Integer accountingRuleType = fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).notNull().inMinMaxRange(1, 3);

        Boolean isDormancyActive = this.fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.isDormancyTrackingActiveParamName,
                element);
        if (isDormancyActive == null) {
            isDormancyActive = false;
        }

        if (AccountingValidations.isCashBasedAccounting(accountingRuleType)
                || AccountingValidations.isAccrualPeriodicBasedAccounting(accountingRuleType)) {
            savingsProductAccountingDataValidator.evaluateProductAccountingData(accountingRuleType, isDormancyActive, element,
                    baseDataValidator, accountType);
        }

        validateTaxWithHoldingParams(baseDataValidator, element, true);
    }

    public void validatePreClosureDetailForCreate(JsonElement element, DataValidatorBuilder baseDataValidator) {
        if (fromApiJsonHelper.parameterExists(preClosurePenalApplicableParamName, element)) {
            final boolean preClosurePenalApplicable = fromApiJsonHelper.extractBooleanNamed(preClosurePenalApplicableParamName, element);

            if (preClosurePenalApplicable) {
                final BigDecimal penalInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(preClosurePenalInterestParamName,
                        element);
                baseDataValidator.reset().parameter(preClosurePenalInterestParamName).value(penalInterestRate)
                        .cantBeBlankWhenParameterProvidedIs(preClosurePenalApplicableParamName, preClosurePenalApplicable)
                        .zeroOrPositiveAmount();

                final Integer preClosurePenalInterestType = this.fromApiJsonHelper
                        .extractIntegerSansLocaleNamed(preClosurePenalInterestOnTypeIdParamName, element);
                baseDataValidator.reset().parameter(preClosurePenalInterestOnTypeIdParamName).value(preClosurePenalInterestType)
                        .cantBeBlankWhenParameterProvidedIs(preClosurePenalApplicableParamName, preClosurePenalApplicable)
                        .isOneOfTheseValues(PreClosurePenalInterestOnType.integerValues());
            }
        }
    }

    public void validateDepositTermDeatilForCreate(JsonElement element, DataValidatorBuilder baseDataValidator) {

        final Integer minTerm = fromApiJsonHelper.extractIntegerSansLocaleNamed(minDepositTermParamName, element);
        baseDataValidator.reset().parameter(minDepositTermParamName).value(minTerm).notNull().integerGreaterThanZero();

        if (fromApiJsonHelper.parameterExists(maxDepositTermParamName, element)) {
            final Integer maxTerm = fromApiJsonHelper.extractIntegerSansLocaleNamed(maxDepositTermParamName, element);
            baseDataValidator.reset().parameter(maxDepositTermParamName).value(maxTerm).integerGreaterThanZero();
        }

        final Integer minDepositTermType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(minDepositTermTypeIdParamName, element);
        baseDataValidator.reset().parameter(minDepositTermTypeIdParamName).value(minDepositTermType).ignoreIfNull()
                .isOneOfTheseValues(SavingsPeriodFrequencyType.integerValues());

        if (fromApiJsonHelper.parameterExists(maxDepositTermTypeIdParamName, element)) {
            final Integer maxDepositTermType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(maxDepositTermTypeIdParamName, element);
            baseDataValidator.reset().parameter(maxDepositTermTypeIdParamName).value(maxDepositTermType)
                    .isOneOfTheseValues(SavingsPeriodFrequencyType.integerValues());
        }

        if (fromApiJsonHelper.parameterExists(inMultiplesOfDepositTermParamName, element)) {
            final Integer inMultiplesOfDepositTerm = fromApiJsonHelper.extractIntegerSansLocaleNamed(inMultiplesOfDepositTermParamName,
                    element);
            baseDataValidator.reset().parameter(inMultiplesOfDepositTermParamName).value(inMultiplesOfDepositTerm).integerGreaterThanZero();
            final Integer inMultiplesOfDepositTermType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(inMultiplesOfDepositTermTypeIdParamName, element);
            baseDataValidator.reset().parameter(inMultiplesOfDepositTermTypeIdParamName).value(inMultiplesOfDepositTermType)
                    .cantBeBlankWhenParameterProvidedIs(inMultiplesOfDepositTermParamName, inMultiplesOfDepositTerm)
                    .isOneOfTheseValues(SavingsPeriodFrequencyType.integerValues());
        }
    }

    private void validateChartsData(JsonElement element, DataValidatorBuilder baseDataValidator) {
        if (element.isJsonObject()) {

            final JsonArray array = this.fromApiJsonHelper.extractJsonArrayNamed(chartsParamName, element);
            baseDataValidator.reset().parameter(chartsParamName).value(array).notNull().jsonArrayNotEmpty();

            for (int i = 0; i < array.size(); i++) {
                final JsonObject interestRateChartElement = array.get(i).getAsJsonObject();
                final String json = this.fromApiJsonHelper.toJson(interestRateChartElement);
                // chart for create
                if (!this.fromApiJsonHelper.parameterExists(idParamName, interestRateChartElement)) {
                    this.chartDataValidator.validateForCreate(json, baseDataValidator);
                } else { // chart for update
                    this.chartDataValidator.validateForUpdate(json, baseDataValidator);
                }
            }
        }
    }

    public void validateDepositDetailForUpdate(final JsonElement element, final FromJsonHelper fromApiJsonHelper,
            final DataValidatorBuilder baseDataValidator) {
        if (fromApiJsonHelper.parameterExists(nameParamName, element)) {
            final String name = fromApiJsonHelper.extractStringNamed(nameParamName, element);
            baseDataValidator.reset().parameter(nameParamName).value(name).notBlank().notExceedingLengthOf(100);
        }

        if (fromApiJsonHelper.parameterExists(shortNameParamName, element)) {
            final String shortName = fromApiJsonHelper.extractStringNamed(shortNameParamName, element);
            baseDataValidator.reset().parameter(shortNameParamName).value(shortName).notBlank().notExceedingLengthOf(4);
        }

        if (fromApiJsonHelper.parameterExists(descriptionParamName, element)) {
            final String description = fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
            baseDataValidator.reset().parameter(descriptionParamName).value(description).notBlank().notExceedingLengthOf(500);
        }

        if (fromApiJsonHelper.parameterExists(currencyCodeParamName, element)) {
            final String currencyCode = fromApiJsonHelper.extractStringNamed(currencyCodeParamName, element);
            baseDataValidator.reset().parameter(currencyCodeParamName).value(currencyCode).notBlank();
        }

        if (fromApiJsonHelper.parameterExists(digitsAfterDecimalParamName, element)) {
            final Integer digitsAfterDecimal = fromApiJsonHelper.extractIntegerSansLocaleNamed(digitsAfterDecimalParamName, element);
            baseDataValidator.reset().parameter(digitsAfterDecimalParamName).value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);
        }

        if (fromApiJsonHelper.parameterExists(inMultiplesOfParamName, element)) {
            final Integer inMultiplesOf = fromApiJsonHelper.extractIntegerNamed(inMultiplesOfParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(inMultiplesOfParamName).value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();
        }

        if (fromApiJsonHelper.parameterExists(nominalAnnualInterestRateParamName, element)) {
            final BigDecimal interestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(nominalAnnualInterestRateParamName, element);
            baseDataValidator.reset().parameter(nominalAnnualInterestRateParamName).value(interestRate).notNull().zeroOrPositiveAmount();
        }

        if (fromApiJsonHelper.parameterExists(interestCompoundingPeriodTypeParamName, element)) {
            final Integer interestCompoundingPeriodType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(interestCompoundingPeriodTypeParamName, element);
            baseDataValidator.reset().parameter(interestCompoundingPeriodTypeParamName).value(interestCompoundingPeriodType).notNull()
                    .isOneOfTheseValues(SavingsCompoundingInterestPeriodType.integerValues());
        }

        if (fromApiJsonHelper.parameterExists(interestCalculationTypeParamName, element)) {
            final Integer interestCalculationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(interestCalculationTypeParamName,
                    element);
            baseDataValidator.reset().parameter(interestCalculationTypeParamName).value(interestCalculationType).notNull().inMinMaxRange(1,
                    2);
        }

        if (fromApiJsonHelper.parameterExists(interestCalculationDaysInYearTypeParamName, element)) {
            final Integer interestCalculationDaysInYearType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(interestCalculationDaysInYearTypeParamName, element);
            baseDataValidator.reset().parameter(interestCalculationDaysInYearTypeParamName).value(interestCalculationDaysInYearType)
                    .notNull().isOneOfTheseValues(360, 365);
        }

        if (fromApiJsonHelper.parameterExists(minRequiredOpeningBalanceParamName, element)) {
            final BigDecimal minOpeningBalance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minRequiredOpeningBalanceParamName,
                    element);
            baseDataValidator.reset().parameter(minRequiredOpeningBalanceParamName).value(minOpeningBalance).ignoreIfNull()
                    .zeroOrPositiveAmount();
        }

        if (fromApiJsonHelper.parameterExists(lockinPeriodFrequencyParamName, element)) {
            final Integer lockinPeriodFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName, element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).ignoreIfNull()
                    .integerZeroOrGreater();
        }

        if (fromApiJsonHelper.parameterExists(lockinPeriodFrequencyTypeParamName, element)) {
            final Integer lockinPeriodFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(lockinPeriodFrequencyTypeParamName,
                    element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).inMinMaxRange(0, 3);
        }

        if (fromApiJsonHelper.parameterExists(withdrawalFeeForTransfersParamName, element)) {
            final Boolean isWithdrawalFeeApplicableForTransfers = fromApiJsonHelper.extractBooleanNamed(withdrawalFeeForTransfersParamName,
                    element);
            baseDataValidator.reset().parameter(withdrawalFeeForTransfersParamName).value(isWithdrawalFeeApplicableForTransfers)
                    .ignoreIfNull().validateForBooleanValue();
        }

        if (fromApiJsonHelper.parameterExists(feeAmountParamName, element)) {
            final BigDecimal annualFeeAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(feeAmountParamName, element);
            baseDataValidator.reset().parameter(feeAmountParamName).value(annualFeeAmount).ignoreIfNull().zeroOrPositiveAmount();
        }

        if (fromApiJsonHelper.parameterExists(feeOnMonthDayParamName, element)) {
            final MonthDay monthDayOfAnnualFee = fromApiJsonHelper.extractMonthDayNamed(feeOnMonthDayParamName, element);
            baseDataValidator.reset().parameter(feeOnMonthDayParamName).value(monthDayOfAnnualFee).ignoreIfNull();
        }

        if (this.fromApiJsonHelper.parameterExists(minBalanceForInterestCalculationParamName, element)) {
            final BigDecimal minBalanceForInterestCalculation = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(minBalanceForInterestCalculationParamName, element);
            baseDataValidator.reset().parameter(minBalanceForInterestCalculationParamName).value(minBalanceForInterestCalculation)
                    .ignoreIfNull().zeroOrPositiveAmount();
        }

        final Long savingsControlAccountId = fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.SAVINGS_CONTROL.getValue(),
                element);
        baseDataValidator.reset().parameter(SavingProductAccountingParams.SAVINGS_CONTROL.getValue()).value(savingsControlAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long savingsReferenceAccountId = fromApiJsonHelper
                .extractLongNamed(SavingProductAccountingParams.SAVINGS_REFERENCE.getValue(), element);
        baseDataValidator.reset().parameter(SavingProductAccountingParams.SAVINGS_REFERENCE.getValue()).value(savingsReferenceAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long transfersInSuspenseAccountId = fromApiJsonHelper
                .extractLongNamed(SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), element);
        baseDataValidator.reset().parameter(SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue()).value(transfersInSuspenseAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long interestOnSavingsAccountId = fromApiJsonHelper
                .extractLongNamed(SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue(), element);
        baseDataValidator.reset().parameter(SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue()).value(interestOnSavingsAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromFeeId = fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.INCOME_FROM_FEES.getValue(), element);
        baseDataValidator.reset().parameter(SavingProductAccountingParams.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId).ignoreIfNull()
                .integerGreaterThanZero();

        final Long incomeFromPenaltyId = fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue(),
                element);
        baseDataValidator.reset().parameter(SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                .ignoreIfNull().integerGreaterThanZero();

        savingsProductAccountingDataValidator.validatePaymentChannelFundSourceMappings(baseDataValidator, element);
        savingsProductAccountingDataValidator.validateChargeToIncomeAccountMappings(baseDataValidator, element);
        validateTaxWithHoldingParams(baseDataValidator, element, false);
    }

    public void validatePreClosureDetailForUpdate(JsonElement element, DataValidatorBuilder baseDataValidator) {
        if (fromApiJsonHelper.parameterExists(preClosurePenalApplicableParamName, element)) {
            final Boolean preClosurePenalApplicable = fromApiJsonHelper.extractBooleanNamed(preClosurePenalApplicableParamName, element);
            baseDataValidator.reset().parameter(preClosurePenalApplicableParamName).value(preClosurePenalApplicable).notNull();
        }

        if (fromApiJsonHelper.parameterExists(preClosurePenalInterestParamName, element)) {
            final BigDecimal penalInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(preClosurePenalInterestParamName,
                    element);
            baseDataValidator.reset().parameter(preClosurePenalInterestParamName).value(penalInterestRate).notNull().zeroOrPositiveAmount();
        }

        if (fromApiJsonHelper.parameterExists(preClosurePenalInterestOnTypeIdParamName, element)) {
            final Integer preClosurePenalInterestType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(preClosurePenalInterestOnTypeIdParamName, element);
            baseDataValidator.reset().parameter(preClosurePenalInterestOnTypeIdParamName).value(preClosurePenalInterestType).notNull()
                    .isOneOfTheseValues(PreClosurePenalInterestOnType.integerValues());
        }
    }

    public void validateDepositTermDetailForUpdate(JsonElement element, DataValidatorBuilder baseDataValidator) {

        if (fromApiJsonHelper.parameterExists(minDepositTermParamName, element)) {
            final Integer minTerm = fromApiJsonHelper.extractIntegerSansLocaleNamed(minDepositTermParamName, element);
            baseDataValidator.reset().parameter(minDepositTermParamName).value(minTerm).integerGreaterThanZero();
        }

        if (fromApiJsonHelper.parameterExists(maxDepositTermParamName, element)) {
            final Integer maxTerm = fromApiJsonHelper.extractIntegerSansLocaleNamed(maxDepositTermParamName, element);
            baseDataValidator.reset().parameter(maxDepositTermParamName).value(maxTerm).integerGreaterThanZero();
        }

        if (fromApiJsonHelper.parameterExists(minDepositTermTypeIdParamName, element)) {
            final Integer minDepositTermType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(minDepositTermTypeIdParamName, element);
            baseDataValidator.reset().parameter(minDepositTermTypeIdParamName).value(minDepositTermType)
                    .isOneOfTheseValues(SavingsPeriodFrequencyType.integerValues());
        }

        if (fromApiJsonHelper.parameterExists(maxDepositTermTypeIdParamName, element)) {
            final Integer maxDepositTermType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(maxDepositTermTypeIdParamName, element);
            baseDataValidator.reset().parameter(maxDepositTermTypeIdParamName).value(maxDepositTermType)
                    .isOneOfTheseValues(SavingsPeriodFrequencyType.integerValues());
        }

        if (fromApiJsonHelper.parameterExists(inMultiplesOfDepositTermParamName, element)) {
            final Integer inMultiplesOfDepositTerm = fromApiJsonHelper.extractIntegerSansLocaleNamed(inMultiplesOfDepositTermParamName,
                    element);
            baseDataValidator.reset().parameter(inMultiplesOfDepositTermParamName).value(inMultiplesOfDepositTerm).integerGreaterThanZero();
        }

        if (fromApiJsonHelper.parameterExists(inMultiplesOfDepositTermTypeIdParamName, element)) {
            final Integer inMultiplesOfDepositTermType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(inMultiplesOfDepositTermTypeIdParamName, element);
            baseDataValidator.reset().parameter(inMultiplesOfDepositTermTypeIdParamName).value(inMultiplesOfDepositTermType)
                    .isOneOfTheseValues(SavingsPeriodFrequencyType.integerValues());
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateRecurringDetailForCreate(JsonElement element, DataValidatorBuilder baseDataValidator) {

        final Boolean isMandatoryDeposit = this.fromApiJsonHelper.extractBooleanNamed(isMandatoryDepositParamName, element);
        baseDataValidator.reset().parameter(isMandatoryDepositParamName).value(isMandatoryDeposit).ignoreIfNull().validateForBooleanValue();
        final Boolean allowWithdrawal = this.fromApiJsonHelper.extractBooleanNamed(allowWithdrawalParamName, element);
        baseDataValidator.reset().parameter(allowWithdrawalParamName).value(allowWithdrawal).ignoreIfNull().validateForBooleanValue();
        final Boolean adjustAdvanceTowardsFuturePayments = this.fromApiJsonHelper
                .extractBooleanNamed(adjustAdvanceTowardsFuturePaymentsParamName, element);
        baseDataValidator.reset().parameter(adjustAdvanceTowardsFuturePaymentsParamName).value(adjustAdvanceTowardsFuturePayments)
                .ignoreIfNull().validateForBooleanValue();
    }

    public void validateRecurringDepositUpdate(JsonElement element, DataValidatorBuilder baseDataValidator) {

        if (this.fromApiJsonHelper.parameterExists(isMandatoryDepositParamName, element)) {
            final Boolean isMandatoryDeposit = this.fromApiJsonHelper.extractBooleanNamed(isMandatoryDepositParamName, element);
            baseDataValidator.reset().parameter(isMandatoryDepositParamName).value(isMandatoryDeposit).ignoreIfNull()
                    .validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(allowWithdrawalParamName, element)) {
            final Boolean allowWithdrawal = this.fromApiJsonHelper.extractBooleanNamed(allowWithdrawalParamName, element);
            baseDataValidator.reset().parameter(allowWithdrawalParamName).value(allowWithdrawal).ignoreIfNull().validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(adjustAdvanceTowardsFuturePaymentsParamName, element)) {
            final Boolean adjustAdvanceTowardsFuturePayments = this.fromApiJsonHelper
                    .extractBooleanNamed(adjustAdvanceTowardsFuturePaymentsParamName, element);
            baseDataValidator.reset().parameter(adjustAdvanceTowardsFuturePaymentsParamName).value(adjustAdvanceTowardsFuturePayments)
                    .ignoreIfNull().validateForBooleanValue();
        }
    }

    private void validateDepositAmountForCreate(JsonElement element, DataValidatorBuilder baseDataValidator) {
        final BigDecimal depositAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(depositAmountParamName, element);
        baseDataValidator.reset().parameter(depositAmountParamName).value(depositAmount).notNull().positiveAmount();

        BigDecimal depositMinAmount = null;
        if (this.fromApiJsonHelper.parameterExists(depositMinAmountParamName, element)) {
            depositMinAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(depositMinAmountParamName, element);
            baseDataValidator.reset().parameter(depositMinAmountParamName).value(depositMinAmount).notNull().positiveAmount();
        }

        BigDecimal depositMaxAmount = null;
        if (this.fromApiJsonHelper.parameterExists(depositMaxAmountParamName, element)) {
            depositMaxAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(depositMaxAmountParamName, element);
            baseDataValidator.reset().parameter(depositMaxAmountParamName).value(depositMaxAmount).notNull().positiveAmount();
        }

        if (depositMaxAmount != null && depositMaxAmount.compareTo(BigDecimal.ZERO) >= 0) {
            if (depositMinAmount != null && depositMinAmount.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(depositMaxAmountParamName).value(depositMaxAmount).notLessThanMin(depositMinAmount);
                if (depositMinAmount.compareTo(depositMaxAmount) <= 0) {
                    baseDataValidator.reset().parameter(depositAmountParamName).value(depositAmount)
                            .inMinAndMaxAmountRange(depositMinAmount, depositMaxAmount);
                }
            } else {
                baseDataValidator.reset().parameter(depositAmountParamName).value(depositAmount).notGreaterThanMax(depositMaxAmount);
            }
        } else if (depositMinAmount != null && depositMinAmount.compareTo(BigDecimal.ZERO) >= 0) {
            baseDataValidator.reset().parameter(depositAmountParamName).value(depositAmount).notLessThanMin(depositMinAmount);
        }
    }

    private void validateDepositAmountForUpdate(JsonElement element, DataValidatorBuilder baseDataValidator) {
        BigDecimal depositAmount = null;

        if (this.fromApiJsonHelper.parameterExists(depositAmountParamName, element)) {
            depositAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(depositAmountParamName, element);
            baseDataValidator.reset().parameter(depositAmountParamName).value(depositAmount).notNull().positiveAmount();
        }

        BigDecimal depositMinAmount = null;
        if (this.fromApiJsonHelper.parameterExists(depositMinAmountParamName, element)) {
            depositMinAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(depositMinAmountParamName, element);
            baseDataValidator.reset().parameter(depositMinAmountParamName).value(depositMinAmount).notNull().positiveAmount();
        }

        BigDecimal depositMaxAmount = null;
        if (this.fromApiJsonHelper.parameterExists(depositMaxAmountParamName, element)) {
            depositMaxAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(depositMaxAmountParamName, element);
            baseDataValidator.reset().parameter(depositMaxAmountParamName).value(depositMaxAmount).notNull().positiveAmount();
        }

        if (depositAmount != null) {
            if (depositMaxAmount != null && depositMaxAmount.compareTo(BigDecimal.ZERO) >= 0) {
                if (depositMinAmount != null && depositMinAmount.compareTo(BigDecimal.ZERO) >= 0) {
                    baseDataValidator.reset().parameter(depositMaxAmountParamName).value(depositMaxAmount).notLessThanMin(depositMinAmount);
                    if (depositMinAmount.compareTo(depositMaxAmount) <= 0) {
                        baseDataValidator.reset().parameter(depositAmountParamName).value(depositAmount)
                                .inMinAndMaxAmountRange(depositMinAmount, depositMaxAmount);
                    }
                } else {
                    baseDataValidator.reset().parameter(depositAmountParamName).value(depositAmount).notGreaterThanMax(depositMaxAmount);
                }
            } else if (depositMinAmount != null && depositMinAmount.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(depositAmountParamName).value(depositAmount).notLessThanMin(depositMinAmount);
            }
        }
    }

    private void validateTaxWithHoldingParams(final DataValidatorBuilder baseDataValidator, final JsonElement element,
            final boolean isCreate) {
        if (this.fromApiJsonHelper.parameterExists(withHoldTaxParamName, element)) {
            final String withHoldTax = this.fromApiJsonHelper.extractStringNamed(withHoldTaxParamName, element);
            baseDataValidator.reset().parameter(withHoldTaxParamName).value(withHoldTax).ignoreIfNull().validateForBooleanValue();
        }
        Boolean withHoldTax = this.fromApiJsonHelper.extractBooleanNamed(withHoldTaxParamName, element);
        if (withHoldTax == null) {
            withHoldTax = false;
        }

        if (this.fromApiJsonHelper.parameterExists(taxGroupIdParamName, element)) {
            final Long taxGroupId = this.fromApiJsonHelper.extractLongNamed(taxGroupIdParamName, element);
            baseDataValidator.reset().parameter(taxGroupIdParamName).value(taxGroupId).ignoreIfNull().longGreaterThanZero();
            if (withHoldTax) {
                baseDataValidator.reset().parameter(taxGroupIdParamName).value(taxGroupId).notBlank();
            }

        } else if (withHoldTax && isCreate) {
            final Long taxGroupId = null;
            baseDataValidator.reset().parameter(taxGroupIdParamName).value(taxGroupId).notBlank();
        }

    }
}
