package org.mifosng.platform.staff.service;

import java.util.Collection;

import org.mifosng.platform.api.data.StaffAccountSummaryCollectionData;
import org.mifosng.platform.api.data.StaffData;

public interface StaffReadPlatformService {

	StaffData retrieveStaff(Long staffId);

	Collection<StaffData> retrieveAllStaff(final String extraCriteria);

	Collection<StaffData> retrieveAllLoanOfficersByOffice(final long officeId);

    StaffAccountSummaryCollectionData retrieveLoanOfficerAccountSummary(final long loanOfficerId);
}