/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores details of {@link LoanTransaction}'s that were reversed or newly
 * created
 * 
 * 
 */
public class ChangedTransactionDetail {

    private final Map<Long, LoanTransaction> newTransactionMappings = new HashMap<>();

    public Map<Long, LoanTransaction> getNewTransactionMappings() {
        return this.newTransactionMappings;
    }

}
