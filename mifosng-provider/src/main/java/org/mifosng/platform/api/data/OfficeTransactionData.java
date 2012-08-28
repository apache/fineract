package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;

/**
 * Immutable data object for office transactions.
 */
public class OfficeTransactionData {

	private final Long id;
	private final LocalDate transactionDate;
	private final Long fromOfficeId;
	private final String fromOfficeName;
	private final Long toOfficeId;
	private final String toOfficeName;
	private CurrencyData currency;
	private final BigDecimal transactionAmount;
	private final String description;
	private final List<CurrencyData> currencyOptions;
	private final List<OfficeLookup> allowedOffices;

	public OfficeTransactionData(final LocalDate transactionDate,
			final List<OfficeLookup> allowedOffices,
			final List<CurrencyData> currencyOptions) {
		this.transactionDate = transactionDate;
		this.allowedOffices = allowedOffices;
		this.currencyOptions = currencyOptions;
		this.id = null;
		this.fromOfficeId = null;
		this.fromOfficeName = null;
		this.toOfficeId = null;
		this.toOfficeName = null;
		this.currency = null;
		this.transactionAmount = null;
		this.description = null;
	}

	public OfficeTransactionData(final Long id,
			final LocalDate transactionDate, final Long fromOfficeId,
			final String fromOfficeName, final Long toOfficeId,
			final String toOfficeName, final CurrencyData currency,
			final BigDecimal transactionAmount, final String description) {
		this.id = id;
		this.fromOfficeId = fromOfficeId;
		this.fromOfficeName = fromOfficeName;
		this.toOfficeId = toOfficeId;
		this.toOfficeName = toOfficeName;
		this.currency = currency;
		this.transactionAmount = transactionAmount;
		this.description = description;
		this.transactionDate = transactionDate;
		this.currencyOptions = null;
		this.allowedOffices = null;

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

	public Long getId() {
		return id;
	}

	public Long getFromOfficeId() {
		return fromOfficeId;
	}

	public String getFromOfficeName() {
		return fromOfficeName;
	}

	public Long getToOfficeId() {
		return toOfficeId;
	}

	public String getToOfficeName() {
		return toOfficeName;
	}

	public CurrencyData getCurrency() {
		return currency;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public String getDescription() {
		return description;
	}

}