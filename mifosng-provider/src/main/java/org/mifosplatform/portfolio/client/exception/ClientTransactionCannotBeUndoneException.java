/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when attempting to delete clients
 */
public class ClientTransactionCannotBeUndoneException extends AbstractPlatformDomainRuleException {

    public ClientTransactionCannotBeUndoneException(final long clientId, final Long transactionId) {
        super("error.msg.clients.transaction.cannot.be.undone", "Client transaction with identifier " + transactionId
                + " for client with identifier " + clientId + " has already been reversed", transactionId);
    }
}