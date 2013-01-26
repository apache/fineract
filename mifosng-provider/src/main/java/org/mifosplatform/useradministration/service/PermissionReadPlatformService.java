package org.mifosplatform.useradministration.service;

import java.util.Collection;

import org.mifosplatform.useradministration.data.PermissionData;

public interface PermissionReadPlatformService {

    Collection<PermissionData> retrieveAllPermissions();
    
    Collection<PermissionData> retrieveAllMakerCheckerablePermissions();

    Collection<PermissionData> retrieveAllRolePermissions(Long roleId);
}