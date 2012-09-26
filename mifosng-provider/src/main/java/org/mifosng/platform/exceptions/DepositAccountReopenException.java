package org.mifosng.platform.exceptions;

import org.joda.time.LocalDate;

public class DepositAccountReopenException extends AbstractPlatformDomainRuleException{

	public DepositAccountReopenException(LocalDate maturedDate) {
		super("error.msg.depositaccount.cannot.reopen.before.mature", "You cannot renew the account that is not matured:"+maturedDate);
	}

}
