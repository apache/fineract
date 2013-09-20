package org.mifosplatform.portfolio.savings.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SavingsAccountClosingNotAllowedException extends AbstractPlatformDomainRuleException {

    public SavingsAccountClosingNotAllowedException(final String entity, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        super("error.msg." + entity + ".saving.account.close.notallowed", defaultUserMessage, defaultUserMessageArgs);
    }

}
