package org.mifosng.platform.api.commands;


public class DepositAccountWithdrawalCommand {
	
	private final Long accountId;
	private final String note;
	
	public DepositAccountWithdrawalCommand(final Long accountId, final String note) {
		this.accountId = accountId;
		this.note = note;
	}

	public Long getAccountId() {
		return accountId;
	}
	
	public String getNote() {
		return note;
	}
}
