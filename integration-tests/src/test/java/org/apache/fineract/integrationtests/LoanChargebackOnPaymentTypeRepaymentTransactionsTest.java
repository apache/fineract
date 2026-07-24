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
package org.apache.fineract.integrationtests;

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.stream.Stream;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LoanChargebackOnPaymentTypeRepaymentTransactionsTest extends FeignLoanTestBase {

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void loanTransactionChargebackForPaymentTypeRepaymentTransactionTest(String strategyCode, boolean advancedAllocation) {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanProductId = createLoanProduct(strategyCode, advancedAllocation, delinquencyBucketId);

        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr, strategyCode);

        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("5 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(500.0));

        reviewLoanTransactionRelations(loanId, repaymentTransaction_1.getResourceId(), 0, Double.valueOf("500.00"));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(500.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));

        PostLoansLoanIdTransactionsResponse chargebackTransactionResponse = chargebackLoanTransaction(loanExternalIdStr,
                repaymentTransaction_1.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest().locale(LoanTestData.LOCALE)
                        .transactionAmount(500.0).paymentTypeId(1L));

        assertNotNull(chargebackTransactionResponse);
        reviewLoanTransactionRelations(loanId, repaymentTransaction_1.getResourceId(), 1, Double.valueOf("500.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionResponse.getResourceId(), 0, Double.valueOf("1000.00"));
        loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));

        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = makeGoodwillCredit(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("6 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(200.0));

        reviewLoanTransactionRelations(loanId, goodwillCredit_1.getResourceId(), 0, Double.valueOf("300.00"));

        loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(800.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));

        chargebackTransactionResponse = chargebackLoanTransaction(loanExternalIdStr, goodwillCredit_1.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale(LoanTestData.LOCALE).transactionAmount(200.0)
                        .paymentTypeId(1L));

        assertNotNull(chargebackTransactionResponse);
        reviewLoanTransactionRelations(loanId, goodwillCredit_1.getResourceId(), 1, Double.valueOf("300.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionResponse.getResourceId(), 0, Double.valueOf("1000.00"));

        final PostLoansLoanIdTransactionsResponse payoutRefund_1 = makePayoutRefund(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("7 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(300.0));

        reviewLoanTransactionRelations(loanId, payoutRefund_1.getResourceId(), 0, Double.valueOf("0.00"));

        loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(700.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));

        chargebackTransactionResponse = chargebackLoanTransaction(loanExternalIdStr, payoutRefund_1.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale(LoanTestData.LOCALE).transactionAmount(300.0)
                        .paymentTypeId(1L));

        assertNotNull(chargebackTransactionResponse);
        reviewLoanTransactionRelations(loanId, payoutRefund_1.getResourceId(), 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionResponse.getResourceId(), 0, Double.valueOf("1000.00"));

        loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));

        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_1 = makeMerchantIssuedRefund(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("8 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(100.0));

        reviewLoanTransactionRelations(loanId, merchantIssuedRefund_1.getResourceId(), 0, Double.valueOf("0.00"));

        loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(900.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));

        chargebackTransactionResponse = chargebackLoanTransaction(loanExternalIdStr, merchantIssuedRefund_1.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale(LoanTestData.LOCALE).transactionAmount(100.0));

        assertNotNull(chargebackTransactionResponse);
        reviewLoanTransactionRelations(loanId, merchantIssuedRefund_1.getResourceId(), 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionResponse.getResourceId(), 0, Double.valueOf("1000.00"));

        loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void loanChargebackNotAllowedForReversedPaymentTypeRepaymentTest(String strategyCode, boolean advancedAllocation) {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanProductId = createLoanProduct(strategyCode, advancedAllocation, delinquencyBucketId);

        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr, strategyCode);

        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_2 = makeMerchantIssuedRefund(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("8 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(50.0));

        reverseRepayment(loanId, merchantIssuedRefund_2.getResourceId(), "8 September 2022");

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> applyChargebackTransaction(loanId, merchantIssuedRefund_2.getResourceId(), 50.0, getPaymentTypeId(1)));
        assertEquals(503, exception.getStatus());
    }

    private Long createLoanProduct(String strategyCode, boolean advancedAllocation, Long delinquencyBucketId) {
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().principal(1000.0)
                .numberOfRepayments(1).repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).interestType(LoanTestData.InterestType.FLAT)
                .daysInMonthType(LoanTestData.DaysInMonthType.DAYS_30).daysInYearType(LoanTestData.DaysInYearType.DAYS_365)
                .transactionProcessingStrategyCode(strategyCode).delinquencyBucketId(delinquencyBucketId);
        if (advancedAllocation) {
            product.loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()).loanScheduleProcessingType("HORIZONTAL")
                    .addPaymentAllocationItem(LoanRequestBuilders.defaultPaymentAllocation())
                    .addPaymentAllocationItem(LoanRequestBuilders.paymentAllocation("REPAYMENT", "NEXT_INSTALLMENT", "PAST_DUE_PENALTY",
                            "PAST_DUE_FEE", "PAST_DUE_INTEREST", "PAST_DUE_PRINCIPAL", "DUE_PENALTY", "DUE_FEE", "DUE_INTEREST",
                            "DUE_PRINCIPAL", "IN_ADVANCE_PENALTY", "IN_ADVANCE_FEE", "IN_ADVANCE_PRINCIPAL", "IN_ADVANCE_INTEREST"));
        }
        return createLoanProduct(product);
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId, final String repaymentStrategy) {
        PostLoansRequest request = applyLoanRequest(clientId, loanProductId, "01 September 2022", 1000.0, 1,
                req -> req.externalId(externalId).expectedDisbursementDate("03 September 2022").repaymentEvery(1)
                        .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS).loanTermFrequency(1)
                        .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).interestType(LoanTestData.InterestType.FLAT)
                        .transactionProcessingStrategyCode(repaymentStrategy));

        Long loanId = applyForLoan(request);
        approveLoan(loanId, approveLoanRequest(1000.0, "02 September 2022", "03 September 2022"));
        disburseLoan(loanId, "03 September 2022", 1000.0);
        return loanId;
    }

    private static Stream<Arguments> loanProductFactory() {
        return Stream.of(Arguments.of(Named.of("DEFAULT_STRATEGY", DEFAULT_STRATEGY), false),
                Arguments.of(Named.of("ADVANCED_PAYMENT_ALLOCATION_STRATEGY", ADVANCED_PAYMENT_ALLOCATION_STRATEGY), true));
    }

}
