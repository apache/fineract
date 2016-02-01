/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LoanProductDateException extends AbstractPlatformDomainRuleException {

    public LoanProductDateException(final Object... defaultUserMessageArgs) {
        super("error.msg.loan.product.close.date.cannot.be.before.start.date.close.date",
                "Loan product close date cannot be before the start date", defaultUserMessageArgs);
    }

}
