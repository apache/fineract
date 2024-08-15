/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
public class FromJsonHelper {

    private final Gson gsonConverter;
    private final JsonParserHelper helperDelegator;

    public FromJsonHelper() {
        this.gsonConverter = new Gson();
        this.helperDelegator = new JsonParserHelper();
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

    public void checkForUnsupportedParameters(final Type typeOfMap, final String json, final Collection<String> supportedParams) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Map<String, Object> requestMap = this.gsonConverter.fromJson(json, typeOfMap);

        final List<String> unsupportedParameterList = new ArrayList<>();
        for (final String providedParameter : requestMap.keySet()) {
            if (!supportedParams.contains(providedParameter)) {
                unsupportedParameterList.add(providedParameter);
            }
        }

        if (!unsupportedParameterList.isEmpty()) {
            throw new UnsupportedParameterException(unsupportedParameterList);
        }
    }

    public void checkForUnsupportedParameters(final JsonObject object, final Collection<String> supportedParams) {
        if (object == null) {
            throw new InvalidParameterException();
        }

        final Set<Map.Entry<String, JsonElement>> entries = object.entrySet();
        final List<String> unsupportedParameterList = new ArrayList<>();

        for (final Map.Entry<String, JsonElement> providedParameter : entries) {
            if (!supportedParams.contains(providedParameter.getKey())) {
                unsupportedParameterList.add(providedParameter.getKey());
            }
        }

        if (!unsupportedParameterList.isEmpty()) {
            throw new UnsupportedParameterException(unsupportedParameterList);
        }
    }

    /**
     * @param parentPropertyName
     *            The full json path to this property,the value is appended to the parameter name while generating an
     *            error message <br>
     *            Ex: property "name" in Object "person" would be named as "person.name"
     * @param object
     * @param supportedParams
     */
    @SuppressWarnings("AvoidHidingCauseException")
    public void checkForUnsupportedNestedParameters(final String parentPropertyName, final JsonObject object,
            final Set<String> supportedParams) {
        try {
            checkForUnsupportedParameters(object, supportedParams);
        } catch (UnsupportedParameterException exception) {
            List<String> unsupportedParameters = exception.getUnsupportedParameters();
            List<String> updatedUnsupportedParameters = new ArrayList<>();
            for (String unsupportedParameter : unsupportedParameters) {
                String updatedUnsupportedParameter = parentPropertyName + "." + unsupportedParameter;
                updatedUnsupportedParameters.add(updatedUnsupportedParameter);
            }
            throw new UnsupportedParameterException(updatedUnsupportedParameters);
        }

    }

    public JsonElement parse(final String json) {

        JsonElement parsedElement = null;
        if (StringUtils.isNotBlank(json)) {
            parsedElement = JsonParser.parseString(json);
        }
        return parsedElement;
    }

    public boolean parameterExists(final String parameterName, final JsonElement element) {
        return this.helperDelegator.parameterExists(parameterName, element);
    }

    /**
     * Check Parameter has a non-blank value
     */
    public boolean parameterHasValue(final String parameterName, final JsonElement element) {
        return this.helperDelegator.parameterHasValue(parameterName, element);
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

    public MonthDay extractMonthDayNamed(final String parameterName, final JsonObject object, final String dateFormat,
            final Locale clientApplicationLocale) {
        return this.helperDelegator.extractMonthDayNamed(parameterName, object, dateFormat, clientApplicationLocale);
    }

    public LocalDate extractLocalDateNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractLocalDateNamed(parameterName, element, new HashSet<>());
    }

    public LocalTime extractLocalTimeNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractLocalTimeNamed(parameterName, element, new HashSet<>());
    }

    public LocalDateTime extractLocalDateTimeNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractLocalDateTimeNamed(parameterName, element, new HashSet<>());
    }

    public LocalTime extractLocalTimeNamed(final String parameterName, final JsonElement element, final String dateFormat,
            final Locale locale) {
        return this.helperDelegator.extractLocalTimeNamed(parameterName, element, dateFormat, locale, new HashSet<>());
    }

    public LocalTime extractLocalTimeNamed(final String parameterName, final JsonElement element, String timeFormat) {
        return this.helperDelegator.extractLocalTimeNamed(parameterName, element, timeFormat, new HashSet<>());
    }

    public LocalDateTime extractLocalDateTimeNamed(final String parameterName, final JsonElement element, final String dateFormat,
            final Locale locale) {
        return this.helperDelegator.extractLocalDateTimeNamed(parameterName, element, dateFormat, locale, new HashSet<>());
    }

    public LocalDateTime extractLocalDateTimeNamed(final String parameterName, final JsonElement element, String timeFormat) {
        return this.helperDelegator.extractLocalDateTimeNamed(parameterName, element, timeFormat, new HashSet<>());
    }

    public LocalDate extractLocalDateNamed(final String parameterName, final JsonElement element, final String dateFormat,
            final Locale locale) {
        return this.helperDelegator.extractLocalDateNamed(parameterName, element.getAsJsonObject(), dateFormat, locale, new HashSet<>());
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

    public String extractMonthDayFormatParameter(final JsonObject element) {
        return this.helperDelegator.extractMonthDayFormatParameter(element);
    }

    public JsonObject extractJsonObjectNamed(final String parameterName, final JsonElement element) {
        return this.helperDelegator.extractJsonObjectNamed(parameterName, element);
    }

    public Gson getGsonConverter() {
        return this.gsonConverter;
    }

}
