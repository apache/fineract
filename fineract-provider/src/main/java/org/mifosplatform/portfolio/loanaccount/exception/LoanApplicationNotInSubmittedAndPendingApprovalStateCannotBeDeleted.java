/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when trying to delete a
 * loan in an invalid state.
 */
public class LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeDeleted extends AbstractPlatformDomainRuleException {

    public LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeDeleted(final Long id) {
        super("error.msg.loan.cannot.delete.loan.in.its.present.state", "Loan with identifier " + id
                + " cannot be deleted in its current state.", id);
    }

}