package org.mifosplatform.portfolio.savingsdepositaccount.exception;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DepositAccountReopenException extends AbstractPlatformDomainRuleException {

    public DepositAccountReopenException(LocalDate maturedDate) {
        super("error.msg.depositaccount.cannot.reopen.before.mature", "You cannot renew the account that is not matured:" + maturedDate);
    }
}
