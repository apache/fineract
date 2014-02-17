package org.mifosplatform.portfolio.loanaccount.loanschedule.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class MultiDisbursementOutstandingAmoutException extends AbstractPlatformDomainRuleException {

    public MultiDisbursementOutstandingAmoutException(final String defaultUserMessage, final Object... defaultUserMessageArgs) {
        super("error.msg.loanschedule.exceeding.max.outstanding.balance", defaultUserMessage, defaultUserMessageArgs);
    }

}
