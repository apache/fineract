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
package org.apache.fineract.portfolio.client.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClientDataValidatorTest {

    @Mock
    private ConfigurationReadPlatformService configurationReadPlatformService;

    private ClientDataValidator validator;

    @BeforeEach
    void setUp() {
        FromJsonHelper fromApiJsonHelper = new FromJsonHelper();
        when(configurationReadPlatformService.retrieveGlobalConfiguration(anyString()))
                .thenReturn(new GlobalConfigurationPropertyData().setEnabled(false));
        validator = new ClientDataValidator(fromApiJsonHelper, configurationReadPlatformService);
    }

    private static String validMinimalCreateJson(String dateFormat) {
        return """
                {
                  "officeId": 1,
                  "firstname": "John",
                  "lastname": "Doe",
                  "active": false,
                  "legalFormId": 1,
                  "locale": "en",
                  "dateFormat": "%s"
                }
                """.formatted(dateFormat);
    }

    @Test
    void validateForCreate_withInvalidDateFormat_throwsPlatformApiDataValidationException() {
        String json = validMinimalCreateJson("02 February 2026");

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForCreate(json));

        boolean hasDateFormatError = ex.getErrors().stream()
                .anyMatch(e -> ClientApiConstants.dateFormatParamName.equals(e.getParameterName()));
        assertTrue(hasDateFormatError, "Expected validation error for parameter 'dateFormat'");
        assertTrue(
                ex.getErrors().stream().filter(e -> ClientApiConstants.dateFormatParamName.equals(e.getParameterName()))
                        .anyMatch(e -> e.getDefaultUserMessage().contains("Invalid dateFormat")
                                || "validation.msg.invalid.dateFormat.format".equals(e.getDeveloperMessage())),
                "Expected dateFormat error to mention invalid dateFormat or use validation.msg.invalid.dateFormat.format");
    }

    @Test
    void validateForCreate_withValidDateFormat_doesNotThrow() {
        String json = validMinimalCreateJson("dd MMMM yyyy");

        assertDoesNotThrow(() -> validator.validateForCreate(json));
    }

    @Test
    void validateForCreate_withAnotherInvalidDateFormat_throwsPlatformApiDataValidationException() {
        String json = validMinimalCreateJson("dd bbb yyyy");

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForCreate(json));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ClientApiConstants.dateFormatParamName.equals(e.getParameterName())));
    }

    private static String validMinimalUpdateJson(String dateFormat) {
        return """
                {
                  "firstname": "Jane",
                  "lastname": "Doe",
                  "locale": "en",
                  "dateFormat": "%s"
                }
                """.formatted(dateFormat);
    }

    @Test
    void validateForUpdate_withInvalidDateFormat_throwsPlatformApiDataValidationException() {
        String json = validMinimalUpdateJson("02 February 2026");

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForUpdate(json));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ClientApiConstants.dateFormatParamName.equals(e.getParameterName())));
    }

    @Test
    void validateForUpdate_withValidDateFormat_doesNotThrow() {
        String json = validMinimalUpdateJson("yyyy-MM-dd");

        assertDoesNotThrow(() -> validator.validateForUpdate(json));
    }
}
