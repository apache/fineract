package org.mifosplatform.infrastructure.core.api;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.organisation.monetary.command.CurrencyCommand;
import org.mifosplatform.organisation.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.organisation.office.command.OfficeCommand;
import org.mifosplatform.organisation.staff.command.StaffCommand;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.fund.command.FundCommand;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.mifosplatform.useradministration.command.RoleCommand;
import org.mifosplatform.useradministration.command.RolePermissionCommand;
import org.mifosplatform.useradministration.command.UserCommand;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
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
    private final Gson gsonConverter;

    public PortfolioCommandDeerializerServiceGoogleGson() {
        parser = new JsonParser();
        gsonConverter = new Gson();
    }

    @Override
    public RoleCommand deserializeRoleCommand(final Long roleId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final String name = helper.extractStringNamed("name", element, parametersPassedInRequest);
        final String description = helper.extractStringNamed("description", element, parametersPassedInRequest);

        return new RoleCommand(parametersPassedInRequest, makerCheckerApproval, roleId, name, description);
    }

    @Override
    public RolePermissionCommand deserializeRolePermissionCommand(final Long roleId, final String commandAsJson,
            final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final RolePermissionCommand command = gsonConverter.fromJson(commandAsJson, RolePermissionCommand.class);

        return new RolePermissionCommand(roleId, command.getPermissions(), makerCheckerApproval);
    }

    @Override
    public PermissionsCommand deserializePermissionsCommand(final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final PermissionsCommand command = gsonConverter.fromJson(commandAsJson, PermissionsCommand.class);

        return new PermissionsCommand(command.getPermissions(), makerCheckerApproval);
    }

    @Override
    public UserCommand deserializeUserCommand(final Long userId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final String username = helper.extractStringNamed("username", element, parametersPassedInRequest);
        final String firstname = helper.extractStringNamed("firstname", element, parametersPassedInRequest);

        final String lastname = helper.extractStringNamed("lastname", element, parametersPassedInRequest);
        final String password = helper.extractStringNamed("password", element, parametersPassedInRequest);
        final String repeatPassword = helper.extractStringNamed("repeatPassword", element, parametersPassedInRequest);
        final String email = helper.extractStringNamed("email", element, parametersPassedInRequest);
        final Long officeId = helper.extractLongNamed("officeId", element, parametersPassedInRequest);

        // check array
        String[] notSelectedRoles = null;
        String[] roles = null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("notSelectedRoles")) {
                parametersPassedInRequest.add("notSelectedRoles");
                JsonArray array = object.get("notSelectedRoles").getAsJsonArray();
                notSelectedRoles = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    notSelectedRoles[i] = array.get(i).getAsString();
                }
            }

            if (object.has("roles")) {
                parametersPassedInRequest.add("roles");
                JsonArray array = object.get("roles").getAsJsonArray();
                roles = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    roles[i] = array.get(i).getAsString();
                }
            }
        }

        return new UserCommand(parametersPassedInRequest, makerCheckerApproval, userId, username, firstname, lastname, password,
                repeatPassword, email, officeId, notSelectedRoles, roles);
    }

    @Override
    public CodeCommand deserializeCodeCommand(final Long codeId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final String name = helper.extractStringNamed("name", element, parametersPassedInRequest);

        return new CodeCommand(parametersPassedInRequest, makerCheckerApproval, codeId, name);
    }

    @Override
    public StaffCommand deserializeStaffCommand(final Long staffId, final String commandAsJson, final boolean makerCheckerApproval) {

        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final String firstname = helper.extractStringNamed("firstname", element, parametersPassedInRequest);
        final String lastname = helper.extractStringNamed("lastname", element, parametersPassedInRequest);
        final Boolean isLoanOfficer = helper.extractBooleanNamed("isLoanOfficer", element, parametersPassedInRequest);
        final Long officeId = helper.extractLongNamed("officeId", element, parametersPassedInRequest);

        return new StaffCommand(parametersPassedInRequest, makerCheckerApproval, staffId, officeId, firstname, lastname, isLoanOfficer);
    }

    @Override
    public FundCommand deserializeFundCommand(final Long fundId, final String commandAsJson, final boolean makerCheckerApproval) {

        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final String name = helper.extractStringNamed("name", element, parametersPassedInRequest);
        final String externalId = helper.extractStringNamed("externalId", element, parametersPassedInRequest);

        return new FundCommand(parametersPassedInRequest, makerCheckerApproval, fundId, name, externalId);
    }

    @Override
    public OfficeCommand deserializeOfficeCommand(final Long officeId, final String commandAsJson, final boolean makerCheckerApproval) {

        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final String name = helper.extractStringNamed("name", element, parametersPassedInRequest);
        final String externalId = helper.extractStringNamed("externalId", element, parametersPassedInRequest);
        final Long parentId = helper.extractLongNamed("parentId", element, parametersPassedInRequest);
        final LocalDate openingLocalDate = helper.extractLocalDateAsArrayNamed("openingDate", element, parametersPassedInRequest);

        return new OfficeCommand(parametersPassedInRequest, makerCheckerApproval, officeId, name, externalId, parentId, openingLocalDate);
    }

    @Override
    public BranchMoneyTransferCommand deserializeOfficeTransactionCommand(final String commandAsJson, final boolean makerCheckerApproval) {

        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final Long fromOfficeId = helper.extractLongNamed("fromOfficeId", element, parametersPassedInRequest);
        final Long toOfficeId = helper.extractLongNamed("toOfficeId", element, parametersPassedInRequest);
        final LocalDate transactionLocalDate = helper.extractLocalDateAsArrayNamed("transactionDate", element, parametersPassedInRequest);
        final String currencyCode = helper.extractStringNamed("currencyCode", element, parametersPassedInRequest);
        final BigDecimal transactionAmount = helper.extractBigDecimalNamed("transactionAmount", element.getAsJsonObject(), Locale.US,
                parametersPassedInRequest);
        final String description = helper.extractStringNamed("description", element, parametersPassedInRequest);

        return new BranchMoneyTransferCommand(parametersPassedInRequest, makerCheckerApproval, fromOfficeId, toOfficeId,
                transactionLocalDate, currencyCode, transactionAmount, description);
    }

    @Override
    public CurrencyCommand deserializeCurrencyCommand(final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final String[] currencies = helper.extractArrayNamed("currencies", element, parametersPassedInRequest);

        return new CurrencyCommand(makerCheckerApproval, currencies);
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