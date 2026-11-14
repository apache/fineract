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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanTransferabilityServiceImplTest {

    private static Stream<Arguments> amountDataProvider() {
        return Stream.of(Arguments.of(BigDecimal.ONE, true), Arguments.of(BigDecimal.ZERO, false),
                Arguments.of(BigDecimal.ONE.negate(), false));
    }

    @ParameterizedTest
    @MethodSource("amountDataProvider")
    void saleUnderDefaultSettlementIsValidated(final BigDecimal loanOutstandingAmount, final boolean expectedResult) {
        // given
        TestContext testContext = new TestContext();
        Loan loan = loanWithOutstanding(loanOutstandingAmount);
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = transfer(ExternalTransferStatus.PENDING);
        when(testContext.delayedSettlementAttributeService.isSaleUnderDelayedSettlement(externalAssetOwnerTransfer)).thenReturn(false);

        // when
        boolean result = testContext.testSubject.isTransferable(loan, externalAssetOwnerTransfer);

        // then
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource("amountDataProvider")
    void saleToIntermediaryIsValidated(final BigDecimal loanOutstandingAmount, final boolean expectedResult) {
        // given
        TestContext testContext = new TestContext();
        Loan loan = loanWithOutstanding(loanOutstandingAmount);
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = transfer(ExternalTransferStatus.PENDING_INTERMEDIATE);

        // when
        boolean result = testContext.testSubject.isTransferable(loan, externalAssetOwnerTransfer);

        // then
        assertEquals(expectedResult, result);
        verifyNoInteractions(testContext.delayedSettlementAttributeService);
    }

    @Test
    void saleFromIntermediaryToInvestorIsNotValidated() {
        // given
        TestContext testContext = new TestContext();
        Loan loan = Mockito.mock(Loan.class);
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = transfer(ExternalTransferStatus.PENDING);
        when(testContext.delayedSettlementAttributeService.isSaleUnderDelayedSettlement(externalAssetOwnerTransfer)).thenReturn(true);

        // when
        boolean result = testContext.testSubject.isTransferable(loan, externalAssetOwnerTransfer);

        // then
        assertTrue(result);
        verifyNoInteractions(loan);
    }

    private static Loan loanWithOutstanding(final BigDecimal loanOutstandingAmount) {
        LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
        when(loanSummary.getTotalOutstanding()).thenReturn(loanOutstandingAmount);
        Loan loan = Mockito.mock(Loan.class);
        when(loan.getSummary()).thenReturn(loanSummary);
        return loan;
    }

    private static ExternalAssetOwnerTransfer transfer(final ExternalTransferStatus status) {
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();
        externalAssetOwnerTransfer.setStatus(status);
        return externalAssetOwnerTransfer;
    }

    private static Stream<Arguments> declinedSubStatusDataProvider() {
        return Stream.of(Arguments.of(BigDecimal.ONE, ExternalTransferSubStatus.BALANCE_NEGATIVE),
                Arguments.of(BigDecimal.ZERO, ExternalTransferSubStatus.BALANCE_ZERO),
                Arguments.of(null, ExternalTransferSubStatus.BALANCE_ZERO));
    }

    @ParameterizedTest
    @MethodSource("declinedSubStatusDataProvider")
    void getDeclinedSubStatus(final BigDecimal totalOverpaidAmount, final ExternalTransferSubStatus expectedSubStatus) {
        // given
        TestContext testContext = new TestContext();

        Loan loan = Mockito.mock(Loan.class);
        when(loan.getTotalOverpaid()).thenReturn(totalOverpaidAmount);

        // when
        ExternalTransferSubStatus result = testContext.testSubject.getDeclinedSubStatus(loan);

        // then
        assertEquals(expectedSubStatus, result);
    }

    private static class TestContext {

        @Mock
        private DelayedSettlementAttributeService delayedSettlementAttributeService;

        @InjectMocks
        private LoanTransferabilityServiceImpl testSubject;

        TestContext() {
            MockitoAnnotations.openMocks(this);
        }
    }
}
