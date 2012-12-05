package org.mifosplatform.portfolio.charge.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.fund.command.FundCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link FundCommand}'s.
 */
@Component
public final class ChargeDefinitionCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<ChargeDefinitionCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name", "amount", "locale", "currencyCode",
            "currencyOptions", "chargeAppliesTo", "chargeTimeType", "chargeCalculationType", "chargeCalculationTypeOptions", "penalty",
            "active"));

    private final FromJsonHelper fromApiJsonHelper;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public ChargeDefinitionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public ChargeDefinitionCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public ChargeDefinitionCommand commandFromApiJson(final Long chargeId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String name = fromApiJsonHelper.extractStringNamed("name", element, parametersPassedInRequest);
        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element, parametersPassedInRequest);
        final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject(),
                parametersPassedInRequest);

        final Integer chargeTimeType = fromApiJsonHelper
                .extractIntegerWithLocaleNamed("chargeTimeType", element, parametersPassedInRequest);
        final Integer chargeAppliesTo = fromApiJsonHelper.extractIntegerWithLocaleNamed("chargeAppliesTo", element,
                parametersPassedInRequest);
        final Integer chargeCalculationType = fromApiJsonHelper.extractIntegerWithLocaleNamed("chargeCalculationType", element,
                parametersPassedInRequest);

        final Boolean penalty = fromApiJsonHelper.extractBooleanNamed("penalty", element, parametersPassedInRequest);
        final Boolean active = fromApiJsonHelper.extractBooleanNamed("active", element, parametersPassedInRequest);

        return new ChargeDefinitionCommand(parametersPassedInRequest, false, chargeId, name, amount, currencyCode, chargeTimeType,
                chargeAppliesTo, chargeCalculationType, penalty, active);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final ChargeDefinitionCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long roleId, final String json) {
        final ChargeDefinitionCommand command = commandFromApiJson(roleId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}