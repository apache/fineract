/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.data;

public class PaymentTypeToGLAccountMapper {

    @SuppressWarnings("unused")
    private final long paymentTypeId;
    @SuppressWarnings("unused")
    private final long fundSourceAccountId;

    public PaymentTypeToGLAccountMapper(final long paymentTypeId, final long fundSourceAccountId) {
        this.paymentTypeId = paymentTypeId;
        this.fundSourceAccountId = fundSourceAccountId;
    }

}
