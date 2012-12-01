package org.mifosplatform.accounting.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when trying to fetch accounts belonging to
 * an Invalid Type
 */
public class GLAccountInvalidClassificationException extends AbstractPlatformDomainRuleException {

    public GLAccountInvalidClassificationException(final String classification) {
        super("error.msg.glaccount.classification.invalid", "The following COA classification is invalid: " + classification);
    }
}