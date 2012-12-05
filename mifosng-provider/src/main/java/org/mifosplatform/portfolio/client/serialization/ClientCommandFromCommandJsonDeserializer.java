package org.mifosplatform.portfolio.client.serialization;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public final class ClientCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<ClientCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public ClientCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public ClientCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public ClientCommand commandFromCommandJson(final Long clientId, final String commandAsJson) {
        return commandFromCommandJson(clientId, commandAsJson, false);
    }

    @Override
    public ClientCommand commandFromCommandJson(final Long clientId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);
        final Long officeId = fromJsonHelper.extractLongNamed("officeId", element, parametersPassedInRequest);
        final String externalId = fromJsonHelper.extractStringNamed("externalId", element, parametersPassedInRequest);
        final String firstname = fromJsonHelper.extractStringNamed("firstname", element, parametersPassedInRequest);
        final String lastname = fromJsonHelper.extractStringNamed("lastname", element, parametersPassedInRequest);
        final String clientOrBusinessName = fromJsonHelper.extractStringNamed("clientOrBusinessName", element, parametersPassedInRequest);
        final LocalDate joiningDate = fromJsonHelper.extractLocalDateAsArrayNamed("joiningDate", element, parametersPassedInRequest);

        return new ClientCommand(parametersPassedInRequest, clientId, externalId, firstname, lastname, clientOrBusinessName, officeId,
                joiningDate, makerCheckerApproval);
    }
}