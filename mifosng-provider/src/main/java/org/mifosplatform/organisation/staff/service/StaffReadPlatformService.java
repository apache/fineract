package org.mifosplatform.organisation.staff.service;

import java.util.Collection;

import org.mifosplatform.organisation.staff.data.StaffAccountSummaryCollectionData;
import org.mifosplatform.organisation.staff.data.StaffData;

public interface StaffReadPlatformService {

    StaffData retrieveStaff(Long staffId);

    Collection<StaffData> retrieveAllStaff(final String extraCriteria);

    Collection<StaffData> retrieveAllLoanOfficersByOffice(final Long officeId);

    StaffAccountSummaryCollectionData retrieveLoanOfficerAccountSummary(final Long loanOfficerId);
}