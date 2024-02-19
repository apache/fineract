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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanChargebackOnPaymentTypeRepaymentTransactionsTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecErr503;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecErr503 = new ResponseSpecBuilder().expectStatusCode(503).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void loanTransactionChargebackForPaymentTypeRepaymentTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // Client and Loan account creation

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                delinquencyBucketId, loanProductTestBuilder);
        assertNotNull(getLoanProductsProductResponse);

        final Integer loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr,
                loanProductTestBuilder.getTransactionProcessingStrategyCode());

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(500.0));

        // verify transaction relation and outstanding balance
        reviewLoanTransactionRelations(loanId, repaymentTransaction_1.getResourceId(), 0, Double.valueOf("500.00"));

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(loanDetails.getSummary().getTotalOutstanding(), 500.0);

        // chargeback on Repayment
        PostLoansLoanIdTransactionsResponse chargebackTransactionResponse = loanTransactionHelper.chargebackLoanTransaction(
                loanExternalIdStr, repaymentTransaction_1.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(500.0).paymentTypeId(1L));

        // verify transaction relation and outstanding balance
        assertNotNull(chargebackTransactionResponse);
        reviewLoanTransactionRelations(loanId, repaymentTransaction_1.getResourceId(), 1, Double.valueOf("500.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionResponse.getResourceId(), 0, Double.valueOf("1000.00"));
        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(loanDetails.getSummary().getTotalOutstanding(), 1000.0);

        // Goodwill Credit
        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("6 September 2022").locale("en")
                        .transactionAmount(200.0));

        // verify transaction relation and outstanding balance
        reviewLoanTransactionRelations(loanId, goodwillCredit_1.getResourceId(), 0, Double.valueOf("300.00"));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(loanDetails.getSummary().getTotalOutstanding(), 800.0);

        // chargeback on Goodwill Credit Transaction
        chargebackTransactionResponse = loanTransactionHelper.chargebackLoanTransaction(loanExternalIdStr, goodwillCredit_1.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(200.0).paymentTypeId(1L));

        // verify transaction relation and outstanding balance
        assertNotNull(chargebackTransactionResponse);
        reviewLoanTransactionRelations(loanId, goodwillCredit_1.getResourceId(), 1, Double.valueOf("300.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionResponse.getResourceId(), 0, Double.valueOf("1000.00"));

        // Payout Refund

        final PostLoansLoanIdTransactionsResponse payoutRefund_1 = loanTransactionHelper.makePayoutRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(300.0));

        // verify transaction relation and outstanding balance
        reviewLoanTransactionRelations(loanId, payoutRefund_1.getResourceId(), 0, Double.valueOf("0.00"));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(loanDetails.getSummary().getTotalOutstanding(), 700.0);

        // chargeback on Payout Refund Transaction
        chargebackTransactionResponse = loanTransactionHelper.chargebackLoanTransaction(loanExternalIdStr, payoutRefund_1.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(300.0).paymentTypeId(1L));

        // verify transaction relation and outstanding balance
        assertNotNull(chargebackTransactionResponse);
        reviewLoanTransactionRelations(loanId, payoutRefund_1.getResourceId(), 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionResponse.getResourceId(), 0, Double.valueOf("1000.00"));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(loanDetails.getSummary().getTotalOutstanding(), 1000.0);

        // Merchant Issued Refund

        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_1 = loanTransactionHelper.makeMerchantIssuedRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                        .transactionAmount(100.0));

        // verify transaction relation and outstanding balance
        reviewLoanTransactionRelations(loanId, merchantIssuedRefund_1.getResourceId(), 0, Double.valueOf("0.00"));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(loanDetails.getSummary().getTotalOutstanding(), 900.0);

        // chargeback on Merchant Issued Refund Transaction
        chargebackTransactionResponse = loanTransactionHelper.chargebackLoanTransaction(loanExternalIdStr,
                merchantIssuedRefund_1.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(100.0));

        // verify transaction relation and outstanding balance
        assertNotNull(chargebackTransactionResponse);
        reviewLoanTransactionRelations(loanId, merchantIssuedRefund_1.getResourceId(), 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionResponse.getResourceId(), 0, Double.valueOf("1000.00"));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertNotNull(loanDetails);
        assertTrue(loanDetails.getStatus().getActive());
        assertNotNull(loanDetails.getSummary());
        assertEquals(loanDetails.getSummary().getTotalOutstanding(), 1000.0);

    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void loanChargebackNotAllowedForReversedPaymentTypeRepaymentTest(LoanProductTestBuilder loanProductTestBuilder) {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // Client and Loan account creation

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                delinquencyBucketId, loanProductTestBuilder);
        assertNotNull(getLoanProductsProductResponse);

        final Integer loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr,
                loanProductTestBuilder.getTransactionProcessingStrategyCode());

        // Merchant Refund
        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_2 = loanTransactionHelper.makeMerchantIssuedRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                        .transactionAmount(50.0));

        // reverse Merchant Refund
        loanTransactionHelper.reverseRepayment(loanId, merchantIssuedRefund_2.getResourceId().intValue(), "8 September 2022");

        // apply Chargeback should give 503 error
        final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId,
                merchantIssuedRefund_2.getResourceId(), "50.00", 1, responseSpecErr503);

    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId, LoanProductTestBuilder loanProductTestBuilder) {
        final HashMap<String, Object> loanProductMap = loanProductTestBuilder.build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final Integer clientID, final Long loanProductID, final String externalId,
            final String repaymentStrategy) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).withRepaymentStrategy(repaymentStrategy)
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

    private void reviewLoanTransactionRelations(final Integer loanId, final Long transactionId, final Integer expectedSize,
            final Double outstandingBalance) {

        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper.getLoanTransaction(loanId,
                transactionId.intValue());
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());
        assertEquals(expectedSize, getLoansTransactionResponse.getTransactionRelations().size());
        // Outstanding amount
        assertEquals(outstandingBalance, getLoansTransactionResponse.getOutstandingLoanBalance());
    }

    private static AdvancedPaymentData createRepaymentPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("REPAYMENT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_INTEREST, PaymentAllocationType.PAST_DUE_PRINCIPAL,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_INTEREST,
                PaymentAllocationType.DUE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static AdvancedPaymentData createDefaultPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }

    private static Stream<Arguments> loanProductFactory() {
        return Stream.of(Arguments.of(Named.of("DEFAULT_STRATEGY", new LoanProductTestBuilder().withRepaymentStrategy(DEFAULT_STRATEGY))),
                Arguments.of(Named.of("ADVANCED_PAYMENT_ALLOCATION_STRATEGY",
                        new LoanProductTestBuilder().withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                                .addAdvancedPaymentAllocation(createDefaultPaymentAllocation(), createRepaymentPaymentAllocation()))));
    }

}
