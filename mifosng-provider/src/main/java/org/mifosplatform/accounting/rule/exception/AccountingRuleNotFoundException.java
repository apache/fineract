/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when Accounting rule resources are not
 * found.
 */
public class AccountingRuleNotFoundException extends AbstractPlatformResourceNotFoundException {

    public AccountingRuleNotFoundException(final Long id) {
        super("error.msg.accounting.rule.id.invalid", "Accounting Rule with identifier " + id + " does not exist", id);
    }
}