package org.mifosplatform.portfolio.loanaccount.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanaccount.command.LoanApplicationCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link LoanApplicationCommand}'s.
 */
@Component
public final class LoanApplicationCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<LoanApplicationCommand> {

    /**
     * The parameters supported for this command.
     */
    final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("clientId", "groupId", "productId", "externalId", "fundId",
            "transactionProcessingStrategyId", "principal", "inArrearsTolerance", "interestRatePerPeriod", "repaymentEvery",
            "numberOfRepayments", "loanTermFrequency", "loanTermFrequencyType", "charges", "repaymentFrequencyType",
            "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType", "expectedDisbursementDate",
            "repaymentsStartingFromDate", "interestChargedFromDate", "submittedOnDate", "submittedOnNote", "locale", "dateFormat",
            "loanOfficerId", "id"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanApplicationCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public LoanApplicationCommand commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);
        final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long groupId = fromApiJsonHelper.extractLongNamed("groupId", element);
        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        final Long fundId = fromApiJsonHelper.extractLongNamed("fundId", element);
        final Long loanOfficerId = fromApiJsonHelper.extractLongNamed("loanOfficerId", element);
        final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId", element);

        final LocalDate submittedOnDate = fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);
        final String submittedOnNote = fromApiJsonHelper.extractStringNamed("submittedOnNote", element);

        final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
        final Integer interestRateFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestRateFrequencyType", element);
        final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element);
        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationPeriodType",
                element);

        // final InterestMethod interestMethod =
        // InterestMethod.fromInt(interestType);
        // final PeriodFrequencyType interestRatePeriodFrequencyType =
        // PeriodFrequencyType.fromInt(interestRateFrequencyType);
        // final InterestCalculationPeriodMethod interestCalculationPeriodMethod
        // = InterestCalculationPeriodMethod
        // .fromInt(interestCalculationPeriodType);

        final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed("amortizationType", element);
        final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
        final Integer repaymentFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyType", element);
        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        // final PeriodFrequencyType repaymentPeriodFrequencyType =
        // PeriodFrequencyType.fromInt(repaymentFrequencyType);
        // final AmortizationMethod amortizationMethod =
        // AmortizationMethod.fromInt(amortizationType);

        final Integer loanTermFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequency", element);
        final Integer loanTermFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequencyType", element);
        // final PeriodFrequencyType loanTermPeriodFrequencyType =
        // PeriodFrequencyType.fromInt(loanTermFrequencyType);

        final LocalDate expectedDisbursementDate = fromApiJsonHelper.extractLocalDateNamed("expectedDisbursementDate", element);
        final LocalDate repaymentsStartingFromDate = fromApiJsonHelper.extractLocalDateNamed("repaymentsStartingFromDate", element);
        final LocalDate interestChargedFromDate = fromApiJsonHelper.extractLocalDateNamed("interestChargedFromDate", element);

        final BigDecimal inArrearsTolerance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);

        // ///
        LoanChargeCommand[] charges = null;
        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.has("charges") && topLevelJsonElement.get("charges").isJsonArray()) {

                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                charges = new LoanChargeCommand[array.size()];
                for (int i = 0; i < array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();
                    final Set<String> parametersPassedInForChargesCommand = new HashSet<String>();

                    final Long id = fromApiJsonHelper.extractLongNamed("id", loanChargeElement);
                    final Long chargeId = fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    final BigDecimal amount = fromApiJsonHelper.extractBigDecimalNamed("amount", loanChargeElement, locale);
                    final Integer chargeTimeType = fromApiJsonHelper.extractIntegerNamed("chargeTimeType", loanChargeElement, locale);
                    final Integer chargeCalculationType = fromApiJsonHelper.extractIntegerNamed("chargeCalculationType", loanChargeElement,
                            locale);
                    final LocalDate specifiedDueDate = fromApiJsonHelper.extractLocalDateNamed("specifiedDueDate", loanChargeElement,
                            dateFormat, locale);

                    charges[i] = new LoanChargeCommand(parametersPassedInForChargesCommand, id, null, chargeId, amount, chargeTimeType,
                            chargeCalculationType, specifiedDueDate);
                }
            }
        }

        return new LoanApplicationCommand(new HashSet<String>(), null, clientId, groupId, productId, externalId, fundId,
                transactionProcessingStrategyId, submittedOnDate, submittedOnNote, expectedDisbursementDate, repaymentsStartingFromDate,
                interestChargedFromDate, principal, interestRatePerPeriod, interestRateFrequencyType, interestType,
                interestCalculationPeriodType, repaymentEvery, repaymentFrequencyType, numberOfRepayments, amortizationType,
                loanTermFrequency, loanTermFrequencyType, inArrearsTolerance, charges, loanOfficerId);
    }
}