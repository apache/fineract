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
package org.apache.fineract.infrastructure.core.api;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;

public final class ApiParameterHelper {

    private static final String GENERIC_RESULT_SET = "genericResultSet";
    private static final Pattern COMMA_SEPARATOR = Pattern.compile("\\s*,\\s*");

    private ApiParameterHelper() {}

    public static Long commandId(final MultivaluedMap<String, String> queryParams) {
        if (queryParams != null && queryParams.getFirst("commandId") != null) {
            final String value = queryParams.getFirst("commandId");
            if (StringUtils.isNotBlank(value)) {
                return Long.valueOf(value);
            }
        }
        return null;
    }

    public static Set<String> extractFieldsForResponseIfProvided(final MultivaluedMap<String, String> queryParams) {
        if (queryParams != null && queryParams.getFirst("fields") != null) {
            final String value = queryParams.getFirst("fields");
            if (StringUtils.isNotBlank(value)) {
                return new HashSet<>(Arrays.asList(COMMA_SEPARATOR.split(value)));
            }
        }
        return new HashSet<>();
    }

    public static Set<String> extractAssociationsForResponseIfProvided(final MultivaluedMap<String, String> queryParams) {
        if (queryParams != null && queryParams.getFirst("associations") != null) {
            final String value = queryParams.getFirst("associations");
            if (StringUtils.isNotBlank(value)) {
                return new HashSet<>(Arrays.asList(COMMA_SEPARATOR.split(value)));
            }
        }
        return new HashSet<>();
    }

    public static void excludeAssociationsForResponseIfProvided(final String commaSeparatedParameters, final Set<String> fields) {
        if (StringUtils.isNotBlank(commaSeparatedParameters) && fields != null) {
            fields.removeAll(new HashSet<>(Arrays.asList(COMMA_SEPARATOR.split(commaSeparatedParameters))));
        }
    }

    public static void excludeAssociationsForResponseIfProvided(final MultivaluedMap<String, String> queryParams, final Set<String> fields) {
        if (queryParams != null && queryParams.getFirst("exclude") != null) {
            excludeAssociationsForResponseIfProvided(queryParams.getFirst("exclude"), fields);
        }
    }

    public static Locale extractLocale(final MultivaluedMap<String, String> queryParams) {
        if (queryParams != null && queryParams.getFirst("locale") != null) {
            final String localeAsString = queryParams.getFirst("locale");
            if (StringUtils.isNotBlank(localeAsString)) {
                return JsonParserHelper.localeFromString(localeAsString);
            }
        }
        return null;
    }

    public static boolean parameterType(final MultivaluedMap<String, String> queryParams) {
        return isTrue(queryParams, "parameterType");
    }

    public static boolean template(final MultivaluedMap<String, String> queryParams) {
        return isTrue(queryParams, "template");
    }

    public static boolean makerCheckerable(final MultivaluedMap<String, String> queryParams) {
        return isTrue(queryParams, "makerCheckerable");
    }

    public static boolean includeJson(final MultivaluedMap<String, String> queryParams) {
        return isTrue(queryParams, "includeJson");
    }

    public static boolean genericResultSet(final MultivaluedMap<String, String> queryParams) {
        return isTrue(queryParams, GENERIC_RESULT_SET);
    }

    public static boolean genericResultSetPassed(final MultivaluedMap<String, String> queryParams) {
        return queryParams != null && queryParams.getFirst(GENERIC_RESULT_SET) != null;
    }

    private static boolean isTrue(final MultivaluedMap<String, String> queryParams, final String key) {
        if (queryParams != null && queryParams.getFirst(key) != null) {
            return "true".equalsIgnoreCase(queryParams.getFirst(key));
        }
        return false;
    }
}