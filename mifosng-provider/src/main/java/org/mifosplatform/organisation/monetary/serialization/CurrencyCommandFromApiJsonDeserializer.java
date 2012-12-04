package org.mifosplatform.organisation.monetary.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.monetary.command.CurrencyCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link CurrencyCommand}
 * 's.
 */
@Component
public final class CurrencyCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<CurrencyCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("currencies"));

    private final FromJsonHelper fromApiJsonHelper;
    private final CurrencyCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public CurrencyCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final CurrencyCommandFromCommandJsonDeserializer commandFromCommandJsonDeserializer,
            final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.commandFromCommandJsonDeserializer = commandFromCommandJsonDeserializer;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public CurrencyCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public CurrencyCommand commandFromApiJson(final Long resourceId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        return this.commandFromCommandJsonDeserializer.commandFromCommandJson(resourceId, json);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final CurrencyCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long roleId, final String json) {
        final CurrencyCommand command = commandFromApiJson(roleId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}