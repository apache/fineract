package org.mifosplatform.accounting.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when trying to fetch accounts belonging to
 * an Invalid Type
 */
public class GLAccountInvalidUsageException extends AbstractPlatformDomainRuleException {

    public GLAccountInvalidUsageException(final Integer type) {
        super("error.msg.glaccount.classification.invalid", "The following COA type is invalid: " + type);
    }
}