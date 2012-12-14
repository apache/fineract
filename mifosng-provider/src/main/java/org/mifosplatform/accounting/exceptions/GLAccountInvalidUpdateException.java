package org.mifosplatform.accounting.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when an error is encountered during
 * updating a GL Account
 */
public class GLAccountInvalidUpdateException extends AbstractPlatformDomainRuleException {

    /*** Enum of reasons for invalid delete **/
    public static enum GL_ACCOUNT_INVALID_UPDATE_REASON {
        TRANSANCTIONS_LOGGED;

        public String errorMessage() {
            if (name().toString().equalsIgnoreCase("TRANSANCTIONS_LOGGED")) { return "This Usage of this (detail) GL Account as it already has transactions logged against it"; }
            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("TRANSANCTIONS_LOGGED")) { return "error.msg.glaccount.glcode.invalid.update.transactions.logged"; }
            return name().toString();
        }
    }

    public GLAccountInvalidUpdateException(final GL_ACCOUNT_INVALID_UPDATE_REASON reason, final Long glAccountId) {
        super(reason.errorCode(), reason.errorMessage(), glAccountId);
    }
}