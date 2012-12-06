package org.mifosplatform.useradministration.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface PermissionWritePlatformService {

    EntityIdentifier updateMakerCheckerPermissions(JsonCommand command);
}