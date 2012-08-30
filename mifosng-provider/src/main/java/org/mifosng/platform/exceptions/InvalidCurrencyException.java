package org.mifosng.platform.exceptions;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when currency mismatch occurs
 */
public class InvalidCurrencyException extends AbstractPlatformDomainRuleException {

    public InvalidCurrencyException(final String entity, final String postFix, String defaultUserMessage, Object... defaultUserMessageArgs) {
        super("error.msg."+entity+"."+postFix+".invalid.currency", defaultUserMessage, defaultUserMessageArgs);
    }

}
