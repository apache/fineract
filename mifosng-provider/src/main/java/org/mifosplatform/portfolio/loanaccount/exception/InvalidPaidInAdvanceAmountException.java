/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown an action to transition a
 * loan from one state to another violates a domain rule.
 */
public class InvalidPaidInAdvanceAmountException extends AbstractPlatformDomainRuleException {
    
    public InvalidPaidInAdvanceAmountException(final String refundAmountString) {
        super("error.msg.loan.refund.amount.invalid", "The refund amount `" + refundAmountString + "`"
                + "` is invalid or loan is not paid in advance.", new Object[] { refundAmountString});
    }

}