package org.mifosplatform.portfolio.loanaccount.loanschedule.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class MultiDisbursementEmiAmountException extends AbstractPlatformDomainRuleException {

    public MultiDisbursementEmiAmountException(final String defaultUserMessage, final Object... defaultUserMessageArgs) {
        super("error.msg.loanschedule.emi.amount.must.be.greter.than.interest", defaultUserMessage, defaultUserMessageArgs);
    }

}
