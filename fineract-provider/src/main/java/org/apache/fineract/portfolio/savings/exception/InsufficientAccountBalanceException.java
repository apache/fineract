/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.exception;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * Thrown when an attempt is made to withdraw money that is greater than the
 * account balance.
 */
public class InsufficientAccountBalanceException extends AbstractPlatformDomainRuleException {

    public InsufficientAccountBalanceException(final String paramName, final BigDecimal accountBalance, final BigDecimal withdrawalFee,
            final BigDecimal transactionAmount) {
        super(withdrawalFee != null ? "error.msg.savingsaccount.transaction.insufficient.account.balance.withdraw"
                : "error.msg.savingsaccount.transaction.insufficient.account.balance", "Insufficient account balance.", paramName,
                accountBalance, withdrawalFee, transactionAmount);
    }
}