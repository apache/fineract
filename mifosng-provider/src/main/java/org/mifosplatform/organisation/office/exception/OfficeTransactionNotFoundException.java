/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.office.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when office transaction resources are not found.
 */
public class OfficeTransactionNotFoundException extends AbstractPlatformResourceNotFoundException {

    public OfficeTransactionNotFoundException(final Long id) {
        super("error.msg.officetransaction.id.invalid", "Office transaction with identifier " + id + " does not exist", id);
    }
}