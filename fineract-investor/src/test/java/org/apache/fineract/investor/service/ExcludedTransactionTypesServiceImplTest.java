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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.investor.data.attribute.ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExcludedTransactionTypesServiceImplTest {

    private static final Long LOAN_PRODUCT_ID = 7L;

    @Mock
    private ExternalAssetOwnerLoanProductAttributesReadService externalAssetOwnerLoanProductAttributesReadService;

    @InjectMocks
    private ExcludedTransactionTypesServiceImpl underTest;

    @ParameterizedTest
    @MethodSource("shortCircuitedArguments")
    public void testUnresolvableInputIsNotExcludedWithoutReadingTheAttribute(String testName, Long loanProductId,
            LoanTransactionType transactionType) {
        assertFalse(underTest.isExcluded(loanProductId, transactionType), testName);
        verifyNoInteractions(externalAssetOwnerLoanProductAttributesReadService);
    }

    private static Stream<Arguments> shortCircuitedArguments() {
        return Stream.of(Arguments.of("null loan product id", null, LoanTransactionType.BUY_DOWN_FEE),
                Arguments.of("null transaction type", LOAN_PRODUCT_ID, null),
                Arguments.of("invalid transaction type", LOAN_PRODUCT_ID, LoanTransactionType.INVALID));
    }

    @ParameterizedTest
    @MethodSource("attributeValues")
    public void testTypeIsExcludedAccordingToTheConfiguredValue(String testName, String attributeValue, LoanTransactionType transactionType,
            boolean expected) {
        stubAttributeValue(attributeValue);

        assertTrue(expected == underTest.isExcluded(LOAN_PRODUCT_ID, transactionType), testName);
    }

    private static Stream<Arguments> attributeValues() {
        return Stream.of(Arguments.of("single matching value", "BUY_DOWN_FEE", LoanTransactionType.BUY_DOWN_FEE, true),
                Arguments.of("one of several matching values", "BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION",
                        LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION, true),
                Arguments.of("non matching value", "BUY_DOWN_FEE", LoanTransactionType.REPAYMENT, false),
                Arguments.of("value stored in lower case does not match, as validate() rejects it on the way in", "buy_down_fee",
                        LoanTransactionType.BUY_DOWN_FEE, false),
                Arguments.of("value stored with padding", " BUY_DOWN_FEE , REPAYMENT ", LoanTransactionType.REPAYMENT, true),
                Arguments.of("unknown token is ignored", "NOT_A_TYPE,BUY_DOWN_FEE", LoanTransactionType.BUY_DOWN_FEE, true),
                Arguments.of("only unknown tokens", "NOT_A_TYPE", LoanTransactionType.BUY_DOWN_FEE, false),
                Arguments.of("blank value", "", LoanTransactionType.BUY_DOWN_FEE, false),
                Arguments.of("null value", null, LoanTransactionType.BUY_DOWN_FEE, false));
    }

    @Test
    public void testMissingAttributeIsNotExcluded() {
        when(externalAssetOwnerLoanProductAttributesReadService.retrieveAllLoanProductAttributesByLoanProductId(eq(LOAN_PRODUCT_ID),
                any())).thenReturn(new Page<>(List.of(), 0));

        assertFalse(underTest.isExcluded(LOAN_PRODUCT_ID, LoanTransactionType.BUY_DOWN_FEE));
    }

    private void stubAttributeValue(final String attributeValue) {
        ExternalTransferLoanProductAttributesData data = new ExternalTransferLoanProductAttributesData();
        data.setAttributeKey(ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.ATTRIBUTE_KEY);
        data.setAttributeValue(attributeValue);
        when(externalAssetOwnerLoanProductAttributesReadService.retrieveAllLoanProductAttributesByLoanProductId(eq(LOAN_PRODUCT_ID),
                eq(ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.ATTRIBUTE_KEY))).thenReturn(new Page<>(List.of(data), 1));
    }
}
