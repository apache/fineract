package org.mifosplatform.portfolio.loanproduct.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link LoanProductCommand}'s.
 */
@Component
public final class LoanProductCommandFromApiJsonDeserializer implements FromApiJsonDeserializer<LoanProductCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name", "description", "fundId",
            "transactionProcessingStrategyId", "currencyCode", "digitsAfterDecimal", "principal", "inArrearsTolerance",
            "interestRatePerPeriod", "repaymentEvery", "numberOfRepayments", "repaymentFrequencyType", "interestRateFrequencyType",
            "amortizationType", "interestType", "interestCalculationPeriodType", "charges", "locale"));

    private final FromJsonHelper fromApiJsonHelper;
    private final CommandSerializer commandSerializerService;

    @Autowired
    public LoanProductCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final CommandSerializer commandSerializerService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public LoanProductCommand commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public LoanProductCommand commandFromApiJson(final Long loanProductId, final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromApiJsonHelper.parse(json);
        final String name = fromApiJsonHelper.extractStringNamed("name", element, parametersPassedInRequest);
        final String description = fromApiJsonHelper.extractStringNamed("description", element, parametersPassedInRequest);
        final Long fundId = fromApiJsonHelper.extractLongNamed("fundId", element, parametersPassedInRequest);

        final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId", element,
                parametersPassedInRequest);

        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element, parametersPassedInRequest);
        final Integer digitsAfterDecimal = fromApiJsonHelper.extractIntegerWithLocaleNamed("digitsAfterDecimal", element,
                parametersPassedInRequest);

        final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element, parametersPassedInRequest);
        final BigDecimal inArrearsTolerance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element,
                parametersPassedInRequest);
        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element,
                parametersPassedInRequest);
        final Integer repaymentEvery = fromApiJsonHelper
                .extractIntegerWithLocaleNamed("repaymentEvery", element, parametersPassedInRequest);
        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element,
                parametersPassedInRequest);
        final Integer repaymentFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyType", element,
                parametersPassedInRequest);
        final Integer interestRateFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestRateFrequencyType", element,
                parametersPassedInRequest);
        final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed("amortizationType", element,
                parametersPassedInRequest);
        final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element, parametersPassedInRequest);
        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationPeriodType",
                element, parametersPassedInRequest);
        final String[] charges = fromApiJsonHelper.extractArrayNamed("charges", element, parametersPassedInRequest);

        return new LoanProductCommand(parametersPassedInRequest, false, loanProductId, name, description, fundId,
                transactionProcessingStrategyId, currencyCode, digitsAfterDecimal, principal, inArrearsTolerance, numberOfRepayments,
                repaymentEvery, interestRatePerPeriod, repaymentFrequencyType, interestRateFrequencyType, amortizationType, interestType,
                interestCalculationPeriodType, charges);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final LoanProductCommand command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long loanProductId, final String json) {
        final LoanProductCommand command = commandFromApiJson(loanProductId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}