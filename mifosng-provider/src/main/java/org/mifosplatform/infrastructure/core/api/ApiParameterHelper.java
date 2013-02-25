/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;

public class ApiParameterHelper {

    public static Long commandId(final MultivaluedMap<String, String> queryParams) {
        Long id = null;
        if (queryParams.getFirst("commandId") != null) {
            final String value = queryParams.getFirst("commandId");
            if (StringUtils.isNotBlank(value)) {
                id = Long.valueOf(value);
            }
        }
        return id;
    }

    public static Set<String> extractFieldsForResponseIfProvided(final MultivaluedMap<String, String> queryParams) {
        Set<String> fields = new HashSet<String>();
        String commaSerperatedParameters = "";
        if (queryParams.getFirst("fields") != null) {
            commaSerperatedParameters = queryParams.getFirst("fields");
            if (StringUtils.isNotBlank(commaSerperatedParameters)) {
                fields = new HashSet<String>(Arrays.asList(commaSerperatedParameters.split("\\s*,\\s*")));
            }
        }
        return fields;
    }

    public static Set<String> extractAssociationsForResponseIfProvided(final MultivaluedMap<String, String> queryParams) {
        Set<String> fields = new HashSet<String>();
        String commaSerperatedParameters = "";
        if (queryParams.getFirst("associations") != null) {
            commaSerperatedParameters = queryParams.getFirst("associations");
            if (StringUtils.isNotBlank(commaSerperatedParameters)) {
                fields = new HashSet<String>(Arrays.asList(commaSerperatedParameters.split("\\s*,\\s*")));
            }
        }
        return fields;
    }

    public static boolean prettyPrint(final MultivaluedMap<String, String> queryParams) {
        boolean prettyPrint = false;
        if (queryParams.getFirst("pretty") != null) {
            String prettyPrintValue = queryParams.getFirst("pretty");
            prettyPrint = "true".equalsIgnoreCase(prettyPrintValue);
        }
        return prettyPrint;
    }

    public static boolean exportCsv(final MultivaluedMap<String, String> queryParams) {
        boolean exportCsv = false;
        if (queryParams.getFirst("exportCSV") != null) {
            String exportCsvValue = queryParams.getFirst("exportCSV");
            exportCsv = "true".equalsIgnoreCase(exportCsvValue);
        }
        return exportCsv;
    }

    public static boolean exportPdf(final MultivaluedMap<String, String> queryParams) {
        boolean exportPDF = false;
        if (queryParams.getFirst("exportPDF") != null) {
            String exportPdfValue = queryParams.getFirst("exportPDF");
            exportPDF = "true".equalsIgnoreCase(exportPdfValue);
        }
        return exportPDF;
    }

    public static boolean parameterType(final MultivaluedMap<String, String> queryParams) {
        boolean parameterType = false;
        if (queryParams.getFirst("parameterType") != null) {
            String parameterTypeValue = queryParams.getFirst("parameterType");
            parameterType = "true".equalsIgnoreCase(parameterTypeValue);
        }
        return parameterType;
    }

    public static boolean template(final MultivaluedMap<String, String> queryParams) {
        boolean template = false;
        if (queryParams.getFirst("template") != null) {
            String prettyPrintValue = queryParams.getFirst("template");
            template = "true".equalsIgnoreCase(prettyPrintValue);
        }
        return template;
    }

    public static boolean makerCheckerable(final MultivaluedMap<String, String> queryParams) {
        boolean makerCheckerable = false;
        if (queryParams.getFirst("makerCheckerable") != null) {
            String prettyPrintValue = queryParams.getFirst("makerCheckerable");
            makerCheckerable = "true".equalsIgnoreCase(prettyPrintValue);
        }
        return makerCheckerable;
    }

    public static boolean includeJson(final MultivaluedMap<String, String> queryParams) {
        boolean includeJson = false;
        if (queryParams.getFirst("includeJson") != null) {
            String includeJsonValue = queryParams.getFirst("includeJson");
            includeJson = "true".equalsIgnoreCase(includeJsonValue);
        }
        return includeJson;
    }

    public static boolean genericResultSet(final MultivaluedMap<String, String> queryParams) {
        boolean genericResultSet = false;
        if (queryParams.getFirst("genericResultSet") != null) {
            String genericResultSetValue = queryParams.getFirst("genericResultSet");
            genericResultSet = "true".equalsIgnoreCase(genericResultSetValue);
        }
        return genericResultSet;
    }

    public static boolean genericResultSetPassed(MultivaluedMap<String, String> queryParams) {
        return queryParams.getFirst("genericResultSet") != null;
    }
    
    public static String sqlEncodeString(String str) {
        String singleQuote = "'";
        String twoSingleQuotes = "''";
        return singleQuote + StringUtils.replace(str, singleQuote, twoSingleQuotes, -1) + singleQuote;
    }

    public static Map<String, String> asMap(MultivaluedMap<String, String> queryParameters) {

        final Map<String, String> map = new HashMap<String, String>(queryParameters.size());

        for (String parameterName : queryParameters.keySet()) {
            List<String> values = queryParameters.get(parameterName);
            map.put(parameterName, values.get(0));
        }

        return map;
    }
}