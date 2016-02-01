/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;

/**
 * A {@link RuntimeException} thrown when update not allowed.
 */
public class TransactionUpdateNotAllowedException extends AbstractPlatformServiceUnavailableException {

    public TransactionUpdateNotAllowedException(final Long savingsId, final Long transactionId) {
        super("error.msg.saving.account.trasaction.update.notallowed",
                "Savings Account transaction update not allowed with savings identifier " + savingsId + " and trasaction identifier "
                        + transactionId, savingsId, transactionId);
    }
}