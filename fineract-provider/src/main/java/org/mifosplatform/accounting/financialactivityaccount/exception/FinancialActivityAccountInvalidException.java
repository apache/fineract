/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.financialactivityaccount.exception;

import org.mifosplatform.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not
 * found.
 */
public class FinancialActivityAccountInvalidException extends AbstractPlatformDomainRuleException {

    private final static String errorCode = "error.msg.financialActivityAccount.invalid";

    public FinancialActivityAccountInvalidException(final FINANCIAL_ACTIVITY financialActivity, final GLAccount glAccount) {
        super(errorCode, "Financial Activity '" + financialActivity.getCode() + "' with Id :" + financialActivity.getValue()
                + "' can only be associated with a Ledger Account of Type " + financialActivity.getMappedGLAccountType().getCode()
                + " the provided Ledger Account '" + glAccount.getName() + "(" + glAccount.getGlCode()
                + ")'  does not of the required type", financialActivity.getCode(), financialActivity.getValue(), financialActivity
                .getMappedGLAccountType().getCode(), glAccount.getName(), glAccount.getGlCode());
    }

    public static String getErrorcode() {
        return errorCode;
    }
}