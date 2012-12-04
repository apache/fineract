package org.mifosplatform.useradministration.serialization;

import java.lang.reflect.Type;
import java.util.Map;

import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link PermissionsCommand}'s.
 */
@Component
public final class PermissionsCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<PermissionsCommand> {

    private final FromJsonHelper fromApiJsonHelper;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public PermissionsCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public PermissionsCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public PermissionsCommand commandFromApiJson(@SuppressWarnings("unused") final Long resourceId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Boolean>>() {}.getType();
        final Map<String, Boolean> permissionsMap = fromApiJsonHelper.extractMap(typeOfMap, json);

        return new PermissionsCommand(permissionsMap, false);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final PermissionsCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long roleId, final String json) {
        final PermissionsCommand command = commandFromApiJson(roleId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}