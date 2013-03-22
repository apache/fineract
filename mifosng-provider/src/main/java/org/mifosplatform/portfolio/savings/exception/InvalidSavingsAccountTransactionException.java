package org.mifosplatform.portfolio.savings.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * Thrown when an attempt is made to withdraw money that is greater than the
 * account balance.
 */
public class InvalidSavingsAccountTransactionException extends AbstractPlatformDomainRuleException {

    public InvalidSavingsAccountTransactionException(final String errorCode, final String defaultErrorMessage, final String paramName,
            final Object... defaultUserMessageArgs) {
        super("error.msg.savingsaccount.transaction." + errorCode, defaultErrorMessage, paramName, defaultUserMessageArgs);
    }
}