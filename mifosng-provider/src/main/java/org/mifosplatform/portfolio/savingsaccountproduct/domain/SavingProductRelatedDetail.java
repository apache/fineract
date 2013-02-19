/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccountproduct.domain;

import java.math.BigDecimal;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;

@Embeddable
public class SavingProductRelatedDetail {

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal interestRate;

    @SuppressWarnings("unused")
    @Column(name = "min_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal minInterestRate;

    @SuppressWarnings("unused")
    @Column(name = "max_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal maxInterestRate;

    @Column(name = "savings_deposit_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal savingsDepositAmount;
    
    @Column(name = "deposit_every")
    private Integer depositEvery;

    @Column(name = "savings_product_type", nullable = false)
    private Integer savingProductType;

    @Column(name = "tenure_type", nullable = false)
    private Integer tenureType;

    @Column(name = "tenure", nullable = false)
    private Integer tenure;

    @Column(name = "frequency", nullable = false)
    private Integer frequency;

    @Column(name = "interest_type", nullable = false)
    private Integer interestType;

    @Column(name = "interest_calculation_method")
    private Integer interestCalculationMethod;

    @Column(name = "min_bal_for_withdrawal", scale = 6, precision = 19, nullable = false)
    private BigDecimal minimumBalanceForWithdrawal;

    @Column(name = "is_partial_deposit_allowed", nullable = false)
    private boolean isPartialDepositAllowed;

    @Column(name = "is_lock_in_period_allowed", nullable = false)
    private boolean isLockinPeriodAllowed;

    @Column(name = "lock_in_period", nullable = false)
    private Integer lockinPeriod;

    @Column(name = "lock_in_period_type", nullable = false)
    private Integer lockinPeriodType;

    public SavingProductRelatedDetail() {
        this.interestRate = null;
        this.savingsDepositAmount = null;
    }

    public SavingProductRelatedDetail(final MonetaryCurrency currency, final BigDecimal interestRate, final BigDecimal minInterestRate,
            final BigDecimal maxInterestRate, final BigDecimal savingsDepositAmount,final Integer depositEvery, final SavingProductType savingProductType,
            final TenureTypeEnum tenureType, final Integer tenure, final SavingFrequencyType frequency,
            final SavingsInterestType interestType, final SavingInterestCalculationMethod interestCalculationMethod,
            final BigDecimal minimumBalanceForWithdrawal, final boolean isPartialDepositAllowed, final boolean isLockinPeriodAllowed,
            final Integer lockinPeriod, final PeriodFrequencyType lockinPeriodType) {
        this.currency = currency;
        this.interestRate = interestRate;
        this.minInterestRate = minInterestRate;
        this.maxInterestRate = maxInterestRate;
        this.savingsDepositAmount = savingsDepositAmount;
        this.depositEvery=depositEvery;
        this.savingProductType = savingProductType.getValue();
        this.tenureType = tenureType.getValue();
        this.tenure = tenure;
        this.frequency = frequency.getValue();
        this.interestType = interestType.getValue();
        this.interestCalculationMethod = interestCalculationMethod.getValue();
        this.minimumBalanceForWithdrawal = minimumBalanceForWithdrawal;
        this.isPartialDepositAllowed = isPartialDepositAllowed;
        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType.getValue();
    }

    public MonetaryCurrency getCurrency() {
        return this.currency.copy();
    }

    public BigDecimal getInterestRate() {
        return BigDecimal.valueOf(Double.valueOf(this.interestRate.stripTrailingZeros().toString()));
    }

    public BigDecimal getMinimumBalance() {
        return BigDecimal.valueOf(Double.valueOf(this.savingsDepositAmount.stripTrailingZeros().toString()));
    }

    public Map<String, Object>  update(final JsonCommand command, final Map<String, Object> actualChanges) {
    	
    	final String localeAsInput = command.locale();
    	
    	Integer digitsAfterDecimal = this.currency.getDigitsAfterDecimal();
    	final String digitsAfterDecimalParamName = "digitsAfterDecimal";
        if (command.isChangeInIntegerParameterNamed(digitsAfterDecimalParamName, digitsAfterDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
            actualChanges.put(digitsAfterDecimalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            digitsAfterDecimal = newValue;
            this.currency = new MonetaryCurrency(this.currency.getCode(), digitsAfterDecimal);
        }

        String currencyCode = this.currency.getCode();
        final String currencyCodeParamName = "currencyCode";
        if (command.isChangeInStringParameterNamed(currencyCodeParamName, currencyCode)) {
            final String newValue = command.stringValueOfParameterNamed(currencyCodeParamName);
            actualChanges.put(currencyCodeParamName, newValue);
            currencyCode = newValue;
            this.currency = new MonetaryCurrency(currencyCode, this.currency.getDigitsAfterDecimal());
        }
        
        final String interestRateParamName = "interestRate";
        if (command.isChangeInBigDecimalParameterNamed(interestRateParamName, this.interestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(interestRateParamName);
            actualChanges.put(interestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestRate = newValue;
        }

        final String savingsDepositAmountParamName = "savingsDepositAmount";
        if (command.isChangeInBigDecimalParameterNamed(savingsDepositAmountParamName, this.savingsDepositAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(savingsDepositAmountParamName);
            actualChanges.put(savingsDepositAmountParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.savingsDepositAmount = newValue;
        }
        
        final String depositEveryParamName = "depositEvery";
        if (command.isChangeInIntegerParameterNamed(depositEveryParamName, this.depositEvery)) {
            final Integer newValue = command.integerValueOfParameterNamed(depositEveryParamName);
            actualChanges.put(depositEveryParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.depositEvery = newValue;
        }
        
        
        final String savingProductTypeParamName = "savingProductType";
        if (command.isChangeInIntegerParameterNamed(savingProductTypeParamName, this.savingProductType)) {
            final Integer newValue = command.integerValueOfParameterNamed(savingProductTypeParamName);
            actualChanges.put(savingProductTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.savingProductType = SavingProductType.fromInt(newValue).getValue();
        }
        
        final String tenureTypeParamName = "tenureType";
        if (command.isChangeInIntegerParameterNamed(tenureTypeParamName, this.tenureType)) {
            final Integer newValue = command.integerValueOfParameterNamed(tenureTypeParamName);
            actualChanges.put(tenureTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.tenureType = TenureTypeEnum.fromInt(newValue).getValue();
        }

        final String tenureParamName = "tenure";
        if (command.isChangeInIntegerParameterNamed(tenureParamName, this.tenure)) {
            final Integer newValue = command.integerValueOfParameterNamed(tenureParamName);
            actualChanges.put(tenureParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.tenure = newValue;
        }
        
        final String frequencyParamName = "frequency";
        if (command.isChangeInIntegerParameterNamed(frequencyParamName, this.frequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(frequencyParamName);
            actualChanges.put(frequencyParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.frequency = SavingFrequencyType.fromInt(newValue).getValue();
        }
        
        final String interestTypeParamName = "interestType";
        if (command.isChangeInIntegerParameterNamed(interestTypeParamName, this.interestType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestTypeParamName);
            actualChanges.put(interestTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestType = SavingsInterestType.fromInt(newValue).getValue();
        }
        
        final String interestCalculationMethodParamName = "interestCalculationMethod";
        if (command.isChangeInIntegerParameterNamed(interestCalculationMethodParamName, this.interestCalculationMethod)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCalculationMethodParamName);
            actualChanges.put(interestCalculationMethodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestCalculationMethod = SavingInterestCalculationMethod.fromInt(newValue).getValue();
        }

        final String minimumBalanceForWithdrawalParamName = "minimumBalanceForWithdrawal";
        if (command.isChangeInBigDecimalParameterNamed(minimumBalanceForWithdrawalParamName, this.minimumBalanceForWithdrawal)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minimumBalanceForWithdrawalParamName);
            actualChanges.put(minimumBalanceForWithdrawalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minimumBalanceForWithdrawal = newValue;
        }

        final String isPartialDepositAllowedParamName = "isPartialDepositAllowed";
        if (command.isChangeInBooleanParameterNamed(isPartialDepositAllowedParamName, this.isPartialDepositAllowed)) {
        	final Boolean newValue = command.booleanObjectValueOfParameterNamed(isPartialDepositAllowedParamName);
            actualChanges.put(isPartialDepositAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.isPartialDepositAllowed = newValue;
        }
        
        final String isLockinPeriodAllowedParamName = "isLockinPeriodAllowed";
        if (command.isChangeInBooleanParameterNamed(isLockinPeriodAllowedParamName, this.isLockinPeriodAllowed)) {
        	final Boolean newValue = command.booleanObjectValueOfParameterNamed(isLockinPeriodAllowedParamName);
            actualChanges.put(isLockinPeriodAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.isLockinPeriodAllowed = newValue;
        }
		
		final String lockinPeriodParamName = "lockinPeriod";
        if (command.isChangeInIntegerParameterNamed(lockinPeriodParamName, this.lockinPeriod)) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodParamName);
            actualChanges.put(lockinPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.lockinPeriod = newValue;
        }
		
		
		final String lockinPeriodTypeParamName = "lockinPeriodType";
        if (command.isChangeInIntegerParameterNamed(lockinPeriodTypeParamName, this.lockinPeriodType)) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodTypeParamName);
            actualChanges.put(lockinPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.lockinPeriodType = PeriodFrequencyType.fromInt(newValue).getValue();
        }
        
        return actualChanges;

    }

    public BigDecimal getSavingsDepositAmount() {
        return savingsDepositAmount;
    }

    public Integer getDepositEvery() {
		return this.depositEvery;
	}

	public Integer getSavingProductType() {
        return savingProductType;
    }

    public Integer getTenureType() {
        return tenureType;
    }

    public Integer getTenure() {
        return tenure;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public Integer getInterestType() {
        return interestType;
    }

    public Integer getInterestCalculationMethod() {
        return interestCalculationMethod;
    }

    public BigDecimal getMinimumBalanceForWithdrawal() {
        return minimumBalanceForWithdrawal;
    }

    public boolean isPartialDepositAllowed() {
        return isPartialDepositAllowed;
    }

    public boolean isLockinPeriodAllowed() {
        return isLockinPeriodAllowed;
    }

    public Integer getLockinPeriod() {
        return lockinPeriod;
    }

    public Integer getLockinPeriodType() {
        return lockinPeriodType;
    }

}
