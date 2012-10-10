package org.mifosng.platform.api.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class SavingAccountData {
	
	private final Long id;
	private final EnumOptionData status;
	private final String externalId;
	private final Long clientId;
	private final String clientName;
	private final Long productId;
	private final String productName;
	private final EnumOptionData productType;
	private final CurrencyData currencyData;
	private final BigDecimal savingsDepostiAmountPerPeriod;
	private final EnumOptionData savingsFrequencyType;
	private final BigDecimal totalDepositAmount;
	private final BigDecimal reccuringInterestRate;
	private final BigDecimal savingInterestRate;
	private final EnumOptionData interestType;
	private final EnumOptionData interestCalculationMethod;
	private final Integer tenure;
	private final EnumOptionData tenureType;
	private final LocalDate projectedCommencementDate;
	private final LocalDate actualCommencementDate;
	private final LocalDate maturesOnDate;
	private final BigDecimal projectedInterestAccuredOnMaturity;
	private final BigDecimal actualInterestAccured;
	private final BigDecimal projectedMaturityAmount;
	private final BigDecimal actualMaturityAmount;
	private final boolean preClosureAllowed;
	private final BigDecimal preClosureInterestRate; 
	private final LocalDate withdrawnonDate;
	private final LocalDate rejectedonDate;
	private final LocalDate closedonDate;
	private final boolean isLockinPeriodAllowed;
	private final Integer lockinPeriod;
	private final EnumOptionData lockinPeriodType;

	public SavingAccountData(Long id, EnumOptionData status, String externalId, 
			Long clientId, String clientName, Long productId, 
			String productName, EnumOptionData productType, 
			CurrencyData currencyData,
			BigDecimal savingsDepostiAmountPerPeriod,
			EnumOptionData savingsFrequencyType, BigDecimal totalDepositAmount,
			BigDecimal reccuringInterestRate, BigDecimal savingInterestRate,
			EnumOptionData interestType,
			EnumOptionData interestCalculationMethod, Integer tenure,
			EnumOptionData tenureType, LocalDate projectedCommencementDate,
			LocalDate actualCommencementDate, LocalDate maturesOnDate,
			BigDecimal projectedInterestAccuredOnMaturity,
			BigDecimal actualInterestAccured,
			BigDecimal projectedMaturityAmount,
			BigDecimal actualMaturityAmount, boolean preClosureAllowed,
			BigDecimal preClosureInterestRate, LocalDate withdrawnonDate,
			LocalDate rejectedonDate, LocalDate closedonDate,
			boolean isLockinPeriodAllowed, Integer lockinPeriod,
			EnumOptionData lockinPeriodType) {
		
		this.id = id;
		this.status = status;
		this.externalId = externalId;
		this.clientId = clientId;
		this.clientName = clientName;
		this.productId = productId;
		this.productName = productName;
		this.productType = productType;
		this.currencyData = currencyData;
		this.savingsDepostiAmountPerPeriod = savingsDepostiAmountPerPeriod;
		this.savingsFrequencyType = savingsFrequencyType;
		this.totalDepositAmount = totalDepositAmount;
		this.reccuringInterestRate = reccuringInterestRate;
		this.savingInterestRate = savingInterestRate;
		this.interestType =interestType;
		this.interestCalculationMethod = interestCalculationMethod;
		this.tenure = tenure;
		this.tenureType = tenureType;
		this.projectedCommencementDate = projectedCommencementDate;
		this.actualCommencementDate = actualCommencementDate;
		this.maturesOnDate = maturesOnDate;
		this.projectedInterestAccuredOnMaturity = projectedInterestAccuredOnMaturity;
		this.actualInterestAccured = actualInterestAccured;
		this.projectedMaturityAmount = projectedMaturityAmount;
		this.actualMaturityAmount =actualMaturityAmount;
		this.preClosureAllowed = preClosureAllowed;
		this.preClosureInterestRate = preClosureInterestRate;
		this.withdrawnonDate = withdrawnonDate;
		this.rejectedonDate = rejectedonDate;
		this.closedonDate = closedonDate;
		this.isLockinPeriodAllowed = isLockinPeriodAllowed;
		this.lockinPeriod = lockinPeriod;
		this.lockinPeriodType = lockinPeriodType;
		
	}

	public Long getId() {
		return id;
	}

	public EnumOptionData getStatus() {
		return status;
	}

	public String getExternalId() {
		return externalId;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public Long getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public EnumOptionData getProductType() {
		return productType;
	}

	public CurrencyData getCurrencyData() {
		return currencyData;
	}

	public BigDecimal getSavingsDepostiAmountPerPeriod() {
		return savingsDepostiAmountPerPeriod;
	}

	public EnumOptionData getSavingsFrequencyType() {
		return savingsFrequencyType;
	}

	public BigDecimal getTotalDepositAmount() {
		return totalDepositAmount;
	}

	public BigDecimal getReccuringInterestRate() {
		return reccuringInterestRate;
	}

	public BigDecimal getSavingInterestRate() {
		return savingInterestRate;
	}

	public EnumOptionData getInterestType() {
		return interestType;
	}

	public EnumOptionData getInterestCalculationMethod() {
		return interestCalculationMethod;
	}

	public Integer getTenure() {
		return tenure;
	}

	public EnumOptionData getTenureType() {
		return tenureType;
	}

	public LocalDate getProjectedCommencementDate() {
		return projectedCommencementDate;
	}

	public LocalDate getActualCommencementDate() {
		return actualCommencementDate;
	}

	public LocalDate getMaturesOnDate() {
		return maturesOnDate;
	}

	public BigDecimal getProjectedInterestAccuredOnMaturity() {
		return projectedInterestAccuredOnMaturity;
	}

	public BigDecimal getActualInterestAccured() {
		return actualInterestAccured;
	}

	public BigDecimal getProjectedMaturityAmount() {
		return projectedMaturityAmount;
	}

	public BigDecimal getActualMaturityAmount() {
		return actualMaturityAmount;
	}

	public boolean isPreClosureAllowed() {
		return preClosureAllowed;
	}

	public BigDecimal getPreClosureInterestRate() {
		return preClosureInterestRate;
	}

	public LocalDate getWithdrawnonDate() {
		return withdrawnonDate;
	}

	public LocalDate getRejectedonDate() {
		return rejectedonDate;
	}

	public LocalDate getClosedonDate() {
		return closedonDate;
	}

	public boolean isLockinPeriodAllowed() {
		return isLockinPeriodAllowed;
	}

	public Integer getLockinPeriod() {
		return lockinPeriod;
	}

	public EnumOptionData getLockinPeriodType() {
		return lockinPeriodType;
	}

}
