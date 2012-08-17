package org.mifosng.platform.api.data;

import java.io.Serializable;

public class CurrencyData implements Serializable {

	private final String code;
	private final String name;
	private final int decimalPlaces;
	private final String displaySymbol;
	private final String nameCode;
	private final String displayLabel;

	public CurrencyData(final String code, final String name, final int decimalPlaces, final String displaySymbol, final String nameCode) {
		this.code = code;
		this.name = name;
		this.decimalPlaces = decimalPlaces;
		this.displaySymbol = displaySymbol;
		this.nameCode = nameCode;
		this.displayLabel = generateDisplayLabel();
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
	
	private String generateDisplayLabel() {
		
		StringBuilder builder = new StringBuilder(this.name).append(' ');
		
		if (this.displaySymbol != null && !"".equalsIgnoreCase(displaySymbol.trim())) {
			builder.append('(').append(this.displaySymbol).append(')');
		} else {
			builder.append('[').append(this.code).append(']');
		}
		
		return builder.toString();
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	public String getDisplaySymbol() {
		return displaySymbol;
	}

	public String getNameCode() {
		return nameCode;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}
}