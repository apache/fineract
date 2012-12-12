package org.mifosplatform.portfolio.loanproduct.serialization;

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
public final class LoanProductCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<LoanProductCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name", "description", "fundId",
            "transactionProcessingStrategyId", "currencyCode", "digitsAfterDecimal", "principal", "inArrearsTolerance",
            "interestRatePerPeriod", "repaymentEvery", "numberOfRepayments", "repaymentFrequencyType", "interestRateFrequencyType",
            "amortizationType", "interestType", "interestCalculationPeriodType", "charges", "locale"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanProductCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public LoanProductCommand commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);
        final String name = fromApiJsonHelper.extractStringNamed("name", element);
        final String description = fromApiJsonHelper.extractStringNamed("description", element);
        final Long fundId = fromApiJsonHelper.extractLongNamed("fundId", element);

        final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId", element);

        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element);
        final Integer digitsAfterDecimal = fromApiJsonHelper.extractIntegerWithLocaleNamed("digitsAfterDecimal", element);

        final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        final BigDecimal inArrearsTolerance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
        final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        final Integer repaymentFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyType", element);
        final Integer interestRateFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestRateFrequencyType", element);
        final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed("amortizationType", element);
        final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element);
        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationPeriodType",
                element);
        final String[] charges = fromApiJsonHelper.extractArrayNamed("charges", element);

        return new LoanProductCommand(name, description, fundId, transactionProcessingStrategyId, currencyCode, digitsAfterDecimal,
                principal, inArrearsTolerance, numberOfRepayments, repaymentEvery, interestRatePerPeriod, repaymentFrequencyType,
                interestRateFrequencyType, amortizationType, interestType, interestCalculationPeriodType, charges);
    }
}