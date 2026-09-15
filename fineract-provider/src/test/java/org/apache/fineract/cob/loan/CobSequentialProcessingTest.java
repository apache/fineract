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
package org.apache.fineract.cob.loan;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBWorkerConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.task.TaskExecutor;

/**
 * Pins COB item processing to the step thread.
 * <p>
 * Batch 6's {@code ChunkOrientedStep} selects its concurrent path purely on {@code taskExecutor != null}
 * ({@code isConcurrent()}), and in that path submits the ITEMS of a chunk to the executor. Item processing then runs
 * outside the chunk transaction, so {@code COBBusinessStepService.run} opens its own transaction which commits
 * independently of the chunk write - a chunk-write rollback, a skip in scan mode or a step restart then leaves the
 * business-step writes committed while the loan is not marked COB'd, and the next pass re-runs them over it.
 * <p>
 * COB therefore registers no task executor at all, and this is deliberately not configurable: the thread pool size
 * reads like a throughput dial but decides transactional semantics. This test fails if a {@link TaskExecutor} bean is
 * reintroduced on either COB worker configuration, which is how concurrency would come back.
 * <p>
 * Do not relax this until FINERACT-2684 establishes that every COB business step is idempotent under a second pass.
 */
public class CobSequentialProcessingTest {

    private static Stream<Arguments> cobWorkerConfigurations() {
        return Stream.of(//
                Arguments.of(LoanCOBWorkerConfiguration.class), //
                Arguments.of(WorkingCapitalLoanCOBWorkerConfiguration.class));
    }

    @ParameterizedTest(name = "{0} must not expose a TaskExecutor bean")
    @MethodSource("cobWorkerConfigurations")
    public void shouldNotDeclareATaskExecutorBean(Class<?> configurationClass) {
        Stream<Method> taskExecutorFactories = Arrays.stream(configurationClass.getDeclaredMethods())
                .filter(method -> TaskExecutor.class.isAssignableFrom(method.getReturnType()));

        assertThat(taskExecutorFactories)
                .as("%s must not build a TaskExecutor - COB item processing has to stay on the step thread so that "
                        + "business-step writes join the chunk transaction (FINERACT-2621)", configurationClass.getSimpleName())
                .isEmpty();
    }
}
