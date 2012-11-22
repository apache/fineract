package org.mifosng.platform.user.service;

import java.util.Collection;

import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.api.data.PermissionUsageData;

public interface PermissionReadPlatformService {

    Collection<PermissionData> retrieveAllPermissions();

    Collection<PermissionUsageData> retrieveAllRolePermissions(Long roleId);
}