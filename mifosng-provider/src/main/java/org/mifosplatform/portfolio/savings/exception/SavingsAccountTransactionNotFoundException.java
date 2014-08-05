/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class SavingsAccountTransactionNotFoundException extends AbstractPlatformResourceNotFoundException {

    public SavingsAccountTransactionNotFoundException(final Long savingsId, final Long transactionId) {
        super("error.msg.saving.account.trasaction.id.invalid", "Savings account with savings identifier " + savingsId
                + " and trasaction identifier " + transactionId + " does not exist", savingsId, transactionId);
    }

}
