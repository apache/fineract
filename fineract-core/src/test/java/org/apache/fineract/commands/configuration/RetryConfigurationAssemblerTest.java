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
package org.apache.fineract.commands.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.persistence.OptimisticLockException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.ConcurrencyFailureException;

class RetryConfigurationAssemblerTest {

    private RetryConfigurationAssembler underTest;

    @BeforeEach
    void setUp() {
        FineractProperties.RetryProperties.InstancesProperties.ExecuteCommandProperties executeCommand = new FineractProperties.RetryProperties.InstancesProperties.ExecuteCommandProperties();
        executeCommand.setRetryExceptions(new Class[] { ConcurrencyFailureException.class });
        executeCommand.setMaxAttempts(3);
        executeCommand.setWaitDuration(Duration.ZERO);
        FineractProperties.RetryProperties.InstancesProperties.RetryInstanceProperties inlineCob = new FineractProperties.RetryProperties.InstancesProperties.RetryInstanceProperties();
        inlineCob.setMaxAttempts(5);
        inlineCob.setWaitDuration(Duration.ZERO);
        FineractProperties.RetryProperties.InstancesProperties instances = new FineractProperties.RetryProperties.InstancesProperties();
        instances.setExecuteCommand(executeCommand);
        instances.setInlineCob(inlineCob);
        FineractProperties.RetryProperties retry = new FineractProperties.RetryProperties();
        retry.setInstances(instances);
        FineractProperties fineractProperties = new FineractProperties();
        fineractProperties.setRetry(retry);

        underTest = new RetryConfigurationAssembler(RetryRegistry.ofDefaults(), fineractProperties,
                mock(FineractRequestContextHolder.class));
    }

    @Test
    void shouldInlineCobRetryOnAnyFailure() {
        Predicate<Throwable> predicate = underTest.getRetryConfigurationForInlineCob().getRetryConfig().getExceptionPredicate();

        // the configured executeCommand exception list must not narrow this one: an inline COB failure is recorded on
        // the account lock, so the next attempt can take the accounts over and re-run the job whatever went wrong
        assertTrue(predicate.test(new OptimisticLockException("stale")));
        assertTrue(predicate.test(new IllegalStateException("business step blew up")));
        assertTrue(predicate.test(new RuntimeException("anything at all")));
    }

    @Test
    void shouldInlineCobRetryTakeItsPacingFromItsOwnProperties() {
        // 5, not the 3 configured for executeCommand
        assertEquals(5, underTest.getRetryConfigurationForInlineCob().getRetryConfig().getMaxAttempts());
        assertEquals(3, underTest.getRetryConfigurationForExecuteCommand().getRetryConfig().getMaxAttempts());
    }

    @Test
    void shouldInlineCobRetryNotRecordTheEvaluatedExceptionInTheRequestContext() {
        // SynchronousCommandProcessingService#exceptionWhenTheRequestAlreadyProcessed reads that recording to decide
        // whether an UNDER_PROCESSING command is its own retry, so priming it here would suppress the idempotency guard
        Retry retry = underTest.getRetryConfigurationForInlineCob();

        retry.getRetryConfig().getExceptionPredicate().test(new OptimisticLockException("stale"));

        assertNull(underTest.getLastException());
    }

    @Test
    void shouldExecuteCommandBackoffBeDeterministicWithoutJitter() {
        RetryConfigurationAssembler assembler = assemblerWithBackoff(null);

        for (int i = 0; i < 20; i++) {
            assertEquals(1000L, waitBeforeRetry(assembler, 1));
            assertEquals(2000L, waitBeforeRetry(assembler, 2));
        }
    }

    @Test
    void shouldExecuteCommandBackoffBeDeterministicWhenJitterIsZero() {
        RetryConfigurationAssembler assembler = assemblerWithBackoff(0.0);

        assertEquals(1000L, waitBeforeRetry(assembler, 1));
        assertEquals(2000L, waitBeforeRetry(assembler, 2));
    }

    @Test
    void shouldExecuteCommandBackoffVaryWithinTheJitterBandWhenJitterIsSet() {
        RetryConfigurationAssembler assembler = assemblerWithBackoff(0.5);
        Set<Long> firstWaits = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            long first = waitBeforeRetry(assembler, 1);
            long second = waitBeforeRetry(assembler, 2);
            assertTrue(first >= 500 && first <= 1500, "first wait outside +/-50% of 1s: " + first);
            assertTrue(second >= 1000 && second <= 3000, "second wait outside +/-50% of 2s: " + second);
            firstWaits.add(first);
        }

        // callers that failed together must not all retry after the same wait
        assertTrue(firstWaits.size() > 20, "waits were not randomized: " + firstWaits.size() + " distinct values");
    }

    private RetryConfigurationAssembler assemblerWithBackoff(Double jitter) {
        FineractProperties.RetryProperties.InstancesProperties.ExecuteCommandProperties executeCommand = new FineractProperties.RetryProperties.InstancesProperties.ExecuteCommandProperties();
        executeCommand.setRetryExceptions(new Class[] { ConcurrencyFailureException.class });
        executeCommand.setMaxAttempts(3);
        executeCommand.setWaitDuration(Duration.ofSeconds(1));
        executeCommand.setEnableExponentialBackoff(true);
        executeCommand.setExponentialBackoffMultiplier(2.0);
        executeCommand.setExponentialBackoffJitter(jitter);
        FineractProperties.RetryProperties.InstancesProperties instances = new FineractProperties.RetryProperties.InstancesProperties();
        instances.setExecuteCommand(executeCommand);
        FineractProperties.RetryProperties retry = new FineractProperties.RetryProperties();
        retry.setInstances(instances);
        FineractProperties fineractProperties = new FineractProperties();
        fineractProperties.setRetry(retry);
        return new RetryConfigurationAssembler(RetryRegistry.ofDefaults(), fineractProperties, mock(FineractRequestContextHolder.class));
    }

    private long waitBeforeRetry(RetryConfigurationAssembler assembler, int attempt) {
        return assembler.getRetryConfigurationForExecuteCommand().getRetryConfig().getIntervalBiFunction().apply(attempt, null);
    }
}
