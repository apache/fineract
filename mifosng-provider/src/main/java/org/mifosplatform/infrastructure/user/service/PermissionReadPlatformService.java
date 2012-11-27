package org.mifosplatform.infrastructure.user.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.user.api.data.PermissionUsageData;

public interface PermissionReadPlatformService {

    Collection<PermissionUsageData> retrieveAllPermissions();
    
    Collection<PermissionUsageData> retrieveAllMakerCheckerablePermissions();

    Collection<PermissionUsageData> retrieveAllRolePermissions(Long roleId);
}