package org.mifosplatform.accounting.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when trying to map invalid parents to a GL
 * account
 */
public class GLAccountInvalidParentException extends AbstractPlatformDomainRuleException {

    public GLAccountInvalidParentException(long glAccountId) {
        super("error.msg.glaccount.parent.invalid", "The account with id " + glAccountId
                + " is a 'Detail' account and cannot be used as a parent", glAccountId);
    }
}