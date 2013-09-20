/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class FromJsonHelper {

    private final Gson gsonConverter;
    private final JsonParserHelper helperDelegator;
    private final JsonParser parser;

    public FromJsonHelper() {
        this.gsonConverter = new Gson();
        this.helperDelegator = new JsonParserHelper();
        this.parser = new JsonParser();
    }

    public Map<String, Boolean> extractMap(final Type typeOfMap, final String json) {
        return this.gsonConverter.fromJson(json, typeOfMap);
    }

    public Map<String, String> extractDataMap(final Type typeOfMap, final String json) {
        return this.gsonConverter.fromJson(json, typeOfMap);
    }

    public Map<String, Object> extractObjectMap(final Type typeOfMap, final String json) {
        return this.gsonConverter.fromJson(json, typeOfMap);
    }

    public <T> T fromJson(final String json, final Class<T> classOfT) {
        return this.gsonConverter.fromJson(json, classOfT);
    }

    public String toJson(final JsonElement jsonElement) {
        return this.gsonConverter.toJson(jsonElement);
    }

    public String toJson(final Object object) {
        return this.gsonConverter.toJson(object);
    }

    public void checkForUnsupportedParameters(final Type typeOfMap, final String json, final Set<String> supportedParams) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Map<String, Object> requestMap = this.gsonConverter.fromJson(json, typeOfMap);

        final List<String> unsupportedParameterList = new ArrayList<String>();
        for (final String providedParameter : requestMap.keySet()) {
            if (!supportedParams.contains(providedParameter)) {
                unsupportedParameterList.add(providedParameter);
            }
        }

        if (!unsupportedParameterList.isEmpty()) { throw new UnsupportedParameterException(unsupportedParameterList); }
    }

    public void checkForUnsupportedParameters(final JsonObject object, final Set<String> supportedParams) {
        if (object == null) { throw new InvalidParameterException(); }

        final Set<Entry<String, JsonElement>> entries = object.entrySet();
        final List<String> unsupportedParameterList = new ArrayList<String>();

        for (final Entry<String, JsonElement> providedParameter : entries) {
            if (!supportedParams.contains(providedParameter.getKey())) {
                unsupportedParameterList.add(providedParameter.getKey());
            }
        }

        if (!unsupportedParameterList.isEmpty()) { throw new UnsupportedParameterException(unsupportedParameterList); }
    }

    public JsonElement parse(final String json) {

        JsonElement parsedElement = null;
        if (StringUtils.isNotBlank(json)) {
            parsedElement = this.parser.parse(json);
        }

        return parsedElement;
    }

    public boolean parameterExists(final String parameterName, final JsonElement element) {
        return this.helperDelegator.parameterExists(parameterName, element);
    }

    public String extractStringNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractStringNamed(parameterName, element, new HashSet<String>());
    }

    public String extractStringNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        return this.helperDelegator.extractStringNamed(parameterName, element, parametersPassedInRequest);
    }

    public Long extractLongNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractLongNamed(parameterName, element, new HashSet<String>());
    }

    public Long extractLongNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        return this.helperDelegator.extractLongNamed(parameterName, element, parametersPassedInRequest);
    }

    public JsonArray extractJsonArrayNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractJsonArrayNamed(parameterName, element);
    }

    public String[] extractArrayNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractArrayNamed(parameterName, element, new HashSet<String>());
    }

    public String[] extractArrayNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        return this.helperDelegator.extractArrayNamed(parameterName, element, parametersPassedInRequest);
    }

    public Boolean extractBooleanNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractBooleanNamed(parameterName, element, new HashSet<String>());
    }

    public Boolean extractBooleanNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        return this.helperDelegator.extractBooleanNamed(parameterName, element, parametersPassedInRequest);
    }

    public MonthDay extractMonthDayNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractMonthDayNamed(parameterName, element);
    }

    public LocalDate extractLocalDateNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractLocalDateNamed(parameterName, element, new HashSet<String>());
    }

    public LocalDate extractLocalDateNamed(final String parameterName, final JsonElement element, final String dateFormat,
            final Locale locale) {
        return this.helperDelegator.extractLocalDateNamed(parameterName, element.getAsJsonObject(), dateFormat, locale,
                new HashSet<String>());
    }

    public LocalDate extractLocalDateNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return this.helperDelegator.extractLocalDateNamed(parameterName, element, parametersPassedInRequest);
    }

    public LocalDate extractLocalDateAsArrayNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return this.helperDelegator.extractLocalDateAsArrayNamed(parameterName, element, parametersPassedInRequest);
    }

    public BigDecimal extractBigDecimalWithLocaleNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractBigDecimalWithLocaleNamed(parameterName, element, new HashSet<String>());
    }

    public BigDecimal extractBigDecimalWithLocaleNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return this.helperDelegator.extractBigDecimalWithLocaleNamed(parameterName, element, parametersPassedInRequest);
    }

    public BigDecimal extractBigDecimalNamed(final String parameterName, final JsonElement element, final Locale locale) {
        return this.helperDelegator.extractBigDecimalNamed(parameterName, element.getAsJsonObject(), locale, new HashSet<String>());
    }

    public BigDecimal extractBigDecimalNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return this.helperDelegator.extractBigDecimalNamed(parameterName, element.getAsJsonObject(), Locale.US, parametersPassedInRequest);
    }

    public Integer extractIntegerWithLocaleNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractIntegerWithLocaleNamed(parameterName, element.getAsJsonObject(), new HashSet<String>());
    }

    public Integer extractIntegerSansLocaleNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractIntegerSansLocaleNamed(parameterName, element.getAsJsonObject(), new HashSet<String>());
    }

    public Integer extractIntegerWithLocaleNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return this.helperDelegator.extractIntegerWithLocaleNamed(parameterName, element.getAsJsonObject(), parametersPassedInRequest);
    }

    public Integer extractIntegerNamed(final String parameterName, final JsonElement element, final Locale locale) {
        return this.helperDelegator.extractIntegerNamed(parameterName, element.getAsJsonObject(), locale, new HashSet<String>());
    }

    public Integer extractIntegerNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        return this.helperDelegator.extractIntegerNamed(parameterName, element.getAsJsonObject(), Locale.US, parametersPassedInRequest);
    }

    public Locale extractLocaleParameter(final JsonObject element) {
        return this.helperDelegator.extractLocaleParameter(element);
    }

    public String extractDateFormatParameter(final JsonObject element) {
        return this.helperDelegator.extractDateFormatParameter(element);
    }
}