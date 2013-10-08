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
            // equal transaction dates
            if (o1.isInterestPostingAndNotReversed() && !o2.isInterestPostingAndNotReversed()) {
                compareResult = -1;
            } else if (!o1.isInterestPostingAndNotReversed() && o2.isInterestPostingAndNotReversed()) {
                compareResult = 1;
            } else if (!o1.isDepositAndNotReversed() && o2.isDepositAndNotReversed()) {
                // push all deposit transactions before charge/withdrawal transactions on same day.
                // activation charge is charged on account activation date, any
                // adjustments with deposit amount
                // resulting into negative balance, to fix this push deposit
                // transactions before charge/withdrawal charges transactions.
                compareResult = 1;
            } else if (o1.isDepositAndNotReversed() && !o2.isDepositAndNotReversed()) {
                compareResult = -1;
            } else {
                compareResult = 0;
            }
        } else {
            compareResult = comparsion;
        }

        return compareResult;
    }
}