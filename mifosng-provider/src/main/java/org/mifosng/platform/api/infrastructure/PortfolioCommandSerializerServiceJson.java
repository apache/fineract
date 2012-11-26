package org.mifosng.platform.api.infrastructure;

import java.util.HashSet;

import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.api.commands.RolePermissionCommand;
import org.mifosng.platform.infrastructure.api.GoogleGsonSerializerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

/**
 * Implementation of {@link PortfolioCommandSerializerService} that serializes
 * the commands into JSON using google-gson.
 */
@Service
public class PortfolioCommandSerializerServiceJson implements PortfolioCommandSerializerService {

    private final GoogleGsonSerializerHelper helper;

    @Autowired
    public PortfolioCommandSerializerServiceJson(final GoogleGsonSerializerHelper helper) {
        this.helper = helper;
    }

    @Override
    public String serializeRoleCommandToJson(final RoleCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false,
                new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeRolePermissionCommandToJson(final RolePermissionCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeUserCommandToJson(final UserCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }
}