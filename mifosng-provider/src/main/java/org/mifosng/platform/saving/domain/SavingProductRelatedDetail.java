package org.mifosng.platform.saving.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.currency.domain.MonetaryCurrency;

@Embeddable
public class SavingProductRelatedDetail {
	
	@Embedded
	private MonetaryCurrency currency;
	
	@Column(name = "interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal interestRate;
	
	public SavingProductRelatedDetail() {
		this.interestRate=null;
	}
	
	public SavingProductRelatedDetail(final MonetaryCurrency currency,final BigDecimal interestRate) {
		this.currency=currency;
		this.interestRate=interestRate;
	}
	
	public MonetaryCurrency getCurrency() {
		return this.currency.copy();
	}

	public BigDecimal getInterestRate() {
		return BigDecimal.valueOf(Double.valueOf(this.interestRate.stripTrailingZeros().toString()));
	}
	
	public void update(SavingProductCommand command){
		Integer digitsAfterDecimalChanged = this.currency.getDigitsAfterDecimal();
		if (command.isDigitsAfterDecimalChanged()) {
			digitsAfterDecimalChanged = command.getDigitsAfterDecimal();
		}
		
		String currencyCodeChanged = this.currency.getCode();
		if (command.isCurrencyCodeChanged()) {
			currencyCodeChanged = command.getCurrencyCode();
		}
		
		if (command.isDigitsAfterDecimalChanged() || command.isCurrencyCodeChanged()) {
			this.currency = new MonetaryCurrency(currencyCodeChanged, digitsAfterDecimalChanged);
		}
		
		if (command.isInterestRateChanged()) {
			this.interestRate = command.getInterestRate();
		}
	}
}
