package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.Comparator;

/**
 * Sort loan transactions by transaction date and transaction type placing
 */
public class LoanTransactionComparator implements Comparator<LoanTransaction> {

    @Override
    public int compare(final LoanTransaction o1, final LoanTransaction o2) {
    	int compareResult = 0;
		final int comparsion = o1.getTransactionDate().compareTo(o2.getTransactionDate());
		if (comparsion == 0) {
			// equal transaction dates
			if (o1.isWaiver() && o2.isNotWaiver()) {
				compareResult = -1;
			} else if (o1.isNotWaiver() && o2.isWaiver()) {
				compareResult = 1;
			} else {
				compareResult = 0;
			}
		} else {
			compareResult = comparsion;
		}
		
		return compareResult;
    }

}
