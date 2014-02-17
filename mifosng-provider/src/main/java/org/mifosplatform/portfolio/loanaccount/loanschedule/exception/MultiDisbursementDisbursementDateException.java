package org.mifosplatform.portfolio.loanaccount.loanschedule.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class MultiDisbursementDisbursementDateException extends AbstractPlatformDomainRuleException {

    public MultiDisbursementDisbursementDateException(final String defaultUserMessage, final Object... defaultUserMessageArgs) {
        super("error.msg.loanschedule.out.of.schedule.dusbursement.date", defaultUserMessage, defaultUserMessageArgs);
    }

}
