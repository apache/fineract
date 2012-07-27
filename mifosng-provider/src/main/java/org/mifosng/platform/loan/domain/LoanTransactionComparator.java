package org.mifosng.platform.loan.domain;

import java.util.Comparator;

/**
 *
 */
public class LoanTransactionComparator implements Comparator<LoanTransaction> {

    @Override
    public int compare(final LoanTransaction o1, final LoanTransaction o2) {
		return o1.getTransactionDate().compareTo(o2.getTransactionDate());
    }

}
