/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when trying to modify a
 * loan in an invalid state.
 */
public class LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified extends AbstractPlatformDomainRuleException {

    public LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified(final Long id) {
        super("error.msg.loan.cannot.modify.loan.in.its.present.state", "Loan application with identifier " + id
                + " cannot be modified in its current state.", id);
    }

}