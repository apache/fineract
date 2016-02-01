/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * Indicates that a cashier could not be found.
 *
 * @author Markus Geiss
 * @since 2.0.0
 */
public class CashierNotFoundException extends AbstractPlatformResourceNotFoundException {

    private static final String ERROR_MESSAGE_CODE = "error.msg.cashier.not.found";
    private static final String DEFAULT_ERROR_MESSAGE = "Cashier with identifier {0,number,long} not found!";

    /**
     * Creates a new instance.
     *
     * @param cashierId the primary key of the cashier
     */
    public CashierNotFoundException(Long cashierId) {
        super(ERROR_MESSAGE_CODE, DEFAULT_ERROR_MESSAGE, cashierId);
    }
}
