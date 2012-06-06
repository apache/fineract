package org.mifosng.platform.loan.domain;

import java.util.Comparator;

import org.mifosng.platform.api.data.LoanTransactionData;


public class LoanRepaymentDataComparator implements Comparator<LoanTransactionData> {

    @Override
    public int compare(final LoanTransactionData o1, final LoanTransactionData o2) {
		return o2.getDate().compareTo(o1.getDate());
    }

}
