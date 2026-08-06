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
package org.apache.fineract.portfolio.loanorigination.service;

import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants.ENABLE_ORIGINATOR_CREATION_DURING_LOAN_APPLICATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanorigination.data.LoanApplicationOriginatorData;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorStatus;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorCreationNotAllowedException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotActiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link LoanOriginatorHelper} test class.
 */
@ExtendWith(MockitoExtension.class)
class LoanOriginatorHelperTest {

    @Mock
    private LoanOriginatorRepository loanOriginatorRepository;

    @Mock
    private GlobalConfigurationRepositoryWrapper globalConfigurationRepository;

    @Mock
    private CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    @Mock
    private GlobalConfigurationProperty globalConfigurationProperty;

    private LoanOriginatorHelper helper;

    @BeforeEach
    void setUp() {
        helper = new LoanOriginatorHelper(loanOriginatorRepository, globalConfigurationRepository, codeValueRepositoryWrapper);
    }

    @Test
    void findOrCreateOriginatorIdForLoanDisbursementReturnsIdWhenExternalIdMatchesActive() {
        final LoanApplicationOriginatorData data = new LoanApplicationOriginatorData(null, "EXT-A", "n", null, null);
        final LoanOriginator existing = activeOriginator(7L, "EXT-A");
        when(loanOriginatorRepository.findByExternalId(any(ExternalId.class))).thenReturn(Optional.of(existing));

        final Long id = helper.findOrCreateOriginatorIdForLoanDisbursement(data);

        assertEquals(7L, id);
        verify(loanOriginatorRepository, never()).saveAndFlush(any());
        verifyNoInteractions(globalConfigurationRepository);
    }

    @Test
    void findOrCreateOriginatorIdForLoanDisbursementThrowsNotActiveWhenExternalIdMatchesInactive() {
        final LoanApplicationOriginatorData data = new LoanApplicationOriginatorData(null, "EXT-B", "n", null, null);
        final LoanOriginator existing = inactiveOriginator(8L, "EXT-B");
        when(loanOriginatorRepository.findByExternalId(any(ExternalId.class))).thenReturn(Optional.of(existing));

        assertThrows(LoanOriginatorNotActiveException.class, () -> helper.findOrCreateOriginatorIdForLoanDisbursement(data));

        verify(loanOriginatorRepository, never()).saveAndFlush(any());
        verifyNoInteractions(globalConfigurationRepository);
    }

    @Test
    void findOrCreateOriginatorIdForLoanDisbursementCreatesNewWhenExternalIdMissing() {
        final LoanApplicationOriginatorData data = new LoanApplicationOriginatorData(null, "EXT-NEW", "New Co", null, null);
        when(loanOriginatorRepository.findByExternalId(any(ExternalId.class))).thenReturn(Optional.empty());
        when(loanOriginatorRepository.saveAndFlush(any(LoanOriginator.class))).thenAnswer(invocation -> {
            final LoanOriginator saved = invocation.getArgument(0);
            saved.setId(9L);
            return saved;
        });

        final Long id = helper.findOrCreateOriginatorIdForLoanDisbursement(data);

        assertEquals(9L, id);
        final ArgumentCaptor<LoanOriginator> originatorCaptor = ArgumentCaptor.forClass(LoanOriginator.class);
        verify(loanOriginatorRepository).saveAndFlush(originatorCaptor.capture());
        assertEquals(LoanOriginatorStatus.ACTIVE, originatorCaptor.getValue().getStatus());
        assertEquals("EXT-NEW", originatorCaptor.getValue().getExternalId().getValue());
        verifyNoInteractions(globalConfigurationRepository);
    }

    @Test
    void findOrCreateOriginatorIdThrowsCreationNotAllowedWhenFlagDisabledOnApplicationPath() {
        final LoanApplicationOriginatorData data = new LoanApplicationOriginatorData(null, "EXT-APP", "n", null, null);
        when(loanOriginatorRepository.findByExternalId(any(ExternalId.class))).thenReturn(Optional.empty());
        when(globalConfigurationProperty.isEnabled()).thenReturn(false);
        when(globalConfigurationRepository.findOneByNameWithNotFoundDetection(ENABLE_ORIGINATOR_CREATION_DURING_LOAN_APPLICATION))
                .thenReturn(globalConfigurationProperty);

        assertThrows(LoanOriginatorCreationNotAllowedException.class, () -> helper.findOrCreateOriginatorId(data));

        verify(loanOriginatorRepository, never()).saveAndFlush(any());
    }

    private static LoanOriginator activeOriginator(final Long id, final String externalId) {
        final LoanOriginator originator = LoanOriginator.create(new ExternalId(externalId), externalId, LoanOriginatorStatus.ACTIVE, null,
                null);
        originator.setId(id);
        return originator;
    }

    private static LoanOriginator inactiveOriginator(final Long id, final String externalId) {
        final LoanOriginator originator = LoanOriginator.create(new ExternalId(externalId), externalId, LoanOriginatorStatus.INACTIVE, null,
                null);
        originator.setId(id);
        return originator;
    }
}
