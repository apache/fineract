package org.mifosplatform.portfolio.loanaccount.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link LoanChargeCommand}'s.
 */
@Component
public final class LoanChargeCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<LoanChargeCommand> {

    /**
     * The parameters supported for this command.
     */
    final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("chargeId", "amount", "chargeTimeType",
            "chargeCalculationType", "specifiedDueDate", "locale", "dateFormat"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanChargeCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public LoanChargeCommand commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);
        final Long chargeId = fromApiJsonHelper.extractLongNamed("chargeId", element);
        final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        final Integer chargeTimeType = fromApiJsonHelper.extractIntegerWithLocaleNamed("chargeTimeType", element);
        final Integer chargeCalculationType = fromApiJsonHelper.extractIntegerWithLocaleNamed("chargeCalculationType", element);
        final LocalDate specifiedDueDate = fromApiJsonHelper.extractLocalDateNamed("specifiedDueDate", element);

        return new LoanChargeCommand(chargeId, amount, chargeTimeType, chargeCalculationType, specifiedDueDate);
    }
}