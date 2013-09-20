/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores details of {@link LoanTransaction}'s that were reversed or newly
 * created
 * 
 * 
 */
public class ChangedTransactionDetail {

    private final List<LoanTransaction> newTransactions = new ArrayList<LoanTransaction>();

    private final List<LoanTransaction> reversedTransactions = new ArrayList<LoanTransaction>();

    public List<LoanTransaction> getNewTransactions() {
        return this.newTransactions;
    }

    public List<LoanTransaction> getReversedTransactions() {
        return this.reversedTransactions;
    }

}
