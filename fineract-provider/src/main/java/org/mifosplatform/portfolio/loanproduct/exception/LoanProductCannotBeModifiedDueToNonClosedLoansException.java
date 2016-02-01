/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LoanProductCannotBeModifiedDueToNonClosedLoansException  extends AbstractPlatformDomainRuleException {
    public LoanProductCannotBeModifiedDueToNonClosedLoansException(final Long id) {
        super("error.msg.loanproduct.not.modifiable.due.to.non.closed.loans", 
        		"Loan product with identifier " + id + " cannot be modified due to non closed loans associated with it.", id);
    }
}
