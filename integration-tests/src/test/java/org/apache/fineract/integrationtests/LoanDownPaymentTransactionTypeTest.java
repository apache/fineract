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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanDownPaymentTransactionTypeTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ClientHelper clientHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanDownPaymentTransactionTypeTest() {

        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // client creation
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        // Loan Product
        GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, delinquencyBucketId);
        assertNotNull(getLoanProductsProductResponse);

        final Integer loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);

        // make down payment for loan
        final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = loanTransactionHelper.makeLoanDownPayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(100.0));
        assertNotNull(downPaymentTransaction_1);

        // verify details for down payment transaction

        GetLoansLoanIdTransactionsTransactionIdResponse loanDownPaymentTransaction = loanTransactionHelper.getLoanTransaction(loanId,
                downPaymentTransaction_1.getResourceId().intValue());

        assertNotNull(loanDownPaymentTransaction);
        assertEquals(loanDownPaymentTransaction.getAmount(), 100.0);
        assertEquals(loanDownPaymentTransaction.getPrincipalPortion(), 100.0);
        assertEquals("loanTransactionType.downPayment", loanDownPaymentTransaction.getType().getCode());

        // undo/adjust down payment
        LocalDate adjustmentDate = LocalDate.of(2022, 9, 7);
        String formattedDate = dateFormatter.format(adjustmentDate);
        PostLoansLoanIdTransactionsResponse adjustmentResult = loanTransactionHelper.reverseLoanTransaction(loanExternalIdStr,
                loanDownPaymentTransaction.getId(), new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate)
                        .locale("en").dateFormat("dd MMMM yyyy").transactionAmount(0.0));

        assertNotNull(adjustmentResult);
        assertEquals(loanDownPaymentTransaction.getId(), adjustmentResult.getResourceId());

        // Down Payment Transaction with External Id Test

        // make downpayment with transaction external Id
        String downPaymentExternalIdStr = UUID.randomUUID().toString();

        final PostLoansLoanIdTransactionsResponse downPaymentTransaction_2 = loanTransactionHelper.makeLoanDownPayment(loanId.longValue(),
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(200.0).externalId(downPaymentExternalIdStr));
        assertNotNull(downPaymentTransaction_2);
        assertEquals(downPaymentExternalIdStr, downPaymentTransaction_2.getResourceExternalId());

        // verify details for down payment transaction

        GetLoansLoanIdTransactionsTransactionIdResponse loanDownPaymentTransaction_1 = loanTransactionHelper.getLoanTransaction(loanId,
                downPaymentTransaction_2.getResourceId().intValue());

        assertNotNull(loanDownPaymentTransaction_1);
        assertEquals(loanDownPaymentTransaction_1.getAmount(), 200.0);
        assertEquals(loanDownPaymentTransaction_1.getPrincipalPortion(), 200.0);
        assertEquals("loanTransactionType.downPayment", loanDownPaymentTransaction_1.getType().getCode());

        // undo/adjust down payment
        adjustmentDate = LocalDate.of(2022, 9, 12);
        formattedDate = dateFormatter.format(adjustmentDate);
        PostLoansLoanIdTransactionsResponse adjustmentResult_1 = loanTransactionHelper.reverseLoanTransaction(loanExternalIdStr,
                downPaymentExternalIdStr, new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate).locale("en")
                        .dateFormat("dd MMMM yyyy").transactionAmount(0.0));

        assertNotNull(adjustmentResult_1);
        assertEquals(loanDownPaymentTransaction_1.getId(), adjustmentResult_1.getResourceId());
        assertEquals(downPaymentExternalIdStr, adjustmentResult_1.getResourceExternalId());
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

}
