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
public class GLAccountDuplicateException extends AbstractPlatformDomainRuleException {

    public GLAccountDuplicateException(final String glCode) {
        super("error.msg.glaccount.glcode.duplicate", "General Ledger Account with GL code " + glCode + " is already present", glCode);
    }

}