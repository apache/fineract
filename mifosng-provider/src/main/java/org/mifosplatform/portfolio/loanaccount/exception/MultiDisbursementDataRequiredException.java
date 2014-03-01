package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class MultiDisbursementDataRequiredException extends AbstractPlatformDomainRuleException {

    public MultiDisbursementDataRequiredException(final String entity, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        super("error.msg." + entity + ".required", defaultUserMessage, defaultUserMessageArgs);
    }

}
