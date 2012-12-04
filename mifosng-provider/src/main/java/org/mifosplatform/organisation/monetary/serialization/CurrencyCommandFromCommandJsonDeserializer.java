package org.mifosplatform.organisation.monetary.serialization;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.monetary.command.CurrencyCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public final class CurrencyCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<CurrencyCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public CurrencyCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CurrencyCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public CurrencyCommand commandFromCommandJson(final Long resourceId, final String commandAsJson) {
        return commandFromCommandJson(resourceId, commandAsJson, false);
    }

    @Override
    public CurrencyCommand commandFromCommandJson(@SuppressWarnings("unused") final Long resourceId, final String commandAsJson,
            final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);
        final String[] currencies = fromJsonHelper.extractArrayNamed("currencies", element, parametersPassedInRequest);
        return new CurrencyCommand(makerCheckerApproval, currencies);
    }
}