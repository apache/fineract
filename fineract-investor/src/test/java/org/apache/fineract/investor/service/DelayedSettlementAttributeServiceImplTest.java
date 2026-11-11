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

import static org.apache.fineract.investor.data.attribute.SettlementModelExternalAssetOwnerLoanProductAttribute.DEFAULT_SETTLEMENT;
import static org.apache.fineract.investor.data.attribute.SettlementModelExternalAssetOwnerLoanProductAttribute.DELAYED_SETTLEMENT;
import static org.apache.fineract.investor.service.LoanAccountOwnerTransferServiceImpl.FUTURE_DATE_9999_12_31;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DelayedSettlementAttributeServiceImplTest {

    private static final Long LOAN_ID = 2L;
    private static final Long LOAN_PRODUCT_ID = 1L;

    private static Stream<Arguments> attributesDataProvider() {
        ExternalTransferLoanProductAttributesData enabledAttributesData = new ExternalTransferLoanProductAttributesData();
        enabledAttributesData.setAttributeValue(DELAYED_SETTLEMENT.getAttributeValue());

        ExternalTransferLoanProductAttributesData disabledAttributesData = new ExternalTransferLoanProductAttributesData();
        disabledAttributesData.setAttributeValue(DEFAULT_SETTLEMENT.getAttributeValue());

        ExternalTransferLoanProductAttributesData lowerCaseEnabledAttributesData = new ExternalTransferLoanProductAttributesData();
        lowerCaseEnabledAttributesData.setAttributeValue("delayed_settlement");

        return Stream.of(Arguments.of(enabledAttributesData.getAttributeValue(), true),
                Arguments.of(lowerCaseEnabledAttributesData.getAttributeValue(), true),
                Arguments.of(disabledAttributesData.getAttributeValue(), false), Arguments.of(null, false));
    }

    @ParameterizedTest
    @MethodSource("attributesDataProvider")
    void isEnabled(final String attributeValue, final boolean expectedResult) {
        // given
        TestContext testContext = new TestContext();

        when(testContext.externalAssetOwnerLoanAttributesReadService.resolveAttributeValue(LOAN_ID, LOAN_PRODUCT_ID,
                DELAYED_SETTLEMENT.getAttributeKey())).thenReturn(Optional.ofNullable(attributeValue));

        // when
        boolean result = testContext.testSubject.isEnabled(LOAN_ID, LOAN_PRODUCT_ID);

        // then
        assertEquals(expectedResult, result);
    }

    @Test
    void intermediarySaleIsUnderDelayedSettlement() {
        TestContext testContext = new TestContext();

        assertTrue(testContext.testSubject.isSaleUnderDelayedSettlement(saleTransfer(ExternalTransferStatus.PENDING_INTERMEDIATE)));
        verifyNoInteractions(testContext.externalAssetOwnerTransferRepository, testContext.externalAssetOwnerLoanAttributesReadService);
    }

    @Test
    void saleFollowingAnActiveIntermediarySaleIsUnderDelayedSettlement() {
        TestContext testContext = new TestContext();
        when(testContext.externalAssetOwnerTransferRepository.findAllByLoanIdAndStatusInAndEffectiveDateTo(LOAN_ID,
                List.of(ExternalTransferStatus.ACTIVE_INTERMEDIATE), FUTURE_DATE_9999_12_31))
                .thenReturn(List.of(new ExternalAssetOwnerTransfer()));

        assertTrue(testContext.testSubject.isSaleUnderDelayedSettlement(saleTransfer(ExternalTransferStatus.PENDING)));
        verifyNoInteractions(testContext.externalAssetOwnerLoanAttributesReadService);
    }

    @Test
    void saleWithoutAnActiveIntermediarySaleIsUnderDefaultSettlement() {
        TestContext testContext = new TestContext();
        when(testContext.externalAssetOwnerTransferRepository.findAllByLoanIdAndStatusInAndEffectiveDateTo(LOAN_ID,
                List.of(ExternalTransferStatus.ACTIVE_INTERMEDIATE), FUTURE_DATE_9999_12_31)).thenReturn(List.of());

        assertFalse(testContext.testSubject.isSaleUnderDelayedSettlement(saleTransfer(ExternalTransferStatus.PENDING)));
        verifyNoInteractions(testContext.externalAssetOwnerLoanAttributesReadService);
    }

    private static ExternalAssetOwnerTransfer saleTransfer(final ExternalTransferStatus status) {
        ExternalAssetOwnerTransfer transfer = new ExternalAssetOwnerTransfer();
        transfer.setLoanId(LOAN_ID);
        transfer.setStatus(status);
        return transfer;
    }

    private static class TestContext {

        @Mock
        private ExternalAssetOwnerLoanAttributesReadService externalAssetOwnerLoanAttributesReadService;

        @Mock
        private ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;

        @InjectMocks
        private DelayedSettlementAttributeServiceImpl testSubject;

        TestContext() {
            MockitoAnnotations.openMocks(this);
        }
    }
}
