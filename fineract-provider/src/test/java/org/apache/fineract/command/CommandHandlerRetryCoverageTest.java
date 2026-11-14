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
package org.apache.fineract.command;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.retry.annotation.Retry;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.command.core.CommandHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * Holds every command handler to the retry policy, so that executing a command is as resilient as recording it.
 *
 * <p>
 * Retrying is applied per handler rather than once around the dispatcher, because each command gets its own policy to
 * tune and because the retry has to sit outside the handler's own transaction - a second attempt inside a transaction
 * already marked for rollback would achieve nothing. The cost of that arrangement is that a new handler is retried only
 * if somebody remembers to say so, and nothing fails when they do not: the command simply gives up on the first
 * contention, where its neighbours would have tried again.
 *
 * <p>
 * This is the reminder. It walks every {@link CommandHandler} on the classpath rather than a list that would itself
 * need maintaining.
 */
class CommandHandlerRetryCoverageTest {

    /**
     * The sample handlers that exist to exercise the framework, including one that is deliberately made to fail. They
     * are not commands the application serves.
     */
    private static final String SAMPLE_HANDLERS = "org.apache.fineract.command.test";

    @Test
    void everyCommandHandlerRetriesItsExecution() {
        var withoutRetry = new ArrayList<String>();

        for (Class<?> handler : commandHandlers()) {
            if (handler.getName().startsWith(SAMPLE_HANDLERS)) {
                continue;
            }

            if (handleMethodOf(handler).map(method -> method.getAnnotation(Retry.class)).isEmpty()) {
                withoutRetry.add(handler.getSimpleName());
            }
        }

        assertThat(withoutRetry).as("These handlers execute their command without a retry, so they give up on the first contention "
                + "where every other command tries again. Add @Retry(name = \"commandXxx\", fallbackMethod = "
                + "\"fallback\") and configure that instance in application.properties.").isEmpty();
    }

    private static java.util.Optional<Method> handleMethodOf(Class<?> handler) {
        return java.util.Arrays.stream(handler.getDeclaredMethods())
                .filter(method -> "handle".equals(method.getName()) && !method.isSynthetic()).findFirst();
    }

    private static List<Class<?>> commandHandlers() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AssignableTypeFilter(CommandHandler.class));

        var found = new ArrayList<Class<?>>();

        for (BeanDefinition definition : scanner.findCandidateComponents("org.apache.fineract")) {
            try {
                found.add(Class.forName(definition.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Scanned a handler that cannot be loaded: " + definition.getBeanClassName(), e);
            }
        }

        assertThat(found).as("The scan must actually find the handlers, or this test passes by finding nothing.").isNotEmpty();

        return found;
    }
}
