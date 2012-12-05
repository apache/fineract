package org.mifosplatform.portfolio.client.serialization;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public final class ClientIdentifierCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<ClientIdentifierCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public ClientIdentifierCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public ClientIdentifierCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public ClientIdentifierCommand commandFromCommandJson(final Long clientIdentifierId, final String commandAsJson) {
        return commandFromCommandJson(clientIdentifierId, commandAsJson, false);
    }

    @Override
    public ClientIdentifierCommand commandFromCommandJson(final Long clientIdentifierId, final String commandAsJson,
            final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);
        Long actualClientId = null; // FIXME - kw -clientId
        final Long clientIdInternal = fromJsonHelper.extractLongNamed("clientId", element, parametersPassedInRequest);
        if (clientIdInternal != null) {
            actualClientId = clientIdInternal;
        }
        final Long documentTypeId = fromJsonHelper.extractLongNamed("documentTypeId", element, parametersPassedInRequest);
        final String documentKey = fromJsonHelper.extractStringNamed("documentKey", element, parametersPassedInRequest);
        final String documentDescription = fromJsonHelper.extractStringNamed("documentDescription", element, parametersPassedInRequest);

        return new ClientIdentifierCommand(parametersPassedInRequest, makerCheckerApproval, clientIdentifierId, actualClientId,
                documentTypeId, documentKey, documentDescription);
    }
}