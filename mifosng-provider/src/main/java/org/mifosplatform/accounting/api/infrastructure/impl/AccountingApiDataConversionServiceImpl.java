package org.mifosplatform.accounting.api.infrastructure.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.api.commands.GLAccountCommand;
import org.mifosplatform.accounting.api.commands.GLClosureCommand;
import org.mifosplatform.accounting.api.infrastructure.AccountingApiDataConversionService;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
                "manualEntriesAllowed", "classification", "headerAccount", "description", "locale", "dateFormat"));

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
        final String category = helper.extractStringNamed("category", element, requestParamatersDetected);
        final Boolean headerAccount = helper.extractBooleanNamed("headerAccount", element, requestParamatersDetected);
        final String description = helper.extractStringNamed("description", element, requestParamatersDetected);

        return new GLAccountCommand(requestParamatersDetected, resourceIdentifier, name, parentId, glCode, disabled, manualEntriesAllowed,
                category, headerAccount, description);
    }

    @Override
    public GLClosureCommand convertJsonToGLClosureCommand(Long resourceIdentifier, String json) {
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
}