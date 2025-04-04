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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class LoopGuardTest {

    static class TestContext implements LoopContext {

        int iteration;
    }

    @ParameterizedTest
    // target value, max iterations
    @CsvSource({ "3, 5", //
            "5, 6", //
            "2, 10", //
            "2, 2", //
    })
    void testSafeDoWhileLoopExecutesCorrectly(int targetValue, int maxIterations) {
        TestContext context = new TestContext();

        Predicate<TestContext> condition = ctx -> ctx.iteration < targetValue;
        LoopGuard.LoopBody<TestContext> body = ctx -> ctx.iteration++;

        LoopGuard.runSafeDoWhileLoop(maxIterations, context, condition, body);

        Assertions.assertEquals(targetValue, context.iteration);
    }

    @ParameterizedTest
    // target value, max iterations
    @CsvSource({ "2, 5", //
            "4, 5", //
            "6, 10", //
            "2, 2" //
    })
    void testSafeWhileLoopExecutesCorrectly(int targetValue, int maxIterations) {
        TestContext context = new TestContext();

        Predicate<TestContext> condition = ctx -> ctx.iteration < targetValue;
        LoopGuard.LoopBody<TestContext> body = ctx -> ctx.iteration++;

        LoopGuard.runSafeWhileLoop(maxIterations, context, condition, body);

        Assertions.assertEquals(targetValue, context.iteration);
    }

    @ParameterizedTest
    // max iterations
    @CsvSource({ "10", //
            "5", //
            "1", //
            "0", //
            "-1" //
    })
    void testSafeDoWhileLoopThrowsOnExceedingMaxIterations(int maxIterations) {
        TestContext context = new TestContext();

        Predicate<TestContext> condition = ctx -> true; // infinite
        LoopGuard.LoopBody<TestContext> body = ctx -> ctx.iteration++;

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> LoopGuard.runSafeDoWhileLoop(maxIterations, context, condition, body));

        Assertions.assertTrue(exception.getMessage().contains("Loop exceeded " + maxIterations));
    }

    @ParameterizedTest
    // max iterations
    @CsvSource({ "10", //
            "3", //
            "2" //
    })
    void testSafeWhileLoopThrowsOnExceedingMaxIterations(int maxIterations) {
        TestContext context = new TestContext();

        Predicate<TestContext> condition = ctx -> true; // infinite
        LoopGuard.LoopBody<TestContext> body = ctx -> ctx.iteration++;

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> LoopGuard.runSafeWhileLoop(maxIterations, context, condition, body));

        Assertions.assertTrue(exception.getMessage().contains("Loop exceeded " + maxIterations));
    }

    @Test
    void testSafeDoWhileLoopRunsAtLeastOnce() {
        TestContext context = new TestContext();
        AtomicInteger counter = new AtomicInteger();

        Predicate<TestContext> condition = ctx -> false;
        LoopGuard.LoopBody<TestContext> body = ctx -> counter.incrementAndGet();

        LoopGuard.runSafeDoWhileLoop(3, context, condition, body);

        Assertions.assertEquals(1, counter.get(), "Do-while loop should execute at least once");
    }

    @Test
    void testSafeWhileLoopSkipsIfConditionFalseInitially() {
        TestContext context = new TestContext();
        AtomicInteger counter = new AtomicInteger();

        Predicate<TestContext> condition = ctx -> false;
        LoopGuard.LoopBody<TestContext> body = ctx -> counter.incrementAndGet();

        LoopGuard.runSafeWhileLoop(3, context, condition, body);

        Assertions.assertEquals(0, counter.get(), "While loop should skip execution if condition is false");
    }
}
