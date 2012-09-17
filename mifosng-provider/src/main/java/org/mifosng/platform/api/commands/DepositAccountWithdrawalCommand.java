package org.mifosng.platform.api.commands;

import java.math.BigDecimal;

public class DepositAccountWithdrawalCommand {
	
	private final Long accountId;
	private final boolean renewAccount;
	private final BigDecimal deposit;
	
	private final String note;
	
	public DepositAccountWithdrawalCommand(final Long accountId, final boolean renewAccount, final BigDecimal deposit, final String note) {
		this.accountId = accountId;
		this.renewAccount = renewAccount;
		this.deposit = deposit;
		this.note = note;
	}

	public Long getAccountId() {
		return accountId;
	}

	public boolean isRenewAccount() {
		return renewAccount;
	}
	
	public String getNote() {
		return note;
	}

	public BigDecimal getDeposit() {
		return deposit;
	}
}
