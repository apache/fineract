/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.localeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nameParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.withdrawalFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.withdrawalFeeTypeParamName;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
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
     * A value from the {@link SavingsInterestPostingPeriodType} enumeration.
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

    @Column(name = "withdrawal_fee_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal withdrawalFeeAmount;

    @Column(name = "withdrawal_fee_type_enum", nullable = true)
    private Integer withdrawalFeeType;

    public static SavingsProduct createNew(final String name, final String description, final MonetaryCurrency currency,
            final BigDecimal interestRate, final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsInterestPostingPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final BigDecimal withdrawalFeeAmount, final SavingsWithdrawalFeesType withdrawalFeeType) {

        return new SavingsProduct(name, description, currency, interestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeAmount, withdrawalFeeType);
    }

    protected SavingsProduct() {
        this.name = null;
        this.description = null;
    }

    private SavingsProduct(final String name, final String description, final MonetaryCurrency currency, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsInterestPostingPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final BigDecimal withdrawalFeeAmount, final SavingsWithdrawalFeesType withdrawalFeeType) {

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
        if (lockinPeriodFrequencyType != null) {
            this.lockinPeriodFrequencyType = lockinPeriodFrequencyType.getValue();
        }

        this.withdrawalFeeAmount = withdrawalFeeAmount;
        if (withdrawalFeeType != null) {
            this.withdrawalFeeType = withdrawalFeeType.getValue();
        }

        validateLockinDetails();
        validateWithdrawalFeeDetails();
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

    public SavingsInterestPostingPeriodType interestPostingPeriodType() {
        return SavingsInterestPostingPeriodType.fromInt(this.interestPostingPeriodType);
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
            this.interestPostingPeriodType = SavingsInterestPostingPeriodType.fromInt(newValue).getValue();
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

        if (command.isChangeInBigDecimalParameterNamed(minRequiredOpeningBalanceParamName, this.minRequiredOpeningBalance)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minRequiredOpeningBalanceParamName);
            actualChanges.put(minRequiredOpeningBalanceParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.minRequiredOpeningBalance = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(lockinPeriodFrequencyParamName, this.lockinPeriodFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyParamName);
            actualChanges.put(lockinPeriodFrequencyParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.lockinPeriodFrequency = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(lockinPeriodFrequencyTypeParamName, this.lockinPeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
            actualChanges.put(lockinPeriodFrequencyTypeParamName, newValue);
            this.lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(newValue).getValue();
        }

        if (command.isChangeInBigDecimalParameterNamed(withdrawalFeeAmountParamName, this.withdrawalFeeAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(withdrawalFeeAmountParamName);
            actualChanges.put(withdrawalFeeAmountParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.withdrawalFeeAmount = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(withdrawalFeeTypeParamName, this.withdrawalFeeType)) {
            final Integer newValue = command.integerValueOfParameterNamed(withdrawalFeeTypeParamName);
            actualChanges.put(withdrawalFeeTypeParamName, newValue);
            this.withdrawalFeeType = SavingsWithdrawalFeesType.fromInt(newValue).getValue();
        }

        validateLockinDetails();
        validateWithdrawalFeeDetails();

        return actualChanges;
    }

    private void validateWithdrawalFeeDetails() {
        if (isInvalidConfigurationOfWithdrawalFeeSettings()) {
            Object[] defaultUserMessageArgs = new Object[] { withdrawalFeeAmountParamName };
            throw new GeneralPlatformDomainRuleException("error.msg.product.savings.invalid.withdrawalfee.settings",
                    "Invalid configuration of withdrawal fee settings.", defaultUserMessageArgs);
        }
    }

    private boolean isInvalidConfigurationOfWithdrawalFeeSettings() {
        return (this.withdrawalFeeAmount == null && this.withdrawalFeeType != null)
                || (this.withdrawalFeeType == null && this.withdrawalFeeAmount != null);
    }

    private void validateLockinDetails() {
        if (isInvalidConfigurationOfLockinSettings()) {
            Object[] defaultUserMessageArgs = new Object[] { lockinPeriodFrequencyParamName };
            throw new GeneralPlatformDomainRuleException("error.msg.product.savings.invalid.lockin.settings",
                    "Invalid configuration of lock in settings.", defaultUserMessageArgs);
        }
    }

    private boolean isInvalidConfigurationOfLockinSettings() {
        return (this.lockinPeriodFrequency == null && this.lockinPeriodFrequencyType != null)
                || (this.lockinPeriodFrequencyType == null && this.lockinPeriodFrequency != null);
    }
}
