package org.mifosplatform.infrastructure.core.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
}