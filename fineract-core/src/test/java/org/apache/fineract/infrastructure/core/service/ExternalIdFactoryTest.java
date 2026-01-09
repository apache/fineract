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
package org.apache.fineract.infrastructure.core.service;

import java.util.Arrays;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ExternalIdFactory} static factory methods.
 */
class ExternalIdFactoryTest {

    @Test
    void testProduceWithValidStringShouldReturnExternalIdWithValue() {
        // given
        String value = "test-external-id-123";

        // when
        ExternalId result = ExternalIdFactory.produce(value);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(value, result.getValue());
    }

    @Test
    void testProduceWithNullShouldReturnEmptyExternalId() {
        // when
        ExternalId result = ExternalIdFactory.produce((String) null);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertNull(result.getValue());
    }

    @Test
    void testProduceWithEmptyStringShouldReturnEmptyExternalId() {
        // when
        ExternalId result = ExternalIdFactory.produce("");

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertNull(result.getValue());
    }

    @Test
    void testProduceWithBlankStringShouldReturnEmptyExternalId() {
        // when
        ExternalId result = ExternalIdFactory.produce("   ");

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertNull(result.getValue());
    }

    @Test
    void testProduceListWithValidValuesShouldReturnListOfExternalIds() {
        // given
        List<String> values = Arrays.asList("id-1", "id-2", "id-3");

        // when
        List<ExternalId> result = ExternalIdFactory.produce(values);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals("id-1", result.get(0).getValue());
        Assertions.assertEquals("id-2", result.get(1).getValue());
        Assertions.assertEquals("id-3", result.get(2).getValue());
    }

    @Test
    void testProduceListWithMixedValuesShouldHandleBlankValues() {
        // given
        List<String> values = Arrays.asList("valid-id", "", null, "   ", "another-valid-id");

        // when
        List<ExternalId> result = ExternalIdFactory.produce(values);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(5, result.size());
        Assertions.assertFalse(result.get(0).isEmpty());
        Assertions.assertEquals("valid-id", result.get(0).getValue());
        Assertions.assertTrue(result.get(1).isEmpty());
        Assertions.assertTrue(result.get(2).isEmpty());
        Assertions.assertTrue(result.get(3).isEmpty());
        Assertions.assertFalse(result.get(4).isEmpty());
        Assertions.assertEquals("another-valid-id", result.get(4).getValue());
    }

    @Test
    void testProduceListWithNullShouldThrowNullPointerException() {
        // when / then
        Assertions.assertThrows(NullPointerException.class, () -> ExternalIdFactory.produce((List<String>) null));
    }

    @Test
    void testProduceListWithEmptyListShouldReturnEmptyList() {
        // given
        List<String> values = List.of();

        // when
        List<ExternalId> result = ExternalIdFactory.produce(values);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }
}
