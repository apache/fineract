/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestRateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestRatePeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.localeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nameParamName;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savings.exception.InvalidSavingsProductSettingsException;
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

    @Column(name = "nominal_interest_rate_per_period", scale = 6, precision = 19, nullable = false)
    private BigDecimal interestRate;

    @Column(name = "nominal_interest_rate_period_frequency_enum", nullable = false)
    private Integer interestRatePeriodFrequencyType;

    @Column(name = "min_required_opening_balance", scale = 6, precision = 19, nullable = false)
    private BigDecimal minRequiredOpeningBalance;

    @Column(name = "lockin_period_frequency", nullable = false)
    private Integer lockinPeriodFrequency;

    @Column(name = "lockin_period_frequency_enum", nullable = false)
    private Integer lockinPeriodFrequencyType;

    public static SavingsProduct createNew(final String name, final String description, final MonetaryCurrency currency,
            final BigDecimal interestRate, final PeriodFrequencyType interestRatePeriodFrequencyType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final PeriodFrequencyType lockinPeriodFrequencyType) {

        return new SavingsProduct(name, description, currency, interestRate, interestRatePeriodFrequencyType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType);
    }

    protected SavingsProduct() {
        this.name = null;
        this.description = null;
    }

    private SavingsProduct(final String name, final String description, final MonetaryCurrency currency, final BigDecimal interestRate,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final PeriodFrequencyType lockinPeriodFrequencyType) {

        this.name = name;
        this.description = description;

        this.currency = currency;
        this.interestRate = interestRate;
        this.interestRatePeriodFrequencyType = interestRatePeriodFrequencyType.getValue();

        if (minRequiredOpeningBalance != null) {
            this.minRequiredOpeningBalance = Money.of(currency, minRequiredOpeningBalance).getAmount();
        }

        this.lockinPeriodFrequency = lockinPeriodFrequency;
        if (lockinPeriodFrequencyType != null) {
            this.lockinPeriodFrequencyType = lockinPeriodFrequencyType.getValue();
        }

        validateLockinDetails();
    }

    public MonetaryCurrency currency() {
        return this.currency.copy();
    }

    public BigDecimal interestRate() {
        return Money.of(this.currency, this.interestRate).getAmount();
    }

    public PeriodFrequencyType interestRatePeriodFrequencyType() {
        PeriodFrequencyType type = null;
        if (this.interestRatePeriodFrequencyType != null) {
            type = PeriodFrequencyType.fromInt(this.interestRatePeriodFrequencyType);
        }
        return type;
    }

    public BigDecimal minRequiredOpeningBalance() {
        return this.minRequiredOpeningBalance;
    }

    public Integer lockinPeriodFrequency() {
        return this.lockinPeriodFrequency;
    }

    public PeriodFrequencyType lockinPeriodFrequencyType() {
        PeriodFrequencyType type = null;
        if (this.lockinPeriodFrequencyType != null) {
            type = PeriodFrequencyType.fromInt(this.lockinPeriodFrequencyType);
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

        if (command.isChangeInBigDecimalParameterNamed(interestRateParamName, this.interestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(interestRateParamName);
            actualChanges.put(interestRateParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.interestRate = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(interestRatePeriodFrequencyTypeParamName, this.interestRatePeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestRatePeriodFrequencyTypeParamName);
            actualChanges.put(interestRatePeriodFrequencyTypeParamName, newValue);
            this.interestRatePeriodFrequencyType = PeriodFrequencyType.fromInt(newValue).getValue();
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
            this.lockinPeriodFrequencyType = PeriodFrequencyType.fromInt(newValue).getValue();
        }

        validateLockinDetails();

        return actualChanges;
    }

    private void validateLockinDetails() {
        if (isInvalidConfigurationOfLockinSettings()) { throw new InvalidSavingsProductSettingsException(
                "error.msg.product.savings.invalid.lockin.settings", "Invalid configuration of lock in settings.",
                lockinPeriodFrequencyParamName); }
    }

    private boolean isInvalidConfigurationOfLockinSettings() {
        return (this.lockinPeriodFrequency == null && this.lockinPeriodFrequencyType != null)
                || (this.lockinPeriodFrequencyType == null && this.lockinPeriodFrequency != null);
    }
}