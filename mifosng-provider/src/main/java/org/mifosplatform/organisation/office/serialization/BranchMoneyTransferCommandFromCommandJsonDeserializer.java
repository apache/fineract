package org.mifosplatform.organisation.office.serialization;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.office.command.BranchMoneyTransferCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public final class BranchMoneyTransferCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<BranchMoneyTransferCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public BranchMoneyTransferCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public BranchMoneyTransferCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public BranchMoneyTransferCommand commandFromCommandJson(final Long resourceId, final String commandAsJson) {
        return commandFromCommandJson(resourceId, commandAsJson, false);
    }

    @Override
    public BranchMoneyTransferCommand commandFromCommandJson(@SuppressWarnings("unused") final Long resourceId, final String commandAsJson,
            final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);
        final Long fromOfficeId = fromJsonHelper.extractLongNamed("fromOfficeId", element, parametersPassedInRequest);
        final Long toOfficeId = fromJsonHelper.extractLongNamed("toOfficeId", element, parametersPassedInRequest);
        final LocalDate transactionLocalDate = fromJsonHelper.extractLocalDateAsArrayNamed("transactionDate", element, parametersPassedInRequest);
        final String currencyCode = fromJsonHelper.extractStringNamed("currencyCode", element, parametersPassedInRequest);
        final BigDecimal transactionAmountValue = fromJsonHelper.extractBigDecimalNamed("transactionAmount", element,
                parametersPassedInRequest);
        final String description = fromJsonHelper.extractStringNamed("description", element, parametersPassedInRequest);

        return new BranchMoneyTransferCommand(parametersPassedInRequest, makerCheckerApproval, fromOfficeId, toOfficeId,
                transactionLocalDate, currencyCode, transactionAmountValue, description);
    }
}