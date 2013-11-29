/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.data;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.portfolio.charge.data.ChargeData;

public class ChargeToGLAccountMapper {

    @SuppressWarnings("unused")
    private final ChargeData charge;
    @SuppressWarnings("unused")
    private final GLAccountData incomeAccount;

    public ChargeToGLAccountMapper(final ChargeData charge, final GLAccountData incomeAccount) {
        this.charge = charge;
        this.incomeAccount = incomeAccount;
    }

}
