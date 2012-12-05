package org.mifosplatform.portfolio.fund.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.fund.command.FundCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link FundCommand}'s.
 */
@Component
public final class FundCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<FundCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name", "externalId"));

    private final FromJsonHelper fromApiJsonHelper;
    private final FundCommandFromCommandJsonDeserializer fromCommandJsonDeserializer;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public FundCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final FundCommandFromCommandJsonDeserializer fromCommandJsonDeserializer, final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.fromCommandJsonDeserializer = fromCommandJsonDeserializer;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public FundCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public FundCommand commandFromApiJson(final Long fundId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        return this.fromCommandJsonDeserializer.commandFromCommandJson(fundId, json);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final FundCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long fundId, final String json) {
        final FundCommand command = commandFromApiJson(fundId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}