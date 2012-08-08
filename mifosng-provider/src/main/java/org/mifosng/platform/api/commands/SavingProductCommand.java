package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

public class SavingProductCommand {
	private final Long id;
	private final String name;
	private final String description;

	private final String currencyCode;
	private final Integer digitsAfterDecimal;
	private final BigDecimal interestRate;
	
	private final BigDecimal minimumBalance;
	private final BigDecimal maximumBalance;

	private final Set<String> modifiedParameters;

	public SavingProductCommand(final Set<String> modifiedParameters,
			final Long id, final String name, final String description,
			final String currencyCode, final Integer digitsAfterDecimal,
			final BigDecimal interestRate, final BigDecimal minimumBalance, final BigDecimal maximumBalance) {
		this.id = id;
		this.name = name;
		this.description = description;

		this.currencyCode = currencyCode;
		this.digitsAfterDecimal = digitsAfterDecimal;
		this.interestRate = interestRate;
		
		this.minimumBalance=minimumBalance;
		this.maximumBalance=maximumBalance;

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
	
	
	public BigDecimal getMinimumBalance() {
		return minimumBalance;
	}

	public BigDecimal getMaximumBalance() {
		return maximumBalance;
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
	
	public boolean isMinimumBalanceChanged(){
		return this.modifiedParameters.contains("minimumBalance");
	}
	
	public boolean isMaximumBalanceChanged(){
		return this.modifiedParameters.contains("maximumBalance");
	}
}
