package org.mifosplatform.accounting.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Account with a given GL Code of
 * the particular type is already present
 */
public class GLAccountDuplicateException extends AbstractPlatformDomainRuleException {

    public GLAccountDuplicateException(final String glCode) {
        super("error.msg.glaccount.glcode.duplicate", "General Ledger Account with GL code " + glCode + " is already present", glCode);
    }

}