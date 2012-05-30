package org.mifosng.platform.user.service;

import java.util.Collection;

import org.mifosng.platform.api.data.PermissionData;

public interface PermissionReadPlatformService {

	Collection<PermissionData> retrieveAllPermissions();
}