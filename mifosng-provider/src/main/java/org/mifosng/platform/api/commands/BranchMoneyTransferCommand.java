package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

import org.joda.time.LocalDate;

/**
 * Immutable command for transfering money between two branches.
 */
public class BranchMoneyTransferCommand {

	private final Long fromOfficeId;
	private final Long toOfficeId;
	private final LocalDate transactionDate;
	private final String currencyCode;
	private final BigDecimal transactionAmount;
	private final String description;
	
	private final Set<String> modifiedParameters;
	
	public BranchMoneyTransferCommand(final Set<String> modifiedParameters, final Long fromOfficeId, final Long toOfficeId, 
			final LocalDate transactionDate, final String currencyCode, final BigDecimal transactionAmount, final String description) {
		this.modifiedParameters = modifiedParameters;
		this.fromOfficeId = fromOfficeId;
		this.toOfficeId = toOfficeId;
		this.transactionDate = transactionDate;
		this.currencyCode = currencyCode;
		this.transactionAmount = transactionAmount;
		this.description = description;
	}

	public Long getFromOfficeId() {
		return fromOfficeId;
	}

	public Long getToOfficeId() {
		return toOfficeId;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isFromOfficeIdChanged() {
		return this.modifiedParameters.contains("fromOfficeId");
	}
}