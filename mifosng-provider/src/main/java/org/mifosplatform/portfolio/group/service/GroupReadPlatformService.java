/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.util.Collection;

import org.mifosplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;

public interface GroupReadPlatformService {

    GroupGeneralData retrieveTemplate(Long officeId, boolean isCenterGroup);

    Collection<GroupGeneralData> retrieveAll(SearchParameters searchCriteria);

    GroupGeneralData retrieveOne(Long groupId);

    //
    GroupAccountSummaryCollectionData retrieveGroupAccountDetails(Long groupId);

    Collection<GroupAccountSummaryData> retrieveGroupLoanAccountsByLoanOfficerId(Long groupId, Long loanOfficerId);
}