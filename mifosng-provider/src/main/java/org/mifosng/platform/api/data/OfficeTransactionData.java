package org.mifosng.platform.api.data;

import java.util.List;

import org.joda.time.LocalDate;

/**
 * Immutable data object for office transactions.
 */
public class OfficeTransactionData {

	private final LocalDate transactionDate;
	private final List<CurrencyData> currencyOptions;
	private final List<OfficeLookup> allowedOffices;

	public OfficeTransactionData(final LocalDate transactionDate, final List<OfficeLookup> allowedOffices, final List<CurrencyData> currencyOptions) {
		this.transactionDate = transactionDate;
		this.allowedOffices = allowedOffices;
		this.currencyOptions = currencyOptions;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public List<CurrencyData> getCurrencyOptions() {
		return currencyOptions;
	}

	public List<OfficeLookup> getAllowedOffices() {
		return allowedOffices;
	}
}