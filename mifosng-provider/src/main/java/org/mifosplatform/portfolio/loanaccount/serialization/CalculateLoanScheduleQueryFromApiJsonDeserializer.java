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
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.loanschedule.query.CalculateLoanScheduleQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link CalculateLoanScheduleQuery}'s.
 */
@Component
public final class CalculateLoanScheduleQueryFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<CalculateLoanScheduleQuery> {

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
    public CalculateLoanScheduleQueryFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CalculateLoanScheduleQuery commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);
        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
        final Integer interestRateFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestRateFrequencyType", element);
        final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element);
        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationPeriodType",
                element);

        final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
        final Integer repaymentFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyType", element);
        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed("amortizationType", element);
        final Integer loanTermFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequency", element);
        final Integer loanTermFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequencyType", element);
        final LocalDate expectedDisbursementDate = fromApiJsonHelper.extractLocalDateNamed("expectedDisbursementDate", element);
        final LocalDate repaymentsStartingFromDate = fromApiJsonHelper.extractLocalDateNamed("repaymentsStartingFromDate", element);
        final LocalDate interestChargedFromDate = fromApiJsonHelper.extractLocalDateNamed("interestChargedFromDate", element);

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

                    // final Long id = fromApiJsonHelper.extractLongNamed("id",
                    // loanChargeElement);
                    final Long chargeId = fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    final BigDecimal amount = fromApiJsonHelper.extractBigDecimalNamed("amount", loanChargeElement, locale);
                    final Integer chargeTimeType = fromApiJsonHelper.extractIntegerNamed("chargeTimeType", loanChargeElement, locale);
                    final Integer chargeCalculationType = fromApiJsonHelper.extractIntegerNamed("chargeCalculationType", loanChargeElement,
                            locale);
                    final LocalDate specifiedDueDate = fromApiJsonHelper.extractLocalDateNamed("specifiedDueDate", loanChargeElement,
                            dateFormat, locale);

                    charges[i] = new LoanChargeCommand(chargeId, amount, chargeTimeType, chargeCalculationType, specifiedDueDate);
                }
            }
        }
        // /

        return new CalculateLoanScheduleQuery(productId, principal, interestRatePerPeriod, interestRateFrequencyType, interestType,
                interestCalculationPeriodType, repaymentEvery, repaymentFrequencyType, numberOfRepayments, amortizationType,
                loanTermFrequency, loanTermFrequencyType, expectedDisbursementDate, repaymentsStartingFromDate, interestChargedFromDate,
                charges);
    }
}