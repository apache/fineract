package org.mifosng.platform.api.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class DepositProductData implements Serializable {
	
	private Long id;
	private String name;
	private String description;
	private String currencyCode;
	private Integer digitsAfterDecimal;
	private BigDecimal minimumBalance = BigDecimal.ZERO;
	private BigDecimal maximumBalance = BigDecimal.ZERO;
	private Integer tenureMonths;
	private BigDecimal maturityDefaultInterestRate = BigDecimal.ZERO;
	private BigDecimal maturityMinInterestRate = BigDecimal.ZERO;
	private BigDecimal maturityMaxInterestRate = BigDecimal.ZERO;
	private Boolean canRenew;
	private Boolean canPreClose;
	private BigDecimal preClosureInterestRate = BigDecimal.ZERO;
	private DateTime createdOn;
	private DateTime lastModifedOn;
	
	private List<CurrencyData> currencyOptions = new ArrayList<CurrencyData>();
	
	public DepositProductData() {
		//
	}
	
	public DepositProductData(DateTime createdOn, DateTime lastModifedOn, Long id,String name, String description, String currencyCode, Integer digitsAfterDecimal,BigDecimal minimumBalance,BigDecimal maximumBalance,
			Integer tenureMonths, BigDecimal maturityDefaultInterestRate, BigDecimal maturityMinInterestRate, BigDecimal maturityMaxInterestRate,
			Boolean canRenew, Boolean canPreClose, BigDecimal preClosureInterestRate) {
		
		this.createdOn=createdOn;
		this.lastModifedOn=lastModifedOn;
		this.id=id;
		this.name=name;
		this.description=description;
		this.currencyCode=currencyCode;
		this.digitsAfterDecimal=digitsAfterDecimal;
		this.minimumBalance=minimumBalance;
		this.maximumBalance=maximumBalance;
		
		this.tenureMonths=tenureMonths;
		this.maturityDefaultInterestRate = maturityDefaultInterestRate;
		this.maturityMinInterestRate=maturityMinInterestRate;
		this.maturityMaxInterestRate=maturityMaxInterestRate;
		this.canRenew=canRenew;
		this.canPreClose=canPreClose;
		this.preClosureInterestRate=preClosureInterestRate;
	
	}
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public Integer getDigitsAfterDecimal() {
		return digitsAfterDecimal;
	}

	public void setDigitsAfterDecimal(Integer digitsAfterDecimal) {
		this.digitsAfterDecimal = digitsAfterDecimal;
	}

	public BigDecimal getMinimumBalance() {
		return minimumBalance;
	}

	public void setMinimumBalance(BigDecimal minimumBalance) {
		this.minimumBalance = minimumBalance;
	}

	public BigDecimal getMaximumBalance() {
		return maximumBalance;
	}

	public void setMaximumBalance(BigDecimal maximumBalance) {
		this.maximumBalance = maximumBalance;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public DateTime getLastModifedOn() {
		return lastModifedOn;
	}

	public void setLastModifedOn(DateTime lastModifedOn) {
		this.lastModifedOn = lastModifedOn;
	}

	public List<CurrencyData> getCurrencyOptions() {
		return currencyOptions;
	}

	public void setCurrencyOptions(List<CurrencyData> currencyOptions) {
		this.currencyOptions = currencyOptions;
	}

	public Integer getTenureMonths() {
		return tenureMonths;
	}

	public void setTenureMonths(Integer tenureMonths) {
		this.tenureMonths = tenureMonths;
	}

	public BigDecimal getMaturityDefaultInterestRate() {
		return maturityDefaultInterestRate;
	}

	public void setMaturityDefaultInterestRate(
			BigDecimal maturityDefaultInterestRate) {
		this.maturityDefaultInterestRate = maturityDefaultInterestRate;
	}

	public BigDecimal getMaturityMinInterestRate() {
		return maturityMinInterestRate;
	}

	public void setMaturityMinInterestRate(BigDecimal maturityMinInterestRate) {
		this.maturityMinInterestRate = maturityMinInterestRate;
	}

	public BigDecimal getMaturityMaxInterestRate() {
		return maturityMaxInterestRate;
	}

	public void setMaturityMaxInterestRate(BigDecimal maturityMaxInterestRate) {
		this.maturityMaxInterestRate = maturityMaxInterestRate;
	}

	public Boolean getCanRenew() {
		return canRenew;
	}

	public void setCanRenew(Boolean canRenew) {
		this.canRenew = canRenew;
	}

	public Boolean getCanPreClose() {
		return canPreClose;
	}

	public void setCanPreClose(Boolean canPreClose) {
		this.canPreClose = canPreClose;
	}

	public BigDecimal getPreClosureInterestRate() {
		return preClosureInterestRate;
	}

	public void setPreClosureInterestRate(BigDecimal preClosureInterestRate) {
		this.preClosureInterestRate = preClosureInterestRate;
	}
}
