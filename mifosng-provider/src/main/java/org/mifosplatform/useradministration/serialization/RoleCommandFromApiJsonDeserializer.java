package org.mifosplatform.useradministration.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.useradministration.command.RoleCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link RoleCommand}'s.
 */
@Component
public final class RoleCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<RoleCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "name", "description"));

    private final FromJsonHelper fromApiJsonHelper;
    private final RoleCommandFromCommandJsonDeserializer fromCommandJsonDeserializer;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public RoleCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final RoleCommandFromCommandJsonDeserializer fromCommandJsonDeserializer, final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.fromCommandJsonDeserializer = fromCommandJsonDeserializer;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public RoleCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public RoleCommand commandFromApiJson(final Long roleId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        return this.fromCommandJsonDeserializer.commandFromCommandJson(roleId, json);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final RoleCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long roleId, final String json) {
        final RoleCommand command = commandFromApiJson(roleId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}