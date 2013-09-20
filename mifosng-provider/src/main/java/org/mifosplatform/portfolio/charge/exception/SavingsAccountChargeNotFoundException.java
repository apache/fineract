/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when savings account
 * charge does not exist.
 */
public class SavingsAccountChargeNotFoundException extends AbstractPlatformResourceNotFoundException {

    public SavingsAccountChargeNotFoundException(final Long id) {
        super("error.msg.savings.account.charge.id.invalid", "Savings Account charge with identifier " + id + " does not exist", id);
    }

    public SavingsAccountChargeNotFoundException(final Long id, final Long savingsAccountId) {
        super("error.msg.savings.account.charge.id.invalid.for.given.savings.account", "Savings Account charge with identifier " + id
                + " does not exist for Savings Account " + savingsAccountId, id, savingsAccountId);
    }
}