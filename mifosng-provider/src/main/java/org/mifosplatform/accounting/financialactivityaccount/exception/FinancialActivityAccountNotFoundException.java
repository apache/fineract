/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.financialactivityaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not
 * found.
 */
public class FinancialActivityAccountNotFoundException extends AbstractPlatformResourceNotFoundException {

    public FinancialActivityAccountNotFoundException(final Long id) {
        super("error.msg.financialActivityAccount.not.found", "Financial Activity account with Id " + id + " does not exist", id);
    }

    public FinancialActivityAccountNotFoundException(final Integer financialActivityType) {
        super("error.msg.financialActivityAccount.not.found", "Financial Activity account with for the financial Activity with Id "
                + financialActivityType + " does not exist", financialActivityType);
    }

}