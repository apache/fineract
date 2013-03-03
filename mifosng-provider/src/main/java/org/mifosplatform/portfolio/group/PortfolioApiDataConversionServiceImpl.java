/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

@Service
public class PortfolioApiDataConversionServiceImpl implements PortfolioApiDataConversionService {

    /**
     * Google-gson class for converting to and from json.
     */
    private final Gson gsonConverter;

    public PortfolioApiDataConversionServiceImpl() {
        this.gsonConverter = new Gson();
    }

    @Override
    public GroupCommand convertJsonToGroupCommand(final Long resourceIdentifier, final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);

        final Set<String> supportedParams = new HashSet<String>(Arrays.asList("name", "officeId", "levelId", "parentId", "loanOfficerId",
                "externalId", "clientMembers"));

        checkForUnsupportedParameters(requestMap, supportedParams);

        final Set<String> modifiedParameters = new HashSet<String>();

        final String name = extractStringParameter("name", requestMap, modifiedParameters);
        final Long officeId = extractLongParameter("officeId", requestMap, modifiedParameters);
        final Long levelId = extractLongParameter("levelId", requestMap, modifiedParameters);
        final Long parentId = extractLongParameter("parentId", requestMap, modifiedParameters);
        final Long loanOfficerId = extractLongParameter("loanOfficerId", requestMap, modifiedParameters);
        final String externalId = extractStringParameter("externalId", requestMap, modifiedParameters);

        final JsonParser parser = new JsonParser();

        String[] clientMembers = null;
        JsonElement element = parser.parse(json);
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("clientMembers")) {
                modifiedParameters.add("clientMembers");
                JsonArray array = object.get("clientMembers").getAsJsonArray();
                clientMembers = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    clientMembers[i] = array.get(i).getAsString();
                }
            }
        }

        return new GroupCommand(modifiedParameters, resourceIdentifier, externalId, name, officeId, loanOfficerId, clientMembers, parentId,
                levelId);
    }

    private void checkForUnsupportedParameters(final Map<String, ?> requestMap, final Set<String> supportedParams) {
        List<String> unsupportedParameterList = new ArrayList<String>();
        for (String providedParameter : requestMap.keySet()) {
            if (!supportedParams.contains(providedParameter)) {
                unsupportedParameterList.add(providedParameter);
            }
        }

        if (!unsupportedParameterList.isEmpty()) { throw new UnsupportedParameterException(unsupportedParameterList); }
    }

    private String extractStringParameter(final String paramName, final Map<String, ?> requestMap, final Set<String> modifiedParameters) {
        String paramValue = null;
        if (requestMap.containsKey(paramName)) {
            paramValue = (String) requestMap.get(paramName);
            modifiedParameters.add(paramName);
        }

        if (paramValue != null) {
            paramValue = paramValue.trim();
        }

        return paramValue;
    }

    private Long extractLongParameter(final String paramName, final Map<String, ?> requestMap, final Set<String> modifiedParameters) {
        Long paramValue = null;
        if (requestMap.containsKey(paramName)) {
            String valueAsString = (String) requestMap.get(paramName);
            if (StringUtils.isNotBlank(valueAsString)) {
                paramValue = Long.valueOf(Double.valueOf(valueAsString).longValue());
            }
            modifiedParameters.add(paramName);
        }
        return paramValue;
    }
}