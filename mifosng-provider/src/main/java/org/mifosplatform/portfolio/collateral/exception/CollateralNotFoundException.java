/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collateral.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when guarantor resources are not found.
 */
public class CollateralNotFoundException extends AbstractPlatformResourceNotFoundException {

    public CollateralNotFoundException(final Long loanId, final Long collateralId) {
        super("error.msg.loan.collateral.", "Collateral with Id " + collateralId + " does not exist for loan with Id " + loanId, loanId,
                collateralId);
    }

    public CollateralNotFoundException(final Long id) {
        super("error.msg.loan.collateral.id.invalid", "Loan collateral with identifier " + id + " does not exist", id);
    }
}