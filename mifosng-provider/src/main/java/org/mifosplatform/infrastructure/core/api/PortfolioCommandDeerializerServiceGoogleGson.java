package org.mifosplatform.infrastructure.core.api;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Implementation of {@link PortfolioCommandDeserializerService} that
 * de-serializes JSON of commands into Java object representation using
 * google-gson.
 */
@Service
public class PortfolioCommandDeerializerServiceGoogleGson implements PortfolioCommandDeserializerService {

    private final JsonParser parser;

    public PortfolioCommandDeerializerServiceGoogleGson() {
        parser = new JsonParser();
    }

    @Override
    public ChargeDefinitionCommand deserializeChargeDefinitionCommand(final Long chargeDefinitionId, final String commandAsJson,
            final boolean makerCheckerApproval) {

        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final String name = helper.extractStringNamed("name", element, parametersPassedInRequest);
        final String currencyCode = helper.extractStringNamed("currencyCode", element, parametersPassedInRequest);
        final BigDecimal amount = helper.extractBigDecimalNamed("amount", element.getAsJsonObject(), Locale.US, parametersPassedInRequest);

        final Integer chargeTimeType = helper.extractIntegerNamed("chargeTimeType", element, Locale.US, parametersPassedInRequest);
        final Integer chargeAppliesTo = helper.extractIntegerNamed("chargeAppliesTo", element, Locale.US, parametersPassedInRequest);
        final Integer chargeCalculationType = helper.extractIntegerNamed("chargeCalculationType", element, Locale.US,
                parametersPassedInRequest);

        final Boolean penalty = helper.extractBooleanNamed("penalty", element, parametersPassedInRequest);
        final Boolean active = helper.extractBooleanNamed("active", element, parametersPassedInRequest);

        return new ChargeDefinitionCommand(parametersPassedInRequest, makerCheckerApproval, chargeDefinitionId, name, amount, currencyCode,
                chargeTimeType, chargeAppliesTo, chargeCalculationType, penalty, active);
    }

    @Override
    public ClientCommand deserializeClientCommand(final Long clientId, final String commandAsJson, final boolean makerCheckerApproval) {

        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final Long officeId = helper.extractLongNamed("officeId", element, parametersPassedInRequest);
        final String externalId = helper.extractStringNamed("externalId", element, parametersPassedInRequest);
        final String firstname = helper.extractStringNamed("firstname", element, parametersPassedInRequest);
        final String lastname = helper.extractStringNamed("lastname", element, parametersPassedInRequest);
        final String clientOrBusinessName = helper.extractStringNamed("clientOrBusinessName", element, parametersPassedInRequest);
        final LocalDate joiningDate = helper.extractLocalDateAsArrayNamed("joiningDate", element, parametersPassedInRequest);

        return new ClientCommand(parametersPassedInRequest, clientId, externalId, firstname, lastname, clientOrBusinessName, officeId,
                joiningDate, makerCheckerApproval);
    }

    @Override
    public LoanProductCommand deserializeLoanProductCommand(final Long loanProductId, final String commandAsJson,
            final boolean makerCheckerApproval) {

        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final String name = helper.extractStringNamed("name", element, parametersPassedInRequest);
        final String description = helper.extractStringNamed("description", element, parametersPassedInRequest);
        final Long fundId = helper.extractLongNamed("fundId", element, parametersPassedInRequest);

        final Long transactionProcessingStrategyId = helper.extractLongNamed("transactionProcessingStrategyId", element,
                parametersPassedInRequest);

        final String currencyCode = helper.extractStringNamed("currencyCode", element, parametersPassedInRequest);
        final Integer digitsAfterDecimal = helper.extractIntegerNamed("digitsAfterDecimal", element, Locale.US, parametersPassedInRequest);

        final BigDecimal principal = helper.extractBigDecimalNamed("principal", element.getAsJsonObject(), Locale.US,
                parametersPassedInRequest);
        final BigDecimal inArrearsTolerance = helper.extractBigDecimalNamed("inArrearsTolerance", element.getAsJsonObject(), Locale.US,
                parametersPassedInRequest);
        final BigDecimal interestRatePerPeriod = helper.extractBigDecimalNamed("interestRatePerPeriod", element.getAsJsonObject(),
                Locale.US, parametersPassedInRequest);
        final Integer repaymentEvery = helper.extractIntegerNamed("repaymentEvery", element, Locale.US, parametersPassedInRequest);
        final Integer numberOfRepayments = helper.extractIntegerNamed("numberOfRepayments", element, Locale.US, parametersPassedInRequest);
        final Integer repaymentFrequencyType = helper.extractIntegerNamed("repaymentFrequencyType", element, Locale.US,
                parametersPassedInRequest);
        final Integer interestRateFrequencyType = helper.extractIntegerNamed("interestRateFrequencyType", element, Locale.US,
                parametersPassedInRequest);
        final Integer amortizationType = helper.extractIntegerNamed("amortizationType", element, Locale.US, parametersPassedInRequest);
        final Integer interestType = helper.extractIntegerNamed("interestType", element, Locale.US, parametersPassedInRequest);
        final Integer interestCalculationPeriodType = helper.extractIntegerNamed("interestCalculationPeriodType", element, Locale.US,
                parametersPassedInRequest);

        String[] charges = null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("charges")) {
                parametersPassedInRequest.add("charges");
                JsonArray array = object.get("charges").getAsJsonArray();
                charges = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    charges[i] = array.get(i).getAsString();
                }
            }
        }

        return new LoanProductCommand(parametersPassedInRequest, makerCheckerApproval, loanProductId, name, description, fundId,
                transactionProcessingStrategyId, currencyCode, digitsAfterDecimal, principal, inArrearsTolerance, numberOfRepayments,
                repaymentEvery, interestRatePerPeriod, repaymentFrequencyType, interestRateFrequencyType, amortizationType, interestType,
                interestCalculationPeriodType, charges);
    }

    @Override
    public ClientIdentifierCommand deserializeClientIdentifierCommand(final Long clientIdentifierId, final Long clientId,
            final String commandAsJson, final boolean makerCheckerApproval) {

        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        Long actualClientId = clientId;
        final Long clientIdInternal = helper.extractLongNamed("clientId", element, parametersPassedInRequest);
        if (clientId != null) {
            actualClientId = clientIdInternal;
        }
        final Long documentTypeId = helper.extractLongNamed("documentTypeId", element, parametersPassedInRequest);
        final String documentKey = helper.extractStringNamed("documentKey", element, parametersPassedInRequest);
        final String documentDescription = helper.extractStringNamed("documentDescription", element, parametersPassedInRequest);

        return new ClientIdentifierCommand(parametersPassedInRequest, makerCheckerApproval, clientIdentifierId, actualClientId,
                documentTypeId, documentKey, documentDescription);
    }
}