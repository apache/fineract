package org.mifosng.platform.loan.domain;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.configuration.domain.MonetaryCurrency;
import org.mifosplatform.infrastructure.configuration.domain.Money;

public class MoneyBuilder {

	private MonetaryCurrency currencyDetail = new MonetaryCurrencyBuilder().build();
	private BigDecimal newAmount = BigDecimal.ZERO;
	
	public Money build() {
		return Money.of(currencyDetail, newAmount);
	}

	public MoneyBuilder with(MonetaryCurrency withDetail) {
		this.currencyDetail = withDetail;
		return this;
	}
	
	public MoneyBuilder with(String withAmount) {
		this.newAmount = BigDecimal.valueOf(Double.valueOf(withAmount));
		return this;
	}
}