/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymenttype.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;


public class PaymentTypeNotFoundException extends AbstractPlatformResourceNotFoundException {

    public PaymentTypeNotFoundException(final Long id) {
        super("error.msg.payment.type.invalid", "PaymentType with " + id + " does not exist", id);
    }
} 
    


