/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ClientTransactionNotFoundException extends AbstractPlatformDomainRuleException {

    public ClientTransactionNotFoundException(final Long clientId, final Long transactionId) {
        super("error.msg.client.transaction.not.found.exception",
                "The Transaction with id `" + transactionId + "` does not exist for a Client with id `" + clientId, transactionId,
                clientId);
    }

}
