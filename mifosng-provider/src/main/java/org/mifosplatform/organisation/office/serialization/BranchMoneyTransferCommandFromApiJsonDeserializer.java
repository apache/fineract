package org.mifosplatform.organisation.office.serialization;

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
import org.mifosplatform.organisation.office.command.BranchMoneyTransferCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link BranchMoneyTransferCommand} 's.
 */
@Component
public final class BranchMoneyTransferCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<BranchMoneyTransferCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("fromOfficeId", "toOfficeId", "transactionDate",
            "currencyCode", "transactionAmount", "description", "locale", "dateFormat"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public BranchMoneyTransferCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public BranchMoneyTransferCommand commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);
        final Long fromOfficeId = fromApiJsonHelper.extractLongNamed("fromOfficeId", element);
        final Long toOfficeId = fromApiJsonHelper.extractLongNamed("toOfficeId", element);
        final LocalDate transactionLocalDate = fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);

        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element);
        final BigDecimal transactionAmountValue = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        final String description = fromApiJsonHelper.extractStringNamed("description", element);

        return new BranchMoneyTransferCommand(fromOfficeId, toOfficeId, transactionLocalDate, currencyCode, transactionAmountValue,
                description);
    }
}