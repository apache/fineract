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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.junit.jupiter.api.Test;

class HashingServiceTest {

    private final Random rnd = new Random();

    private HashingService underTest = new HashingService();

    @Test
    public void testConsistentHashGeneratesHashesConsistentlyForTheSameValue() {
        // given
        int initialResult = underTest.consistentHash(1L, 20);
        // when & then
        for (int i = 0; i < 20_000; i++) {
            int result = underTest.consistentHash(1L, 20);
            assertThat(result).isEqualTo(initialResult);
        }
    }

    @Test
    public void testConsistentHashWorksForNegativeValues() {
        // given
        // when
        int result = underTest.consistentHash(-1L, 20);
        // then
        assertThat(result).isBetween(0, 19);
    }

    @Test
    public void testConsistentHashGeneratesHashesWithinBuckets() {
        // given
        int buckets = 10;
        // when & then
        for (int i = 0; i < 20_000; i++) {
            int result = underTest.consistentHash(rnd.nextLong(), buckets);
            assertThat(result).isLessThan(buckets);
        }
    }
}
