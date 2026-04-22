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
package org.apache.fineract.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class StreamUtilTest {

    @Test
    void foldLeft() {
        List<Integer> numbers = List.of(1, 2, 3, 4);

        Integer result = numbers.stream().collect(StreamUtil.foldLeft(0, Integer::sum));

        assertEquals(10, result);
    }

    @Test
    void foldLeftWithEmptyStream() {
        List<Integer> numbers = List.of();

        Integer result = numbers.stream().collect(StreamUtil.foldLeft(0, Integer::sum));

        assertEquals(0, result);
    }

    @Test
    void foldLeftPreservesOrder() {
        List<String> input = List.of("a", "b", "c");

        String result = input.stream().collect(StreamUtil.foldLeft("", (acc, s) -> acc + s));

        assertEquals("abc", result);
    }

    @Test
    void mergeMapsOfLists() {
        Map<String, List<Integer>> map1 = Map.of("a", new ArrayList<>(List.of(1, 2)));
        Map<String, List<Integer>> map2 = Map.of("a", new ArrayList<>(List.of(3, 4)));

        Map<String, List<Integer>> merged = Stream.of(map1, map2).collect(StreamUtil.mergeMapsOfLists());

        assertTrue(merged.get("a").containsAll(List.of(1, 2, 3, 4)));
        assertEquals(4, merged.get("a").size());
    }

    @Test
    void mergeMapsOfListsWithEmptyStream() {
        Map<String, List<Integer>> merged = Stream.<Map<String, List<Integer>>>of().collect(StreamUtil.mergeMapsOfLists());

        assertTrue(merged.isEmpty());
    }

    @Test
    void mergeMapsOfListsWithDifferentKeys() {
        Map<String, List<Integer>> map1 = Map.of("a", new ArrayList<>(List.of(1)));
        Map<String, List<Integer>> map2 = Map.of("b", new ArrayList<>(List.of(2)));

        Map<String, List<Integer>> merged = Stream.of(map1, map2).collect(StreamUtil.mergeMapsOfLists());

        assertEquals(List.of(1), merged.get("a"));
        assertEquals(List.of(2), merged.get("b"));
    }

    @Test
    void mergeMapsOfListsResultIsMutable() {
        Map<String, List<Integer>> map = Map.of("a", new ArrayList<>(List.of(1)));

        Map<String, List<Integer>> merged = Stream.of(map).collect(StreamUtil.mergeMapsOfLists());

        merged.put("b", new ArrayList<>(List.of(2)));

        assertTrue(merged.containsKey("b"));
    }
}
