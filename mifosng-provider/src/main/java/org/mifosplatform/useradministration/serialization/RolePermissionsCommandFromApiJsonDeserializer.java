package org.mifosplatform.useradministration.serialization;

import java.lang.reflect.Type;
import java.util.Map;

import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.useradministration.command.RoleCommand;
import org.mifosplatform.useradministration.command.RolePermissionCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link RoleCommand}'s.
 */
@Component
public final class RolePermissionsCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<RolePermissionCommand> {

    private final FromJsonHelper fromApiJsonHelper;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public RolePermissionsCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public RolePermissionCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public RolePermissionCommand commandFromApiJson(final Long roleId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Boolean>>() {}.getType();

        final Map<String, Boolean> permissionsMap = fromApiJsonHelper.extractMap(typeOfMap, json);

        return new RolePermissionCommand(roleId, permissionsMap, false);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final RolePermissionCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long roleId, final String json) {
        final RolePermissionCommand command = commandFromApiJson(roleId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}