package org.mifosng.platform.api.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonFilter;
import org.joda.time.LocalDate;

@JsonFilter("myFilter")
public class OfficeTransactionData implements Serializable {

	private LocalDate transactionDate;
	private List<CurrencyData> currencyOptions = new ArrayList<CurrencyData>();
	private List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>();

	public OfficeTransactionData() {
		//
	}

	public OfficeTransactionData(final LocalDate transactionDate) {
		this.transactionDate = transactionDate;
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