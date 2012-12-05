package org.mifosplatform.organisation.office.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.office.command.OfficeCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link OfficeCommand} 
 * 's.
 */
@Component
public final class OfficeCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<OfficeCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name", "parentId", "openingDate", "externalId", "locale", "dateFormat"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public OfficeCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper, final CommandSerializer commandSerializerService) {
        super(commandSerializerService);
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public OfficeCommand commandFromApiJson(final Long officeId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromApiJsonHelper.parse(json);
        final String name = fromApiJsonHelper.extractStringNamed("name", element, parametersPassedInRequest);
        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element, parametersPassedInRequest);
        final Long parentId = fromApiJsonHelper.extractLongNamed("parentId", element, parametersPassedInRequest);
        final LocalDate openingLocalDate = fromApiJsonHelper.extractLocalDateNamed("openingDate", element, parametersPassedInRequest);

        return new OfficeCommand(parametersPassedInRequest, false, officeId, name, externalId, parentId, openingLocalDate);
    }
}