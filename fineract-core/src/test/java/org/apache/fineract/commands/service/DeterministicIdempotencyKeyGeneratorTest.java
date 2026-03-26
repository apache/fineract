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
package org.apache.fineract.commands.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DeterministicIdempotencyKeyGeneratorTest {

    private final DeterministicIdempotencyKeyGenerator underTest = new DeterministicIdempotencyKeyGenerator();

    @Test
    void shouldGenerateSameKeyForSameInputAndContext() {
        String json = "{\"b\":2,\"a\":1}";
        String context = "action:entity:/endpoint:client1";

        String key1 = underTest.generate(json, context);
        String key2 = underTest.generate("{\"a\":1,\"b\":2}", context);

        assertEquals(key1, key2);
    }

    @Test
    void shouldGenerateDifferentKeysForDifferentContext() {
        String json = "{\"a\":1}";

        String key1 = underTest.generate(json, "context1");
        String key2 = underTest.generate(json, "context2");

        assertNotEquals(key1, key2);
    }

    @Test
    void shouldGenerateDifferentKeysForDifferentPayload() {
        String context = "same-context";

        String key1 = underTest.generate("{\"a\":1}", context);
        String key2 = underTest.generate("{\"a\":2}", context);

        assertNotEquals(key1, key2);
    }

    @Test
    void shouldGenerateSameKeyWithinSameTimeWindow() {
        String json = "{\"a\":1}";
        String context = "ctx";

        String key1 = underTest.generate(json, context);
        String key2 = underTest.generate(json, context);

        assertEquals(key1, key2);
    }

    @Test
    void shouldFailForInvalidJson() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> underTest.generate("{invalid-json", "test-context"));
        assertEquals("Failed to canonicalize JSON", exception.getMessage());
    }
}
