/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when guarantor resources are not found.
 */
public class GuarantorNotFoundException extends AbstractPlatformResourceNotFoundException {

    public GuarantorNotFoundException(final Long id) {
        super("error.msg.loan.guarantor.not.found", "Guarantor with identifier " + id + " does not exist", id);
    }

    public GuarantorNotFoundException(final Long loanId, final Long guarantorId) {
        super("error.msg.loan.guarantor.not.found", "Guarantor with identifier " + guarantorId
                + " does not exist for loan with Identifier " + loanId, loanId, guarantorId);
    }

    public GuarantorNotFoundException(final Long loanId, final Long guarantorId, final Long guarantorFundingId) {
        super("error.msg.loan.guarantor.not.found", "Guarantor with identifier " + guarantorId + "and with funding detail "
                + guarantorFundingId + " does not exist for loan with Identifier " + loanId, loanId, guarantorId, guarantorFundingId);
    }
}