/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown an action to transition a
 * loan from one state to another violates a domain rule.
 */
public class SavingsActivityPriorToClientTransferException extends AbstractPlatformDomainRuleException {

    public SavingsActivityPriorToClientTransferException(final String action, final Object... defaultUserMessageArgs) {
        super("error.msg.savings." + action + "." + "not.permitted.before.client.transfer.date",
                "Transactions on savings account prior to the customer joining date are not permitted", defaultUserMessageArgs);
    }

}