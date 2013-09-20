/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.Comparator;

import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;

public class LoanRepaymentDataComparator implements Comparator<LoanTransactionData> {

    @Override
    public int compare(final LoanTransactionData o1, final LoanTransactionData o2) {
        return o2.dateOf().compareTo(o1.dateOf());
    }
}