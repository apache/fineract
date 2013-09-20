package org.mifosplatform.portfolio.loanaccount.guarantor.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DuplicateGuarantorException extends AbstractPlatformDomainRuleException {

    public DuplicateGuarantorException(final String action, final String postFix, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        super("error.msg." + action + "." + postFix, defaultUserMessage, defaultUserMessageArgs);
        // TODO Auto-generated constructor stub
    }
}
