package org.mifosng.platform.api.infrastructure;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mifosng.platform.api.data.AppUserData;
import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.api.data.RoleData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

/**
 * Implementation of {@link ApiJsonSerializerService} that uses google-gson to serialize Java object representation into JSON.
 */
@Service
public class GoogleGsonApiJsonSerializerService implements ApiJsonSerializerService {

	private static final Set<String> PERMISSION_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "description", "code"));
	private static final Set<String> ROLE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "description", "availablePermissions", "selectedPermissions"));
	private static final Set<String> APP_USER_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "officeId", "officeName", "username", "firstname", "lastname", "email",
			"allowedOffices", "availableRoles", "selectedRoles"));

	private final GoogleGsonSerializerHelper helper;
	
	@Autowired
	public GoogleGsonApiJsonSerializerService(final GoogleGsonSerializerHelper helper) {
		this.helper = helper;
	}
	
	@Override
	public String serializePermissionDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final Collection<PermissionData> permissions) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(PERMISSION_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, permissions.toArray(new PermissionData[permissions.size()]));
	}
	
	@Override
	public String serializeRoleDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final Collection<RoleData> roles) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(ROLE_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, roles.toArray(new RoleData[roles.size()]));
	}
	
	@Override
	public String serializeRoleDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final RoleData role) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(ROLE_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, role);
	}

	@Override
	public String serializeAppUserDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final Collection<AppUserData> users) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(APP_USER_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, users.toArray(new AppUserData[users.size()]));
	}

	@Override
	public String serializeAppUserDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final AppUserData user) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(APP_USER_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, user);
	}
}