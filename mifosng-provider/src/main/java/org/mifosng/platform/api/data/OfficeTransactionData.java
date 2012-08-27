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
	private final String currencyCode;
	private final Integer digitsAfterDecimal;
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
		this.currencyCode = null;
		this.digitsAfterDecimal = null;
		this.transactionAmount = null;
		this.description = null;
	}

	public OfficeTransactionData(final Long id,
			final LocalDate transactionDate, final Long fromOfficeId,
			final String fromOfficeName, final Long toOfficeId,
			final String toOfficeName, final String currencyCode,
			final Integer digitsAfterDecimal,
			final BigDecimal transactionAmount, final String description) {
		this.id = id;
		this.fromOfficeId = fromOfficeId;
		this.fromOfficeName = fromOfficeName;
		this.toOfficeId = toOfficeId;
		this.toOfficeName = toOfficeName;
		this.currencyCode = currencyCode;
		this.digitsAfterDecimal = digitsAfterDecimal;
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

	public String getCurrencyCode() {
		return currencyCode;
	}

	public Integer getDigitsAfterDecimal() {
		return digitsAfterDecimal;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public String getDescription() {
		return description;
	}

}