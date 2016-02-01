/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ClientHasBeenClosedException extends AbstractPlatformDomainRuleException {

    public ClientHasBeenClosedException(final Long clientId) {
        super("error.msg.client.closed", "Client with identifier " + clientId + " has already been closed");
    }

}
