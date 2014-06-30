/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.SAVINGS_PRODUCT_REQUEST_DATA_PARAMETERS;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.allowOverdraftParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.feeAmountParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.feeOnMonthDayParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.inMultiplesOfParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minBalanceForInterestCalculationParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nameParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.overdraftLimitParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.shortNameParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.MonthDay;
import org.mifosplatform.accounting.common.AccountingConstants.SAVINGS_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class SavingsProductDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingsProductDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

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

        final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
        baseDataValidator.reset().parameter(descriptionParamName).value(description).notBlank().notExceedingLengthOf(500);

        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(currencyCodeParamName, element);
        baseDataValidator.reset().parameter(currencyCodeParamName).value(currencyCode).notBlank();

        final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(digitsAfterDecimalParamName, element);
        baseDataValidator.reset().parameter(digitsAfterDecimalParamName).value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);

        if (this.fromApiJsonHelper.parameterExists(inMultiplesOfParamName, element)) {
            final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed(inMultiplesOfParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(inMultiplesOfParamName).value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();
        }
        final BigDecimal nominalAnnualInterestRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                nominalAnnualInterestRateParamName, element);
        baseDataValidator.reset().parameter(nominalAnnualInterestRateParamName).value(nominalAnnualInterestRate).notNull()
                .zeroOrPositiveAmount();

        final Integer interestCompoundingPeriodType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                interestCompoundingPeriodTypeParamName, element);
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

        final Integer interestCalculationDaysInYearType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                interestCalculationDaysInYearTypeParamName, element);
        baseDataValidator.reset().parameter(interestCalculationDaysInYearTypeParamName).value(interestCalculationDaysInYearType).notNull()
                .isOneOfTheseValues(SavingsInterestCalculationDaysInYearType.integerValues());

        if (this.fromApiJsonHelper.parameterExists(minRequiredOpeningBalanceParamName, element)) {
            final BigDecimal minOpeningBalance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    minRequiredOpeningBalanceParamName, element);
            baseDataValidator.reset().parameter(minRequiredOpeningBalanceParamName).value(minOpeningBalance).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(lockinPeriodFrequencyParamName, element)) {

            final Integer lockinPeriodFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName,
                    element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).integerZeroOrGreater();

            if (lockinPeriodFrequency != null) {
                final Integer lockinPeriodFrequencyType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                        lockinPeriodFrequencyTypeParamName, element);
                baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).notNull()
                        .inMinMaxRange(0, 3);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(lockinPeriodFrequencyTypeParamName, element)) {
            final Integer lockinPeriodFrequencyType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    lockinPeriodFrequencyTypeParamName, element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).inMinMaxRange(0, 3);

            if (lockinPeriodFrequencyType != null) {
                final Integer lockinPeriodFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName,
                        element);
                baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).notNull()
                        .integerZeroOrGreater();
            }
        }

        /*
         * if
         * (this.fromApiJsonHelper.parameterExists(withdrawalFeeAmountParamName,
         * element)) {
         * 
         * final BigDecimal withdrawalFeeAmount =
         * this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed
         * (withdrawalFeeAmountParamName, element);
         * baseDataValidator.reset().parameter
         * (withdrawalFeeAmountParamName).value
         * (withdrawalFeeAmount).zeroOrPositiveAmount();
         * 
         * if (withdrawalFeeAmount != null) { final Integer withdrawalFeeType =
         * this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
         * withdrawalFeeTypeParamName, element);
         * baseDataValidator.reset().parameter
         * (withdrawalFeeTypeParamName).value(withdrawalFeeType)
         * .isOneOfTheseValues(SavingsWithdrawalFeesType.integerValues()); } }
         * 
         * if
         * (this.fromApiJsonHelper.parameterExists(withdrawalFeeTypeParamName,
         * element)) { final Integer withdrawalFeeType =
         * this.fromApiJsonHelper.extractIntegerSansLocaleNamed
         * (withdrawalFeeTypeParamName, element);
         * baseDataValidator.reset().parameter
         * (withdrawalFeeTypeParamName).value(withdrawalFeeType).ignoreIfNull()
         * .isOneOfTheseValues(1, 2);
         * 
         * if (withdrawalFeeType != null) { final BigDecimal withdrawalFeeAmount
         * = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
         * withdrawalFeeAmountParamName, element);
         * baseDataValidator.reset().parameter
         * (withdrawalFeeAmountParamName).value(withdrawalFeeAmount).notNull()
         * .zeroOrPositiveAmount(); } }
         */
        if (this.fromApiJsonHelper.parameterExists(withdrawalFeeForTransfersParamName, element)) {
            final Boolean isWithdrawalFeeApplicableForTransfers = this.fromApiJsonHelper.extractBooleanNamed(
                    withdrawalFeeForTransfersParamName, element);
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

        // accounting related data validation
        final Integer accountingRuleType = this.fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).notNull().inMinMaxRange(1, 3);

        if (isCashBasedAccounting(accountingRuleType)) {

            final Long savingsControlAccountId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue())
                    .value(savingsControlAccountId).notNull().integerGreaterThanZero();

            final Long savingsReferenceAccountId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue())
                    .value(savingsReferenceAccountId).notNull().integerGreaterThanZero();

            final Long transfersInSuspenseAccountId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue())
                    .value(transfersInSuspenseAccountId).notNull().integerGreaterThanZero();

            final Long interestOnSavingsAccountId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue())
                    .value(interestOnSavingsAccountId).notNull().integerGreaterThanZero();

            final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromPenaltyId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue())
                    .value(incomeFromPenaltyId).notNull().integerGreaterThanZero();

            final Long overdraftControlAccountId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue())
                    .value(overdraftControlAccountId).notNull().integerGreaterThanZero();

            final Long incomeFromInterest = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue())
                    .value(incomeFromInterest).notNull().integerGreaterThanZero();

            final Long writtenoff = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue()).value(writtenoff)
                    .notNull().integerGreaterThanZero();

            validatePaymentChannelFundSourceMappings(baseDataValidator, element);
            validateChargeToIncomeAccountMappings(baseDataValidator, element);
        }

        validateOverdraftParams(baseDataValidator, element);

        if (this.fromApiJsonHelper.parameterExists(minBalanceForInterestCalculationParamName, element)) {
            final BigDecimal minBalanceForInterestCalculation = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    minBalanceForInterestCalculationParamName, element);
            baseDataValidator.reset().parameter(minBalanceForInterestCalculationParamName).value(minBalanceForInterestCalculation)
                    .ignoreIfNull().zeroOrPositiveAmount();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

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
            final Integer interestCompoundingPeriodType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    interestCompoundingPeriodTypeParamName, element);
            baseDataValidator.reset().parameter(interestCompoundingPeriodTypeParamName).value(interestCompoundingPeriodType).notNull()
                    .isOneOfTheseValues(SavingsCompoundingInterestPeriodType.integerValues());
        }

        if (this.fromApiJsonHelper.parameterExists(interestCalculationTypeParamName, element)) {
            final Integer interestCalculationType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(interestCalculationTypeParamName,
                    element);
            baseDataValidator.reset().parameter(interestCalculationTypeParamName).value(interestCalculationType).notNull()
                    .inMinMaxRange(1, 2);
        }

        if (this.fromApiJsonHelper.parameterExists(interestCalculationDaysInYearTypeParamName, element)) {
            final Integer interestCalculationDaysInYearType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    interestCalculationDaysInYearTypeParamName, element);
            baseDataValidator.reset().parameter(interestCalculationDaysInYearTypeParamName).value(interestCalculationDaysInYearType)
                    .notNull().isOneOfTheseValues(360, 365);
        }

        if (this.fromApiJsonHelper.parameterExists(minRequiredOpeningBalanceParamName, element)) {
            final BigDecimal minOpeningBalance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    minRequiredOpeningBalanceParamName, element);
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
            final Integer lockinPeriodFrequencyType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    lockinPeriodFrequencyTypeParamName, element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).inMinMaxRange(0, 3);
        }

        if (this.fromApiJsonHelper.parameterExists(withdrawalFeeForTransfersParamName, element)) {
            final Boolean isWithdrawalFeeApplicableForTransfers = this.fromApiJsonHelper.extractBooleanNamed(
                    withdrawalFeeForTransfersParamName, element);
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

        final Long savingsControlAccountId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(), element);
        baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue()).value(savingsControlAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long savingsReferenceAccountId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(), element);
        baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue())
                .value(savingsReferenceAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long transfersInSuspenseAccountId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), element);
        baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue())
                .value(transfersInSuspenseAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long interestOnSavingsAccountId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(), element);
        baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue())
                .value(interestOnSavingsAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
                element);
        baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromPenaltyId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
        baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long overdraftAccountId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), element);
        baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue())
                .value(overdraftAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromInterest = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue(), element);
        baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue()).value(incomeFromInterest)
                .ignoreIfNull().integerGreaterThanZero();

        final Long writtenoff = this.fromApiJsonHelper.extractLongNamed(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
                element);
        baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue()).value(writtenoff)
                .ignoreIfNull().integerGreaterThanZero();

        validatePaymentChannelFundSourceMappings(baseDataValidator, element);
        validateChargeToIncomeAccountMappings(baseDataValidator, element);
        validateOverdraftParams(baseDataValidator, element);

        if (this.fromApiJsonHelper.parameterExists(minBalanceForInterestCalculationParamName, element)) {
            final BigDecimal minBalanceForInterestCalculation = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    minBalanceForInterestCalculationParamName, element);
            baseDataValidator.reset().parameter(minBalanceForInterestCalculationParamName).value(minBalanceForInterestCalculation)
                    .ignoreIfNull().zeroOrPositiveAmount();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private boolean isCashBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.CASH_BASED.getValue().equals(accountingRuleType);
    }

    /**
     * Validation for advanced accounting options
     */
    private void validatePaymentChannelFundSourceMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (this.fromApiJsonHelper.parameterExists(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
                element)) {
            final JsonArray paymentChannelMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
            if (paymentChannelMappingArray != null && paymentChannelMappingArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                    final Long paymentTypeId = jsonObject.get(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_TYPE.getValue()).getAsLong();
                    final Long paymentSpecificFundAccountId = jsonObject.get(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue())
                            .getAsLong();
                    baseDataValidator
                            .reset()
                            .parameter(
                                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue() + "[" + i + "]."
                                            + SAVINGS_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_TYPE.toString()).value(paymentTypeId).notNull()
                            .integerGreaterThanZero();
                    baseDataValidator
                            .reset()
                            .parameter(
                                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue() + "[" + i + "]."
                                            + SAVINGS_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).value(paymentSpecificFundAccountId)
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
            parameterName = SAVINGS_PRODUCT_ACCOUNTING_PARAMS.PENALTY_INCOME_ACCOUNT_MAPPING.getValue();
        } else {
            parameterName = SAVINGS_PRODUCT_ACCOUNTING_PARAMS.FEE_INCOME_ACCOUNT_MAPPING.getValue();
        }

        if (this.fromApiJsonHelper.parameterExists(parameterName, element)) {
            final JsonArray chargeToIncomeAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(parameterName, element);
            if (chargeToIncomeAccountMappingArray != null && chargeToIncomeAccountMappingArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = chargeToIncomeAccountMappingArray.get(i).getAsJsonObject();
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.CHARGE_ID.getValue(),
                            jsonObject);
                    final Long incomeAccountId = this.fromApiJsonHelper.extractLongNamed(
                            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_ACCOUNT_ID.getValue(), jsonObject);
                    baseDataValidator.reset()
                            .parameter(parameterName + "[" + i + "]." + SAVINGS_PRODUCT_ACCOUNTING_PARAMS.CHARGE_ID.getValue())
                            .value(chargeId).notNull().integerGreaterThanZero();
                    baseDataValidator.reset()
                            .parameter(parameterName + "[" + i + "]." + SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_ACCOUNT_ID.getValue())
                            .value(incomeAccountId).notNull().integerGreaterThanZero();
                    i++;
                } while (i < chargeToIncomeAccountMappingArray.size());
            }
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

    }

}