package org.mifosplatform.useradministration.service;

import java.util.Collection;

import org.mifosplatform.useradministration.data.PermissionUsageData;

public interface PermissionReadPlatformService {

    Collection<PermissionUsageData> retrieveAllPermissions();
    
    Collection<PermissionUsageData> retrieveAllMakerCheckerablePermissions();

    Collection<PermissionUsageData> retrieveAllRolePermissions(Long roleId);
}