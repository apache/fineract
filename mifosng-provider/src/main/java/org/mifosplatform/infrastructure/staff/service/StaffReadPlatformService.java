package org.mifosplatform.infrastructure.staff.service;

import java.util.Collection;

import org.mifosng.platform.api.data.StaffAccountSummaryCollectionData;
import org.mifosplatform.infrastructure.staff.data.StaffData;

public interface StaffReadPlatformService {

    StaffData retrieveStaff(Long staffId);

    Collection<StaffData> retrieveAllStaff(final String extraCriteria);

    Collection<StaffData> retrieveAllLoanOfficersByOffice(final Long officeId);

    StaffAccountSummaryCollectionData retrieveLoanOfficerAccountSummary(final Long loanOfficerId);
}