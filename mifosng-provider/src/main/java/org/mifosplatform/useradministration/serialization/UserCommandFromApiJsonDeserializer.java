package org.mifosplatform.useradministration.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.useradministration.command.UserCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link UserCommand}'s.
 */
@Component
public final class UserCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<UserCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("username", "firstname", "lastname", "password",
            "repeatPassword", "email", "officeId", "notSelectedRoles", "roles"));

    private final FromJsonHelper fromApiJsonHelper;
    private final UserCommandFromCommandJsonDeserializer fromCommandJsonDeserializer;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public UserCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final UserCommandFromCommandJsonDeserializer fromCommandJsonDeserializer, final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.fromCommandJsonDeserializer = fromCommandJsonDeserializer;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public UserCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public UserCommand commandFromApiJson(final Long userId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        return this.fromCommandJsonDeserializer.commandFromCommandJson(userId, json);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final UserCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long userId, final String json) {
        final UserCommand command = commandFromApiJson(userId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}