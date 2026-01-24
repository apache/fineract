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
package org.apache.fineract.infrastructure.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@DisplayName("SpringConfig Thread Pool Tests")
class SpringConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withUserConfiguration(SpringConfig.class)
            .withBean(ThreadPoolTaskExecutorBuilder.class, ThreadPoolTaskExecutorBuilder::new)
            .withInitializer(context -> context.getBeanFactory().registerSingleton("propertySourcesPlaceholderConfigurer",
                    new org.springframework.context.support.PropertySourcesPlaceholderConfigurer()));

    @Test
    @DisplayName("SimpleAsyncTaskExecutor creates unbounded threads")
    void simpleAsyncTaskExecutorCreatesUnboundedThreads() throws Exception {
        String prefix = "SimpleAsync-";
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor(prefix);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch readyLatch = new CountDownLatch(100);
        CountDownLatch doneLatch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            executor.execute(() -> {
                readyLatch.countDown();
                try {
                    startGate.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        boolean allStarted = readyLatch.await(5, TimeUnit.SECONDS);
        assertThat(allStarted).as("All 100 threads should start").isTrue();

        long asyncThreadCount = Thread.getAllStackTraces().keySet().stream().filter(t -> t.getName().startsWith(prefix)).count();

        startGate.countDown();
        boolean allFinished = doneLatch.await(5, TimeUnit.SECONDS);

        assertThat(asyncThreadCount).as("Unbounded executor creates ~100 threads for 100 tasks").isGreaterThan(90);
        assertThat(allFinished).isTrue();
    }

    @Test
    @DisplayName("ThreadPoolTaskExecutor bounds thread creation at maxPoolSize")
    void threadPoolTaskExecutorBoundsThreadCreation() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Bounded-");
        executor.initialize();

        try {
            CountDownLatch latch = new CountDownLatch(500);
            Set<String> threadNames = ConcurrentHashMap.newKeySet();

            for (int i = 0; i < 500; i++) {
                executor.execute(() -> {
                    threadNames.add(Thread.currentThread().getName());
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }
            assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();

            assertThat(threadNames.size()).as("Thread count capped at maxPoolSize").isLessThanOrEqualTo(10);
            assertThat(threadNames.size()).as("Parallelism proof: multiple threads used").isGreaterThan(1);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("Smart defaults: CPU-aware pool sizing")
    void smartDefaultsUseCpuAwarePoolSizing() {
        ThreadPoolTaskExecutorBuilder builder = new ThreadPoolTaskExecutorBuilder();
        SpringConfig config = new SpringConfig();

        ThreadPoolTaskExecutor executor = config.fineractEventExecutor(builder, -1, -1, -1);

        int expectedCore = Runtime.getRuntime().availableProcessors() * 2;
        int expectedMax = Runtime.getRuntime().availableProcessors() * 5;

        assertThat(executor.getCorePoolSize()).isEqualTo(expectedCore);
        assertThat(executor.getMaxPoolSize()).isEqualTo(expectedMax);
        assertThat(executor.getQueueCapacity()).isEqualTo(100);
    }

    @Test
    @DisplayName("User properties override smart defaults")
    void userPropertiesOverrideSmartDefaults() {
        ThreadPoolTaskExecutorBuilder builder = new ThreadPoolTaskExecutorBuilder();
        SpringConfig config = new SpringConfig();

        ThreadPoolTaskExecutor executor = config.fineractEventExecutor(builder, 16, 40, 200);

        assertThat(executor.getCorePoolSize()).isEqualTo(16);
        assertThat(executor.getMaxPoolSize()).isEqualTo(40);
        assertThat(executor.getQueueCapacity()).isEqualTo(200);
    }

    @Test
    @DisplayName("Absolute safety: maxSize adjusted when user sets high core but low max")
    void absoluteSafetyAdjustsMaxWhenUserSetsHighCoreButLowMax() {
        ThreadPoolTaskExecutorBuilder builder = new ThreadPoolTaskExecutorBuilder();
        SpringConfig config = new SpringConfig();

        ThreadPoolTaskExecutor executor = config.fineractEventExecutor(builder, 50, 40, -1);

        assertThat(executor.getCorePoolSize()).isEqualTo(50);
        assertThat(executor.getMaxPoolSize()).as("Max adjusted to match core").isEqualTo(50);
    }

    @Test
    @DisplayName("Absolute safety: maxSize adjusted when smart default too small for user core")
    void absoluteSafetyAdjustsMaxWhenSmartDefaultTooSmallForUserCore() {
        ThreadPoolTaskExecutorBuilder builder = new ThreadPoolTaskExecutorBuilder();
        SpringConfig config = new SpringConfig();

        int cpus = Runtime.getRuntime().availableProcessors();
        int smartMax = cpus * 5;

        ThreadPoolTaskExecutor executor = config.fineractEventExecutor(builder, smartMax + 10, -1, -1);

        assertThat(executor.getCorePoolSize()).isEqualTo(smartMax + 10);
        assertThat(executor.getMaxPoolSize()).as("Max adjusted above smart default to match core").isEqualTo(smartMax + 10);
    }

    @Test
    @DisplayName("CallerRunsPolicy provides backpressure when pool is saturated")
    void threadPoolWithCallerRunsPolicyProvidesBackpressure() throws Exception {
        String mainThread = Thread.currentThread().getName();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        try {
            AtomicInteger callerExecutions = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(50);

            for (int i = 0; i < 50; i++) {
                executor.execute(() -> {
                    if (Thread.currentThread().getName().equals(mainThread)) {
                        callerExecutions.incrementAndGet();
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }
            assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();

            assertThat(callerExecutions.get()).as("Caller thread executed rejected tasks").isGreaterThan(0);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("Configured executor has CallerRunsPolicy rejection handler")
    void configuredExecutorHasCallerRunsPolicy() {
        ThreadPoolTaskExecutorBuilder builder = new ThreadPoolTaskExecutorBuilder();
        SpringConfig config = new SpringConfig();

        ThreadPoolTaskExecutor executor = config.fineractEventExecutor(builder, -1, -1, -1);
        executor.initialize();

        try {
            assertThat(executor.getThreadPoolExecutor().getRejectedExecutionHandler())
                    .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("Bounded pool prevents thread explosion under 500 concurrent tasks")
    void boundedPoolPreventsThreadExplosionUnderHighLoad() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("FineractEvent-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        try {
            int taskCount = 500;
            CountDownLatch latch = new CountDownLatch(taskCount);
            Set<String> threadNames = ConcurrentHashMap.newKeySet();

            for (int i = 0; i < taskCount; i++) {
                executor.execute(() -> {
                    threadNames.add(Thread.currentThread().getName());
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }

            assertThat(latch.await(60, TimeUnit.SECONDS)).isTrue();

            long poolThreadCount = threadNames.stream().filter(name -> name.startsWith("FineractEvent-")).count();

            assertThat(poolThreadCount).as("Pool creates max 10 threads, not 500").isLessThanOrEqualTo(10);

            assertThat(poolThreadCount).as("Pool threads were created").isGreaterThan(0);

            boolean mainThreadHelped = threadNames.stream().anyMatch(name -> !name.startsWith("FineractEvent-"));

            assertThat(mainThreadHelped).as("Main thread executed rejected tasks (CallerRunsPolicy)").isTrue();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("CallerRunsPolicy provides natural backpressure when pool saturated")
    void callerRunsPolicyProvidesBackpressureUnderLoad() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        try {
            String mainThreadName = Thread.currentThread().getName();
            AtomicInteger mainThreadExecutions = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(50);

            for (int i = 0; i < 50; i++) {
                executor.execute(() -> {
                    if (Thread.currentThread().getName().equals(mainThreadName)) {
                        mainThreadExecutions.incrementAndGet();
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }

            assertThat(latch.await(30, TimeUnit.SECONDS)).as("All tasks completed despite saturation").isTrue();

            assertThat(mainThreadExecutions.get()).as("Main thread executed rejected tasks (natural backpressure)").isGreaterThan(0);
        } finally {
            executor.shutdown();
        }
    }

    @ParameterizedTest(name = "Mode: {0}")
    @DisplayName("Event executor is active in all Fineract modes")
    @CsvSource({ "fineract.mode.read-enabled=true", "fineract.mode.write-enabled=true", "fineract.mode.batch-worker-enabled=true",
            "fineract.mode.batch-manager-enabled=true" })
    void verifyExecutorIsActiveInMode(String modeProperty) {
        contextRunner.withPropertyValues(modeProperty).run(context -> {
            assertThat(context).hasBean("fineractEventExecutor");
            ThreadPoolTaskExecutor executor = context.getBean("fineractEventExecutor", ThreadPoolTaskExecutor.class);
            assertThat(executor.getThreadNamePrefix()).isEqualTo("FineractEvent-");
        });
    }
}
