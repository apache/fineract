package org.mifosplatform.portfolio.savings.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class SavingsAccountTransactionNotFoundException extends AbstractPlatformResourceNotFoundException {

    public SavingsAccountTransactionNotFoundException(final Long savingsId, final Long transactionId) {
        super("error.msg.saving.account.trasaction.id.invalid", "Savings account with savings identifier " + savingsId
                + " and trasaction identifier " + transactionId + " does not exist", savingsId, transactionId);
    }

}
