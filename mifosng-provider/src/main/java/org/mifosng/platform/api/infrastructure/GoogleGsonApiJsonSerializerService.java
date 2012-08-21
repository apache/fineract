package org.mifosng.platform.api.infrastructure;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.AppUserData;
import org.mifosng.platform.api.data.AuthenticatedUserData;
import org.mifosng.platform.api.data.ConfigurationData;
import org.mifosng.platform.api.data.FundData;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.data.OfficeData;
import org.mifosng.platform.api.data.OfficeTransactionData;
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
	private static final Set<String> OFFICE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "name", "nameDecorated", "externalId", "openingDate", 
					"hierarchy", "parentId", "parentName", "allowedParents")
			);
	private static final Set<String> OFFICE_TRANSACTIONS_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("transactionDate", "allowedOffices", "currencyOptions"));
	private static final Set<String> CONFIGURATION_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("selectedCurrencyOptions", "currencyOptions"));
	private static final Set<String> FUND_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "externalId"));
	
	private final GoogleGsonSerializerHelper helper;
	
	@Autowired
	public GoogleGsonApiJsonSerializerService(final GoogleGsonSerializerHelper helper) {
		this.helper = helper;
	}
	
	@Override
	public String serializeAuthenticatedUserDataToJson(final boolean prettyPrint, final AuthenticatedUserData authenticatedUserData) {
		final Gson gsonDeserializer = helper.createGsonBuilder(prettyPrint);
		return helper.serializedJsonFrom(gsonDeserializer, authenticatedUserData);
	}

	@Override
	public String serializeGenericResultsetDataToJson(final boolean prettyPrint, final GenericResultsetData resultsetData) {
		final Gson gsonDeserializer = helper.createGsonBuilder(prettyPrint);
		return helper.serializedJsonFrom(gsonDeserializer, resultsetData);
	}

	@Override
	public String serializeAdditionalFieldsSetDataToJson(final boolean prettyPrint, final Collection<AdditionalFieldsSetData> datasets) {
		final Gson gsonDeserializer = helper.createGsonBuilder(prettyPrint);
		return helper.serializedJsonFrom(gsonDeserializer, datasets.toArray(new AdditionalFieldsSetData[datasets.size()]));
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

	@Override
	public String serializeOfficeDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final Collection<OfficeData> offices) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(OFFICE_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, offices.toArray(new OfficeData[offices.size()]));
	}

	@Override
	public String serializeOfficeDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final OfficeData office) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(OFFICE_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, office);
	}

	@Override
	public String serializeOfficeTransactionDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final OfficeTransactionData officeTransaction) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(OFFICE_TRANSACTIONS_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, officeTransaction);
	}

	@Override
	public String serializeConfigurationDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final ConfigurationData configuration) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(CONFIGURATION_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, configuration);
	}

	@Override
	public String serializeFundDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final Collection<FundData> funds) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(FUND_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, funds.toArray(new FundData[funds.size()]));
	}

	@Override
	public String serializeFundDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final FundData fund) {
		final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(FUND_DATA_PARAMETERS, prettyPrint, responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, fund);
	}
}