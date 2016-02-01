/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class InvalidRefundDateException extends AbstractPlatformDomainRuleException {

    public InvalidRefundDateException(final String refundDateAsString) {
        super("error.msg.loan.refund.failed", "The refund date`" + refundDateAsString + "`"
                + "` cannot be before the smallest repayment transaction date", new Object[] { refundDateAsString});
    }

    public InvalidRefundDateException(final String defaultUserMessage,final String entity,final Object... defaultUserMessageArgs) {
        super("error.msg.loan." + entity , defaultUserMessage, defaultUserMessageArgs);
    }
}
