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
package org.apache.fineract.infrastructure.survey.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.survey.data.LikelihoodDataValidator;
import org.apache.fineract.infrastructure.survey.data.LikelihoodStatus;
import org.apache.fineract.infrastructure.survey.domain.Likelihood;
import org.apache.fineract.infrastructure.survey.domain.LikelihoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WriteLikelihoodServiceImplTest {

    private static final Long LIKELIHOOD_ID = 3L;
    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private LikelihoodDataValidator likelihoodDataValidator;
    @Mock
    private LikelihoodRepository repository;

    @InjectMocks
    private WriteLikelihoodServiceImpl service;

    @Test
    void enablingALikelihoodDisablesTheOthersOfTheSamePpi() {
        final Likelihood likelihood = existing(LikelihoodStatus.DISABLED);
        final Likelihood other = new Likelihood().setPpiName("ppi").setEnabled(LikelihoodStatus.ENABLED);
        when(repository.findByPpiNameAndLikeliHoodId("ppi", LIKELIHOOD_ID)).thenReturn(List.of(other));

        final CommandProcessingResult result = service.update(LIKELIHOOD_ID, command("{\"active\":true}"));

        assertEquals(LikelihoodStatus.ENABLED, likelihood.getEnabled());
        assertEquals(LikelihoodStatus.DISABLED, other.getEnabled());
        verify(repository).save(likelihood);
        verify(repository).saveAll(List.of(other));
        assertEquals(LIKELIHOOD_ID, result.getResourceId());
        // the change set is not part of the result
        assertTrue(result.getChanges() == null || result.getChanges().isEmpty());
    }

    @Test
    void disablingALikelihoodLeavesTheOthersAlone() {
        final Likelihood likelihood = existing(LikelihoodStatus.ENABLED);

        service.update(LIKELIHOOD_ID, command("{\"active\":false}"));

        assertEquals(LikelihoodStatus.DISABLED, likelihood.getEnabled());
        verify(repository).save(likelihood);
        verify(repository, never()).findByPpiNameAndLikeliHoodId(any(), any());
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void aMissingActiveFlagDisablesTheLikelihood() {
        final Likelihood likelihood = existing(LikelihoodStatus.ENABLED);

        service.update(LIKELIHOOD_ID, command("{}"));

        assertEquals(LikelihoodStatus.DISABLED, likelihood.getEnabled());
        verify(repository).save(likelihood);
    }

    @Test
    void enablingAnEnabledLikelihoodSavesNothing() {
        final Likelihood likelihood = existing(LikelihoodStatus.ENABLED);

        service.update(LIKELIHOOD_ID, command("{\"active\":true}"));

        assertEquals(LikelihoodStatus.ENABLED, likelihood.getEnabled());
        verify(repository, never()).save(any());
        verify(repository, never()).findByPpiNameAndLikeliHoodId(any(), any());
    }

    /**
     * Documents a known defect, not intended behaviour: an unknown id should be rejected with a 404, but the service uses
     * {@code findById(...).orElse(null)}, so the request fails with a {@link NullPointerException} and a 500. This test
     * must change when that is fixed.
     */
    @Test
    void anUnknownLikelihoodFailsWithANullPointerException() {
        when(repository.findById(LIKELIHOOD_ID)).thenReturn(Optional.empty());

        assertThrows(NullPointerException.class, () -> service.update(LIKELIHOOD_ID, command("{\"active\":true}")));
    }

    private Likelihood existing(final long enabled) {
        final Likelihood likelihood = new Likelihood().setPpiName("ppi").setLikelihoodId(1L).setEnabled(enabled);
        ReflectionTestUtils.setField(likelihood, "id", LIKELIHOOD_ID);
        when(repository.findById(LIKELIHOOD_ID)).thenReturn(Optional.of(likelihood));
        return likelihood;
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }
}
