package org.mifosplatform.accounting.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when trying to fetch accounts belonging to
 * an Invalid Usage Type
 */
public class GLAccountInvalidClassificationException extends AbstractPlatformDomainRuleException {

    public GLAccountInvalidClassificationException(final Integer usage) {
        super("error.msg.glaccount.usage.invalid", "The following COA usage is invalid: " + usage);
    }
}