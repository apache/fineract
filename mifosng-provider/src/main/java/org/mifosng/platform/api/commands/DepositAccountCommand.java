package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

import org.joda.time.LocalDate;

/**
 * Immutable command used when create/renewing deposit accounts
 */
public class DepositAccountCommand {
	
	private final Long id;
	private final Long clientId;
	private final Long productId;
	private final String externalId;

	private final BigDecimal depositAmount;
	private final BigDecimal maturityInterestRate;
	private final Integer termInMonths;
	
	private final Integer interestCompoundedEvery;
	private final Integer interestCompoundedEveryPeriodType;
	private final LocalDate commencementDate;
	
	private final boolean renewalAllowed;
	private final boolean preClosureAllowed;
	
	private final Set<String> modifiedParameters;

	public DepositAccountCommand(final Set<String> modifiedParameters,
			final Long id,
			final Long clientId, 
			final Long productId, 
			final String externalId,
			final BigDecimal depositAmount, 
			final BigDecimal interestRate, 
			final Integer termInMonths, final Integer compoundingInterestFrequency, final Integer compoundingInterestFrequencyType, final LocalDate commencementDate,
			final boolean renewalAllowed, final boolean preClosureAllowed) {
		this.id = id;
		this.clientId = clientId;
		this.productId = productId;
		this.externalId = externalId;
		
		this.depositAmount = depositAmount;
		this.maturityInterestRate = interestRate;
		this.termInMonths = termInMonths;
		
		this.modifiedParameters = modifiedParameters;
		this.interestCompoundedEvery = compoundingInterestFrequency;
		this.interestCompoundedEveryPeriodType = compoundingInterestFrequencyType;
		this.commencementDate = commencementDate;
		this.renewalAllowed = renewalAllowed;
		this.preClosureAllowed = preClosureAllowed;
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

	public BigDecimal getDepositAmount() {
		return depositAmount;
	}
	
	public BigDecimal getMaturityInterestRate() {
		return maturityInterestRate;
	}

	public Integer getTermInMonths() {
		return termInMonths;
	}
	
	public Integer getInterestCompoundedEvery() {
		return interestCompoundedEvery;
	}

	public Integer getInterestCompoundedEveryPeriodType() {
		return interestCompoundedEveryPeriodType;
	}

	public LocalDate getCommencementDate() {
		return commencementDate;
	}
	
	public boolean isRenewalAllowed() {
		return renewalAllowed;
	}

	public boolean isPreClosureAllowed() {
		return preClosureAllowed;
	}

	public boolean isNoFieldChanged() {
		return this.modifiedParameters.isEmpty();
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
	
	public boolean isRenewalAllowedChanged() {
		return this.modifiedParameters.contains("renewalAllowed");
	}
	
	public boolean isPreClosureAllowedChanged() {
		return this.modifiedParameters.contains("preClosureAllowed");
	}
}