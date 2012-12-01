package org.mifosng.platform.api.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.configuration.data.CurrencyData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object reprsenting a savings account.
 */
public class SavingAccountData {
	
	@SuppressWarnings("unused")
	private final Long id;
	@SuppressWarnings("unused")
	private final EnumOptionData status;
	@SuppressWarnings("unused")
	private final String externalId;
	@SuppressWarnings("unused")
	private final Long clientId;
	@SuppressWarnings("unused")
	private final String clientName;
	@SuppressWarnings("unused")
	private final Long productId;
	@SuppressWarnings("unused")
	private final String productName;
	@SuppressWarnings("unused")
	private final EnumOptionData productType;
	@SuppressWarnings("unused")
	private final CurrencyData currencyData;
	@SuppressWarnings("unused")
	private final BigDecimal savingsDepostiAmountPerPeriod;
	@SuppressWarnings("unused")
	private final EnumOptionData savingsFrequencyType;
	@SuppressWarnings("unused")
	private final BigDecimal totalDepositAmount;
	@SuppressWarnings("unused")
	private final BigDecimal reccuringInterestRate;
	@SuppressWarnings("unused")
	private final BigDecimal savingInterestRate;
	@SuppressWarnings("unused")
	private final EnumOptionData interestType;
	@SuppressWarnings("unused")
	private final EnumOptionData interestCalculationMethod;
	@SuppressWarnings("unused")
	private final Integer tenure;
	@SuppressWarnings("unused")
	private final EnumOptionData tenureType;
	@SuppressWarnings("unused")
	private final LocalDate projectedCommencementDate;
	@SuppressWarnings("unused")
	private final LocalDate actualCommencementDate;
	@SuppressWarnings("unused")
	private final LocalDate maturesOnDate;
	@SuppressWarnings("unused")
	private final BigDecimal projectedInterestAccuredOnMaturity;
	@SuppressWarnings("unused")
	private final BigDecimal actualInterestAccured;
	@SuppressWarnings("unused")
	private final BigDecimal projectedMaturityAmount;
	@SuppressWarnings("unused")
	private final BigDecimal actualMaturityAmount;
	@SuppressWarnings("unused")
	private final boolean preClosureAllowed;
	@SuppressWarnings("unused")
	private final BigDecimal preClosureInterestRate;
	@SuppressWarnings("unused")
	private final LocalDate withdrawnonDate;
	@SuppressWarnings("unused")
	private final LocalDate rejectedonDate;
	@SuppressWarnings("unused")
	private final LocalDate closedonDate;
	@SuppressWarnings("unused")
	private final boolean isLockinPeriodAllowed;
	@SuppressWarnings("unused")
	private final Integer lockinPeriod;
	@SuppressWarnings("unused")
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
}