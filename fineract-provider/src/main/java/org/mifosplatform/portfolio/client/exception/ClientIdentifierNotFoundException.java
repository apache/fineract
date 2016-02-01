/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when client Identifier resources are not
 * found.
 */
public class ClientIdentifierNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ClientIdentifierNotFoundException(final Long id) {
        super("error.msg.clientIdentifier.id.invalid", "Client Identifier with the primary key " + id + " does not exist", id);
    }
}