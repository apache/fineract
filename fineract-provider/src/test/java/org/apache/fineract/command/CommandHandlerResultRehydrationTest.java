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

import static org.apache.fineract.command.core.CommandConstants.COMMAND_JSON_CLASS_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.command.jdbc.store.mapping.JsonCommandMapper;
import org.apache.fineract.infrastructure.core.jackson3.Jackson3ObjectMapperConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.filter.AssignableTypeFilter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * Holds every command handler's result to being readable back from the command store.
 *
 * <p>
 * A repeat of an idempotency key is answered by reading the stored result back into the object the handler returned and
 * handing it to the web layer again. A result type Jackson cannot construct - one with a builder but no constructor it
 * can use, say - stores fine and fails only when a client repeats the request, which is then refused with {@code 409}
 * instead of being given its answer. Nothing else notices: the first request succeeds, and every test that does not
 * repeat a request passes.
 *
 * <p>
 * This walks every {@link CommandHandler} on the classpath, builds a stored row for its result type with every nested
 * object present, and reads it back through the mapper the command store uses.
 */
class CommandHandlerResultRehydrationTest {

    /** The sample handlers that exist to exercise the framework; they are not commands the application serves. */
    private static final String SAMPLE_HANDLERS = "org.apache.fineract.command.test";

    private static final String FINERACT_PACKAGE = "org.apache.fineract";

    private final ObjectMapper mapper = new Jackson3ObjectMapperConfig().jackson3ObjectMapper();

    private final JsonCommandMapper commandJsonMapper = new JsonCommandMapper(mapper);

    @Test
    void everyCommandResultCanBeReadBackForAReplay() {
        var unreadable = new ArrayList<String>();

        for (Class<?> handler : commandHandlers()) {
            if (handler.getName().startsWith(SAMPLE_HANDLERS)) {
                continue;
            }

            var result = ResolvableType.forClass(handler).as(CommandHandler.class).getGeneric(1).resolve();

            if (result == null || Void.class.equals(result)) {
                continue;
            }

            var stored = storedRow(result, new HashSet<>());

            stored.set(COMMAND_JSON_CLASS_ATTRIBUTE, new StringNode(result.getCanonicalName()));

            try {
                assertThat((Object) commandJsonMapper.map(stored)).isInstanceOf(result);
            } catch (RuntimeException | AssertionError e) {
                unreadable.add(handler.getSimpleName() + " -> " + result.getSimpleName() + ": " + rootMessage(e));
            }
        }

        assertThat(unreadable).as("These command results cannot be read back from the command store, so a repeat of the request is "
                + "refused with 409 instead of being given its answer. Give the type, and every type nested in it, a "
                + "constructor Jackson can use - for a Lombok @Builder that means adding @NoArgsConstructor and " + "@AllArgsConstructor.")
                .isEmpty();
    }

    /**
     * A stored row with every nested Fineract type present as an object, so that each one has to be constructed too.
     * Scalars are left out: they never need a constructor, and their formats are covered by the wire-format tests.
     */
    private ObjectNode storedRow(Class<?> type, Set<Class<?>> visiting) {
        var node = mapper.createObjectNode();

        if (!visiting.add(type)) {
            return node;
        }

        for (var current = type; current != null && !Object.class.equals(current); current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }

                var name = propertyName(field);
                var nested = nestedType(field.getType());

                if (nested != null) {
                    node.set(name, storedRow(nested, visiting));
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    var element = nestedType(elementType(field.getGenericType()));

                    if (element != null) {
                        node.putArray(name).add(storedRow(element, visiting));
                    }
                }
            }
        }

        visiting.remove(type);

        return node;
    }

    private static String propertyName(Field field) {
        var property = field.getAnnotation(JsonProperty.class);

        return property != null && !property.value().isEmpty() ? property.value() : field.getName();
    }

    private static Class<?> nestedType(Class<?> type) {
        if (type == null || type.isEnum() || type.isInterface() || type.isPrimitive() || !type.getName().startsWith(FINERACT_PACKAGE)) {
            return null;
        }

        return type;
    }

    private static Class<?> elementType(Type type) {
        if (type instanceof ParameterizedType parameterized && parameterized.getActualTypeArguments().length == 1
                && parameterized.getActualTypeArguments()[0] instanceof Class<?> element) {
            return element;
        }

        return null;
    }

    private static String rootMessage(Throwable t) {
        var root = t;

        while (root.getCause() != null) {
            root = root.getCause();
        }

        var message = root.getMessage();

        return message == null ? root.getClass().getSimpleName() : message.lines().findFirst().orElse(message);
    }

    private static List<Class<?>> commandHandlers() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AssignableTypeFilter(CommandHandler.class));

        var found = new ArrayList<Class<?>>();

        for (BeanDefinition definition : scanner.findCandidateComponents(FINERACT_PACKAGE)) {
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
