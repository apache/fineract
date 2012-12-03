package org.mifosplatform.useradministration.service;

import org.mifosplatform.useradministration.command.PermissionsCommand;

public interface PermissionWritePlatformService {

    void updateMakerCheckerPermissions(PermissionsCommand command);
}