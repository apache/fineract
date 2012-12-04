package org.mifosplatform.organisation.office.serialization;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.office.command.OfficeCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public final class OfficeCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<OfficeCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public OfficeCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public OfficeCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public OfficeCommand commandFromCommandJson(final Long officeId, final String commandAsJson) {
        return commandFromCommandJson(officeId, commandAsJson, false);
    }

    @Override
    public OfficeCommand commandFromCommandJson(final Long officeId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);

        final String name = fromJsonHelper.extractStringNamed("name", element, parametersPassedInRequest);
        final String externalId = fromJsonHelper.extractStringNamed("externalId", element, parametersPassedInRequest);
        final Long parentId = fromJsonHelper.extractLongNamed("parentId", element, parametersPassedInRequest);
        final LocalDate openingLocalDate = fromJsonHelper.extractLocalDateAsArrayNamed("openingDate", element, parametersPassedInRequest);

        return new OfficeCommand(parametersPassedInRequest, makerCheckerApproval, officeId, name, externalId, parentId, openingLocalDate);
    }
}