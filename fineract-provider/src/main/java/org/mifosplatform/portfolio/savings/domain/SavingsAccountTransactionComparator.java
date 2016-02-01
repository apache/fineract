/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.util.Comparator;

/**
 * Sort savings account transactions by transaction date and transaction type
 * placing
 */
public class SavingsAccountTransactionComparator implements Comparator<SavingsAccountTransaction> {

    @Override
    public int compare(final SavingsAccountTransaction o1, final SavingsAccountTransaction o2) {
        int compareResult = 0;
        final int comparsion = o1.transactionLocalDate().compareTo(o2.transactionLocalDate());
        if (comparsion == 0) {
            compareResult = o1.createdDate().compareTo(o2.createdDate());
            if (compareResult == 0 && o1.getId() != null && o2.getId() != null) {
                compareResult = o1.getId().compareTo(o2.getId());
            } else {
                compareResult = comparsion;
            }
        } else {
            compareResult = comparsion;
        }
        return compareResult;
    }
}