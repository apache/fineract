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
	
	@Column(name = "minimum_balance", scale = 6, precision = 19, nullable = false)
	private BigDecimal minimumBalance;
	
	@Column(name = "maximum_balance", scale = 6, precision = 19, nullable = false)
	private BigDecimal maximumBalance;
	
	public SavingProductRelatedDetail() {
		this.interestRate=null;
		this.maximumBalance=null;
		this.minimumBalance=null;
	}
	
	public SavingProductRelatedDetail(final MonetaryCurrency currency,final BigDecimal interestRate, final BigDecimal minimumBalance,final BigDecimal maximumBalance) {
		this.currency=currency;
		this.interestRate=interestRate;
		this.minimumBalance=minimumBalance;
		this.maximumBalance=maximumBalance;
	}
	
	public MonetaryCurrency getCurrency() {
		return this.currency.copy();
	}

	public BigDecimal getInterestRate() {
		return BigDecimal.valueOf(Double.valueOf(this.interestRate.stripTrailingZeros().toString()));
	}
	
	public BigDecimal getMinimumBalance(){
		return BigDecimal.valueOf(Double.valueOf(this.minimumBalance.stripTrailingZeros().toString()));
	}
	
	public BigDecimal getMaximumBalance(){
		return BigDecimal.valueOf(Double.valueOf(this.maximumBalance.stripTrailingZeros().toString()));
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
		
		if(command.isMaximumBalanceChanged()){
			this.minimumBalance=command.getMinimumBalance();
		}
		
		if(command.isMaximumBalanceChanged()){
			this.maximumBalance=command.getMaximumBalance();
		}
	}
}
