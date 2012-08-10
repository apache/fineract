package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Immutable command used when create/renewing deposit accounts
 */
public class DepositAccountCommand {
	
	private final Long id;
	private final Long clientId;
	private final Long productId;
	private final String externalId;

	private final String currencyCode;
	private final Integer digitsAfterDecimal;
	private final BigDecimal depositAmount;
	private final BigDecimal maturityInterestRate;
	private final Integer termInMonths;
	
	private final Set<String> modifiedParameters;

	public DepositAccountCommand(final Set<String> modifiedParameters,
			final Long id,
			final Long clientId, 
			final Long productId, 
			final String externalId,
			final String currencyCode, 
			final Integer digitsAfterDecimal,
			final BigDecimal depositAmount, 
			final BigDecimal interestRate, 
			final Integer termInMonths) {
		this.id = id;
		this.clientId = clientId;
		this.productId = productId;
		this.externalId = externalId;
		
		this.currencyCode = currencyCode;
		this.digitsAfterDecimal = digitsAfterDecimal;
		this.depositAmount = depositAmount;
		this.maturityInterestRate = interestRate;
		this.termInMonths = termInMonths;
		
		this.modifiedParameters = modifiedParameters;
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

	public BigDecimal getDepositAmount() {
		return depositAmount;
	}
	
	public BigDecimal getMaturityInterestRate() {
		return maturityInterestRate;
	}

	public Integer getTermInMonths() {
		return termInMonths;
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