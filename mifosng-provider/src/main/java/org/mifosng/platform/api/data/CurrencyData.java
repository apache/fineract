package org.mifosng.platform.api.data;

import java.io.Serializable;

public class CurrencyData implements Serializable {

	private String code;
	private String name;
	private int decimalPlaces;
	private String displaySymbol;
	private String nameCode;

	public CurrencyData() {
		//
	}

	public CurrencyData(final String code, final String name, final int decimalPlaces, String displaySymbol, String nameCode) {
		this.code = code;
		this.name = name;
		this.decimalPlaces = decimalPlaces;
		this.displaySymbol = displaySymbol;
		this.nameCode = nameCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		CurrencyData currencyData = (CurrencyData) obj;
		return currencyData.code.equals(this.code);
	}
	
	@Override
	public int hashCode() {
		return this.code.hashCode();
	}
	
	public String getDisplayLabel() {
		
		StringBuilder builder = new StringBuilder(this.name).append(' ');
		
		if (this.displaySymbol != null && !"".equalsIgnoreCase(displaySymbol.trim())) {
			builder.append('(').append(this.displaySymbol).append(')');
		} else {
			builder.append('[').append(this.code).append(']');
		}
		
		return builder.toString();
	}

	public String getName() {
		return this.name;
	}

	public String getCode() {
		return this.code;
	}

	public int getDecimalPlaces() {
		return this.decimalPlaces;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setDecimalPlaces(final int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public String getDisplaySymbol() {
		return displaySymbol;
	}

	public void setDisplaySymbol(String displaySymbol) {
		this.displaySymbol = displaySymbol;
	}

	public String getNameCode() {
		return nameCode;
	}

	public void setNameCode(String nameCode) {
		this.nameCode = nameCode;
	}
}