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

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.allowOverdraftParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.currencyCodeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.daysToDormancyParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.daysToEscheatParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.daysToInactiveParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.feeAmountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.feeOnMonthDayParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.inMultiplesOfParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.isDormancyTrackingActiveParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lienAllowedParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.maxAllowedLienLimitParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minBalanceForInterestCalculationParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minOverdraftForInterestCalculationParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nameParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.overdraftLimitParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.shortNameParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.taxGroupIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withHoldTaxParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.common.AccountingConstants.SavingProductAccountingParams;
import org.apache.fineract.accounting.common.AccountingValidations;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SavingsProductDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final SavingsProductAccountingDataValidator savingsProductAccountingDataValidator;
    private static final Set<String> SAVINGS_PRODUCT_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            SavingsApiConstants.localeParamName, SavingsApiConstants.monthDayFormatParamName, nameParamName, shortNameParamName,
            descriptionParamName, currencyCodeParamName, digitsAfterDecimalParamName, inMultiplesOfParamName,
            nominalAnnualInterestRateParamName, interestCompoundingPeriodTypeParamName, interestPostingPeriodTypeParamName,
            interestCalculationTypeParamName, interestCalculationDaysInYearTypeParamName, minRequiredOpeningBalanceParamName,
            lockinPeriodFrequencyParamName, lockinPeriodFrequencyTypeParamName, SavingsApiConstants.withdrawalFeeAmountParamName,
            SavingsApiConstants.withdrawalFeeTypeParamName, withdrawalFeeForTransfersParamName, feeAmountParamName, feeOnMonthDayParamName,
            SavingsApiConstants.accountingRuleParamName, SavingsApiConstants.chargesParamName,
            SavingProductAccountingParams.INCOME_FROM_FEES.getValue(), SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue(),
            SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue(), SavingProductAccountingParams.PENALTIES_RECEIVABLE.getValue(),
            SavingProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
            SavingProductAccountingParams.SAVINGS_CONTROL.getValue(), SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue(),
            SavingProductAccountingParams.SAVINGS_REFERENCE.getValue(), SavingProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue(),
            SavingProductAccountingParams.PENALTY_INCOME_ACCOUNT_MAPPING.getValue(),
            SavingProductAccountingParams.FEES_RECEIVABLE.getValue(), SavingProductAccountingParams.INTEREST_PAYABLE.getValue(),
            SavingProductAccountingParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
            SavingProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), SavingProductAccountingParams.INCOME_FROM_INTEREST.getValue(),
            SavingProductAccountingParams.ESCHEAT_LIABILITY.getValue(), isDormancyTrackingActiveParamName, daysToDormancyParamName,
            daysToInactiveParamName, daysToEscheatParamName, allowOverdraftParamName, overdraftLimitParamName,
            nominalAnnualInterestRateOverdraftParamName, minOverdraftForInterestCalculationParamName,
            SavingsApiConstants.minRequiredBalanceParamName, SavingsApiConstants.enforceMinRequiredBalanceParamName,
            SavingsApiConstants.maxAllowedLienLimitParamName, SavingsApiConstants.lienAllowedParamName,
            minBalanceForInterestCalculationParamName, withHoldTaxParamName, taxGroupIdParamName));

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SAVINGS_PRODUCT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_PRODUCT_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(nameParamName, element);
        baseDataValidator.reset().parameter(nameParamName).value(name).notBlank().notExceedingLengthOf(100);

        final String shortName = this.fromApiJsonHelper.extractStringNamed(shortNameParamName, element);
        baseDataValidator.reset().parameter(shortNameParamName).value(shortName).notBlank().notExceedingLengthOf(4);

        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(currencyCodeParamName, element);
        baseDataValidator.reset().parameter(currencyCodeParamName).value(currencyCode).notBlank();

        final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(digitsAfterDecimalParamName, element);
        baseDataValidator.reset().parameter(digitsAfterDecimalParamName).value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);

        if (this.fromApiJsonHelper.parameterExists(inMultiplesOfParamName, element)) {
            final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed(inMultiplesOfParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(inMultiplesOfParamName).value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();
        }
        final BigDecimal nominalAnnualInterestRate = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(nominalAnnualInterestRateParamName, element);
        baseDataValidator.reset().parameter(nominalAnnualInterestRateParamName).value(nominalAnnualInterestRate).notNull()
                .zeroOrPositiveAmount();

        final Integer interestCompoundingPeriodType = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(interestCompoundingPeriodTypeParamName, element);
        baseDataValidator.reset().parameter(interestCompoundingPeriodTypeParamName).value(interestCompoundingPeriodType).notNull()
                .isOneOfTheseValues(SavingsCompoundingInterestPeriodType.integerValues());

        final Integer interestPostingPeriodType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(interestPostingPeriodTypeParamName,
                element);
        baseDataValidator.reset().parameter(interestPostingPeriodTypeParamName).value(interestPostingPeriodType).notNull()
                .isOneOfTheseValues(SavingsPostingInterestPeriodType.integerValues());

        final Integer interestCalculationType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(interestCalculationTypeParamName,
                element);
        baseDataValidator.reset().parameter(interestCalculationTypeParamName).value(interestCalculationType).notNull()
                .isOneOfTheseValues(SavingsInterestCalculationType.integerValues());

        final Integer interestCalculationDaysInYearType = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(interestCalculationDaysInYearTypeParamName, element);
        baseDataValidator.reset().parameter(interestCalculationDaysInYearTypeParamName).value(interestCalculationDaysInYearType).notNull()
                .isOneOfTheseValues(SavingsInterestCalculationDaysInYearType.integerValues());

        if (this.fromApiJsonHelper.parameterExists(descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
            baseDataValidator.reset().parameter(descriptionParamName).value(description).ignoreIfNull().notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(minRequiredOpeningBalanceParamName, element)) {
            final BigDecimal minOpeningBalance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minRequiredOpeningBalanceParamName,
                    element);
            baseDataValidator.reset().parameter(minRequiredOpeningBalanceParamName).value(minOpeningBalance).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(lockinPeriodFrequencyParamName, element)) {

            final Integer lockinPeriodFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName,
                    element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).integerZeroOrGreater();

            if (lockinPeriodFrequency != null) {
                final Integer lockinPeriodFrequencyType = this.fromApiJsonHelper
                        .extractIntegerSansLocaleNamed(lockinPeriodFrequencyTypeParamName, element);
                baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).notNull()
                        .inMinMaxRange(0, 3);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(lockinPeriodFrequencyTypeParamName, element)) {
            final Integer lockinPeriodFrequencyType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(lockinPeriodFrequencyTypeParamName, element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).inMinMaxRange(0, 3);

            if (lockinPeriodFrequencyType != null) {
                final Integer lockinPeriodFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName,
                        element);
                baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).notNull()
                        .integerZeroOrGreater();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(withdrawalFeeForTransfersParamName, element)) {
            final Boolean isWithdrawalFeeApplicableForTransfers = this.fromApiJsonHelper
                    .extractBooleanNamed(withdrawalFeeForTransfersParamName, element);
            baseDataValidator.reset().parameter(withdrawalFeeForTransfersParamName).value(isWithdrawalFeeApplicableForTransfers)
                    .ignoreIfNull().validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(feeAmountParamName, element)) {
            final BigDecimal annualFeeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(feeAmountParamName, element);
            baseDataValidator.reset().parameter(feeAmountParamName).value(annualFeeAmount).zeroOrPositiveAmount();

            if (annualFeeAmount != null) {
                final MonthDay monthDayOfAnnualFee = this.fromApiJsonHelper.extractMonthDayNamed(feeOnMonthDayParamName, element);
                baseDataValidator.reset().parameter(feeOnMonthDayParamName).value(monthDayOfAnnualFee).notNull();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(feeOnMonthDayParamName, element)) {

            final MonthDay monthDayOfAnnualFee = this.fromApiJsonHelper.extractMonthDayNamed(feeOnMonthDayParamName, element);
            if (monthDayOfAnnualFee != null) {
                final BigDecimal annualFeeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(feeAmountParamName, element);
                baseDataValidator.reset().parameter(feeAmountParamName).value(annualFeeAmount).notNull().zeroOrPositiveAmount();
            }
        }

        // dormancy
        Boolean isDormancyActive = this.fromApiJsonHelper.extractBooleanNamed(isDormancyTrackingActiveParamName, element);
        if (isDormancyActive == null) {
            isDormancyActive = false;
        }

        if (isDormancyActive) {
            final Long daysToInact = this.fromApiJsonHelper.extractLongNamed(daysToInactiveParamName, element);
            baseDataValidator.reset().parameter(daysToInactiveParamName).value(daysToInact).notNull().longGreaterThanZero();

            final Long daysToDor = this.fromApiJsonHelper.extractLongNamed(daysToDormancyParamName, element);
            baseDataValidator.reset().parameter(daysToDormancyParamName).value(daysToDor).notNull().longGreaterThanZero();

            final Long daysToEsc = this.fromApiJsonHelper.extractLongNamed(daysToEscheatParamName, element);
            baseDataValidator.reset().parameter(daysToEscheatParamName).value(daysToEsc).notNull().longGreaterThanZero();

            if (null != daysToInact && null != daysToDor && null != daysToEsc) {
                baseDataValidator.reset().parameter(daysToDormancyParamName).value(daysToDor).longGreaterThanNumber(daysToInact);
                baseDataValidator.reset().parameter(daysToEscheatParamName).value(daysToEsc).longGreaterThanNumber(daysToDor);
            }
        }

        // accounting related data validation
        final Integer accountingRuleType = this.fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).notNull().inMinMaxRange(1, 3);

        if (AccountingValidations.isCashBasedAccounting(accountingRuleType)
                || AccountingValidations.isAccrualPeriodicBasedAccounting(accountingRuleType)) {
            savingsProductAccountingDataValidator.evaluateProductAccountingData(accountingRuleType, isDormancyActive, element,
                    baseDataValidator, DepositAccountType.SAVINGS_DEPOSIT, true);
        }

        validateOverdraftParams(baseDataValidator, element);

        if (this.fromApiJsonHelper.parameterExists(minBalanceForInterestCalculationParamName, element)) {
            final BigDecimal minBalanceForInterestCalculation = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(minBalanceForInterestCalculationParamName, element);
            baseDataValidator.reset().parameter(minBalanceForInterestCalculationParamName).value(minBalanceForInterestCalculation)
                    .ignoreIfNull().zeroOrPositiveAmount();
        }
        validateTaxWithHoldingParams(baseDataValidator, element, true);
        validateLienParams(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json, final SavingsProduct product) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SAVINGS_PRODUCT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_PRODUCT_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(nameParamName, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(nameParamName, element);
            baseDataValidator.reset().parameter(nameParamName).value(name).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(shortNameParamName, element)) {
            final String shortName = this.fromApiJsonHelper.extractStringNamed(shortNameParamName, element);
            baseDataValidator.reset().parameter(shortNameParamName).value(shortName).notBlank().notExceedingLengthOf(4);
        }

        if (this.fromApiJsonHelper.parameterExists(descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
            baseDataValidator.reset().parameter(descriptionParamName).value(description).notBlank().notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(currencyCodeParamName, element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed(currencyCodeParamName, element);
            baseDataValidator.reset().parameter(currencyCodeParamName).value(currencyCode).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(digitsAfterDecimalParamName, element)) {
            final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(digitsAfterDecimalParamName, element);
            baseDataValidator.reset().parameter(digitsAfterDecimalParamName).value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);
        }

        if (this.fromApiJsonHelper.parameterExists(inMultiplesOfParamName, element)) {
            final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed(inMultiplesOfParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(inMultiplesOfParamName).value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();
        }

        if (this.fromApiJsonHelper.parameterExists(nominalAnnualInterestRateParamName, element)) {
            final BigDecimal interestRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(nominalAnnualInterestRateParamName,
                    element);
            baseDataValidator.reset().parameter(nominalAnnualInterestRateParamName).value(interestRate).notNull().zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(interestCompoundingPeriodTypeParamName, element)) {
            final Integer interestCompoundingPeriodType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(interestCompoundingPeriodTypeParamName, element);
            baseDataValidator.reset().parameter(interestCompoundingPeriodTypeParamName).value(interestCompoundingPeriodType).notNull()
                    .isOneOfTheseValues(SavingsCompoundingInterestPeriodType.integerValues());
        }

        if (this.fromApiJsonHelper.parameterExists(interestCalculationTypeParamName, element)) {
            final Integer interestCalculationType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(interestCalculationTypeParamName,
                    element);
            baseDataValidator.reset().parameter(interestCalculationTypeParamName).value(interestCalculationType).notNull().inMinMaxRange(1,
                    2);
        }

        if (this.fromApiJsonHelper.parameterExists(interestCalculationDaysInYearTypeParamName, element)) {
            final Integer interestCalculationDaysInYearType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(interestCalculationDaysInYearTypeParamName, element);
            baseDataValidator.reset().parameter(interestCalculationDaysInYearTypeParamName).value(interestCalculationDaysInYearType)
                    .notNull().isOneOfTheseValues(360, 365);
        }

        if (this.fromApiJsonHelper.parameterExists(minRequiredOpeningBalanceParamName, element)) {
            final BigDecimal minOpeningBalance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minRequiredOpeningBalanceParamName,
                    element);
            baseDataValidator.reset().parameter(minRequiredOpeningBalanceParamName).value(minOpeningBalance).ignoreIfNull()
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(lockinPeriodFrequencyParamName, element)) {
            final Integer lockinPeriodFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName,
                    element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).ignoreIfNull()
                    .integerZeroOrGreater();
        }

        if (this.fromApiJsonHelper.parameterExists(lockinPeriodFrequencyTypeParamName, element)) {
            final Integer lockinPeriodFrequencyType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(lockinPeriodFrequencyTypeParamName, element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).inMinMaxRange(0, 3);
        }

        if (this.fromApiJsonHelper.parameterExists(withdrawalFeeForTransfersParamName, element)) {
            final Boolean isWithdrawalFeeApplicableForTransfers = this.fromApiJsonHelper
                    .extractBooleanNamed(withdrawalFeeForTransfersParamName, element);
            baseDataValidator.reset().parameter(withdrawalFeeForTransfersParamName).value(isWithdrawalFeeApplicableForTransfers)
                    .ignoreIfNull().validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(feeAmountParamName, element)) {
            final BigDecimal annualFeeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(feeAmountParamName, element);
            baseDataValidator.reset().parameter(feeAmountParamName).value(annualFeeAmount).ignoreIfNull().zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(feeOnMonthDayParamName, element)) {
            final MonthDay monthDayOfAnnualFee = this.fromApiJsonHelper.extractMonthDayNamed(feeOnMonthDayParamName, element);
            baseDataValidator.reset().parameter(feeOnMonthDayParamName).value(monthDayOfAnnualFee).ignoreIfNull();
        }

        // accounting related data validation
        Integer accountingRuleType = product.getAccountingType();
        if (this.fromApiJsonHelper.parameterExists("accountingRule", element)) {
            accountingRuleType = this.fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
            baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).notNull().inMinMaxRange(1, 3);
        }

        // dormancy
        final Boolean isDormancyActive = this.fromApiJsonHelper.parameterExists(isDormancyTrackingActiveParamName, element)
                ? this.fromApiJsonHelper.extractBooleanNamed(isDormancyTrackingActiveParamName, element)
                : product.isDormancyTrackingActive();

        if (AccountingValidations.isCashBasedAccounting(accountingRuleType)
                || AccountingValidations.isAccrualPeriodicBasedAccounting(accountingRuleType)) {
            savingsProductAccountingDataValidator.evaluateProductAccountingData(accountingRuleType, isDormancyActive, element,
                    baseDataValidator, DepositAccountType.SAVINGS_DEPOSIT, false);
        }

        if (null != isDormancyActive && isDormancyActive) {
            final Long daysToInact = this.fromApiJsonHelper.parameterExists(daysToInactiveParamName, element)
                    ? this.fromApiJsonHelper.extractLongNamed(daysToInactiveParamName, element)
                    : product.getDaysToInactive();
            baseDataValidator.reset().parameter(daysToInactiveParamName).value(daysToInact).notNull().longGreaterThanZero();

            final Long daysToDor = this.fromApiJsonHelper.parameterExists(daysToDormancyParamName, element)
                    ? this.fromApiJsonHelper.extractLongNamed(daysToDormancyParamName, element)
                    : product.getDaysToDormancy();
            baseDataValidator.reset().parameter(daysToDormancyParamName).value(daysToDor).notNull().longGreaterThanZero();

            final Long daysToEsc = this.fromApiJsonHelper.parameterExists(daysToEscheatParamName, element)
                    ? this.fromApiJsonHelper.extractLongNamed(daysToEscheatParamName, element)
                    : product.getDaysToEscheat();
            baseDataValidator.reset().parameter(daysToEscheatParamName).value(daysToEsc).notNull().longGreaterThanZero();

            if (null != daysToInact && null != daysToDor && null != daysToEsc) {
                baseDataValidator.reset().parameter(daysToDormancyParamName).value(daysToDor).longGreaterThanNumber(daysToInact);
                baseDataValidator.reset().parameter(daysToEscheatParamName).value(daysToEsc).longGreaterThanNumber(daysToDor);
            }

            if (this.fromApiJsonHelper.parameterExists(SavingProductAccountingParams.ESCHEAT_LIABILITY.getValue(), element)) {
                final Long escheatLiabilityAccountId = this.fromApiJsonHelper
                        .extractLongNamed(SavingProductAccountingParams.ESCHEAT_LIABILITY.getValue(), element);
                baseDataValidator.reset().parameter(SavingProductAccountingParams.ESCHEAT_LIABILITY.getValue())
                        .value(escheatLiabilityAccountId).notNull().integerGreaterThanZero();
            }
        }

        validateOverdraftParams(baseDataValidator, element);

        if (this.fromApiJsonHelper.parameterExists(minBalanceForInterestCalculationParamName, element)) {
            final BigDecimal minBalanceForInterestCalculation = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(minBalanceForInterestCalculationParamName, element);
            baseDataValidator.reset().parameter(minBalanceForInterestCalculationParamName).value(minBalanceForInterestCalculation)
                    .ignoreIfNull().zeroOrPositiveAmount();
        }

        validateTaxWithHoldingParams(baseDataValidator, element, false);
        validateLienParams(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateOverdraftParams(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (this.fromApiJsonHelper.parameterExists(allowOverdraftParamName, element)) {
            final Boolean allowOverdraft = this.fromApiJsonHelper.extractBooleanNamed(allowOverdraftParamName, element);
            baseDataValidator.reset().parameter(allowOverdraftParamName).value(allowOverdraft).ignoreIfNull().validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(overdraftLimitParamName, element)) {
            final BigDecimal overdraftLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(overdraftLimitParamName, element);
            baseDataValidator.reset().parameter(overdraftLimitParamName).value(overdraftLimit).ignoreIfNull().zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(nominalAnnualInterestRateOverdraftParamName, element)) {
            final BigDecimal nominalAnnualInterestRateOverdraft = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(nominalAnnualInterestRateOverdraftParamName, element);
            baseDataValidator.reset().parameter(nominalAnnualInterestRateOverdraftParamName).value(nominalAnnualInterestRateOverdraft)
                    .ignoreIfNull().zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(minOverdraftForInterestCalculationParamName, element)) {
            final BigDecimal minOverdraftForInterestCalculation = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(minOverdraftForInterestCalculationParamName, element);
            baseDataValidator.reset().parameter(minOverdraftForInterestCalculationParamName).value(minOverdraftForInterestCalculation)
                    .ignoreIfNull().zeroOrPositiveAmount();
        }

    }

    private void validateLienParams(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (this.fromApiJsonHelper.parameterExists(lienAllowedParamName, element)) {
            final Boolean lienAllowed = this.fromApiJsonHelper.extractBooleanNamed(lienAllowedParamName, element);
            baseDataValidator.reset().parameter(lienAllowedParamName).value(lienAllowed).ignoreIfNull().validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(maxAllowedLienLimitParamName, element)) {
            final BigDecimal maxAllowedLienLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxAllowedLienLimitParamName,
                    element);
            baseDataValidator.reset().parameter(maxAllowedLienLimitParamName).value(maxAllowedLienLimit).ignoreIfNull()
                    .zeroOrPositiveAmount();
        }
        if (BooleanUtils.isTrue(this.fromApiJsonHelper.extractBooleanNamed(lienAllowedParamName, element))
                && BooleanUtils.isTrue(this.fromApiJsonHelper.extractBooleanNamed(allowOverdraftParamName, element))) {
            final BigDecimal maxAllowedLienLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxAllowedLienLimitParamName,
                    element);
            final BigDecimal overdraftLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(overdraftLimitParamName, element);
            if (overdraftLimit.compareTo(maxAllowedLienLimit) > 0) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("Overdraft.limit.can.not.be.greater.than.lien.limit");
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
