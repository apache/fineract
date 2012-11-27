package org.mifosplatform.infrastructure.user.service;


import org.mifosplatform.infrastructure.user.command.PermissionsCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PermissionWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS',  'PERMISSIONS_ROLE')")
    Long updateMakerCheckerPermissions(PermissionsCommand command);
}