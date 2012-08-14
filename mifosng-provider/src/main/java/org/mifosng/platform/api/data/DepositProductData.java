package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

/**
 * Immutable data object representing details of a deposit product.
 */
public class DepositProductData {
	
	private final Long id;
	private final String name;
	private final String description;
	private final String currencyCode;
	private final Integer digitsAfterDecimal;
	private final BigDecimal minimumBalance;
	private final BigDecimal maximumBalance;
	private final Integer tenureInMonths;
	private final BigDecimal maturityDefaultInterestRate;
	private final BigDecimal maturityMinInterestRate;
	private final BigDecimal maturityMaxInterestRate;
	private final boolean renewalAllowed;
	private final boolean preClosureAllowed;
	private final BigDecimal preClosureInterestRate;
	
	private final DateTime createdOn;
	private final DateTime lastModifedOn;
	
	private final List<CurrencyData> currencyOptions;
	
	public DepositProductData(
			final DateTime createdOn, 
			final DateTime lastModifedOn, 
			final Long id,
			final String name, 
			final String description, 
			final String currencyCode, 
			final Integer digitsAfterDecimal,
			final BigDecimal minimumBalance,
			final BigDecimal maximumBalance,
			final Integer tenureMonths, 
			final BigDecimal maturityDefaultInterestRate, 
			final BigDecimal maturityMinInterestRate, 
			final BigDecimal maturityMaxInterestRate,
			final boolean renewalAllowed, 
			final boolean preClosureAllowed, 
			final BigDecimal preClosureInterestRate) {
		
		this.createdOn=createdOn;
		this.lastModifedOn=lastModifedOn;
		this.id=id;
		this.name=name;
		this.description=description;
		this.currencyCode=currencyCode;
		this.digitsAfterDecimal=digitsAfterDecimal;
		this.minimumBalance=minimumBalance;
		this.maximumBalance=maximumBalance;
		
		this.tenureInMonths=tenureMonths;
		this.maturityDefaultInterestRate = maturityDefaultInterestRate;
		this.maturityMinInterestRate=maturityMinInterestRate;
		this.maturityMaxInterestRate=maturityMaxInterestRate;
		this.renewalAllowed=renewalAllowed;
		this.preClosureAllowed=preClosureAllowed;
		this.preClosureInterestRate=preClosureInterestRate;
		
		this.currencyOptions = new ArrayList<CurrencyData>();
	}
	
	public DepositProductData(final List<CurrencyData> currencyOptions) {
		this.createdOn=null;
		this.lastModifedOn=null;
		this.id=null;
		this.name=null;
		this.description=null;
		this.currencyCode=null;
		this.digitsAfterDecimal=Integer.valueOf(0);
		this.minimumBalance=BigDecimal.ZERO;
		this.maximumBalance=null;
		
		this.tenureInMonths=Integer.valueOf(0);
		this.maturityDefaultInterestRate = null;
		this.maturityMinInterestRate=BigDecimal.ZERO;
		this.maturityMaxInterestRate=BigDecimal.ZERO;
		this.renewalAllowed=true;
		this.preClosureAllowed=true;
		this.preClosureInterestRate=BigDecimal.ZERO;
		
		this.currencyOptions = currencyOptions;
	}
	
	public DepositProductData(final DepositProductData product, final List<CurrencyData> currencyOptions) {
		this.createdOn=product.getCreatedOn();
		this.lastModifedOn=product.getLastModifedOn();
		this.id=product.getId();
		this.name=product.getName();
		this.description=product.getDescription();
		this.currencyCode=product.getCurrencyCode();
		this.digitsAfterDecimal=product.getDigitsAfterDecimal();
		this.minimumBalance=product.getMinimumBalance();
		this.maximumBalance=product.getMaximumBalance();
		
		this.tenureInMonths=product.getTenureInMonths();
		this.maturityDefaultInterestRate = product.getMaturityDefaultInterestRate();
		this.maturityMinInterestRate=product.getMaturityMinInterestRate();
		this.maturityMaxInterestRate=product.getMaturityMaxInterestRate();
		this.renewalAllowed=product.isRenewalAllowed();
		this.preClosureAllowed=product.isPreClosureAllowed();
		this.preClosureInterestRate=product.getPreClosureInterestRate();
		
		this.currencyOptions = currencyOptions;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public Integer getDigitsAfterDecimal() {
		return digitsAfterDecimal;
	}

	public BigDecimal getMinimumBalance() {
		return minimumBalance;
	}

	public BigDecimal getMaximumBalance() {
		return maximumBalance;
	}

	public Integer getTenureInMonths() {
		return tenureInMonths;
	}

	public BigDecimal getMaturityDefaultInterestRate() {
		return maturityDefaultInterestRate;
	}

	public BigDecimal getMaturityMinInterestRate() {
		return maturityMinInterestRate;
	}

	public BigDecimal getMaturityMaxInterestRate() {
		return maturityMaxInterestRate;
	}

	public boolean isRenewalAllowed() {
		return renewalAllowed;
	}

	public boolean isPreClosureAllowed() {
		return preClosureAllowed;
	}

	public BigDecimal getPreClosureInterestRate() {
		return preClosureInterestRate;
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