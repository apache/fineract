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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanAttributeValueData;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanAttributeSnapshotRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalAssetOwnerLoanAttributesReadServiceImplTest {

    private static final Long LOAN_ID = 5L;
    private static final Long LOAN_PRODUCT_ID = 9L;
    private static final Long SNAPSHOT_ID = 11L;
    private static final String ATTRIBUTE_KEY = "BUY_DOWN_FEE_AMORTIZATION_STRATEGY";

    @Mock
    private ExternalAssetOwnerLoanAttributeSnapshotRepository snapshotRepository;

    @Mock
    private ExternalAssetOwnerLoanProductAttributesReadService loanProductAttributesReadService;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private ExternalAssetOwnerLoanAttributesReadServiceImpl underTest;

    @Test
    void loanWithCopyReturnsTheCopiedValue() {
        when(snapshotRepository.findLoanLevelAttributeValue(LOAN_ID, ATTRIBUTE_KEY))
                .thenReturn(Optional.of(new ExternalAssetOwnerLoanAttributeValueData(SNAPSHOT_ID, "IMMEDIATE")));

        assertEquals(Optional.of("IMMEDIATE"), underTest.resolveAttributeValue(LOAN_ID, LOAN_PRODUCT_ID, ATTRIBUTE_KEY));
        verifyNoInteractions(loanProductAttributesReadService);
    }

    @Test
    void loanWithCopyWithoutTheKeyFallsBackToDefaultNotToTheProduct() {
        when(snapshotRepository.findLoanLevelAttributeValue(LOAN_ID, ATTRIBUTE_KEY))
                .thenReturn(Optional.of(new ExternalAssetOwnerLoanAttributeValueData(SNAPSHOT_ID, null)));

        assertEquals(Optional.empty(), underTest.resolveAttributeValue(LOAN_ID, LOAN_PRODUCT_ID, ATTRIBUTE_KEY));
        verifyNoInteractions(loanProductAttributesReadService);
    }

    @Test
    void loanWithoutCopyReadsTheProduct() {
        when(snapshotRepository.findLoanLevelAttributeValue(LOAN_ID, ATTRIBUTE_KEY)).thenReturn(Optional.empty());
        final ExternalTransferLoanProductAttributesData productAttribute = new ExternalTransferLoanProductAttributesData();
        productAttribute.setAttributeKey(ATTRIBUTE_KEY);
        productAttribute.setAttributeValue("DEFERRED");
        when(loanProductAttributesReadService.retrieveAllLoanProductAttributesByLoanProductId(LOAN_PRODUCT_ID, ATTRIBUTE_KEY))
                .thenReturn(new Page<>(List.of(productAttribute), 1));

        assertEquals(Optional.of("DEFERRED"), underTest.resolveAttributeValue(LOAN_ID, LOAN_PRODUCT_ID, ATTRIBUTE_KEY));
    }

    @Test
    void loanWithoutCopyAndWithoutProductIsDefault() {
        when(snapshotRepository.findLoanLevelAttributeValue(LOAN_ID, ATTRIBUTE_KEY)).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), underTest.resolveAttributeValue(LOAN_ID, null, ATTRIBUTE_KEY));
        verifyNoInteractions(loanProductAttributesReadService);
    }

    @Test
    void transientLoanWithoutIdReadsTheProductWithoutQueryingTheCopy() {
        final ExternalTransferLoanProductAttributesData productAttribute = new ExternalTransferLoanProductAttributesData();
        productAttribute.setAttributeKey(ATTRIBUTE_KEY);
        productAttribute.setAttributeValue("IMMEDIATE");
        when(loanProductAttributesReadService.retrieveAllLoanProductAttributesByLoanProductId(LOAN_PRODUCT_ID, ATTRIBUTE_KEY))
                .thenReturn(new Page<>(List.of(productAttribute), 1));

        assertEquals(Optional.of("IMMEDIATE"), underTest.resolveAttributeValue(null, LOAN_PRODUCT_ID, ATTRIBUTE_KEY));
        verifyNoInteractions(snapshotRepository);
    }
}
