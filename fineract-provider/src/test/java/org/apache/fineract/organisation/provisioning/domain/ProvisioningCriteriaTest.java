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
package org.apache.fineract.organisation.provisioning.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Regression coverage for {@link ProvisioningCriteria#getDefinitionsByCategoryId()}.
 *
 * <p>
 * The public UPDATE payload keys each definition only by {@code categoryId}; it never carries the surrogate {@code id}.
 * The update path used to match definitions by {@code data.getId().equals(def.getId())}, so {@code data.getId()} was
 * null and every update threw a {@link NullPointerException} (HTTP 500). The criteria now exposes its definitions
 * indexed by {@code categoryId}, so the service resolves the definition to mutate with a single direct lookup (and
 * turns a missing key into a clean validation error) instead of rescanning the set per incoming definition.
 */
class ProvisioningCriteriaTest {

    @Test
    void definitionsAreIndexedByCategoryIdForDirectLookup() {
        final ProvisioningCriteriaDefinition first = mock(ProvisioningCriteriaDefinition.class);
        when(first.getCategoryId()).thenReturn(2L);
        final ProvisioningCriteriaDefinition second = mock(ProvisioningCriteriaDefinition.class);
        when(second.getCategoryId()).thenReturn(5L);

        final ProvisioningCriteria criteria = new ProvisioningCriteria();
        criteria.setProvisioningCriteriaDefinitions(Set.of(first, second));

        final Map<Long, ProvisioningCriteriaDefinition> byCategoryId = criteria.getDefinitionsByCategoryId();

        assertThat(byCategoryId).containsOnlyKeys(2L, 5L);
        assertThat(byCategoryId.get(2L)).isSameAs(first);
        assertThat(byCategoryId.get(5L)).isSameAs(second);
        // An unknown categoryId yields no match — the service translates this into a 400 validation error rather than
        // dereferencing a null surrogate id.
        assertThat(byCategoryId.get(99L)).isNull();
    }

    @Test
    void getDefinitionsByCategoryId_withNoDefinitions_returnsEmptyMap() {
        final ProvisioningCriteria criteria = new ProvisioningCriteria();
        criteria.setProvisioningCriteriaDefinitions(Set.of());

        assertThat(criteria.getDefinitionsByCategoryId()).isEmpty();
    }
}
