/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LoanDisbursalException extends AbstractPlatformDomainRuleException {

    public LoanDisbursalException(final String currentProduct, final String restrictedProduct) {
        super("error.msg.loan.disbursal.failed", "This loan could not be disbursed as `" + currentProduct + "` and `" + restrictedProduct
                + "` are not allowed to co-exist", new Object[] { currentProduct, restrictedProduct });
    }

    public LoanDisbursalException(final String defaultUserMessage, final String entity, final Object... defaultUserMessageArgs) {
        super("error.msg.loan." + entity, defaultUserMessage, defaultUserMessageArgs);
    }
}
