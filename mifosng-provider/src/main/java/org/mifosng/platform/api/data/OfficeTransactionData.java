package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

public class OfficeTransactionData {

	private LocalDate transactionDate;
	private List<CurrencyData> currencyOptions = new ArrayList<CurrencyData>();
	private List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>();

	public OfficeTransactionData(final LocalDate transactionDate, final List<OfficeLookup> allowedOffices, final List<CurrencyData> currencyOptions) {
		this.transactionDate = transactionDate;
		this.allowedOffices = allowedOffices;
		this.currencyOptions = currencyOptions;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}

	public List<CurrencyData> getCurrencyOptions() {
		return currencyOptions;
	}

	public void setCurrencyOptions(List<CurrencyData> currencyOptions) {
		this.currencyOptions = currencyOptions;
	}

	public List<OfficeLookup> getAllowedOffices() {
		return allowedOffices;
	}

	public void setAllowedOffices(List<OfficeLookup> allowedOffices) {
		this.allowedOffices = allowedOffices;
	}

}