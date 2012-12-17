package org.mifosplatform.portfolio.savingsaccountproduct.command;

import java.math.BigDecimal;
import java.util.Set;

public class SavingProductCommand {

    private final Long id;
    private final String name;
    private final String description;

    private final String currencyCode;
    private final Integer digitsAfterDecimal;
    private final BigDecimal interestRate;
    private final BigDecimal minInterestRate;
    private final BigDecimal maxInterestRate;

    private final BigDecimal savingsDepositAmount;
    private final Integer depositEvery;
    private final Integer savingProductType;
    private final Integer tenureType;
    private final Integer tenure;
    private final Integer frequency;
    private final Integer interestType;
    private final Integer interestCalculationMethod;
    private final BigDecimal minimumBalanceForWithdrawal;
    private final boolean isPartialDepositAllowed;
    private final boolean isLockinPeriodAllowed;
    private final Integer lockinPeriod;
    private final Integer lockinPeriodType;

    private final Set<String> modifiedParameters;

    public SavingProductCommand(Set<String> modifiedParameters, Long id, String name, String description, String currencyCode,
            Integer digitsAfterDecimal, BigDecimal interestRate, BigDecimal minInterestRate, BigDecimal maxInterestRate,
            BigDecimal savingsDepositAmount, Integer depositEvery,Integer savingProductType, Integer tenureType, Integer tenure, Integer frequency,
            Integer interestType, Integer interestCalculationMethod, BigDecimal minimumBalanceForWithdrawal,
            boolean isPartialDepositAllowed, boolean isLockinPeriodAllowed, Integer lockinPeriod, Integer lockinPeriodType) {

        this.id = id;
        this.name = name;
        this.description = description;

        this.currencyCode = currencyCode;
        this.digitsAfterDecimal = digitsAfterDecimal;
        this.interestRate = interestRate;
        this.minInterestRate = minInterestRate;
        this.maxInterestRate = maxInterestRate;

        this.savingsDepositAmount = savingsDepositAmount;
        this.depositEvery = depositEvery;
        this.savingProductType = savingProductType;
        this.tenureType = tenureType;
        this.tenure = tenure;
        this.frequency = frequency;
        this.interestType = interestType;
        this.interestCalculationMethod = interestCalculationMethod;
        this.minimumBalanceForWithdrawal = minimumBalanceForWithdrawal;
        this.isPartialDepositAllowed = isPartialDepositAllowed;
        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType;

        this.modifiedParameters = modifiedParameters;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Integer getDigitsAfterDecimal() {
        return digitsAfterDecimal;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public BigDecimal getMinInterestRate() {
        return minInterestRate;
    }

    public BigDecimal getMaxInterestRate() {
        return maxInterestRate;
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

    public boolean isNameChanged() {
        return this.modifiedParameters.contains("name");
    }

    public boolean isDescriptionChanged() {
        return this.modifiedParameters.contains("description");
    }

    public boolean isCurrencyCodeChanged() {
        return this.modifiedParameters.contains("currencyCode");
    }

    public boolean isDigitsAfterDecimalChanged() {
        return this.modifiedParameters.contains("digitsAfterDecimal");
    }

    public boolean isInterestRateChanged() {
        return this.modifiedParameters.contains("interestRate");
    }

    public boolean isSavingsDepositAmountChanged() {
        return this.modifiedParameters.contains("savingsDepositAmount");
    }

    public boolean isDepositEveryChanged() {
        return this.modifiedParameters.contains("depositEvery");
    }
    
    public boolean isSavingProductTypeChanged() {
        return this.modifiedParameters.contains("savingProductType");
    }

    public boolean isTenureTypeChanged() {
        return this.modifiedParameters.contains("tenureType");
    }

    public boolean isTenureChanged() {
        return this.modifiedParameters.contains("tenure");
    }

    public boolean isFrequencyChanged() {
        return this.modifiedParameters.contains("frequency");
    }

    public boolean isInterestTypeChanged() {
        return this.modifiedParameters.contains("interestType");
    }

    public boolean isInterestCalculationMethodChanged() {
        return this.modifiedParameters.contains("interestCalculationMethod");
    }

    public boolean isMinimumBalanceForWithdrawalChanged() {
        return this.modifiedParameters.contains("minimumBalanceForWithdrawal");
    }

    public boolean isPartialDepositAllowedChanged() {
        return this.modifiedParameters.contains("isPartialDepositAllowed");
    }

    public boolean isLockinPeriodAllowedChanged() {
        return this.modifiedParameters.contains("isLockinPeriodAllowed");
    }

    public boolean isLockinPeriodChanged() {
        return this.modifiedParameters.contains("lockinPeriod");
    }

    public boolean isLockinPeriodTypeChanged() {
        return this.modifiedParameters.contains("lockinPeriodType");
    }

}
