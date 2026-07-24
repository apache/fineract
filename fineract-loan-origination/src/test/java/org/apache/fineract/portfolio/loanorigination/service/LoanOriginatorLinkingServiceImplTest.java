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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.loanorigination.data.LoanApplicationOriginatorData;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMapping;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMappingRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorStatus;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotActiveException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotFoundException;
import org.apache.fineract.portfolio.loanorigination.serialization.LoanApplicationOriginatorDataValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanOriginatorLinkingServiceImplTest {

    private static final Long LOAN_ID = 100L;

    @Mock
    private LoanOriginatorRepository loanOriginatorRepository;

    @Mock
    private LoanOriginatorMappingRepository loanOriginatorMappingRepository;

    @Mock
    private LoanApplicationOriginatorDataValidator validator;

    @Mock
    private LoanOriginatorHelper loanOriginatorHelper;

    private LoanOriginatorLinkingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LoanOriginatorLinkingServiceImpl(loanOriginatorRepository, validator, loanOriginatorHelper,
                loanOriginatorMappingRepository);
    }

    @Test
    void processOriginatorsForLoanDisbursementWithNullArrayDoesNothing() {
        service.processOriginatorsForLoanDisbursement(LOAN_ID, null);

        verify(loanOriginatorMappingRepository, never()).findByLoanId(LOAN_ID);
        verify(loanOriginatorMappingRepository, never()).deleteAll(any());
        verify(loanOriginatorMappingRepository, never()).saveAll(any());
    }

    @Test
    void processOriginatorsForLoanDisbursementWithEmptyArrayDetachesAllExistingMappings() {
        final LoanOriginatorMapping mapping1 = mapping(originator(1L, "originator-1"));
        final LoanOriginatorMapping mapping2 = mapping(originator(2L, "originator-2"));
        when(loanOriginatorMappingRepository.findByLoanId(LOAN_ID)).thenReturn(List.of(mapping1, mapping2));

        service.processOriginatorsForLoanDisbursement(LOAN_ID, new JsonArray());

        verify(loanOriginatorMappingRepository).deleteAll(List.of(mapping1, mapping2));
        verify(loanOriginatorMappingRepository, never()).saveAll(any());
    }

    @Test
    void processOriginatorsForLoanDisbursementReplacesStaleMapping() {
        final LoanOriginator oldOriginator = originator(1L, "old-originator");
        final LoanOriginator newOriginator = originator(2L, "new-originator");
        final LoanOriginatorMapping oldMapping = mapping(oldOriginator);
        final JsonArray originatorsArray = originatorsArray(2L);

        when(validator.validateAndExtract(any(JsonObject.class))).thenReturn(new LoanApplicationOriginatorData(2L, null, null, null, null));
        when(loanOriginatorRepository.findById(2L)).thenReturn(Optional.of(newOriginator));
        when(loanOriginatorMappingRepository.findByLoanId(LOAN_ID)).thenReturn(List.of(oldMapping));
        when(loanOriginatorRepository.getReferenceById(2L)).thenReturn(newOriginator);

        service.processOriginatorsForLoanDisbursement(LOAN_ID, originatorsArray);

        verify(loanOriginatorMappingRepository).deleteAll(List.of(oldMapping));
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<LoanOriginatorMapping>> mappingCaptor = ArgumentCaptor.forClass(List.class);
        verify(loanOriginatorMappingRepository).saveAll(mappingCaptor.capture());
        assertEquals(1, mappingCaptor.getValue().size());
        assertEquals(LOAN_ID, mappingCaptor.getValue().getFirst().getLoanId());
        assertSame(newOriginator, mappingCaptor.getValue().getFirst().getOriginator());
    }

    @Test
    void processOriginatorsForLoanDisbursementReconcilesOverlappingOriginatorSets() {
        final LoanOriginator originatorA = originator(1L, "originator-a");
        final LoanOriginator originatorB = originator(2L, "originator-b");
        final LoanOriginator originatorC = originator(3L, "originator-c");
        final LoanOriginatorMapping mappingA = mapping(originatorA);
        final LoanOriginatorMapping mappingB = mapping(originatorB);
        final JsonArray originatorsArray = originatorsArray(2L, 3L);

        when(validator.validateAndExtract(any(JsonObject.class))).thenReturn(new LoanApplicationOriginatorData(2L, null, null, null, null),
                new LoanApplicationOriginatorData(3L, null, null, null, null));
        when(loanOriginatorRepository.findById(2L)).thenReturn(Optional.of(originatorB));
        when(loanOriginatorRepository.findById(3L)).thenReturn(Optional.of(originatorC));
        when(loanOriginatorMappingRepository.findByLoanId(LOAN_ID)).thenReturn(List.of(mappingA, mappingB));
        when(loanOriginatorRepository.getReferenceById(3L)).thenReturn(originatorC);

        service.processOriginatorsForLoanDisbursement(LOAN_ID, originatorsArray);

        verify(loanOriginatorMappingRepository).deleteAll(List.of(mappingA));
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<LoanOriginatorMapping>> mappingCaptor = ArgumentCaptor.forClass(List.class);
        verify(loanOriginatorMappingRepository).saveAll(mappingCaptor.capture());
        assertEquals(1, mappingCaptor.getValue().size());
        assertEquals(LOAN_ID, mappingCaptor.getValue().getFirst().getLoanId());
        assertSame(originatorC, mappingCaptor.getValue().getFirst().getOriginator());
    }

    @Test
    void processOriginatorsForLoanDisbursementCreatesMissingExternalId() {
        final LoanOriginator newOriginator = originator(3L, "new-originator");
        final JsonArray originatorsArray = new JsonArray();
        originatorsArray.add(originatorObjectWithExternalId("new-originator"));
        final LoanApplicationOriginatorData originatorData = new LoanApplicationOriginatorData(null, "new-originator", null, null, null);

        when(validator.validateAndExtract(any(JsonObject.class))).thenReturn(originatorData);
        when(loanOriginatorHelper.findOrCreateOriginatorIdForLoanDisbursement(originatorData)).thenReturn(3L);
        when(loanOriginatorMappingRepository.findByLoanId(LOAN_ID)).thenReturn(List.of());
        when(loanOriginatorRepository.getReferenceById(3L)).thenReturn(newOriginator);

        service.processOriginatorsForLoanDisbursement(LOAN_ID, originatorsArray);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<LoanOriginatorMapping>> mappingCaptor = ArgumentCaptor.forClass(List.class);
        verify(loanOriginatorMappingRepository).saveAll(mappingCaptor.capture());
        assertEquals(1, mappingCaptor.getValue().size());
        assertEquals(LOAN_ID, mappingCaptor.getValue().getFirst().getLoanId());
        assertSame(newOriginator, mappingCaptor.getValue().getFirst().getOriginator());
    }

    @Test
    void processOriginatorsForLoanDisbursementFailsFastForMalformedArrayEntry() {
        final JsonArray originatorsArray = new JsonArray();
        originatorsArray.add(new JsonPrimitive("not-an-object"));

        assertThrows(PlatformApiDataValidationException.class,
                () -> service.processOriginatorsForLoanDisbursement(LOAN_ID, originatorsArray));

        verify(loanOriginatorMappingRepository, never()).findByLoanId(LOAN_ID);
        verify(loanOriginatorMappingRepository, never()).saveAll(any());
    }

    @Test
    void processOriginatorsForLoanDisbursementByIdWithInactiveOriginatorThrowsNotActive() {
        final JsonArray originatorsArray = originatorsArray(2L);

        when(validator.validateAndExtract(any(JsonObject.class))).thenReturn(new LoanApplicationOriginatorData(2L, null, null, null, null));
        when(loanOriginatorRepository.findById(2L)).thenReturn(Optional.of(inactiveOriginator(2L, "inactive")));

        assertThrows(LoanOriginatorNotActiveException.class,
                () -> service.processOriginatorsForLoanDisbursement(LOAN_ID, originatorsArray));

        verify(loanOriginatorMappingRepository, never()).findByLoanId(anyLong());
        verify(loanOriginatorMappingRepository, never()).deleteAll(any());
        verify(loanOriginatorMappingRepository, never()).saveAll(any());
    }

    @Test
    void processOriginatorsForLoanDisbursementByIdNotInTableThrowsNotFound() {
        final JsonArray originatorsArray = originatorsArray(999L);

        when(validator.validateAndExtract(any(JsonObject.class)))
                .thenReturn(new LoanApplicationOriginatorData(999L, null, null, null, null));
        when(loanOriginatorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LoanOriginatorNotFoundException.class, () -> service.processOriginatorsForLoanDisbursement(LOAN_ID, originatorsArray));

        verify(loanOriginatorMappingRepository, never()).findByLoanId(anyLong());
        verify(loanOriginatorMappingRepository, never()).deleteAll(any());
        verify(loanOriginatorMappingRepository, never()).saveAll(any());
    }

    @Test
    void processOriginatorsForLoanDisbursementByExternalIdInactivePropagatesNotActiveAndDoesNotReconcileMappings() {
        final JsonArray originatorsArray = new JsonArray();
        originatorsArray.add(originatorObjectWithExternalId("EXT-INACTIVE"));

        when(validator.validateAndExtract(any(JsonObject.class)))
                .thenReturn(new LoanApplicationOriginatorData(null, "EXT-INACTIVE", null, null, null));
        when(loanOriginatorHelper.findOrCreateOriginatorIdForLoanDisbursement(any()))
                .thenThrow(new LoanOriginatorNotActiveException(5L, "INACTIVE"));

        assertThrows(LoanOriginatorNotActiveException.class,
                () -> service.processOriginatorsForLoanDisbursement(LOAN_ID, originatorsArray));

        verify(loanOriginatorMappingRepository, never()).saveAll(any());
        verify(loanOriginatorMappingRepository, never()).deleteAll(any());
    }

    @Test
    void processOriginatorsForLoanDisbursementCollapsesDuplicateRequestEntriesToSingleMapping() {
        final LoanOriginator originatorTwo = originator(2L, "o2");
        final JsonArray originatorsArray = originatorsArray(2L, 2L);

        when(validator.validateAndExtract(any(JsonObject.class))).thenReturn(new LoanApplicationOriginatorData(2L, null, null, null, null),
                new LoanApplicationOriginatorData(2L, null, null, null, null));
        when(loanOriginatorRepository.findById(2L)).thenReturn(Optional.of(originatorTwo));
        when(loanOriginatorMappingRepository.findByLoanId(LOAN_ID)).thenReturn(List.of());
        when(loanOriginatorRepository.getReferenceById(2L)).thenReturn(originatorTwo);

        service.processOriginatorsForLoanDisbursement(LOAN_ID, originatorsArray);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<LoanOriginatorMapping>> mappingCaptor = ArgumentCaptor.forClass(List.class);
        verify(loanOriginatorMappingRepository).saveAll(mappingCaptor.capture());
        assertEquals(1, mappingCaptor.getValue().size());
        verify(loanOriginatorMappingRepository, never()).deleteAll(any());
    }

    @Test
    void processOriginatorsForLoanDisbursementWithValidAndInvalidEntryThrowsAndDoesNotReconcileMappings() {
        final JsonArray originatorsArray = originatorsArray(2L, 999L);

        when(validator.validateAndExtract(any(JsonObject.class))).thenReturn(new LoanApplicationOriginatorData(2L, null, null, null, null),
                new LoanApplicationOriginatorData(999L, null, null, null, null));
        when(loanOriginatorRepository.findById(2L)).thenReturn(Optional.of(originator(2L, "o2")));
        when(loanOriginatorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LoanOriginatorNotFoundException.class, () -> service.processOriginatorsForLoanDisbursement(LOAN_ID, originatorsArray));

        verify(loanOriginatorMappingRepository, never()).findByLoanId(anyLong());
        verify(loanOriginatorMappingRepository, never()).saveAll(any());
        verify(loanOriginatorMappingRepository, never()).deleteAll(any());
    }

    private static JsonArray originatorsArray(final Long... ids) {
        final JsonArray originators = new JsonArray();
        for (final Long id : ids) {
            originators.add(originatorObject(id));
        }
        return originators;
    }

    private static JsonObject originatorObject(final Long id) {
        final JsonObject originator = new JsonObject();
        originator.addProperty("id", id);
        return originator;
    }

    private static JsonObject originatorObjectWithExternalId(final String externalId) {
        final JsonObject originator = new JsonObject();
        originator.addProperty("externalId", externalId);
        return originator;
    }

    private static LoanOriginator originator(final Long id, final String externalId) {
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

    private static LoanOriginatorMapping mapping(final LoanOriginator originator) {
        return LoanOriginatorMapping.create(LOAN_ID, originator);
    }
}
