package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

public class DepositProductCommand {
	
	private final Long id;
	private final String name;
	private final String description;

	private final String currencyCode;
	private final Integer digitsAfterDecimal;
	
	private final BigDecimal minimumBalance;
	private final BigDecimal maximumBalance;
	
	private final Integer tenureInMonths;
	private final BigDecimal maturityDefaultInterestRate;
	private final BigDecimal maturityMinInterestRate;
	private final BigDecimal maturityMaxInterestRate;
	private final boolean renewalAllowed;
	private final boolean preClosureAllowed;
	private final BigDecimal preClosureInterestRate;

	private final Set<String> modifiedParameters;
	
	public DepositProductCommand(final Set<String> modifiedParameters,
			final Long id, final String name, final String description,
			final String currencyCode, final Integer digitsAfterDecimal,
			final BigDecimal minimumBalance, final BigDecimal maximumBalance,
			final Integer tenureInMonths, final BigDecimal maturityDefaultInterestRate,
			final BigDecimal maturityMinInterestRate, final BigDecimal maturityMaxInterestRate,
			final boolean renewalAllowed, final boolean preClosureAllowed, final BigDecimal preClosureInterestRate ) {
		this.id = id;
		this.name = name;
		this.description = description;

		this.currencyCode = currencyCode;
		this.digitsAfterDecimal = digitsAfterDecimal;
		
		this.minimumBalance=minimumBalance;
		this.maximumBalance=maximumBalance;
		
		this.tenureInMonths=tenureInMonths;
		this.maturityDefaultInterestRate = maturityDefaultInterestRate;
		this.maturityMinInterestRate = maturityMinInterestRate;
		this.maturityMaxInterestRate = maturityMaxInterestRate;
		this.preClosureInterestRate = preClosureInterestRate;
		
		this.renewalAllowed = renewalAllowed;
		this.preClosureAllowed = preClosureAllowed;

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
	
	public BigDecimal getMinimumBalance() {
		return minimumBalance;
	}

	public BigDecimal getMaximumBalance() {
		return maximumBalance;
	}

	public Integer getTenureInMonths() {
		return tenureInMonths;
	}

	public BigDecimal getMaturityDefaultInterestRate() {
		return maturityDefaultInterestRate;
	}

	public BigDecimal getMaturityMinInterestRate() {
		return maturityMinInterestRate;
	}

	public BigDecimal getMaturityMaxInterestRate() {
		return maturityMaxInterestRate;
	}
	
	public boolean isRenewalAllowed() {
		return renewalAllowed;
	}

	public boolean isPreClosureAllowed() {
		return preClosureAllowed;
	}

	public BigDecimal getPreClosureInterestRate() {
		return preClosureInterestRate;
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
	
	public boolean isTenureMonthsChanged() {
		return this.modifiedParameters.contains("tenureMonths");	
	}
	
	public boolean isMaturityDefaultInterestRateChanged() {
		return this.modifiedParameters.contains("maturityDefaultInterestRate");
	}
	
	public boolean isMaturityMinInterestRateChanged() {
		return this.modifiedParameters.contains("maturityMinInterestRate");
	}
	
	public boolean isMaturityMaxInterestRateChanged() {
		return this.modifiedParameters.contains("maturityMaxInterestRate");
	}
	
	public boolean isRenewalAllowedChanged() {
		return this.modifiedParameters.contains("renewalAllowed");
	}
	
	public boolean isPreClosureAllowedChanged() {
		return this.modifiedParameters.contains("preClosureAllowed");
	}
	
	public boolean isPreClosureInterestRateChanged() {
		return this.modifiedParameters.contains("preClosureInterestRate");
	}

	public boolean isNoFieldChanged() {
		return this.modifiedParameters.isEmpty();
	}
}