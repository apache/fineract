package org.mifosng.platform.user.service;

import java.util.Collection;

import org.mifosng.data.PermissionData;

public interface PermissionReadPlatformService {

	Collection<PermissionData> retrieveAllPermissions();
}