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
package org.apache.fineract.infrastructure.core.diagnostics.performance;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.util.StopWatch;

public final class MeasuringUtil {

    private MeasuringUtil() {}

    public static void measure(Runnable r, Consumer<Duration> c) {
        measure(() -> {
            r.run();
            return null;
        }, c);
    }

    public static <T> T measure(Supplier<T> s, Consumer<Duration> c) {
        return measure(s, (result, timeTaken) -> c.accept(timeTaken));
    }

    public static <T> T measure(Supplier<T> s, BiConsumer<T, Duration> c) {
        StopWatch sw = new StopWatch();
        sw.start();
        T result = null;
        try {
            result = s.get();
        } finally {
            sw.stop();
            c.accept(result, Duration.ofMillis(sw.getTotalTimeMillis()));
        }
        return result;
    }
}
