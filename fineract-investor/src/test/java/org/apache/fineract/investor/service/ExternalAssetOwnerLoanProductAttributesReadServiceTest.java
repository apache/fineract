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
package org.apache.fineract.investor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributes;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributesRepository;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class ExternalAssetOwnerLoanProductAttributesReadServiceTest {

    @Mock
    private ExternalAssetOwnerLoanProductAttributesRepository externalAssetOwnerLoanProductAttributesRepository;

    @Mock
    private LoanProductRepository loanProductRepository;

    @Mock
    private ExternalAssetOwnerLoanProductAttributesMapper mapper;

    private ExternalAssetOwnerLoanProductAttributesReadService underTest;

    private int offset = 0;

    private int limit = 100;

    @BeforeEach
    public void setUp() {
        underTest = new ExternalAssetOwnerLoanProductAttributesReadServiceImpl(externalAssetOwnerLoanProductAttributesRepository,
                loanProductRepository, mapper);
    }

    @ParameterizedTest
    @MethodSource("testRetrieveAllLoanProductAttributesByLoanProductIdDataProvider")
    public void testRetrieveAllLoanProductAttributesByLoanProductId(Long loanProductId, String attributeKey) {
        // given
        ExternalAssetOwnerLoanProductAttributes attributes = Mockito.mock(ExternalAssetOwnerLoanProductAttributes.class);
        ExternalTransferLoanProductAttributesData data = Mockito.mock(ExternalTransferLoanProductAttributesData.class);
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by("id").ascending());
        org.springframework.data.domain.Page<ExternalAssetOwnerLoanProductAttributes> attributesPage = new PageImpl<>(List.of(attributes),
                pageRequest, 1);

        when(externalAssetOwnerLoanProductAttributesRepository.findAll(any(Specification.class), eq(pageRequest)))
                .thenReturn(attributesPage);
        when(loanProductRepository.existsById(loanProductId)).thenReturn(true);
        when(mapper.mapLoanProductAttributes(attributes)).thenReturn(data);

        // when
        Page<ExternalTransferLoanProductAttributesData> result = underTest.retrieveAllLoanProductAttributesByLoanProductId(loanProductId,
                attributeKey);

        // then
        assertEquals(1, result.getTotalFilteredRecords());
        assertEquals(data, result.getPageItems().get(0));
        verify(loanProductRepository, times(1)).existsById(loanProductId);
        verify(mapper, times(1)).mapLoanProductAttributes(attributes);
    }

    @Test
    public void testRetrieveAllLoanProductAttributesByLoanProductIdAndInvalidAttributeKey() {
        // given
        Long loanProductId = 1L;
        ExternalAssetOwnerLoanProductAttributes attributes = Mockito.mock(ExternalAssetOwnerLoanProductAttributes.class);
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by("id").ascending());
        org.springframework.data.domain.Page<ExternalAssetOwnerLoanProductAttributes> attributesPage = new PageImpl<>(List.of(attributes),
                pageRequest, 0);

        when(externalAssetOwnerLoanProductAttributesRepository.findAll(any(Specification.class), eq(pageRequest)))
                .thenReturn(attributesPage);
        when(loanProductRepository.existsById(loanProductId)).thenReturn(true);
        when(mapper.mapLoanProductAttributes(attributes)).thenReturn(null);

        // when
        Page<ExternalTransferLoanProductAttributesData> result = underTest.retrieveAllLoanProductAttributesByLoanProductId(loanProductId,
                "BAD_KEY");

        // then
        assertEquals(1, result.getTotalFilteredRecords());
        assertNull(result.getPageItems().get(0));
        verify(externalAssetOwnerLoanProductAttributesRepository, times(1)).findAll(any(Specification.class), eq(pageRequest));
        verify(loanProductRepository, times(1)).existsById(loanProductId);
        verify(mapper, times(1)).mapLoanProductAttributes(attributes);
    }

    @Test
    public void testRetrieveLoanProductAttributesDataByLoanProductIdIllegalArgument() {
        // given

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> underTest.retrieveAllLoanProductAttributesByLoanProductId(null, null));

        // then
        assertEquals("At least one of the following parameters must be provided: loanProductId", exception.getMessage());
        verifyNoInteractions(externalAssetOwnerLoanProductAttributesRepository);
        verifyNoInteractions(loanProductRepository);
        verifyNoInteractions(mapper);
    }

    @Test
    public void testRetrieveLoanProductAttributesDataByLoanProductIdNotFound() {
        // given
        Long loanProductId = 1L;
        when(loanProductRepository.existsById(loanProductId)).thenReturn(false);

        // when
        LoanProductNotFoundException exception = assertThrows(LoanProductNotFoundException.class,
                () -> underTest.retrieveAllLoanProductAttributesByLoanProductId(loanProductId, null));

        // then
        assertEquals("Loan product with identifier 1 does not exist", exception.getMessage());
        verify(loanProductRepository, times(1)).existsById(loanProductId);
        verifyNoInteractions(externalAssetOwnerLoanProductAttributesRepository);
        verifyNoInteractions(mapper);
    }

    private static Stream<Arguments> testRetrieveAllLoanProductAttributesByLoanProductIdDataProvider() {
        return Stream.of(Arguments.of(1L, "SETTLEMENT_MODEL"), Arguments.of(1L, null));
    }
}
