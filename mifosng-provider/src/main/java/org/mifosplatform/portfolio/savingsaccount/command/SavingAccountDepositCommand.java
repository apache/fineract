/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.command;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class SavingAccountDepositCommand {
	
	private final Long accountId;
    private final BigDecimal savingsDepostiAmountPerPeriod;
    private final LocalDate depositDate;
    private final String note;
    
    public SavingAccountDepositCommand(final Long accountId, final BigDecimal savingsDepostiAmountPerPeriod, final LocalDate depositDate, final String note) {
		this.accountId = accountId;
		this.savingsDepostiAmountPerPeriod = savingsDepostiAmountPerPeriod;
		this.depositDate = depositDate;
		this.note = note;
	}

	public Long getAccountId() {
		return this.accountId;
	}

	public BigDecimal getSavingsDepostiAmountPerPeriod() {
		return this.savingsDepostiAmountPerPeriod;
	}

	public LocalDate getDepositDate() {
		return this.depositDate;
	}

	public String getNote() {
		return this.note;
	}
    
}
