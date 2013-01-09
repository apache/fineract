package org.mifosplatform.portfolio.group.service;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GroupWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_GROUP')")
    CommandProcessingResult createGroup(GroupCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'UPDATE_GROUP')")
    CommandProcessingResult updateGroup(GroupCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_GROUP')")
    CommandProcessingResult deleteGroup(Long groupId);
    
}
