package org.mifosplatform.portfolio.client.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link ClientCommand} 
 * 's.
 */
@Component
public final class ClientCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<ClientCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "externalId", "firstname", "lastname",
            "clientOrBusinessName", "officeId", "joiningDate", "locale", "dateFormat"));

    private final FromJsonHelper fromApiJsonHelper;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public ClientCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper, final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public ClientCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public ClientCommand commandFromApiJson(final Long clientId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromApiJsonHelper.parse(json);
        final Long officeId = fromApiJsonHelper.extractLongNamed("officeId", element, parametersPassedInRequest);
        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element, parametersPassedInRequest);
        final String firstname = fromApiJsonHelper.extractStringNamed("firstname", element, parametersPassedInRequest);
        final String lastname = fromApiJsonHelper.extractStringNamed("lastname", element, parametersPassedInRequest);
        final String clientOrBusinessName = fromApiJsonHelper
                .extractStringNamed("clientOrBusinessName", element, parametersPassedInRequest);
        final LocalDate joiningDate = fromApiJsonHelper.extractLocalDateNamed("joiningDate", element, parametersPassedInRequest);

        return new ClientCommand(parametersPassedInRequest, clientId, externalId, firstname, lastname, clientOrBusinessName, officeId,
                joiningDate, false);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final ClientCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long fundId, final String json) {
        final ClientCommand command = commandFromApiJson(fundId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}