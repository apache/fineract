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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class StreamUtil {

    private StreamUtil() {}

    public static <A, B> Collector<A, ?, B> foldLeft(final B init, final BiFunction<? super B, ? super A, ? extends B> f) {
        return Collectors.collectingAndThen(Collectors.reducing(Function.<B>identity(), a -> b -> f.apply(b, a), Function::andThen),
                endo -> endo.apply(init));
    }

    /**
     * Collector that merges a stream of maps (with list values) into a single map.
     * <p>
     * If the same key appears in multiple maps, the lists are concatenated.
     *
     * Example:
     *
     * <pre>{@code
     *
     * Map<Long, List<Foo>> merged = streamOfMaps.collect(StreamUtils.mergeMapsOfLists());
     * }</pre>
     *
     * @param <K>
     *            the type of map keys
     * @param <V>
     *            the type of elements in the value lists
     * @return a collector producing a merged map with concatenated list values
     */
    public static <K, V> Collector<Map<K, List<V>>, ?, Map<K, List<V>>> mergeMapsOfLists() {
        return Collectors.collectingAndThen(Collectors.flatMapping((Map<K, List<V>> m) -> m.entrySet().stream(),
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (list1, list2) -> {
                    List<V> merged = new ArrayList<>(list1);
                    merged.addAll(list2);
                    return merged;
                })), HashMap::new // ensures the result is mutable
        );
    }
}
