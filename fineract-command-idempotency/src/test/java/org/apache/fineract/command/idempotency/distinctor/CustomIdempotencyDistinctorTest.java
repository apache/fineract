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
package org.apache.fineract.command.idempotency.distinctor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.idempotency.starter.IdempotencyCommandAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Pins what each distinctor returns and which one {@code fineract.command.idempotency.custom-distinctor} selects.
 */
class CustomIdempotencyDistinctorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IdempotencyCommandAutoConfiguration.class))
            .withBean(CommandStore.class, () -> mock(CommandStore.class));

    @Test
    void noneDistinguishesNothing() {
        assertThat(new NoCustomIdempotencyDistinctor().distinctorFor(command("LOAN", "CREATE"))).isEmpty();
    }

    @Test
    void entityAndActionNameJoinsTheEntityAndTheActionWithABar() {
        assertThat(new EntityAndActionNameCustomIdempotencyDistinctor().distinctorFor(command("LOAN", "CREATE"))).isEqualTo("LOAN|CREATE");
    }

    @Test
    void entityNameAndActionNameIsSelectedWhenNothingIsConfigured() {
        contextRunner.run(context -> assertThat(context).getBean(CustomIdempotencyDistinctor.class)
                .isInstanceOf(EntityAndActionNameCustomIdempotencyDistinctor.class));
    }

    @Test
    void noneSelectsNone() {
        contextRunner.withPropertyValues("fineract.command.idempotency.custom-distinctor=NONE").run(context -> assertThat(context)
                .getBean(CustomIdempotencyDistinctor.class).isInstanceOf(NoCustomIdempotencyDistinctor.class));
    }

    @Test
    void entityNameAndActionNameSelectsEntityAndActionName() {
        contextRunner.withPropertyValues("fineract.command.idempotency.custom-distinctor=ENTITY_NAME_AND_ACTION_NAME")
                .run(context -> assertThat(context).getBean(CustomIdempotencyDistinctor.class)
                        .isInstanceOf(EntityAndActionNameCustomIdempotencyDistinctor.class));
    }

    @Test
    void anUnknownValueRefusesToStart() {
        contextRunner.withPropertyValues("fineract.command.idempotency.custom-distinctor=ENTITY_ONLY")
                .run(context -> assertThat(context).as("A typo must not silently fall back to another scope.").hasFailed());
    }

    @Test
    void aDistinctorTheApplicationDefinesReplacesTheConfiguredOne() {
        CustomIdempotencyDistinctor own = command -> "own";

        contextRunner.withBean(CustomIdempotencyDistinctor.class, () -> own)
                .run(context -> assertThat(context).getBean(CustomIdempotencyDistinctor.class).isSameAs(own));
    }

    private static Command<Object> command(String entity, String action) {
        return new Command<>() {

            @Override
            public String entityName() {
                return entity;
            }

            @Override
            public String actionName() {
                return action;
            }
        };
    }
}
