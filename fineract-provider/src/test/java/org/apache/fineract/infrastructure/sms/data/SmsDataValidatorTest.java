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
package org.apache.fineract.infrastructure.sms.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmsDataValidatorTest {

    private SmsDataValidator validator;

    @BeforeEach
    void setUp() {
        FromJsonHelper fromApiJsonHelper = new FromJsonHelper();
        validator = new SmsDataValidator(fromApiJsonHelper);
    }

    @Test
    void validateForCreate_withExplicitNullStaffId_throws() {
        String json = """
                {
                  "staffId": null,
                  "message": "test message"
                }
                """;
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForCreate(json));
        assertEquals("validation.msg.sms.staffId.cannot.be.blank", exception.getErrors().get(0).getUserMessageGlobalisationCode());
    }

    @Test
    void validateForCreate_withValidStaffId_doesNotThrow() {
        String json = """
                {
                  "staffId": 3,
                  "message": "test message"
                }
                """;
        assertDoesNotThrow(() -> validator.validateForCreate(json));
    }

    @Test
    void validateForCreate_withStaffIdZero_throws() {
        String json = """
                {
                  "staffId": 0,
                  "message": "test message"
                }
                """;
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void validateForCreate_withExplicitNullClientId_throws() {
        String json = """
                {
                  "clientId": null,
                  "message": "test message"
                }
                """;
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void validateForCreate_withExplicitNullGroupId_throws() {
        String json = """
                {
                  "groupId": null,
                  "message": "test message"
                }
                """;
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void validateForCreate_withNoEntityProvided_throws() {
        String json = """
                {
                  "message": "test message"
                }
                """;
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void validateForCreate_withGroupIdAndStaffId_throws() {
        String json = """
                {
                  "groupId": 1,
                  "staffId": 2,
                  "message": "test message"
                }
                """;
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void validateForCreate_withBlankJson_throwsInvalidJsonException() {
        assertThrows(InvalidJsonException.class, () -> validator.validateForCreate(""));
    }
}
