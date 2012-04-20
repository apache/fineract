package org.mifosng.ui.loan;

import java.io.Serializable;
import java.math.BigDecimal;

public class UnitInformationOfProduct implements Serializable {

	private String name;
	private Number unitsSold = Integer.valueOf(0);
	private Number costPerUnit = BigDecimal.ZERO;
	private Number pricePerUnit = BigDecimal.ZERO;

	public UnitInformationOfProduct() {
		//
	}
	
	public UnitInformationOfProduct(String name, Integer unitsSold,
			BigDecimal costPerUnit, BigDecimal pricePerUnit) {
		this.name = name;
		this.unitsSold = unitsSold;
		this.costPerUnit = costPerUnit;
		this.pricePerUnit = pricePerUnit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getUnitsSold() {
		return unitsSold;
	}

	public void setUnitsSold(Number unitsSold) {
		this.unitsSold = unitsSold;
	}

	public Number getCostPerUnit() {
		return costPerUnit;
	}

	public void setCostPerUnit(Number costPerUnit) {
		this.costPerUnit = costPerUnit;
	}

	public Number getPricePerUnit() {
		return pricePerUnit;
	}

	public void setPricePerUnit(Number pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}

}
