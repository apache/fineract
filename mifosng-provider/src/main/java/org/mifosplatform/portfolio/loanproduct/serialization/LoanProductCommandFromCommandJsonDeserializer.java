package org.mifosplatform.portfolio.loanproduct.serialization;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public final class LoanProductCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<LoanProductCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public LoanProductCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public LoanProductCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public LoanProductCommand commandFromCommandJson(final Long loanProductId, final String commandAsJson) {
        return commandFromCommandJson(loanProductId, commandAsJson, false);
    }

    @Override
    public LoanProductCommand commandFromCommandJson(final Long loanProductId, final String commandAsJson,
            final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);

        final String name = fromJsonHelper.extractStringNamed("name", element, parametersPassedInRequest);
        final String description = fromJsonHelper.extractStringNamed("description", element, parametersPassedInRequest);
        final Long fundId = fromJsonHelper.extractLongNamed("fundId", element, parametersPassedInRequest);
        final Long transactionProcessingStrategyId = fromJsonHelper.extractLongNamed("transactionProcessingStrategyId", element,
                parametersPassedInRequest);
        final String currencyCode = fromJsonHelper.extractStringNamed("currencyCode", element, parametersPassedInRequest);
        final Integer digitsAfterDecimal = fromJsonHelper.extractIntegerNamed("digitsAfterDecimal", element, parametersPassedInRequest);

        final BigDecimal principal = fromJsonHelper.extractBigDecimalNamed("principal", element.getAsJsonObject(),
                parametersPassedInRequest);
        final BigDecimal inArrearsTolerance = fromJsonHelper.extractBigDecimalNamed("inArrearsTolerance", element.getAsJsonObject(),
                parametersPassedInRequest);
        final BigDecimal interestRatePerPeriod = fromJsonHelper.extractBigDecimalNamed("interestRatePerPeriod", element.getAsJsonObject(),
                parametersPassedInRequest);
        final Integer repaymentEvery = fromJsonHelper.extractIntegerNamed("repaymentEvery", element, parametersPassedInRequest);
        final Integer numberOfRepayments = fromJsonHelper.extractIntegerNamed("numberOfRepayments", element, parametersPassedInRequest);
        final Integer repaymentFrequencyType = fromJsonHelper.extractIntegerNamed("repaymentFrequencyType", element,
                parametersPassedInRequest);
        final Integer interestRateFrequencyType = fromJsonHelper.extractIntegerNamed("interestRateFrequencyType", element,
                parametersPassedInRequest);
        final Integer amortizationType = fromJsonHelper.extractIntegerNamed("amortizationType", element, parametersPassedInRequest);
        final Integer interestType = fromJsonHelper.extractIntegerNamed("interestType", element, parametersPassedInRequest);
        final Integer interestCalculationPeriodType = fromJsonHelper.extractIntegerNamed("interestCalculationPeriodType", element,
                parametersPassedInRequest);
        final String[] charges = fromJsonHelper.extractArrayNamed("charges", element, parametersPassedInRequest);

        return new LoanProductCommand(parametersPassedInRequest, makerCheckerApproval, loanProductId, name, description, fundId,
                transactionProcessingStrategyId, currencyCode, digitsAfterDecimal, principal, inArrearsTolerance, numberOfRepayments,
                repaymentEvery, interestRatePerPeriod, repaymentFrequencyType, interestRateFrequencyType, amortizationType, interestType,
                interestCalculationPeriodType, charges);
    }
}