/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.data;

public class ChargeToGLAccountMapper {

    @SuppressWarnings("unused")
    private final long chargeId;
    @SuppressWarnings("unused")
    private final long incomeAccountId;

    public ChargeToGLAccountMapper(final long chargeId, final long incomeAccountId) {
        this.chargeId = chargeId;
        this.incomeAccountId = incomeAccountId;
    }

}
