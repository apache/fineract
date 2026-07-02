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
package org.apache.fineract.organisation.provisioning.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCriteria;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCriteriaDefinition;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCriteriaRepository;
import org.apache.fineract.organisation.provisioning.serialization.ProvisioningCriteriaDefinitionJsonDeserializer;
import org.junit.jupiter.api.Test;

/**
 * Regression coverage for the update path that FINERACT-2657 fixes:
 * {@link ProvisioningCriteriaWritePlatformServiceJpaRepositoryImpl#updateProvisioningCriteria(Long, JsonCommand)}.
 *
 * <p>
 * The public UPDATE payload keys each definition only by {@code categoryId}; it never carries the surrogate {@code id}.
 * The service used to match definitions by that always-null surrogate id, so every update threw a
 * {@link NullPointerException} (HTTP 500). It now resolves the definition to mutate by a direct {@code categoryId}
 * lookup and rejects an unknown category with a clean {@link PlatformApiDataValidationException} (HTTP 400).
 */
class ProvisioningCriteriaWritePlatformServiceJpaRepositoryImplTest {

    private static final long CRITERIA_ID = 1L;

    private final ProvisioningCriteriaDefinitionJsonDeserializer deserializer = mock(ProvisioningCriteriaDefinitionJsonDeserializer.class);
    private final ProvisioningCriteriaAssembler assembler = mock(ProvisioningCriteriaAssembler.class);
    private final ProvisioningCriteriaRepository criteriaRepository = mock(ProvisioningCriteriaRepository.class);
    private final GLAccountRepository glAccountRepository = mock(GLAccountRepository.class);
    private final ProvisioningEntriesReadPlatformService entriesReadService = mock(ProvisioningEntriesReadPlatformService.class);

    private final ProvisioningCriteriaWritePlatformServiceJpaRepositoryImpl service = new ProvisioningCriteriaWritePlatformServiceJpaRepositoryImpl(
            deserializer, assembler, criteriaRepository, new FromJsonHelper(), glAccountRepository, entriesReadService);

    @Test
    void update_appliesChangesToDefinitionMatchedByCategoryId() {
        final ProvisioningCriteriaDefinition definition = mock(ProvisioningCriteriaDefinition.class);
        stubCriteriaWith(definition);

        service.updateProvisioningCriteria(CRITERIA_ID, command("""
                { "locale": "en", "criteriaName": "Standard", "loanProducts": [],
                  "definitions": [ { "categoryId": 7, "minAge": 0, "maxAge": 30, "provisioningPercentage": 1.5,
                                     "liabilityAccount": 5, "expenseAccount": 12 } ] }
                """));

        // The definition keyed by categoryId 7 is mutated with the new ages — never an NPE.
        verify(definition, times(1)).update(eq(0L), eq(30L), any(), any(), any());
    }

    @Test
    void update_withUnknownCategoryId_throwsValidationExceptionNotNpe() {
        final ProvisioningCriteriaDefinition definition = mock(ProvisioningCriteriaDefinition.class);
        stubCriteriaWith(definition);

        // categoryId 99 has no definition on the criteria, and the payload carries no surrogate id (the real shape).
        final JsonCommand command = command("""
                { "locale": "en", "criteriaName": "Standard", "loanProducts": [],
                  "definitions": [ { "categoryId": 99, "minAge": 0, "maxAge": 30, "provisioningPercentage": 1.5,
                                     "liabilityAccount": 5, "expenseAccount": 12 } ] }
                """);

        assertThrows(PlatformApiDataValidationException.class, () -> service.updateProvisioningCriteria(CRITERIA_ID, command));
        verify(definition, never()).update(any(), any(), any(), any(), any());
    }

    private void stubCriteriaWith(final ProvisioningCriteriaDefinition definition) {
        final ProvisioningCriteria criteria = mock(ProvisioningCriteria.class);
        // Non-empty changes so the service proceeds to apply the definition edits (the changes gate).
        when(criteria.update(any(), any())).thenReturn(Map.of("loanProducts", "changed"));
        // The criteria exposes its definitions indexed by categoryId; the service looks up directly into this.
        when(criteria.getDefinitionsByCategoryId()).thenReturn(Map.of(7L, definition));
        when(criteriaRepository.findById(CRITERIA_ID)).thenReturn(Optional.of(criteria));
        when(assembler.parseLoanProducts(any())).thenReturn(List.of());
        when(glAccountRepository.findById(anyLong())).thenReturn(Optional.of(mock(GLAccount.class)));
    }

    private JsonCommand command(final String json) {
        final FromJsonHelper fromJsonHelper = new FromJsonHelper();
        return new JsonCommand(1L, fromJsonHelper.parse(json), fromJsonHelper);
    }
}
