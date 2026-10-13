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

import static org.apache.fineract.investor.data.attribute.BuyDownFeeAmortizationStrategyExternalAssetOwnerLoanProductAttribute.DEFERRED;
import static org.apache.fineract.investor.data.attribute.BuyDownFeeAmortizationStrategyExternalAssetOwnerLoanProductAttribute.IMMEDIATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BuyDownFeeAmortizationStrategyServiceImplTest {

    private static final Long LOAN_ID = 11L;
    private static final Long LOAN_PRODUCT_ID = 22L;

    @Mock
    private ExternalAssetOwnerLoanProductAttributesReadService externalAssetOwnerLoanProductAttributesReadService;

    @Mock
    private ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;

    @Mock
    private Loan loan;

    @InjectMocks
    private BuyDownFeeAmortizationStrategyServiceImpl underTest;

    private static Stream<Arguments> soldLoanAttributeProvider() {
        return Stream.of(Arguments.of(IMMEDIATE.getAttributeValue(), true), Arguments.of("immediate", true),
                Arguments.of(DEFERRED.getAttributeValue(), false), Arguments.of(null, false));
    }

    @Test
    void shouldRecognizeImmediately_whenLoanNotSold_skipsAttributeLookup() {
        when(loan.getId()).thenReturn(LOAN_ID);
        when(externalAssetOwnerTransferRepository.findActiveByLoanId(LOAN_ID)).thenReturn(Optional.empty());

        assertFalse(underTest.shouldRecognizeImmediately(loan));

        verify(externalAssetOwnerTransferRepository).findActiveByLoanId(LOAN_ID);
        verifyNoInteractions(externalAssetOwnerLoanProductAttributesReadService);
    }

    @ParameterizedTest
    @MethodSource("soldLoanAttributeProvider")
    void shouldRecognizeImmediately_whenLoanSold_usesProductAttribute(final String attributeValue, final boolean expectedResult) {
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.productId()).thenReturn(LOAN_PRODUCT_ID);
        when(externalAssetOwnerTransferRepository.findActiveByLoanId(LOAN_ID)).thenReturn(Optional.of(new ExternalAssetOwnerTransfer()));
        when(externalAssetOwnerLoanProductAttributesReadService.getAttributeValue(LOAN_PRODUCT_ID, IMMEDIATE.getAttributeKey()))
                .thenReturn(Optional.ofNullable(attributeValue));

        assertEquals(expectedResult, underTest.shouldRecognizeImmediately(loan));
    }
}
