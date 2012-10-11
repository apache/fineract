package org.mifosng.platform.api.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @deprecated - Want to remove this and its usage around loans in full. In its place we just pass back a {@link BigDecimal} for the amount and a {@link CurrencyData} is also passed back.
 */
@Deprecated
public class MoneyData implements Serializable, Comparable<MoneyData> {

	private String currencyCode;
	private Integer digitsAfterDecimal;
	private BigDecimal amount;
	private String defaultName;
	private String nameCode;
	private String displaySymbol;
	
	public static MoneyData of(final CurrencyData currency, final BigDecimal amount) {
		return new MoneyData(currency.getCode(), currency.getDecimalPlaces(), currency.getName(), currency.getNameCode(), currency.getDisplaySymbol(), amount);
	}
	
	public static MoneyData zero(final CurrencyData currency) {
		return new MoneyData(currency.getCode(), currency.getDecimalPlaces(), currency.getName(), currency.getNameCode(), currency.getDisplaySymbol(), BigDecimal.ZERO);
	}
	
	protected MoneyData() {
		//
	}

	private MoneyData(final String currencyCode,
			final int currencyDigitsAfterDecimal, 
			String defaultName, String nameCode, String displaySymbol,
			final BigDecimal amount) {
		this.currencyCode = currencyCode;
		this.digitsAfterDecimal = currencyDigitsAfterDecimal;
		this.defaultName = defaultName;
		this.nameCode = nameCode;
		this.displaySymbol = displaySymbol;
		BigDecimal amountStripped = amount.stripTrailingZeros();
		this.amount = amountStripped.setScale(this.digitsAfterDecimal,
				RoundingMode.HALF_EVEN);
	}
	
	public MoneyData plus(final Iterable<? extends MoneyData> moniesToAdd) {
		BigDecimal total = this.amount;
		for (MoneyData moneyProvider : moniesToAdd) {
			MoneyData money = this.checkCurrencyEqual(moneyProvider);
			total = total.add(money.amount);
		}
		return MoneyData.of(currencyData(this), total);
	}

	public MoneyData plus(final MoneyData moneyToAdd) {
		MoneyData toAdd = this.checkCurrencyEqual(moneyToAdd);
		return this.plus(toAdd.getAmount());
	}

	public MoneyData plus(final BigDecimal amountToAdd) {
		if (amountToAdd.compareTo(BigDecimal.ZERO) == 0) {
			return this;
		}
		BigDecimal newAmount = this.amount.add(amountToAdd);
		return MoneyData.of(currencyData(this), newAmount);
	}

	public MoneyData plus(final double amountToAdd) {
		if (amountToAdd == 0) {
			return this;
		}
		BigDecimal newAmount = this.amount.add(BigDecimal.valueOf(amountToAdd));
		return MoneyData.of(currencyData(this), newAmount);
	}
	
	private static CurrencyData currencyData(MoneyData moneyData) {
		String code = moneyData.getCurrencyCode();
		String name = moneyData.getDefaultName();
		int decimalPlaces = moneyData.getDigitsAfterDecimal();
		String displaySymbol = moneyData.getDisplaySymbol();
		String nameCode = moneyData.getNameCode();
		return new CurrencyData(code, name, decimalPlaces, displaySymbol, nameCode);
	}

	private MoneyData checkCurrencyEqual(final MoneyData money) {
		if (this.isSameCurrency(money) == false) {
			throw new UnsupportedOperationException("currencies are different.");
		}
		return money;
	}

	public boolean isSameCurrency(final MoneyData money) {
		return this.currencyCode.equals(money.getCurrencyCode());
	}

	public String getCurrencyCode() {
		return this.currencyCode;
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}
	
	public String getDefaultName() {
		return defaultName;
	}

	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	public String getNameCode() {
		return nameCode;
	}

	public void setNameCode(String nameCode) {
		this.nameCode = nameCode;
	}

	public String getDisplaySymbol() {
		return displaySymbol;
	}

	public void setDisplaySymbol(String displaySymbol) {
		this.displaySymbol = displaySymbol;
	}

	public boolean isGreaterThanZero() {
		return this.isGreaterThan(MoneyData.of(currencyData(this), BigDecimal.ZERO));
	}

	public boolean isGreaterThan(final MoneyData other) {
		return this.compareTo(other) > 0;
	}
	
    public boolean isZero() {
        return this.isEqualTo(MoneyData.zero(currencyData(this)));
    }
    
    public boolean isEqualTo(final MoneyData other) {
        return this.compareTo(other) == 0;
    }

	@Override
	public int compareTo(final MoneyData other) {
		MoneyData otherMoney = other;
		if (this.currencyCode.equals(otherMoney.currencyCode) == false) {
			throw new UnsupportedOperationException(
					"currencies arent different");
		}
		return this.amount.compareTo(otherMoney.amount);
	}
	
	@Override
	public boolean equals(Object obj) {
		MoneyData money = (MoneyData) obj;
		return money.toString().equals(this.toString());
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(this.amount.toPlainString()).toString();
	}
	
	public String getDisplaySymbolValue() {
		return new StringBuilder().append(this.amount.toPlainString()).append(' ').append(this.displaySymbol).toString().trim();
	}

	public Integer getDigitsAfterDecimal() {
		return digitsAfterDecimal;
	}

	public void setDigitsAfterDecimal(Integer digitsAfterDecimal) {
		this.digitsAfterDecimal = digitsAfterDecimal;
	}
}