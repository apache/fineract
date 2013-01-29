package org.mifosplatform.accounting.api.infrastructure.impl;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.api.commands.GLAccountCommand;
import org.mifosplatform.accounting.api.commands.GLClosureCommand;
import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;
import org.mifosplatform.accounting.api.commands.SingleDebitOrCreditEntryCommand;
import org.mifosplatform.accounting.api.infrastructure.AccountingApiDataConversionService;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Service specifically for converting JSON requests into command objects for
 * accounting API.
 */
@Service
public class AccountingApiDataConversionServiceImpl implements AccountingApiDataConversionService {

    private final Gson gsonConverter;

    public AccountingApiDataConversionServiceImpl() {
        gsonConverter = new Gson();
    }

    private void checkForUnsupportedParameters(Map<String, ?> requestMap, Set<String> supportedParams) {
        List<String> unsupportedParameterList = new ArrayList<String>();
        for (String providedParameter : requestMap.keySet()) {
            if (!supportedParams.contains(providedParameter)) {
                unsupportedParameterList.add(providedParameter);
            }
        }

        if (!unsupportedParameterList.isEmpty()) { throw new UnsupportedParameterException(unsupportedParameterList); }
    }

    @Override
    public GLAccountCommand convertJsonToGLAccountCommand(final Long resourceIdentifier, final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);

        final Set<String> supportedParams = new HashSet<String>(Arrays.asList("name", "parentId", "glCode", "disabled",
                "manualEntriesAllowed", "classification", "usage", "description", "locale", "dateFormat"));

        checkForUnsupportedParameters(requestMap, supportedParams);

        final JsonParser parser = new JsonParser();
        final JsonElement element = parser.parse(json);
        final JsonParserHelper helper = new JsonParserHelper();

        final Set<String> requestParamatersDetected = new HashSet<String>();
        final String name = helper.extractStringNamed("name", element, requestParamatersDetected);
        final Long parentId = helper.extractLongNamed("parentId", element, requestParamatersDetected);
        final String glCode = helper.extractStringNamed("glCode", element, requestParamatersDetected);
        final Boolean disabled = helper.extractBooleanNamed("disabled", element, requestParamatersDetected);
        final Boolean manualEntriesAllowed = helper.extractBooleanNamed("manualEntriesAllowed", element, requestParamatersDetected);
        final Integer category = helper.extractIntegerSansLocaleNamed("classification", element, requestParamatersDetected);
        final Integer usage = helper.extractIntegerSansLocaleNamed("usage", element, requestParamatersDetected);
        final String description = helper.extractStringNamed("description", element, requestParamatersDetected);

        return new GLAccountCommand(requestParamatersDetected, resourceIdentifier, name, parentId, glCode, disabled, manualEntriesAllowed,
                category, usage, description);
    }

    @Override
    public GLClosureCommand convertJsonToGLClosureCommand(Long resourceIdentifier, String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);

        final Set<String> supportedParams = new HashSet<String>(
                Arrays.asList("officeId", "closingDate", "comments", "locale", "dateFormat"));

        checkForUnsupportedParameters(requestMap, supportedParams);

        final JsonParser parser = new JsonParser();
        final JsonElement element = parser.parse(json);
        final JsonParserHelper helper = new JsonParserHelper();

        final Set<String> requestParamatersDetected = new HashSet<String>();
        final Long officeId = helper.extractLongNamed("officeId", element, requestParamatersDetected);
        final String comments = helper.extractStringNamed("comments", element, requestParamatersDetected);
        final LocalDate closingDate = helper.extractLocalDateNamed("closingDate", element, requestParamatersDetected);

        return new GLClosureCommand(requestParamatersDetected, resourceIdentifier, officeId, closingDate, comments);
    }

    @Override
    public GLJournalEntryCommand convertJsonToGLJournalEntryCommand(String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);

        final Set<String> supportedParams = new HashSet<String>(Arrays.asList("officeId", "entryDate", "comments", "credits", "debits",
                "locale", "dateFormat"));

        checkForUnsupportedParameters(requestMap, supportedParams);

        final JsonParser parser = new JsonParser();
        final JsonElement element = parser.parse(json);
        final JsonParserHelper helper = new JsonParserHelper();

        final Set<String> requestParamatersDetected = new HashSet<String>();
        final Long officeId = helper.extractLongNamed("officeId", element, requestParamatersDetected);
        final String comments = helper.extractStringNamed("comments", element, requestParamatersDetected);
        final LocalDate entryDate = helper.extractLocalDateNamed("entryDate", element, requestParamatersDetected);

        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = helper.extractLocaleParameter(topLevelJsonElement);

        SingleDebitOrCreditEntryCommand[] credits = null;
        SingleDebitOrCreditEntryCommand[] debits = null;
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has("credits") && topLevelJsonElement.get("credits").isJsonArray()) {
                credits = populateCreditsOrDebitsArray(topLevelJsonElement, helper, requestParamatersDetected, locale, credits, "credits");
            }
            if (topLevelJsonElement.has("debits") && topLevelJsonElement.get("debits").isJsonArray()) {
                debits = populateCreditsOrDebitsArray(topLevelJsonElement, helper, requestParamatersDetected, locale, debits, "debits");
            }
        }
        return new GLJournalEntryCommand(requestParamatersDetected, officeId, entryDate, comments, credits, debits);
    }

    /**
     * @param helper
     * @param requestParamatersDetected
     * @param comments
     * @param topLevelJsonElement
     * @param locale
     */
    private SingleDebitOrCreditEntryCommand[] populateCreditsOrDebitsArray(final JsonObject topLevelJsonElement,
            final JsonParserHelper helper, final Set<String> requestParamatersDetected, final Locale locale,
            SingleDebitOrCreditEntryCommand[] debitOrCredits, String paramName) {
        requestParamatersDetected.add(paramName);
        final JsonArray array = topLevelJsonElement.get(paramName).getAsJsonArray();
        debitOrCredits = new SingleDebitOrCreditEntryCommand[array.size()];
        for (int i = 0; i < array.size(); i++) {

            final JsonObject creditElement = array.get(i).getAsJsonObject();
            final Set<String> parametersPassedInForCreditsCommand = new HashSet<String>();

            final Long glAccountId = helper.extractLongNamed("glAccountId", creditElement, parametersPassedInForCreditsCommand);
            final String comments = helper.extractStringNamed("comments", creditElement, parametersPassedInForCreditsCommand);
            final BigDecimal amount = helper.extractBigDecimalNamed("amount", creditElement, locale, parametersPassedInForCreditsCommand);

            debitOrCredits[i] = new SingleDebitOrCreditEntryCommand(parametersPassedInForCreditsCommand, glAccountId, amount, comments);
        }
        return debitOrCredits;
    }
}