package org.mifosplatform.organisation.office.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
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
public final class BranchMoneyTransferCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<BranchMoneyTransferCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("fromOfficeId", "toOfficeId", "transactionDate",
            "currencyCode", "transactionAmount", "description", "locale", "dateFormat"));

    private final FromJsonHelper fromApiJsonHelper;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public BranchMoneyTransferCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public BranchMoneyTransferCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public BranchMoneyTransferCommand commandFromApiJson(@SuppressWarnings("unused") final Long resourceId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromApiJsonHelper.parse(json);
        final Long fromOfficeId = fromApiJsonHelper.extractLongNamed("fromOfficeId", element, parametersPassedInRequest);
        final Long toOfficeId = fromApiJsonHelper.extractLongNamed("toOfficeId", element, parametersPassedInRequest);
        final LocalDate transactionLocalDate = fromApiJsonHelper.extractLocalDateNamed("transactionDate", element,
                parametersPassedInRequest);

        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element, parametersPassedInRequest);
        final BigDecimal transactionAmountValue = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element,
                parametersPassedInRequest);
        final String description = fromApiJsonHelper.extractStringNamed("description", element, parametersPassedInRequest);

        return new BranchMoneyTransferCommand(parametersPassedInRequest, false, fromOfficeId, toOfficeId, transactionLocalDate,
                currencyCode, transactionAmountValue, description);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final BranchMoneyTransferCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long roleId, final String json) {
        final BranchMoneyTransferCommand command = commandFromApiJson(roleId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}