/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when trying to map invalid parents to a GL
 * account
 */
public class GLAccountInvalidParentException extends AbstractPlatformDomainRuleException {

    public GLAccountInvalidParentException(final long glAccountId) {
        super("error.msg.glaccount.parent.invalid", "The account with id " + glAccountId
                + " is a 'Detail' account and cannot be used as a parent", glAccountId);
    }
}