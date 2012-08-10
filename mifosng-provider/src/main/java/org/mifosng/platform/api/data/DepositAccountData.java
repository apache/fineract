package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

/**
 * Immutable data object for deposit accounts.
 */
public class DepositAccountData {

	private final Long id;
	
	private final Long clientId;
	private final String clientName;
	
	private final Long productId;
	private final String productName;
	
	private final CurrencyData currency;
	private final BigDecimal deposit;
	private final BigDecimal maturityInterestRate;
	
	private final DateTime createdOn;
	private final DateTime lastModifedOn;
	
	private final List<CurrencyData> currencyOptions;

	public DepositAccountData() {
		this.createdOn = null;
		this.lastModifedOn = null;
		this.id = null;
		this.clientId = null;
		this.clientName = null;
		this.productId = null;
		this.productName = null;
		this.currency = null;
		this.deposit = null;
		this.maturityInterestRate = null;
		this.currencyOptions = new ArrayList<CurrencyData>();
	}
	
	public DepositAccountData(final DepositAccountData account, final List<CurrencyData> currencies) {
		this.createdOn = account.getCreatedOn();
		this.lastModifedOn = account.getLastModifedOn();
		this.id = account.getId();
		this.clientId = account.getClientId();
		this.clientName = account.getClientName();
		this.productId = account.getProductId();
		this.productName = account.getProductName();
		this.currency = account.getCurrency();
		this.deposit = account.getDeposit();
		this.maturityInterestRate = account.getMaturityInterestRate();
		this.currencyOptions = currencies;
	}
	
	public DepositAccountData(
			final DateTime createdOn, 
			final DateTime lastModifedOn, 
			final Long id,
			final Long clientId, 
			final String clientName, 
			final Long productId, 
			final String productName, 
			final CurrencyData currency,
			final BigDecimal deposit, BigDecimal interestRate) {
		this.createdOn=createdOn;
		this.lastModifedOn=lastModifedOn;
		this.id=id;
		this.clientId = clientId;
		this.clientName = clientName;
		this.productId = productId;
		this.productName = productName;
		this.currency = currency;
		this.deposit = deposit;
		this.maturityInterestRate=interestRate;
		this.currencyOptions = new ArrayList<CurrencyData>();
	}

	public Long getId() {
		return id;
	}
	
	public Long getClientId() {
		return clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public Long getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public CurrencyData getCurrency() {
		return currency;
	}

	public BigDecimal getDeposit() {
		return deposit;
	}
	
	public BigDecimal getMaturityInterestRate() {
		return maturityInterestRate;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public DateTime getLastModifedOn() {
		return lastModifedOn;
	}

	public List<CurrencyData> getCurrencyOptions() {
		return currencyOptions;
	}
}