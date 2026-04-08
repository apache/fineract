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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
     * @return the query parameters in a map with URL-decoded values
     */
    public static Map<String, String> getQueryParameters(final String relativeUrl) {
        final String queryParameterStr = StringUtils.substringAfter(relativeUrl, "?");
        final String[] queryParametersArray = StringUtils.split(queryParameterStr, "&");
        final Map<String, String> queryParametersMap = new HashMap<>();
        for (String parameterStr : queryParametersArray) {
            String[] keyValue = StringUtils.split(parameterStr, "=");
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = "";
            if (keyValue.length > 1 && StringUtils.isNotEmpty(keyValue[1])) {
                value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            } else if (keyValue.length > 1) {
                value = keyValue[1];
            }
            queryParametersMap.put(key, value);
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

    /**
     * Builds a request object from query parameters using reflection and the builder pattern. This method automatically
     * detects field types and converts string values to the appropriate type (Integer, String, etc.).
     *
     * @param queryParameters
     *            map of query parameter names to their string values
     * @param requestClass
     *            the class of the request object to build (must have a builder() method)
     * @param <T>
     *            the type of the request object
     * @return the built request object with fields populated from query parameters
     * @throws RuntimeException
     *             if the request class doesn't have a builder or if reflection fails
     */
    public static <T> T buildRequestFromQueryParameters(final Map<String, String> queryParameters, final Class<T> requestClass) {
        try {
            // Get the builder
            Method builderMethod = requestClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            Class<?> builderClass = builder.getClass();

            // Iterate through all declared fields of the request class
            for (Field field : requestClass.getDeclaredFields()) {
                String fieldName = field.getName();
                String paramValue = queryParameters.get(fieldName);

                // Skip if parameter is not present or is the serialVersionUID field
                if (paramValue == null || "serialVersionUID".equals(fieldName)) {
                    continue;
                }

                // Find the builder method for this field
                Method builderSetter = builderClass.getMethod(fieldName, field.getType());

                // Convert the string value to the appropriate type and invoke builder method
                Object convertedValue = convertValue(paramValue, field.getType());
                builderSetter.invoke(builder, convertedValue);
            }

            // Call build() method to create the final object
            Method buildMethod = builderClass.getMethod("build");
            return requestClass.cast(buildMethod.invoke(builder));
        } catch (Exception e) {
            throw new RuntimeException("Failed to build request object from query parameters", e);
        }
    }

    /**
     * Converts a string value to the target type.
     *
     * @param value
     *            the string value to convert
     * @param targetType
     *            the target type
     * @return the converted value
     */
    private static Object convertValue(final String value, final Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == BigDecimal.class) {
            return new BigDecimal(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            // Default to string for unknown types
            return value;
        }
    }

}
