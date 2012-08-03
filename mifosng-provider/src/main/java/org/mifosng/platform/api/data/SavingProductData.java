package org.mifosng.platform.api.data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.DateTime;

public class SavingProductData implements Serializable {

	private Long id;
	private String name;
	private String description;
	
	private DateTime createdOn;
	private DateTime lastModifedOn;
	
	private String currencyCode;
	private Integer digitsAfterDecimal;
	
	private BigDecimal interestRate = BigDecimal.ZERO;
	
	public SavingProductData(){
		//
	}

	public SavingProductData(DateTime createdOn, DateTime lastModifedOn, Long id,String name, String description,BigDecimal interestRate,String currencyCode, Integer digitsAfterDecimal) {
		this.createdOn=createdOn;
		this.lastModifedOn=lastModifedOn;
		this.id=id;
		this.name=name;
		this.description=description;
		this.interestRate=interestRate;
		this.currencyCode=currencyCode;
		this.digitsAfterDecimal=digitsAfterDecimal;
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

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public BigDecimal getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(BigDecimal interestRate) {
		this.interestRate = interestRate;
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
	
	
	
}
