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
package org.apache.fineract.cob.workingcapitalloan;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;

/**
 * Pins the WC COB worker reader, processor and writer to step scope.
 * <p>
 * All three keep per-partition state in instance fields: the reader its queue of loan ids, the processor the step's
 * execution context and business date. With a single shared instance, two partitions running at the same time on one
 * instance (more eligible loans than the partition size in one tenant, or several tenants running WC COB at once)
 * overwrite each other's queue; one of them reads nothing, finishes COMPLETED and leaves its loan one COB day behind
 * with a stale {@code LOAN_COB_CHUNK_PROCESSING} lock. LOAN_COB has always declared these components as
 * {@code @StepScope} beans; this test fails if WC COB drifts back to building them inline.
 */
public class WorkingCapitalLoanCOBWorkerStepScopeTest {

    private static Stream<Arguments> perPartitionComponents() {
        return Stream.of(//
                Arguments.of(WorkingCapitalLoanCOBWorkerItemReader.class), //
                Arguments.of(WorkingCapitalLoanCOBWorkerItemProcessor.class), //
                Arguments.of(WorkingCapitalLoanCOBWorkerItemWriter.class));
    }

    @ParameterizedTest(name = "{0} must be a @StepScope bean")
    @MethodSource("perPartitionComponents")
    public void shouldDeclareAStepScopedBeanReturningTheConcreteClass(Class<?> componentClass) {
        Stream<Method> factories = Arrays.stream(WorkingCapitalLoanCOBWorkerConfiguration.class.getDeclaredMethods())
                .filter(method -> method.getReturnType().equals(componentClass));

        assertThat(factories)
                .as("%s must come from a @Bean @StepScope factory method returning the concrete class, "
                        + "so that each partition gets its own instance and Spring Batch registers it as a StepExecutionListener",
                        componentClass.getSimpleName())
                .singleElement().satisfies(method -> assertThat(method.isAnnotationPresent(Bean.class)).isTrue())
                .satisfies(method -> assertThat(method.isAnnotationPresent(StepScope.class)).isTrue());
    }

    @Test
    public void workerStepShouldBeBuiltFromTheStepScopedBeans() {
        Stream<Method> stepFactories = Arrays.stream(WorkingCapitalLoanCOBWorkerConfiguration.class.getDeclaredMethods())
                .filter(method -> method.getReturnType().equals(Step.class));

        assertThat(stepFactories).as("the worker step must take the reader, processor and writer as injected step-scoped beans")
                .singleElement()
                .satisfies(method -> assertThat(method.getParameterTypes()).containsExactlyInAnyOrder(
                        WorkingCapitalLoanCOBWorkerItemReader.class, WorkingCapitalLoanCOBWorkerItemProcessor.class,
                        WorkingCapitalLoanCOBWorkerItemWriter.class));
    }
}
