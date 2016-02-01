/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when loan charge does not
 * exist.
 */
public class LoanChargeNotFoundException extends AbstractPlatformResourceNotFoundException {

    public LoanChargeNotFoundException(final Long id) {
        super("error.msg.loanCharge.id.invalid", "Loan charge with identifier " + id + " does not exist", id);
    }

    public LoanChargeNotFoundException(final Long id, final Long loanId) {
        super("error.msg.loanCharge.id.invalid.for.given.loan", "Loan charge with identifier " + id + " does not exist for loan " + loanId,
                id, loanId);
    }
}