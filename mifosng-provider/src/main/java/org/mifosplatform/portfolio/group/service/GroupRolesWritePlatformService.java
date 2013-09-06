package org.mifosplatform.portfolio.group.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;


public interface GroupRolesWritePlatformService {

    CommandProcessingResult createRole(JsonCommand command);

    CommandProcessingResult updateRole(JsonCommand command);

    CommandProcessingResult deleteRole(Long ruleId);
    
}
