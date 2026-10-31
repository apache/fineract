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
package org.apache.fineract.infrastructure.core.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

class DefaultPlatformPasswordEncoderTest {

    private static final String RAW_PASSWORD = "QwE!5rTy#9uP0";
    private static final String LEGACY_HASH_WITHOUT_PREFIX = "5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a";
    private final PasswordEncoder delegatingEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final DefaultPlatformPasswordEncoder underTest = new DefaultPlatformPasswordEncoder(delegatingEncoder);

    @Test
    void matchesReturnsTrueForTheEncodedPassword() {
        assertTrue(underTest.matches(RAW_PASSWORD, delegatingEncoder.encode(RAW_PASSWORD)));
    }

    @Test
    void matchesReturnsFalseForADifferentPassword() {
        assertFalse(underTest.matches("a-different-password", delegatingEncoder.encode(RAW_PASSWORD)));
    }

    @Test
    void matchesReturnsFalseWhenStoredHashHasNoEncoderPrefix() {
        // The delegating encoder throws on a hash without an "{id}" prefix; the guard must turn that into a non-match
        // so no caller has to pre-check the prefix (and none can reintroduce the 500 by forgetting to).
        assertDoesNotThrow(() -> underTest.matches(RAW_PASSWORD, LEGACY_HASH_WITHOUT_PREFIX));
        assertFalse(underTest.matches(RAW_PASSWORD, LEGACY_HASH_WITHOUT_PREFIX));
    }

    @Test
    void matchesReturnsFalseWhenStoredHashIsNull() {
        assertDoesNotThrow(() -> underTest.matches(RAW_PASSWORD, null));
        assertFalse(underTest.matches(RAW_PASSWORD, null));
    }
}
