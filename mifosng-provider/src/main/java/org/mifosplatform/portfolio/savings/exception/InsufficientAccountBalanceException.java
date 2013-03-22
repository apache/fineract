package org.mifosplatform.portfolio.savings.exception;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * Thrown when an attempt is made to withdraw money that is greater than the
 * account balance.
 */
public class InsufficientAccountBalanceException extends AbstractPlatformDomainRuleException {

    public InsufficientAccountBalanceException(final String paramName, final BigDecimal accountBalance, final BigDecimal withdrawalRequested) {
        super("error.msg.savingsaccount.transaction.insufficient.account.balance", "Insufficient account balance.", paramName,
                accountBalance, withdrawalRequested);
    }
}