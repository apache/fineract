package org.mifosng.platform;

import java.util.Comparator;

import org.mifosng.data.LoanAccountData;

public class ClosedLoanComparator implements Comparator<LoanAccountData> {

	@Override
	public int compare(final LoanAccountData o1,
			final LoanAccountData o2) {
		return o1.getClosedOnDate().compareTo(o2.getClosedOnDate());
	}

}
