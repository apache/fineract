package org.mifosplatform.portfolio.savingsaccount.command;

import java.math.BigDecimal;
import java.util.Set;

import org.joda.time.LocalDate;

public class SavingAccountCommand {

    private final Long id;
    private final Long clientId;
    private final Long productId;
    private final String externalId;

    private final String currencyCode;
    private final Integer digitsAfterDecimal;
    private final BigDecimal savingsDepositAmount;
    private final BigDecimal recurringInterestRate;
    private final BigDecimal savingInterestRate;
    private final Integer tenure;

    private final boolean isLockinPeriodAllowed;
    private final Integer lockinPeriod;
    private final Integer lockinPeriodType;

    private final LocalDate commencementDate;
    private final Integer savingProductType;
    private final Integer tenureType;
    private final Integer frequency;
    private final Integer interestType;

    private final Integer interestCalculationMethod;
    private final BigDecimal minimumBalanceForWithdrawal;
    private final boolean isPartialDepositAllowed;
    private final Integer depositEvery;
    
    private final Integer interestPostEvery; 
    private final Integer interestPostFrequency;

    private final Set<String> modifiedParameters;

    public SavingAccountCommand(final Set<String> modifiedParameters, final Long id, final Long clientId, final Long productId,
            final String externalId, final String currencyCode, final Integer digitsAfterDecimal, final BigDecimal savingsDepositAmount,
            final BigDecimal recurringInterestRate, final BigDecimal savingInterestRate, final Integer tenure,
            final LocalDate commencementDate, final Integer savingProductType, final Integer tenureType, final Integer frequency,
            final Integer interestType, final BigDecimal minimumBalanceForWithdrawal, final Integer interestCalculationMethod,
            final boolean isLockinPeriodAllowed, final boolean isPartialDepositAllowed, final Integer lockInPeriod,
            final Integer lockinPeriodType, final Integer depositEvery,final Integer interestPostEvery,final Integer interestPostFrequency) {
        this.id = id;
        this.clientId = clientId;
        this.productId = productId;
        this.externalId = externalId;

        this.currencyCode = currencyCode;
        this.digitsAfterDecimal = digitsAfterDecimal;
        this.savingsDepositAmount = savingsDepositAmount;
        this.recurringInterestRate = recurringInterestRate;
        this.savingInterestRate = savingInterestRate;
        this.tenure = tenure;

        this.modifiedParameters = modifiedParameters;
        this.commencementDate = commencementDate;

        this.savingProductType = savingProductType;
        this.tenureType = tenureType;
        this.frequency = frequency;
        this.interestType = interestType;
        this.isPartialDepositAllowed = isPartialDepositAllowed;

        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockInPeriod;
        this.lockinPeriodType = lockinPeriodType;
        this.minimumBalanceForWithdrawal = minimumBalanceForWithdrawal;
        this.interestCalculationMethod = interestCalculationMethod;
        this.depositEvery = depositEvery;
        
        this.interestPostEvery = interestPostEvery;
        this.interestPostFrequency = interestPostFrequency;
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Integer getDigitsAfterDecimal() {
        return digitsAfterDecimal;
    }

    public BigDecimal getSavingsDepositAmount() {
        return savingsDepositAmount;
    }

    public BigDecimal getRecurringInterestRate() {
        return recurringInterestRate;
    }

    public BigDecimal getSavingInterestRate() {
        return savingInterestRate;
    }

    public Integer getTenure() {
        return tenure;
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

    public LocalDate getCommencementDate() {
        return commencementDate;
    }

    public Integer getSavingProductType() {
        return savingProductType;
    }

    public Integer getTenureType() {
        return tenureType;
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

    public Integer getDepositEvery() {
        return this.depositEvery;
    }

    public Integer getInterestPostEvery() {
		return this.interestPostEvery;
	}

	public Integer getInterestPostFrequency() {
		return this.interestPostFrequency;
	}

	public boolean isNoFieldChanged() {
        return this.modifiedParameters.isEmpty();
    }

    public boolean isProductIdChanged() {
        return this.modifiedParameters.contains("productId");
    }

    public boolean isExternalIdChanged() {
        return this.modifiedParameters.contains("externalId");
    }

    public boolean isDepositAmountChanged() {
        return this.modifiedParameters.contains("deposit");
    }

    public boolean isTenureInMonthsChanged() {
        return this.modifiedParameters.contains("tenureInMonths");
    }

    public boolean isLockinPeriodChanged() {
        return this.modifiedParameters.contains("lockinPeriod");
    }

    public boolean isPreClosureInterestRateChanged() {
        return this.modifiedParameters.contains("preClosureInterestRate");
    }

    public boolean isPreClosureAllowedChanged() {
        return this.modifiedParameters.contains("preClosureAllowed");
    }

    public boolean isCommencementDateChanged() {
        return this.modifiedParameters.contains("commencementDate");
    }

    public boolean isTenureTypeEnumChanged() {
        return this.modifiedParameters.contains("tenureType");
    }

    public boolean isSavingProductTypeChanged() {
        return this.modifiedParameters.contains("savingProductType");
    }

    public boolean isSavingFrequencyTypeChanged() {
        return this.modifiedParameters.contains("frequency");
    }

    public boolean isSavingInterestCalculationMethodChanged() {
        return this.modifiedParameters.contains("interestCalculationMethod");
    }

    public boolean isReccuringInterestRateChanged() {
        return this.modifiedParameters.contains("recurringInterestRate");
    }

    public boolean isSavingInterestRateChanged() {
        return this.modifiedParameters.contains("savingInterestRate");
    }

    public boolean isLockinPeriodAllowedChanged() {
        return this.modifiedParameters.contains("isLockinPeriodAllowed");
    }

    public boolean isLockinPeriodTypeChanged() {
        return this.modifiedParameters.contains("lockinPeriodType");
    }

    public boolean isPartialDepositAllowedChanged() {
        return this.modifiedParameters.contains("isPartialDepositAllowed");
    }
    
    public boolean isDepositEveryChanged() {
        return this.modifiedParameters.contains("depositEvery");
    }
    
    public boolean isInterestPostedEveryChanged(){
    	return this.modifiedParameters.contains(interestPostEvery);
    }
    
    public boolean isInterestPostingChanged(){
    	return this.modifiedParameters.contains(interestPostFrequency);
    }
    

    public CalculateSavingScheduleCommand toCalculateSavingScheduleCommand() {
        return new CalculateSavingScheduleCommand(productId, savingsDepositAmount, depositEvery, frequency, recurringInterestRate,
                commencementDate, tenure, interestPostEvery, interestPostFrequency);
    }

}
