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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.springboot.retry.autoconfigure.RetryAutoConfiguration;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.jdbc.store.JdbcCommandStore;
import org.apache.fineract.command.jdbc.store.domain.CommandEntity;
import org.apache.fineract.command.jdbc.store.domain.JdbcCommandRepository;
import org.apache.fineract.command.jdbc.store.mapping.JdbcCommandMapper;
import org.apache.fineract.command.jdbc.store.mapping.JsonCommandMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Checks that every write to the command row is actually retried, rather than merely annotated.
 *
 * <p>
 * The annotation on its own proves nothing: a method that catches its own exception never gives the retry anything to
 * react to, so the retry reads as present and does nothing. These drive each write through a repository that fails with
 * the contention the policy is configured for, and count how many times it was asked.
 */
class JdbcCommandStoreRetryTest {

    private static final int MAX_ATTEMPTS = 3;

    private final JdbcCommandRepository repository = mock(JdbcCommandRepository.class);

    private final JdbcCommandMapper mapper = mock(JdbcCommandMapper.class);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class, RetryAutoConfiguration.class))
            .withUserConfiguration(StoreConfiguration.class).withBean(JdbcCommandRepository.class, () -> repository)
            .withBean(JdbcCommandMapper.class, () -> mapper)
            .withPropertyValues("resilience4j.retry.instances.commandStore.max-attempts=" + MAX_ATTEMPTS,
                    "resilience4j.retry.instances.commandStore.wait-duration=1ms",
                    "resilience4j.retry.instances.commandStore.retryExceptions=org.springframework.dao.ConcurrencyFailureException");

    @Test
    void claimingAKeyIsRetriedWhenItLosesToContention() {
        when(mapper.map(any(Command.class))).thenReturn(new CommandEntity());
        when(repository.save(any())).thenThrow(new ConcurrencyFailureException("contention"));

        contextRunner.run(context -> {
            var store = context.getBean(CommandStore.class);

            assertThat(catchFailure(() -> store.claim(command()))).isInstanceOf(ConcurrencyFailureException.class);
            verify(repository, times(MAX_ATTEMPTS)).save(any(CommandEntity.class));
        });
    }

    @Test
    void recordingTheAnswerIsRetried() {
        when(repository.updateResponse(anyString(), anyString(), anyInt(), any())).thenThrow(new ConcurrencyFailureException("contention"));

        contextRunner.run(context -> {
            var store = context.getBean(CommandStore.class);

            assertThat(catchFailure(() -> store.recordResponse("a-key", "", 400, "{}"))).isInstanceOf(ConcurrencyFailureException.class);
            verify(repository, times(MAX_ATTEMPTS)).updateResponse(anyString(), anyString(), anyInt(), any());
        });
    }

    private static Throwable catchFailure(Runnable runnable) {
        try {
            runnable.run();

            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    private static Command<Object> command() {
        var command = new TestCommand();

        command.setIdempotencyKey("a-key");

        return command;
    }

    @Configuration
    static class StoreConfiguration {

        @Bean
        CommandStore commandStore(JdbcCommandMapper mapper, JdbcCommandRepository repository) {
            return new JdbcCommandStore(mapper, mock(JsonCommandMapper.class), repository, mock(tools.jackson.databind.ObjectMapper.class),
                    null);
        }
    }

    /** Commands declare their names in code; a test fixture has to as well. */
    private static final class TestCommand extends Command<Object> {

        @Override
        public String entityName() {
            return "DOCUMENT";
        }

        @Override
        public String actionName() {
            return "CREATE";
        }
    }
}
