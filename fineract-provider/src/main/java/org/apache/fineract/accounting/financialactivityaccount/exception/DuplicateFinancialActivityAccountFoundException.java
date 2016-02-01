/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.financialactivityaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not
 * found.
 */
public class DuplicateFinancialActivityAccountFoundException extends AbstractPlatformDomainRuleException {

    private final static String errorCode = "error.msg.financialActivityAccount.exists";

    public DuplicateFinancialActivityAccountFoundException(final Integer financialActivityType) {
        super(errorCode, "Mapping for activity already exists " + financialActivityType, financialActivityType);
    }

    public static String getErrorcode() {
        return errorCode;
    }

}