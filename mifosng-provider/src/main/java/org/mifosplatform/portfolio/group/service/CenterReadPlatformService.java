/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.util.Collection;

import org.mifosplatform.portfolio.group.data.CenterData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;

public interface CenterReadPlatformService {

    CenterData retrieveTemplate(Long officeId);

    CenterData retrieveOne(Long centerId);

    Collection<CenterData> retrieveAllForDropdown(Long officeId);

    Collection<CenterData> retrieveAll(SearchParameters searchCriteria);

    GroupGeneralData retrieveCenterGroupTemplate(Long centerId);
    
    Collection<GroupGeneralData> retrieveAssociatedGroups(Long centerId);
}
