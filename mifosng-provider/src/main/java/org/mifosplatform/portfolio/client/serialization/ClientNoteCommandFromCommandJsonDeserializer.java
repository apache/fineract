package org.mifosplatform.portfolio.client.serialization;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.command.ClientNoteCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public final class ClientNoteCommandFromCommandJsonDeserializer extends AbstractFromCommandJsonDeserializer<ClientNoteCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public ClientNoteCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public ClientNoteCommand commandFromCommandJson(final Long noteId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);
        final Long clientId = fromJsonHelper.extractLongNamed("clientId", element, parametersPassedInRequest);
        final String note = fromJsonHelper.extractStringNamed("note", element, parametersPassedInRequest);

        return new ClientNoteCommand(parametersPassedInRequest, makerCheckerApproval, noteId, clientId, note);
    }
}