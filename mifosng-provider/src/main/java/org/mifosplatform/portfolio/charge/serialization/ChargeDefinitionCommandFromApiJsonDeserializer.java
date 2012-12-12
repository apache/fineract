package org.mifosplatform.portfolio.charge.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
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
public final class ChargeDefinitionCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<ChargeDefinitionCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name", "amount", "locale", "currencyCode",
            "currencyOptions", "chargeAppliesTo", "chargeTimeType", "chargeCalculationType", "chargeCalculationTypeOptions", "penalty",
            "active"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ChargeDefinitionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public ChargeDefinitionCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String name = fromApiJsonHelper.extractStringNamed("name", element);
        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element);
        final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());

        final Integer chargeTimeType = fromApiJsonHelper.extractIntegerWithLocaleNamed("chargeTimeType", element);
        final Integer chargeAppliesTo = fromApiJsonHelper.extractIntegerWithLocaleNamed("chargeAppliesTo", element);
        final Integer chargeCalculationType = fromApiJsonHelper.extractIntegerWithLocaleNamed("chargeCalculationType", element);

        final Boolean penalty = fromApiJsonHelper.extractBooleanNamed("penalty", element);
        final Boolean active = fromApiJsonHelper.extractBooleanNamed("active", element);

        return new ChargeDefinitionCommand(name, amount, currencyCode, chargeTimeType, chargeAppliesTo, chargeCalculationType, penalty,
                active);
    }
}