/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.data;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;

public class PaymentTypeToGLAccountMapper {

    @SuppressWarnings("unused")
    private final CodeValueData paymentType;
    @SuppressWarnings("unused")
    private final GLAccountData fundSourceAccount;

    public PaymentTypeToGLAccountMapper(final CodeValueData paymentType, final GLAccountData fundSourceAccount) {
        this.paymentType = paymentType;
        this.fundSourceAccount = fundSourceAccount;
    }

}
