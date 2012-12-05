package org.mifosplatform.portfolio.client.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.command.ClientNoteCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link ClientCommand} 
 * 's.
 */
@Component
public final class ClientNoteCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<ClientNoteCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("clientId", "note"));

    private final FromJsonHelper fromApiJsonHelper;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public ClientNoteCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper, final CommandSerializer commandSerializerService) {
        super(commandSerializerService);
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public ClientNoteCommand commandFromApiJson(final Long noteId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromApiJsonHelper.parse(json);
        final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element, parametersPassedInRequest);
        final String note = fromApiJsonHelper.extractStringNamed("note", element, parametersPassedInRequest);

        return new ClientNoteCommand(parametersPassedInRequest, false, noteId, clientId, note);
    }

    public String serializedCommandJsonFromApiJsonWithSubResource(final String json, final Long clientId) {
        return serializedCommandJsonFromApiJsonWithSubResource(null, json, clientId);
    }

    public String serializedCommandJsonFromApiJsonWithSubResource(final Long noteId, final String json, final Long clientId) {
        final ClientNoteCommand command = commandFromApiJson(noteId, json);
        final ClientNoteCommand commandWithSubResource = ClientNoteCommand.clientSubResource(command, clientId);
        return this.commandSerializerService.serializeCommandToJson(commandWithSubResource);
    }
}