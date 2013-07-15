/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.accountingRuleParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.annualFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.annualFeeOnMonthDayParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.localeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nameParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.withdrawalFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.withdrawalFeeTypeParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.joda.time.MonthDay;
import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsWithdrawalFeesType;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_savings_product")
public class SavingsProduct extends AbstractPersistable<Long> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "nominal_annual_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal nominalAnnualInterestRate;

    /**
     * The interest period is the span of time at the end of which savings in a
     * client's account earn interest.
     * 
     * A value from the {@link SavingsCompoundingInterestPeriodType}
     * enumeration.
     */
    @Column(name = "interest_compounding_period_enum", nullable = false)
    private Integer interestCompoundingPeriodType;

    /**
     * A value from the {@link SavingsPostingInterestPeriodType} enumeration.
     */
    @Column(name = "interest_posting_period_enum", nullable = false)
    private Integer interestPostingPeriodType;

    /**
     * A value from the {@link SavingsInterestCalculationType} enumeration.
     */
    @Column(name = "interest_calculation_type_enum", nullable = false)
    private Integer interestCalculationType;

    /**
     * A value from the {@link SavingsInterestCalculationDaysInYearType}
     * enumeration.
     */
    @Column(name = "interest_calculation_days_in_year_type_enum", nullable = false)
    private Integer interestCalculationDaysInYearType;

    @Column(name = "min_required_opening_balance", scale = 6, precision = 19, nullable = true)
    private BigDecimal minRequiredOpeningBalance;

    @Column(name = "lockin_period_frequency", nullable = true)
    private Integer lockinPeriodFrequency;

    @Column(name = "lockin_period_frequency_enum", nullable = true)
    private Integer lockinPeriodFrequencyType;

    /**
     * A value from the {@link AccountingRuleType} enumeration.
     */
    @Column(name = "accounting_type", nullable = false)
    private Integer accountingRule;

    @Column(name = "withdrawal_fee_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal withdrawalFeeAmount;

    @Column(name = "withdrawal_fee_type_enum", nullable = true)
    private Integer withdrawalFeeType;

    @Column(name = "annual_fee_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal annualFeeAmount;

    @Column(name = "annual_fee_on_month", nullable = true)
    private Integer annualFeeOnMonth;

    @Column(name = "annual_fee_on_day", nullable = true)
    private Integer annualFeeOnDay;

    public static SavingsProduct createNew(final String name, final String description, final MonetaryCurrency currency,
            final BigDecimal interestRate, final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final BigDecimal withdrawalFeeAmount, final SavingsWithdrawalFeesType withdrawalFeeType, final BigDecimal annualFeeAmount,
            final MonthDay annualFeeOnMonthDay, final AccountingRuleType accountingRuleType) {

        return new SavingsProduct(name, description, currency, interestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeAmount, withdrawalFeeType, annualFeeAmount, annualFeeOnMonthDay, accountingRuleType);
    }

    protected SavingsProduct() {
        this.name = null;
        this.description = null;
    }

    private SavingsProduct(final String name, final String description, final MonetaryCurrency currency, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final BigDecimal withdrawalFeeAmount, final SavingsWithdrawalFeesType withdrawalFeeType, final BigDecimal annualFeeAmount,
            final MonthDay annualFeeOnMonthDay, final AccountingRuleType accountingRuleType) {

        this.name = name;
        this.description = description;

        this.currency = currency;
        this.nominalAnnualInterestRate = interestRate;
        this.interestCompoundingPeriodType = interestCompoundingPeriodType.getValue();
        this.interestPostingPeriodType = interestPostingPeriodType.getValue();
        this.interestCalculationType = interestCalculationType.getValue();
        this.interestCalculationDaysInYearType = interestCalculationDaysInYearType.getValue();

        if (minRequiredOpeningBalance != null) {
            this.minRequiredOpeningBalance = Money.of(currency, minRequiredOpeningBalance).getAmount();
        }

        this.lockinPeriodFrequency = lockinPeriodFrequency;
        if (lockinPeriodFrequency != null && lockinPeriodFrequencyType != null) {
            this.lockinPeriodFrequencyType = lockinPeriodFrequencyType.getValue();
        }

        this.withdrawalFeeAmount = withdrawalFeeAmount;
        if (withdrawalFeeAmount != null && withdrawalFeeType != null) {
            this.withdrawalFeeType = withdrawalFeeType.getValue();
        }
        this.annualFeeAmount = annualFeeAmount;
        if (annualFeeAmount != null && annualFeeOnMonthDay != null) {
            this.annualFeeOnMonth = annualFeeOnMonthDay.getMonthOfYear();
            this.annualFeeOnDay = annualFeeOnMonthDay.getDayOfMonth();
        }

        if (accountingRuleType != null) {
            this.accountingRule = accountingRuleType.getValue();
        }

        validateLockinDetails();
        validateWithdrawalFeeDetails();
        validateAnnualFeeDetails();
    }

    public MonetaryCurrency currency() {
        return this.currency.copy();
    }

    public BigDecimal nominalAnnualInterestRate() {
        return Money.of(this.currency, this.nominalAnnualInterestRate).getAmount();
    }

    public SavingsCompoundingInterestPeriodType interestCompoundingPeriodType() {
        return SavingsCompoundingInterestPeriodType.fromInt(this.interestCompoundingPeriodType);
    }

    public SavingsPostingInterestPeriodType interestPostingPeriodType() {
        return SavingsPostingInterestPeriodType.fromInt(this.interestPostingPeriodType);
    }

    public SavingsInterestCalculationType interestCalculationType() {
        return SavingsInterestCalculationType.fromInt(this.interestCalculationType);
    }

    public SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType() {
        return SavingsInterestCalculationDaysInYearType.fromInt(this.interestCalculationDaysInYearType);
    }

    public BigDecimal minRequiredOpeningBalance() {
        return this.minRequiredOpeningBalance;
    }

    public Integer lockinPeriodFrequency() {
        return this.lockinPeriodFrequency;
    }

    public SavingsPeriodFrequencyType lockinPeriodFrequencyType() {
        SavingsPeriodFrequencyType type = null;
        if (this.lockinPeriodFrequencyType != null) {
            type = SavingsPeriodFrequencyType.fromInt(this.lockinPeriodFrequencyType);
        }
        return type;
    }

    public BigDecimal withdrawalFeeAmount() {
        return this.withdrawalFeeAmount;
    }

    public SavingsWithdrawalFeesType withdrawalFeeType() {
        SavingsWithdrawalFeesType type = null;
        if (this.withdrawalFeeType != null) {
            type = SavingsWithdrawalFeesType.fromInt(this.withdrawalFeeType);
        }
        return type;
    }

    public BigDecimal annualFeeAmount() {
        return this.annualFeeAmount;
    }

    public MonthDay monthDayOfAnnualFee() {
        MonthDay monthDay = null;
        if (this.annualFeeOnMonth != null && this.annualFeeOnDay != null) {
            monthDay = new MonthDay(this.annualFeeOnMonth, this.annualFeeOnDay);
        }
        return monthDay;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(10);

        final String localeAsInput = command.locale();

        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        Integer digitsAfterDecimal = this.currency.getDigitsAfterDecimal();
        if (command.isChangeInIntegerParameterNamed(digitsAfterDecimalParamName, digitsAfterDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
            actualChanges.put(digitsAfterDecimalParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            digitsAfterDecimal = newValue;
            this.currency = new MonetaryCurrency(this.currency.getCode(), digitsAfterDecimal);
        }

        String currencyCode = this.currency.getCode();
        if (command.isChangeInStringParameterNamed(currencyCodeParamName, currencyCode)) {
            final String newValue = command.stringValueOfParameterNamed(currencyCodeParamName);
            actualChanges.put(currencyCodeParamName, newValue);
            currencyCode = newValue;
            this.currency = new MonetaryCurrency(currencyCode, this.currency.getDigitsAfterDecimal());
        }

        if (command.isChangeInBigDecimalParameterNamed(nominalAnnualInterestRateParamName, this.nominalAnnualInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);
            actualChanges.put(nominalAnnualInterestRateParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.nominalAnnualInterestRate = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(interestCompoundingPeriodTypeParamName, this.interestCompoundingPeriodType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
            actualChanges.put(interestCompoundingPeriodTypeParamName, newValue);
            this.interestCompoundingPeriodType = SavingsCompoundingInterestPeriodType.fromInt(newValue).getValue();
        }

        if (command.isChangeInIntegerParameterNamed(interestPostingPeriodTypeParamName, this.interestPostingPeriodType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
            actualChanges.put(interestPostingPeriodTypeParamName, newValue);
            this.interestPostingPeriodType = SavingsPostingInterestPeriodType.fromInt(newValue).getValue();
        }

        if (command.isChangeInIntegerParameterNamed(interestCalculationTypeParamName, this.interestCalculationType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCalculationTypeParamName);
            actualChanges.put(interestCalculationTypeParamName, newValue);
            this.interestCalculationType = SavingsInterestCalculationType.fromInt(newValue).getValue();
        }

        if (command.isChangeInIntegerParameterNamed(interestCalculationDaysInYearTypeParamName, this.interestCalculationDaysInYearType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCalculationDaysInYearTypeParamName);
            actualChanges.put(interestCalculationDaysInYearTypeParamName, newValue);
            this.interestCalculationDaysInYearType = SavingsInterestCalculationDaysInYearType.fromInt(newValue).getValue();
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(minRequiredOpeningBalanceParamName,
                this.minRequiredOpeningBalance)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(minRequiredOpeningBalanceParamName);
            actualChanges.put(minRequiredOpeningBalanceParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.minRequiredOpeningBalance = newValue;
        }

        if (command.isChangeInIntegerParameterNamedDefaultingZeroToNull(lockinPeriodFrequencyParamName, this.lockinPeriodFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamedDefaultToNullIfZero(lockinPeriodFrequencyParamName);
            actualChanges.put(lockinPeriodFrequencyParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.lockinPeriodFrequency = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(lockinPeriodFrequencyTypeParamName, this.lockinPeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
            actualChanges.put(lockinPeriodFrequencyTypeParamName, newValue);
            this.lockinPeriodFrequencyType = newValue != null ? SavingsPeriodFrequencyType.fromInt(newValue).getValue() : newValue;
        }

        // set period type to null if frequency is null
        if (this.lockinPeriodFrequency == null) {
            this.lockinPeriodFrequencyType = null;
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(withdrawalFeeAmountParamName, this.withdrawalFeeAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(withdrawalFeeAmountParamName);
            actualChanges.put(withdrawalFeeAmountParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.withdrawalFeeAmount = newValue;
        }

        if (command.isChangeInIntegerParameterNamedDefaultingZeroToNull(withdrawalFeeTypeParamName, this.withdrawalFeeType)) {
            final Integer newValue = command.integerValueOfParameterNamedDefaultToNullIfZero(withdrawalFeeTypeParamName);
            actualChanges.put(withdrawalFeeTypeParamName, newValue);
            this.withdrawalFeeType = newValue != null ? SavingsWithdrawalFeesType.fromInt(newValue).getValue() : newValue;
        }

        // set period type to null if frequency is null
        if (this.withdrawalFeeAmount == null) {
            this.withdrawalFeeType = null;
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(annualFeeAmountParamName, this.annualFeeAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(annualFeeAmountParamName);
            actualChanges.put(annualFeeAmountParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.annualFeeAmount = newValue;
        }

        if (command.isChangeInIntegerParameterNamedDefaultingZeroToNull(annualFeeOnMonthDayParamName, this.annualFeeOnDay)) {
            final MonthDay monthDay = command.extractMonthDayNamed(annualFeeOnMonthDayParamName);
            final String actualValueEntered = command.stringValueOfParameterNamed(annualFeeOnMonthDayParamName);
            final Integer newValue = monthDay != null ? monthDay.getDayOfMonth() : null;
            actualChanges.put(annualFeeOnMonthDayParamName, actualValueEntered);
            actualChanges.put(localeParamName, localeAsInput);
            this.annualFeeOnDay = newValue;
        }

        if (command.isChangeInIntegerParameterNamedDefaultingZeroToNull(annualFeeOnMonthDayParamName, this.annualFeeOnMonth)) {
            final MonthDay monthDay = command.extractMonthDayNamed(annualFeeOnMonthDayParamName);
            final String actualValueEntered = command.stringValueOfParameterNamed(annualFeeOnMonthDayParamName);
            final Integer newValue = monthDay != null ? monthDay.getMonthOfYear() : null;
            actualChanges.put(annualFeeOnMonthDayParamName, actualValueEntered);
            actualChanges.put(localeParamName, localeAsInput);
            this.annualFeeOnMonth = newValue;
        }

        // set period type to null if frequency is null
        if (this.annualFeeAmount == null) {
            this.annualFeeOnDay = null;
            this.annualFeeOnMonth = null;
        }

        if (command.isChangeInIntegerParameterNamed(accountingRuleParamName, this.accountingRule)) {
            final Integer newValue = command.integerValueOfParameterNamed(accountingRuleParamName);
            actualChanges.put(accountingRuleParamName, newValue);
            this.accountingRule = newValue;
        }

        validateLockinDetails();
        validateWithdrawalFeeDetails();
        validateAnnualFeeDetails();

        return actualChanges;
    }

    private void validateAnnualFeeDetails() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_PRODUCT_RESOURCE_NAME);

        if (this.annualFeeAmount == null) {

            if (this.annualFeeOnMonth != null || this.annualFeeOnDay != null) {
                baseDataValidator.reset().parameter(annualFeeAmountParamName).value(this.annualFeeAmount).notNull();
            }
        } else {

            if (this.annualFeeOnMonth == null || this.annualFeeOnDay == null) {
                baseDataValidator.reset().parameter(annualFeeOnMonthDayParamName).value(this.annualFeeOnMonth).notNull();
            }

            baseDataValidator.reset().parameter(annualFeeAmountParamName).value(this.annualFeeAmount).zeroOrPositiveAmount();
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validateWithdrawalFeeDetails() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_PRODUCT_RESOURCE_NAME);

        if (this.withdrawalFeeAmount == null) {
            baseDataValidator.reset().parameter(withdrawalFeeTypeParamName).value(this.withdrawalFeeType).ignoreIfNull()
                    .isOneOfTheseValues(1, 2);

            if (this.withdrawalFeeType != null) {
                baseDataValidator.reset().parameter(withdrawalFeeAmountParamName).value(this.withdrawalFeeAmount).notNull();
            }
        } else {
            baseDataValidator.reset().parameter(withdrawalFeeAmountParamName).value(this.withdrawalFeeAmount).zeroOrPositiveAmount();
            baseDataValidator.reset().parameter(withdrawalFeeTypeParamName).value(this.withdrawalFeeType).notNull()
                    .isOneOfTheseValues(1, 2);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validateLockinDetails() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_PRODUCT_RESOURCE_NAME);

        if (this.lockinPeriodFrequency == null) {
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).ignoreIfNull()
                    .inMinMaxRange(0, 3);

            if (this.lockinPeriodFrequencyType != null) {
                baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).notNull()
                        .integerZeroOrGreater();
            }
        } else {
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequencyType).integerZeroOrGreater();
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).notNull()
                    .inMinMaxRange(0, 3);
        }


        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public boolean isCashBasedAccountingEnabled() {
        return AccountingRuleType.CASH_BASED.getValue().equals(this.accountingRule);
    }

    public boolean isAccrualBasedAccountingEnabled() {
        return AccountingRuleType.ACCRUAL_BASED.getValue().equals(this.accountingRule);
    }

    public Integer getAccountingType() {
        return this.accountingRule;
    }
}