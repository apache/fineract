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
package org.apache.fineract.infrastructure.core.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PhoneNumberValidationServiceTest {

    private static final String DEFAULT_REGEX = "^\\+?[0-9]{7,15}$";

    @Mock
    private FineractProperties fineractProperties;

    private PhoneNumberValidationService underTest;

    @BeforeEach
    public void setUp() {
        FineractProperties.FineractPhoneProperties phoneProperties = new FineractProperties.FineractPhoneProperties();
        phoneProperties.setRegex(DEFAULT_REGEX);
        lenient().when(fineractProperties.getPhone()).thenReturn(phoneProperties);
        underTest = new PhoneNumberValidationService(fineractProperties);
    }

    @Test
    public void getRegexReturnsConfiguredRegex() {
        assertEquals(DEFAULT_REGEX, underTest.getRegex());
    }

    @Test
    public void nullPhoneNumberIsConsideredValid() {
        assertTrue(underTest.isValid(null));
    }

    @Test
    public void emptyPhoneNumberIsConsideredValid() {
        assertTrue(underTest.isValid(""));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1234567", "+1234567", "123456789012345", "+123456789012345" })
    public void validPhoneNumbersPassValidation(String phoneNumber) {
        assertTrue(underTest.isValid(phoneNumber));
    }

    @ParameterizedTest
    @ValueSource(strings = { "123456", "abcdefg", "12345678901234567", "+", "123-456-7890" })
    public void invalidPhoneNumbersFailValidation(String phoneNumber) {
        assertFalse(underTest.isValid(phoneNumber));
    }

    @Test
    public void isValidUsesRegexFromFineractProperties() {
        FineractProperties.FineractPhoneProperties strictProperties = new FineractProperties.FineractPhoneProperties();
        strictProperties.setRegex("^[0-9]{10}$");
        lenient().when(fineractProperties.getPhone()).thenReturn(strictProperties);

        assertTrue(underTest.isValid("1234567890"));
        assertFalse(underTest.isValid("+1234567890"));
    }
}
