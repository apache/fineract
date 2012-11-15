package org.mifosng.platform.group.service;

import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GroupWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_GROUP')")
    EntityIdentifier createGroup(GroupCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_GROUP')")
    EntityIdentifier updateGroup(GroupCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_GROUP')")
    EntityIdentifier deleteGroup(Long groupId);
    
}
