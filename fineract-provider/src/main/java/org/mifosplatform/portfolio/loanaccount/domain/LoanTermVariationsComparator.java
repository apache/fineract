/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.Comparator;

/**
 * Sort loan term variations
 */
public class LoanTermVariationsComparator implements Comparator<LoanTermVariations> {

    @Override
    public int compare(final LoanTermVariations o1, final LoanTermVariations o2) {
        int compareResult = 0;
        final int comparsion = o1.getTermApplicableFrom().compareTo(o2.getTermApplicableFrom());
        /**
         * For Terms bearing the same effective date, we sort based on modified
         * date (when available) and new inserted installment
         **/
        if (comparsion == 0) {
            if (o2.getTermType().isDueDateVariation() || o2.getTermType().isInsertInstallment()) {
                compareResult = 1;
            }
        } else {
            compareResult = comparsion;
        }

        return compareResult;
    }

}
