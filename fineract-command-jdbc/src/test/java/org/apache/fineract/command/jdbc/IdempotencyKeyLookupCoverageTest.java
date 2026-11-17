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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.jdbc.store.domain.JdbcCommandRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.jdbc.repository.query.Query;

/**
 * Holds every lookup of a command by its idempotency key to also matching its distinctor.
 *
 * <p>
 * The key is unique only together with {@code custom_idempotency_distinctor}, so the same key can be on several rows. A
 * lookup by the key alone finds whichever of them comes first - a repeat answered with another command's result, or a
 * response recorded against another command's row - and a derived single-row finder fails outright. Nothing else
 * notices while every row under a key has the same distinctor, which is why this is checked by name and query text
 * rather than left to a test that happens to use two distinctors.
 */
class IdempotencyKeyLookupCoverageTest {

    @Test
    void everyRepositoryQueryByKeyAlsoMatchesTheDistinctor() {
        var keyOnly = new ArrayList<String>();

        for (Method method : JdbcCommandRepository.class.getDeclaredMethods()) {
            var query = method.getAnnotation(Query.class);

            if (query != null) {
                var sql = query.value().toLowerCase(Locale.ROOT);

                if (sql.contains("idempotency_key") && !sql.contains("custom_idempotency_distinctor")) {
                    keyOnly.add(method.getName() + ": " + query.value().strip());
                }
            } else if (method.getName().contains("IdempotencyKey") && !method.getName().contains("CustomIdempotencyDistinctor")) {
                keyOnly.add(method.getName());
            }
        }

        assertThat(keyOnly).as("These repository methods look a command up by its idempotency key without its distinctor. Add "
                + "custom_idempotency_distinctor to the query, or AndCustomIdempotencyDistinctor to the derived finder.").isEmpty();
    }

    @Test
    void everyStoreMethodTakingAKeyAlsoTakesTheDistinctor() {
        var keyOnly = new ArrayList<String>();

        for (Method method : CommandStore.class.getDeclaredMethods()) {
            var names = Arrays.stream(method.getParameters()).map(Parameter::getName).toList();

            assertThat(Arrays.stream(method.getParameters()).allMatch(Parameter::isNamePresent))
                    .as("Parameter names must be compiled in (-parameters) for this check to mean anything.").isTrue();

            if (names.contains("key") && !names.contains("distinctor")) {
                keyOnly.add(method.getName() + names);
            }
        }

        assertThat(keyOnly).as("These store methods take an idempotency key without its distinctor, so they cannot tell apart the "
                + "commands sharing that key.").isEmpty();
    }
}
