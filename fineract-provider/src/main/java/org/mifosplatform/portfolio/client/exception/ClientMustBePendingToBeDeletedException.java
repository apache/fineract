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
public class ClientMustBePendingToBeDeletedException extends AbstractPlatformDomainRuleException {

    public ClientMustBePendingToBeDeletedException(final Long id) {
        super("error.msg.clients.cannot.be.deleted",
                "Client with identifier " + id + " cannot be deleted as it is not in `Pending` state.", id);
    }
}