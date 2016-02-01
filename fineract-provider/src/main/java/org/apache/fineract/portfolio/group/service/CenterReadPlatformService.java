/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.util.Collection;
import java.util.Date;

import org.mifosplatform.infrastructure.core.data.PaginationParameters;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.SearchParameters;
import org.mifosplatform.portfolio.group.data.CenterData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.data.StaffCenterData;

public interface CenterReadPlatformService {

    CenterData retrieveTemplate(Long officeId, boolean staffInSelectedOfficeOnly);

    CenterData retrieveOne(Long centerId);

    Collection<CenterData> retrieveAllForDropdown(Long officeId);

    Page<CenterData> retrievePagedAll(SearchParameters searchParameters, PaginationParameters parameters);

    Collection<CenterData> retrieveAll(SearchParameters searchParameters, PaginationParameters parameters);

    GroupGeneralData retrieveCenterGroupTemplate(Long centerId);

    Collection<GroupGeneralData> retrieveAssociatedGroups(Long centerId);

    CenterData retrieveCenterWithClosureReasons();

    Collection<StaffCenterData> retriveAllCentersByMeetingDate(Long officeId, Date meetingDate, Long staffId);
}
