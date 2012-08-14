package org.mifosng.platform.group.service;

import java.util.Collection;

import org.mifosng.platform.api.data.ClientLookup;
import org.mifosng.platform.api.data.GroupData;

public interface GroupReadPlatformService {

    Collection<GroupData> retrieveAllGroups();
    
    GroupData retrieveGroup(Long groupId);

    GroupData retrieveNewGroupDetails();

    Collection<ClientLookup> retrieveClientMembers(Long groupId);
}
