package org.mifosplatform.portfolio.savingsdepositaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class InvalidDepositStateTransitionException extends AbstractPlatformDomainRuleException {

    public InvalidDepositStateTransitionException(final String action, String postFix, String defaultUserMessage,
            Object... defaultUserMessageArgs) {
        super("error.msg.deposit." + action + "." + postFix, defaultUserMessage, defaultUserMessageArgs);
    }
}