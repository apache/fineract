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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PhoneNumberValidatorTest {

    @Mock
    private PhoneNumberValidationService phoneNumberValidationService;

    private PhoneNumberValidator underTest;

    @BeforeEach
    public void setUp() {
        underTest = new PhoneNumberValidator(phoneNumberValidationService);
    }

    @Test
    public void delegatesToServiceWhenValid() {
        when(phoneNumberValidationService.isValid("1234567")).thenReturn(true);

        assertTrue(underTest.isValid("1234567", null));
    }

    @Test
    public void delegatesToServiceWhenInvalid() {
        when(phoneNumberValidationService.isValid("abcdef")).thenReturn(false);

        assertFalse(underTest.isValid("abcdef", null));
    }
}
