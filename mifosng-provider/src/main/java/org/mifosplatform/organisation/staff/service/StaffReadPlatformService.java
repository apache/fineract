/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.staff.service;

import java.util.Collection;

import org.mifosplatform.organisation.staff.data.StaffData;

public interface StaffReadPlatformService {

    StaffData retrieveStaff(Long staffId);

    Collection<StaffData> retrieveAllStaff(final String extraCriteria);

    Collection<StaffData> retrieveAllStaffForDropdown(Long officeId);

    Collection<StaffData> retrieveAllLoanOfficersByOffice(final Long officeId);
}