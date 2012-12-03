package org.mifosplatform.organisation.office.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when office mismatch occurs
 */
public class InvalidOfficeException extends AbstractPlatformDomainRuleException {

    public InvalidOfficeException(final String entity, final String postFix, String defaultUserMessage, Object... defaultUserMessageArgs) {
        super("error.msg."+entity+"."+postFix+".invalid.office", defaultUserMessage, defaultUserMessageArgs);
    }
}
