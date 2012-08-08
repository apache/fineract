package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Immutable command used when create/renewing deposit accounts
 */
public class DepositAccountCommand {
	
	private final Long id;
	private final String externalId;

	private final String currencyCode;
	private final Integer digitsAfterDecimal;
	private final BigDecimal maturityActualInterestRate;

	private final Set<String> modifiedParameters;

	public DepositAccountCommand(final Set<String> modifiedParameters,
			final Long id,
			final String externalId,
			final String currencyCode, 
			final Integer digitsAfterDecimal,
			final BigDecimal maturityActualInterestRate) {
		this.id = id;
		this.externalId = externalId;
		this.currencyCode = currencyCode;
		this.digitsAfterDecimal = digitsAfterDecimal;
		this.maturityActualInterestRate = maturityActualInterestRate;

		this.modifiedParameters = modifiedParameters;
	}

	public Long getId() {
		return id;
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

	public BigDecimal getMaturityActualInterestRate() {
		return maturityActualInterestRate;
	}

	public boolean isCurrencyCodeChanged() {
		return this.modifiedParameters.contains("currencyCode");
	}

	public boolean isDigitsAfterDecimalChanged() {
		return this.modifiedParameters.contains("digitsAfterDecimal");
	}
	
	public boolean isMaturityActualInterestRateChanged() {
		return this.modifiedParameters.contains("interestRate");
	}
}
