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
package org.apache.fineract.batch.command;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.infrastructure.core.api.MutableUriInfo;

public final class CommandStrategyUtils {

    private static final Pattern VERSIONED_RELATIVE_URL_PATTERN = Pattern.compile("^(v[1-9][0-9]*/)(.*)$");

    private CommandStrategyUtils() {

    }

    /**
     * Get query parameters from relative URL.
     *
     * @param relativeUrl
     *            the relative URL
     * @return the query parameters in a map
     */
    public static Map<String, String> getQueryParameters(final String relativeUrl) {
        final String queryParameterStr = StringUtils.substringAfter(relativeUrl, "?");
        final String[] queryParametersArray = StringUtils.split(queryParameterStr, "&");
        final Map<String, String> queryParametersMap = new HashMap<>();
        for (String parameterStr : queryParametersArray) {
            String[] keyValue = StringUtils.split(parameterStr, "=");
            queryParametersMap.put(keyValue[0], keyValue[1]);
        }
        return queryParametersMap;
    }

    /**
     * Add query parameters(received in the relative URL) to URI info query parameters.
     *
     * @param uriInfo
     *            the URI info
     * @param queryParameters
     *            the query parameters
     */
    public static void addQueryParametersToUriInfo(final MutableUriInfo uriInfo, final Map<String, String> queryParameters) {
        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            uriInfo.addAdditionalQueryParameter(entry.getKey(), entry.getValue());
        }
    }

    public static String relativeUrlWithoutVersion(BatchRequest request) {
        String relativeUrl = request.getRelativeUrl();
        Matcher m = VERSIONED_RELATIVE_URL_PATTERN.matcher(relativeUrl);
        if (m.matches()) {
            return m.group(2);
        } else {
            return relativeUrl;
        }
    }

    public static boolean isResourceVersioned(CommandContext commandContext) {
        String relativeUrl = commandContext.getResource();
        return isRelativeUrlVersioned(relativeUrl);
    }

    public static boolean isRelativeUrlVersioned(String relativeUrl) {
        Matcher m = VERSIONED_RELATIVE_URL_PATTERN.matcher(relativeUrl);
        return m.matches();
    }

}
