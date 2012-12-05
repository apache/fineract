package org.mifosplatform.infrastructure.core.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@Component
public class FromJsonHelper {

    private final Gson gsonConverter;
    private final JsonParserHelper helperDelegator;
    private final JsonParser parser;

    public FromJsonHelper() {
        this.gsonConverter = new Gson();
        helperDelegator = new JsonParserHelper();
        parser = new JsonParser();
    }

    public Map<String, Boolean> extractMap(final Type typeOfMap, final String json) {
        return this.gsonConverter.fromJson(json, typeOfMap);
    }

    public <T> T fromJson(final String json, Class<T> classOfT) {
        return this.gsonConverter.fromJson(json, classOfT);
    }

    public void checkForUnsupportedParameters(final Type typeOfMap, final String json, final Set<String> supportedParams) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);

        List<String> unsupportedParameterList = new ArrayList<String>();
        for (String providedParameter : requestMap.keySet()) {
            if (!supportedParams.contains(providedParameter)) {
                unsupportedParameterList.add(providedParameter);
            }
        }

        if (!unsupportedParameterList.isEmpty()) { throw new UnsupportedParameterException(unsupportedParameterList); }
    }

    public JsonElement parse(final String json) {
        return parser.parse(json);
    }

    public String extractStringNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        return helperDelegator.extractStringNamed(parameterName, element, parametersPassedInRequest);
    }

    public Long extractLongNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        return helperDelegator.extractLongNamed(parameterName, element, parametersPassedInRequest);
    }

    public String[] extractArrayNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        return helperDelegator.extractArrayNamed(parameterName, element, parametersPassedInRequest);
    }

    public Boolean extractBooleanNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        return helperDelegator.extractBooleanNamed(parameterName, element, parametersPassedInRequest);
    }

    public LocalDate extractLocalDateNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return helperDelegator.extractLocalDateNamed(parameterName, element, parametersPassedInRequest);
    }

    public LocalDate extractLocalDateAsArrayNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return helperDelegator.extractLocalDateAsArrayNamed(parameterName, element, parametersPassedInRequest);
    }

    public BigDecimal extractBigDecimalWithLocaleNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return helperDelegator.extractBigDecimalWithLocaleNamed(parameterName, element, parametersPassedInRequest);
    }

    public BigDecimal extractBigDecimalNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return helperDelegator.extractBigDecimalNamed(parameterName, element.getAsJsonObject(), Locale.US, parametersPassedInRequest);
    }

    public Integer extractIntegerWithLocaleNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return helperDelegator.extractIntegerWithLocaleNamed(parameterName, element.getAsJsonObject(), parametersPassedInRequest);
    }

    public Integer extractIntegerNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        return helperDelegator.extractIntegerNamed(parameterName, element.getAsJsonObject(), Locale.US, parametersPassedInRequest);
    }
}