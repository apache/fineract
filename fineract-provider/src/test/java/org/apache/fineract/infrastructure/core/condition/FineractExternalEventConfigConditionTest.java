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
package org.apache.fineract.infrastructure.core.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class FineractExternalEventConfigConditionTest {

    @Mock
    private ConditionContext conditionContext;

    @Mock
    private AnnotatedTypeMetadata metadata;

    private MockEnvironment environment;

    @InjectMocks
    private FineractExternalEventConfigCondition underTest = new FineractExternalEventConfigCondition();

    @BeforeEach
    public void setUp() {
        environment = new MockEnvironment();
        given(conditionContext.getEnvironment()).willReturn(environment);
    }

    @Test
    public void testMatchesShouldReturnFalseWhenPartitionSizeIsNotBiggerThanLimit() {
        // given
        environment.withProperty("fineract.events.external.partition-size", "25000");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testMatchesShouldReturnTrueWhenPartitionSizeIsBiggerThanLimit() {
        // given
        environment.withProperty("fineract.events.external.partition-size", "25001");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testMatchesShouldReturnTrueWhenPartitionSizeIsNotPositive() {
        // given
        environment.withProperty("fineract.events.external.partition-size", "0");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isTrue();
    }
}
