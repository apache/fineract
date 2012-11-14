package org.mifosng.platform.group.service;

import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GroupWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER')")
    EntityIdentifier createGroup(GroupCommand command);

    @PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER')")
    EntityIdentifier updateGroup(GroupCommand command);

    @PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER')")
    EntityIdentifier deleteGroup(Long groupId);
    
}
