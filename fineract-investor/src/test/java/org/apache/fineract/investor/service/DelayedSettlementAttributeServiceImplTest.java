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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DelayedSettlementAttributeServiceImplTest {

    private static final Long LOAN_PRODUCT_ID = 1L;

    private static Stream<Arguments> attributesDataProvider() {
        ExternalTransferLoanProductAttributesData enabledAttributesData = new ExternalTransferLoanProductAttributesData();
        enabledAttributesData.setAttributeValue(DELAYED_SETTLEMENT.getAttributeValue());

        ExternalTransferLoanProductAttributesData disabledAttributesData = new ExternalTransferLoanProductAttributesData();
        disabledAttributesData.setAttributeValue(DEFAULT_SETTLEMENT.getAttributeValue());

        return Stream.of(Arguments.of(new Page(List.of(enabledAttributesData), 1), true),
                Arguments.of(new Page(List.of(disabledAttributesData), 1), false), Arguments.of(new Page(List.of(), 0), false));
    }

    @ParameterizedTest
    @MethodSource("attributesDataProvider")
    void isEnabled(final Page<ExternalTransferLoanProductAttributesData> attributesDataPage, final boolean expectedResult) {
        // given
        TestContext testContext = new TestContext();

        when(testContext.externalAssetOwnerLoanProductAttributesReadService.retrieveAllLoanProductAttributesByLoanProductId(LOAN_PRODUCT_ID,
                DELAYED_SETTLEMENT.getAttributeKey())).thenReturn(attributesDataPage);

        // when
        boolean result = testContext.testSubject.isEnabled(LOAN_PRODUCT_ID);

        // then
        assertEquals(expectedResult, result);
    }

    private static class TestContext {

        @Mock
        private ExternalAssetOwnerLoanProductAttributesReadService externalAssetOwnerLoanProductAttributesReadService;

        @InjectMocks
        private DelayedSettlementAttributeServiceImpl testSubject;

        TestContext() {
            MockitoAnnotations.openMocks(this);
        }
    }
}
