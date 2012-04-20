package org.mifosng.data;

import java.util.Comparator;

import org.mifosng.data.LoanRepaymentData;


public class LoanRepaymentDataComparator implements Comparator<LoanRepaymentData> {

    @Override
    public int compare(final LoanRepaymentData o1, final LoanRepaymentData o2) {
		return o2.getDate().compareTo(o1.getDate());
    }

}
