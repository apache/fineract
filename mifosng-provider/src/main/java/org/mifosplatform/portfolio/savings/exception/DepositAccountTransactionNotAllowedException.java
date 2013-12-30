package org.mifosplatform.portfolio.savings.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;

/**
 * A {@link RuntimeException} thrown when deposit account transaction not
 * allowed.
 */
public class DepositAccountTransactionNotAllowedException extends AbstractPlatformServiceUnavailableException {

    public DepositAccountTransactionNotAllowedException(final Long accountId, final String action, final DepositAccountType type) {
        super("error.msg." + type.resourceName() + ".account.trasaction." + action + ".notallowed", SavingsEnumerations.depositType(type).getValue()
                + "account " + action + " transaction not allowed with account identifier " + accountId, accountId);
    }
}