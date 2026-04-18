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

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiParameterHelperTest {

    private static final String COMMAND_ID = "commandId";
    private static final String FIELDS = "fields";
    private static final String ASSOCIATIONS = "associations";
    private static final String EXCLUDE = "exclude";
    private static final String LOCALE = "locale";
    private static final String PARAMETER_TYPE = "parameterType";
    private static final String TEMPLATE = "template";
    private static final String MAKER_CHECKERABLE = "makerCheckerable";
    private static final String INCLUDE_JSON = "includeJson";
    private static final String GENERIC_RESULT_SET = "genericResultSet";
    private static final String CHARGES = "charges";
    private static final String REPAYMENTS = "repayments";
    private static final String GUARANTORS = "guarantors";
    private static final String BLANK_SPACES = "   ";
    private static final String TRUE_UPPERCASE = "TRUE";
    private static final String FALSE_LOWERCASE = "false";

    @Test
    void givenMissingOrBlankCommandId_whenExtractingCommandId_thenReturnsNull() {
        Assertions.assertNull(ApiParameterHelper.commandId(queryParams()));
        Assertions.assertNull(ApiParameterHelper.commandId(queryParams(COMMAND_ID, BLANK_SPACES)));
    }

    @Test
    void givenValidCommandId_whenExtractingCommandId_thenReturnsLongValue() {
        Assertions.assertEquals(42L, ApiParameterHelper.commandId(queryParams(COMMAND_ID, "42")));
    }

    @Test
    void givenInvalidCommandId_whenExtractingCommandId_thenThrowsNumberFormatException() {
        Assertions.assertThrows(NumberFormatException.class,
                () -> ApiParameterHelper.commandId(queryParams(COMMAND_ID, "abc")));
    }

    @Test
    void givenValidFields_whenExtractingFields_thenReturnsCorrectSet() {
        Set<String> fields = ApiParameterHelper.extractFieldsForResponseIfProvided(queryParams(FIELDS, "id,name,description"));

        Assertions.assertEquals(3, fields.size());
        Assertions.assertTrue(fields.contains("id"));
        Assertions.assertTrue(fields.contains("name"));
        Assertions.assertTrue(fields.contains("description"));
    }

    @Test
    void givenMissingOrBlankFields_whenExtractingFields_thenReturnsEmptySet() {
        Assertions.assertTrue(ApiParameterHelper.extractFieldsForResponseIfProvided(queryParams()).isEmpty());
        Assertions.assertTrue(ApiParameterHelper.extractFieldsForResponseIfProvided(queryParams(FIELDS, BLANK_SPACES)).isEmpty());
    }

    @Test
    void givenCommaSeparatedFields_whenExtractingFields_thenReturnsTrimmedDistinctSet() {
        Assertions.assertEquals(Set.of("id", "name"),
                ApiParameterHelper.extractFieldsForResponseIfProvided(queryParams(FIELDS, "id, name, id")));
    }

    @Test
    void givenMissingOrBlankAssociations_whenExtractingAssociations_thenReturnsEmptySet() {
        Assertions.assertTrue(ApiParameterHelper.extractAssociationsForResponseIfProvided(queryParams()).isEmpty());
        Assertions.assertTrue(
                ApiParameterHelper.extractAssociationsForResponseIfProvided(queryParams(ASSOCIATIONS, BLANK_SPACES)).isEmpty());
    }

    @Test
    void givenCommaSeparatedAssociations_whenExtractingAssociations_thenReturnsTrimmedSet() {
        Assertions.assertEquals(Set.of(CHARGES, REPAYMENTS),
                ApiParameterHelper.extractAssociationsForResponseIfProvided(queryParams(ASSOCIATIONS, CHARGES + ", " + REPAYMENTS)));
    }

    @Test
    void givenExcludeString_whenExcludingAssociations_thenRemovesOnlyRequestedValues() {
        Set<String> fields = new HashSet<>(Set.of(CHARGES, REPAYMENTS, GUARANTORS));

        ApiParameterHelper.excludeAssociationsForResponseIfProvided(CHARGES + ", " + GUARANTORS, fields);

        Assertions.assertEquals(Set.of(REPAYMENTS), fields);
    }

    @Test
    void givenBlankExcludeString_whenExcludingAssociations_thenKeepsOriginalSet() {
        Set<String> fields = new HashSet<>(Set.of(CHARGES, REPAYMENTS));

        ApiParameterHelper.excludeAssociationsForResponseIfProvided("  ", fields);

        Assertions.assertEquals(Set.of(CHARGES, REPAYMENTS), fields);
    }

    @Test
    void givenExcludeQueryParam_whenExcludingAssociations_thenRemovesRequestedValues() {
        Set<String> fields = new HashSet<>(Set.of(CHARGES, REPAYMENTS, GUARANTORS));

        ApiParameterHelper.excludeAssociationsForResponseIfProvided(queryParams(EXCLUDE, REPAYMENTS), fields);

        Assertions.assertEquals(Set.of(CHARGES, GUARANTORS), fields);
    }

    @Test
    void givenMissingExcludeQueryParam_whenExcludingAssociations_thenDoesNothing() {
        Set<String> fields = new HashSet<>(Set.of(CHARGES, REPAYMENTS));

        ApiParameterHelper.excludeAssociationsForResponseIfProvided(queryParams(), fields);

        Assertions.assertEquals(Set.of(CHARGES, REPAYMENTS), fields);
    }

    @Test
    void givenMissingLocale_whenExtractingLocale_thenReturnsNull() {
        Assertions.assertNull(ApiParameterHelper.extractLocale(queryParams()));
    }

    @Test
    void givenValidLocale_whenExtractingLocale_thenReturnsParsedLocale() {
        Locale locale = ApiParameterHelper.extractLocale(queryParams(LOCALE, "en_US"));

        Assertions.assertEquals(Locale.forLanguageTag("en-US"), locale);
    }

    @Test
    void givenBlankLocale_whenExtractingLocale_thenThrowsValidationException() {
        Assertions.assertThrows(PlatformApiDataValidationException.class,
                () -> ApiParameterHelper.extractLocale(queryParams(LOCALE, " ")));
    }

    @Test
    void givenInvalidLocale_whenExtractingLocale_thenThrowsValidationException() {
        Assertions.assertThrows(PlatformApiDataValidationException.class,
                () -> ApiParameterHelper.extractLocale(queryParams(LOCALE, "zz_US")));
    }

    @Test
    void givenParameterTypeQueryParam_whenParsingBoolean_thenRespectsTrueFalseAndMissing() {
        Assertions.assertTrue(ApiParameterHelper.parameterType(queryParams(PARAMETER_TYPE, TRUE_UPPERCASE)));
        Assertions.assertFalse(ApiParameterHelper.parameterType(queryParams(PARAMETER_TYPE, FALSE_LOWERCASE)));
        Assertions.assertFalse(ApiParameterHelper.parameterType(queryParams()));
    }

    @Test
    void givenTemplateQueryParam_whenParsingBoolean_thenRespectsTrueFalseAndMissing() {
        Assertions.assertTrue(ApiParameterHelper.template(queryParams(TEMPLATE, "TrUe")));
        Assertions.assertFalse(ApiParameterHelper.template(queryParams(TEMPLATE, "no")));
        Assertions.assertFalse(ApiParameterHelper.template(queryParams()));
    }

    @Test
    void givenMakerCheckerableQueryParam_whenParsingBoolean_thenRespectsTrueFalseAndMissing() {
        Assertions.assertTrue(ApiParameterHelper.makerCheckerable(queryParams(MAKER_CHECKERABLE, TRUE_UPPERCASE)));
        Assertions.assertFalse(ApiParameterHelper.makerCheckerable(queryParams(MAKER_CHECKERABLE, "0")));
        Assertions.assertFalse(ApiParameterHelper.makerCheckerable(queryParams()));
    }

    @Test
    void givenIncludeJsonQueryParam_whenParsingBoolean_thenRespectsTrueFalseAndMissing() {
        Assertions.assertTrue(ApiParameterHelper.includeJson(queryParams(INCLUDE_JSON, "true")));
        Assertions.assertFalse(ApiParameterHelper.includeJson(queryParams(INCLUDE_JSON, "False")));
        Assertions.assertFalse(ApiParameterHelper.includeJson(queryParams()));
    }

    @Test
    void givenGenericResultSetQueryParam_whenParsingBoolean_thenRespectsTrueFalseAndMissing() {
        Assertions.assertTrue(ApiParameterHelper.genericResultSet(queryParams(GENERIC_RESULT_SET, TRUE_UPPERCASE)));
        Assertions.assertFalse(ApiParameterHelper.genericResultSet(queryParams(GENERIC_RESULT_SET, FALSE_LOWERCASE)));
        Assertions.assertFalse(ApiParameterHelper.genericResultSet(queryParams()));
    }

    @Test
    void givenGenericResultSetPresence_whenCheckingPassedFlag_thenDependsOnlyOnPresence() {
        Assertions.assertTrue(ApiParameterHelper.genericResultSetPassed(queryParams(GENERIC_RESULT_SET, FALSE_LOWERCASE)));
        Assertions.assertFalse(ApiParameterHelper.genericResultSetPassed(queryParams()));
    }

    private static MultivaluedMap<String, String> queryParams(final String... keyValuePairs) {
        final MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            params.add(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return params;
    }
}