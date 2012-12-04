package org.mifosplatform.portfolio.savingsaccount.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.organisation.monetary.data.CurrencyData;

public class SavingScheduleData {
	
	private final CurrencyData currency;
	private final BigDecimal cumulativeDepositDue;
	private final Collection<SavingSchedulePeriodData> periods;
	
	public SavingScheduleData(final CurrencyData currency, final BigDecimal cumulativeDepositDue,
			final Collection<SavingSchedulePeriodData> periods) {
		this.currency = currency;
		this.cumulativeDepositDue= cumulativeDepositDue;
		this.periods= periods;
		
	}

	public CurrencyData getCurrency() {
		return this.currency;
	}

	public BigDecimal getCumulativeDepositDue() {
		return this.cumulativeDepositDue;
	}
	
	public Collection<SavingSchedulePeriodData> getPeriods() {
		return this.periods;
	}

}
