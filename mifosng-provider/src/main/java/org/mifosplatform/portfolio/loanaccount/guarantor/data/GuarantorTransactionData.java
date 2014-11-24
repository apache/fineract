/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.data;

import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;
import org.mifosplatform.portfolio.savings.data.DepositAccountOnHoldTransactionData;

public class GuarantorTransactionData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final DepositAccountOnHoldTransactionData onHoldTransactionData;
    @SuppressWarnings("unused")
    private final LoanTransactionData loanTransactionData;
    @SuppressWarnings("unused")
    private final boolean reversed;

    private GuarantorTransactionData(final Long id, final DepositAccountOnHoldTransactionData onHoldTransactionData,
            final LoanTransactionData loanTransactionData, final boolean reversed) {

        this.id = id;
        this.onHoldTransactionData = onHoldTransactionData;
        this.loanTransactionData = loanTransactionData;
        this.reversed = reversed;
    }

    public static GuarantorTransactionData instance(final Long id, final DepositAccountOnHoldTransactionData onHoldTransactionData,
            final LoanTransactionData loanTransactionData, final boolean reversed) {
        return new GuarantorTransactionData(id, onHoldTransactionData, loanTransactionData, reversed);
    }

}
