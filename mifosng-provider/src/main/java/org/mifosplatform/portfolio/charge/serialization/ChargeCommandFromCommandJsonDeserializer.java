package org.mifosplatform.portfolio.charge.serialization;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public final class ChargeCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<ChargeDefinitionCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public ChargeCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public ChargeDefinitionCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public ChargeDefinitionCommand commandFromCommandJson(final Long chargeDefinitionId, final String commandAsJson) {
        return commandFromCommandJson(chargeDefinitionId, commandAsJson, false);
    }

    @Override
    public ChargeDefinitionCommand commandFromCommandJson(final Long chargeDefinitionId, final String commandAsJson,
            final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);

        final String name = fromJsonHelper.extractStringNamed("name", element, parametersPassedInRequest);
        final String currencyCode = fromJsonHelper.extractStringNamed("currencyCode", element, parametersPassedInRequest);
        final BigDecimal amount = fromJsonHelper.extractBigDecimalNamed("amount", element.getAsJsonObject(), parametersPassedInRequest);
        final Integer chargeTimeType = fromJsonHelper.extractIntegerNamed("chargeTimeType", element, parametersPassedInRequest);
        final Integer chargeAppliesTo = fromJsonHelper.extractIntegerNamed("chargeAppliesTo", element, parametersPassedInRequest);
        final Integer chargeCalculationType = fromJsonHelper.extractIntegerNamed("chargeCalculationType", element,
                parametersPassedInRequest);
        final Boolean penalty = fromJsonHelper.extractBooleanNamed("penalty", element, parametersPassedInRequest);
        final Boolean active = fromJsonHelper.extractBooleanNamed("active", element, parametersPassedInRequest);

        return new ChargeDefinitionCommand(parametersPassedInRequest, makerCheckerApproval, chargeDefinitionId, name, amount, currencyCode,
                chargeTimeType, chargeAppliesTo, chargeCalculationType, penalty, active);
    }
}