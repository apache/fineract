package org.mifosng.platform.loan.domain;

import java.util.Comparator;

import org.mifosng.platform.api.data.LoanTransactionNewData;

public class LoanRepaymentDataComparator implements Comparator<LoanTransactionNewData> {

    @Override
    public int compare(final LoanTransactionNewData o1, final LoanTransactionNewData o2) {
		return o2.dateOf().compareTo(o1.dateOf());
    }
}