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
	
	private final Integer tenureMonths;
	private final BigDecimal maturityDefaultInterestRate;
	private final BigDecimal maturityMinInterestRate;
	private final BigDecimal maturityMaxInterestRate;
	private final Boolean canRenew;
	private final Boolean canPreClose;
	private final BigDecimal preClosureInterestRate;

	private final Set<String> modifiedParameters;
	
	public DepositProductCommand(final Set<String> modifiedParameters,
			final Long id, final String name, final String description,
			final String currencyCode, final Integer digitsAfterDecimal,
			final BigDecimal minimumBalance, final BigDecimal maximumBalance,
			final Integer tenureMonths, final BigDecimal maturityDefaultInterestRate,
			final BigDecimal maturityMinInterestRate, final BigDecimal maturityMaxInterestRate,
			final Boolean canRenew, final Boolean canPreClose, final BigDecimal preClosureInterestRate ) {
		this.id = id;
		this.name = name;
		this.description = description;

		this.currencyCode = currencyCode;
		this.digitsAfterDecimal = digitsAfterDecimal;
		
		this.minimumBalance=minimumBalance;
		this.maximumBalance=maximumBalance;
		
		this.tenureMonths=tenureMonths;
		this.maturityDefaultInterestRate = maturityDefaultInterestRate;
		this.maturityMinInterestRate = maturityMinInterestRate;
		this.maturityMaxInterestRate = maturityMaxInterestRate;
		this.preClosureInterestRate = preClosureInterestRate;
		
		this.canRenew = canRenew;
		this.canPreClose = canPreClose;

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

	public Integer getTenureMonths() {
		return tenureMonths;
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

	public Boolean getCanRenew() {
		return canRenew;
	}

	public Boolean getCanPreClose() {
		return canPreClose;
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
	
	public boolean isCanRenewChanged() {
		return this.modifiedParameters.contains("canRenew");
	}
	
	public boolean isCanPreCloseChanged() {
		return this.modifiedParameters.contains("canPreClose");
	}
	
	public boolean isPreClosureInterestRateChanged() {
		return this.modifiedParameters.contains("preClosureInterestRate");
	}

}
