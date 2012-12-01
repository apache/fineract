package org.mifosng.platform.group.service;

import java.util.Collection;

import org.mifosng.platform.api.data.GroupAccountSummaryCollectionData;
import org.mifosng.platform.api.data.GroupAccountSummaryData;
import org.mifosng.platform.api.data.GroupData;
import org.mifosplatform.portfolio.client.data.ClientLookup;

public interface GroupReadPlatformService {

    Collection<GroupData> retrieveAllGroups(String extraCriteria);
    
    GroupData retrieveGroup(Long groupId);

    GroupData retrieveNewGroupDetails(Long officeId);

    Collection<ClientLookup> retrieveClientMembers(Long groupId);

    GroupAccountSummaryCollectionData retrieveGroupAccountDetails(Long groupId);

    Collection<GroupAccountSummaryData> retrieveGroupLoanAccountsByLoanOfficerId(Long groupId, Long loanOfficerId);
}
