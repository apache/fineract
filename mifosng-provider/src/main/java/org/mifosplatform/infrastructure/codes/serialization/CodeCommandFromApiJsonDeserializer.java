package org.mifosplatform.infrastructure.codes.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link CodeCommand}'s.
 */
@Component
public final class CodeCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<CodeCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name"));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CodeCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper, final CommandSerializer commandSerializerService) {
        super(commandSerializerService);
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CodeCommand commandFromApiJson(@SuppressWarnings("unused") final Long codeId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);
        final String name = fromApiJsonHelper.extractStringNamed("name", element);

        return new CodeCommand(name);
    }
}