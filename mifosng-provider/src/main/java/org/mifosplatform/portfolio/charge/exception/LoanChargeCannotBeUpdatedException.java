/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LoanChargeCannotBeUpdatedException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons of why Loan Charge cannot be waived **/
    public static enum LOAN_CHARGE_CANNOT_BE_UPDATED_REASON {
        ALREADY_PAID, ALREADY_WAIVED, LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE;

        public String errorMessage() {
            if (name().toString().equalsIgnoreCase("ALREADY_PAID")) {
                return "This loan charge has been partially/completely paid";
            } else if (name().toString().equalsIgnoreCase("ALREADY_WAIVED")) {
                return "This loan charge has already been waived";
            } else if (name().toString().equalsIgnoreCase("LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE")) { return "This charge cannot be updated as the loan it is associated with is not in submitted and pending approval stage"; }
            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("ALREADY_PAID")) {
                return "error.msg.loan.charge.already.paid";
            } else if (name().toString().equalsIgnoreCase("ALREADY_WAIVED")) {
                return "error.msg.loan.charge.already.waived";
            } else if (name().toString().equalsIgnoreCase("LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE")) { return "error.msg.loan.charge.associated.loan.not.in.submitted.and.pending.approval.stage"; }
            return name().toString();
        }
    }

    public LoanChargeCannotBeUpdatedException(final LOAN_CHARGE_CANNOT_BE_UPDATED_REASON reason, final Long loanChargeId) {
        super(reason.errorCode(), reason.errorMessage(), loanChargeId);
    }
}
