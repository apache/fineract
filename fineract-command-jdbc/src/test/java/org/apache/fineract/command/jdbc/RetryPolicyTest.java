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
package org.apache.fineract.command.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.springboot.retry.autoconfigure.RetryAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;

/**
 * Pins which failures are worth another attempt, and proves the policy reaches an instance that does not state one.
 *
 * <p>
 * The distinction is whether the same call could succeed if it ran again. A deadlock, a lock timeout, a serialisation
 * failure, a connection dropped mid-flight: all of those are about when the call happened rather than what it asked
 * for, and a second attempt is the right response. A unique constraint is the opposite - the row is there, and it will
 * still be there on the third attempt - so retrying only delays the answer and holds a connection while it does.
 *
 * <p>
 * The widened list is set on the command store alone. The jobs that post interest and run close-of-business keep the
 * narrower one they already had, because a connection lost during commit leaves the outcome of that commit unknown, and
 * retrying an interest posting there is how it gets posted twice. Writing the command row is safe to retry for the
 * opposite reason: the idempotency key is unique, so a second attempt cannot write the row twice.
 */
class RetryPolicyTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class, RetryAutoConfiguration.class)).withPropertyValues(
                    // the same two lines the application sets for this instance, and for this instance only
                    "resilience4j.retry.instances.commandStore.retryExceptions="
                            + "org.springframework.dao.TransientDataAccessException,org.springframework.dao.RecoverableDataAccessException",
                    "resilience4j.retry.instances.commandStore.ignoreExceptions=org.springframework.dao.DataIntegrityViolationException",
                    "resilience4j.retry.instances.commandStore.max-attempts=3");

    @Test
    void aFailureThatMightNotHappenAgainIsRetried() {
        contextRunner.run(context -> {
            var predicate = policyOf(context.getBean(RetryRegistry.class));

            assertThat(predicate.test(new DeadlockLoserDataAccessException("deadlock", null))).as("a deadlock").isTrue();
            assertThat(predicate.test(new CannotAcquireLockException("lock timeout"))).as("a lock that could not be taken").isTrue();
            assertThat(predicate.test(new QueryTimeoutException("timeout"))).as("a query timeout").isTrue();
            assertThat(predicate.test(new TransientDataAccessResourceException("connection reset"))).as("a dropped connection").isTrue();
            assertThat(predicate.test(new RecoverableDataAccessException("connection lost"))).as("a recoverable failure").isTrue();
        });
    }

    @Test
    void aFailureThatWouldHappenEveryTimeIsNotRetried() {
        contextRunner.run(context -> {
            var predicate = policyOf(context.getBean(RetryRegistry.class));

            assertThat(predicate.test(new DuplicateKeyException("the key is already claimed")))
                    .as("a unique constraint is a verdict, not a mishap").isFalse();
            assertThat(predicate.test(new DataIntegrityViolationException("a column will not take that value")))
                    .as("the data is wrong and will still be wrong next time").isFalse();
        });
    }

    private static java.util.function.Predicate<Throwable> policyOf(RetryRegistry registry) {
        return registry.retry("commandStore").getRetryConfig().getExceptionPredicate();
    }
}
