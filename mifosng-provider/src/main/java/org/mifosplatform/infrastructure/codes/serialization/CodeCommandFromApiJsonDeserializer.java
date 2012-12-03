package org.mifosplatform.infrastructure.codes.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link CodeCommand}'s.
 */
@Component
public final class CodeCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<CodeCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name"));
    
    private final FromJsonHelper fromApiJsonHelper;
    private final CodeCommandFromCommandJsonDeserializer fromCommandJsonDeserializer;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public CodeCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final CodeCommandFromCommandJsonDeserializer fromCommandJsonDeserializer,
            final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.fromCommandJsonDeserializer = fromCommandJsonDeserializer;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public CodeCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public CodeCommand commandFromApiJson(final Long codeId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        return this.fromCommandJsonDeserializer.commandFromCommandJson(codeId, json);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final CodeCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long codeId, final String json) {
        final CodeCommand command = commandFromApiJson(codeId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}