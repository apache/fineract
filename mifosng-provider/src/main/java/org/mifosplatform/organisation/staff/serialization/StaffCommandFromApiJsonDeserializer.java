package org.mifosplatform.organisation.staff.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.staff.command.StaffCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link StaffCommand}'s.
 */
@Component
public final class StaffCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<StaffCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("firstname", "lastname", "officeId", "isLoanOfficer"));

    private final FromJsonHelper fromApiJsonHelper;
    private final StaffCommandFromCommandJsonDeserializer fromCommandJsonDeserializer;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public StaffCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final StaffCommandFromCommandJsonDeserializer fromCommandJsonDeserializer, 
            final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.fromCommandJsonDeserializer = fromCommandJsonDeserializer;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public StaffCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public StaffCommand commandFromApiJson(final Long roleId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        return this.fromCommandJsonDeserializer.commandFromCommandJson(roleId, json);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final StaffCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long roleId, final String json) {
        final StaffCommand command = commandFromApiJson(roleId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}