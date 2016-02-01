/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Account with a given GL Code of
 * the particular type is already present
 */
public class GLAccountInvalidDeleteException extends AbstractPlatformDomainRuleException {

    /*** Enum of reasons for invalid delete **/
    public static enum GL_ACCOUNT_INVALID_DELETE_REASON {
        TRANSANCTIONS_LOGGED, HAS_CHILDREN;

        public String errorMessage() {
            if (name().toString().equalsIgnoreCase("TRANSANCTIONS_LOGGED")) {
                return "This GL Account cannot be deleted as it has transactions logged against it";
            } else if (name().toString().equalsIgnoreCase("HAS_CHILDREN")) { return "Cannot delete this Header GL Account without first deleting or reassinging its children"; }
            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("TRANSANCTIONS_LOGGED")) {
                return "error.msg.glaccount.glcode.invalid.delete.transactions.logged";
            } else if (name().toString().equalsIgnoreCase("HAS_CHILDREN")) { return "error.msg.glaccount.glcode.invalid.delete.has.children"; }
            return name().toString();
        }
    }

    public GLAccountInvalidDeleteException(final GL_ACCOUNT_INVALID_DELETE_REASON reason, final Long glAccountId) {
        super(reason.errorCode(), reason.errorMessage(), glAccountId);
    }
}