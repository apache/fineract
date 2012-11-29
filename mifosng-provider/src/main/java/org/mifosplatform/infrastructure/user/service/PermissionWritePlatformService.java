package org.mifosplatform.infrastructure.user.service;

import org.mifosplatform.infrastructure.user.command.PermissionsCommand;

public interface PermissionWritePlatformService {

    void updateMakerCheckerPermissions(PermissionsCommand command);
}