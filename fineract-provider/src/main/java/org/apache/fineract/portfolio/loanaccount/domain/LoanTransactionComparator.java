/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.Comparator;

/**
 * Sort loan transactions by transaction date, created date and transaction type
 * placing
 */
public class LoanTransactionComparator implements Comparator<LoanTransaction> {

    @Override
    public int compare(final LoanTransaction o1, final LoanTransaction o2) {
        int compareResult = 0;
        final int comparsion = o1.getTransactionDate().compareTo(o2.getTransactionDate());
        /**
         * For transactions bearing the same transaction date, we sort
         * transactions based on created date (when available) after which
         * sorting for waivers takes place
         **/
        if (comparsion == 0) {
            int comparisonBasedOnCreatedDate = 0;
            if (o1.getCreatedDateTime() != null && o2.getCreatedDateTime() != null) {
                comparisonBasedOnCreatedDate = o1.getCreatedDateTime().compareTo(o2.getCreatedDateTime());
            }
            // equal transaction dates
            if (comparisonBasedOnCreatedDate == 0) {
                if (o1.isWaiver() && o2.isNotWaiver()) {
                    compareResult = -1;
                } else if (o1.isNotWaiver() && o2.isWaiver()) {
                    compareResult = 1;
                } else {
                    compareResult = 0;
                }
            }
        } else {
            compareResult = comparsion;
        }

        return compareResult;
    }

}
