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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.domain.PlatformUser;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

class JsonCommandTest {

    private static final String PASSWORD = "password";
    private static final String RAW_PASSWORD = "QwE!5rTy#9uP0";
    private static final String OTHER_RAW_PASSWORD = "pX268-4Pfv|kF6";
    private static final String LEGACY_RAW_PASSWORD = "password";
    private static final String LEGACY_SHA256_HASH = "{SHA-256}{1}5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a";
    private static final String LEGACY_HASH_WITHOUT_PREFIX = "5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a";
    private static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final PlatformPasswordEncoder PLATFORM_PASSWORD_ENCODER = new PlatformPasswordEncoder() {

        @Override
        public String encode(final PlatformUser appUser) {
            return PASSWORD_ENCODER.encode(appUser.getPassword());
        }

        @Override
        public boolean matches(final String rawPassword, final String encodedPassword) {
            return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
        }
    };

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();

    @Test
    void isChangeInPasswordParameterNamedReturnsFalseForCurrentPassword() {
        JsonCommand command = passwordCommand(RAW_PASSWORD);

        assertFalse(command.isChangeInPasswordParameterNamed(PASSWORD, PASSWORD_ENCODER.encode(RAW_PASSWORD), PLATFORM_PASSWORD_ENCODER));
    }

    @Test
    void isChangeInPasswordParameterNamedReturnsTrueForDifferentPassword() {
        JsonCommand command = passwordCommand(OTHER_RAW_PASSWORD);

        assertTrue(command.isChangeInPasswordParameterNamed(PASSWORD, PASSWORD_ENCODER.encode(RAW_PASSWORD), PLATFORM_PASSWORD_ENCODER));
    }

    @Test
    void isChangeInPasswordParameterNamedReturnsFalseWhenPasswordIsMissing() {
        JsonCommand command = new JsonCommand(1L, fromJsonHelper.parse("{}"), fromJsonHelper);

        assertFalse(command.isChangeInPasswordParameterNamed(PASSWORD, PASSWORD_ENCODER.encode(RAW_PASSWORD), PLATFORM_PASSWORD_ENCODER));
    }

    @Test
    void isChangeInPasswordParameterNamedReturnsFalseForCurrentPasswordWithLegacySha256Hash() {
        JsonCommand command = passwordCommand(LEGACY_RAW_PASSWORD);

        assertFalse(command.isChangeInPasswordParameterNamed(PASSWORD, LEGACY_SHA256_HASH, PLATFORM_PASSWORD_ENCODER));
    }

    @Test
    void isChangeInPasswordParameterNamedReturnsTrueWhenStoredHashHasNoEncoderPrefix() {
        JsonCommand command = passwordCommand(LEGACY_RAW_PASSWORD);

        assertTrue(command.isChangeInPasswordParameterNamed(PASSWORD, LEGACY_HASH_WITHOUT_PREFIX, PLATFORM_PASSWORD_ENCODER));
    }

    private JsonCommand passwordCommand(final String password) {
        return new JsonCommand(1L, fromJsonHelper.parse("{\"" + PASSWORD + "\": \"" + password + "\"}"), fromJsonHelper);
    }
}
