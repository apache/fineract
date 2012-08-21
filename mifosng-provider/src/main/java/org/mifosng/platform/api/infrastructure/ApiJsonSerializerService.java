package org.mifosng.platform.api.infrastructure;

import java.util.Collection;
import java.util.Set;

import org.mifosng.platform.api.data.AppUserData;
import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.api.data.RoleData;

public interface ApiJsonSerializerService {

	String serializePermissionDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<PermissionData> permissions);

	String serializeRoleDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<RoleData> roles);

	String serializeRoleDataToJson(boolean prettyPrint, Set<String> responseParameters, RoleData role);

	String serializeAppUserDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<AppUserData> users);

	String serializeAppUserDataToJson(boolean prettyPrint, Set<String> responseParameters, AppUserData user);
	
}