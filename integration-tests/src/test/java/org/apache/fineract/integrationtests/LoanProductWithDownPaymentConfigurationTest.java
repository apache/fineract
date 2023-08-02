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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanProductWithDownPaymentConfigurationTest {

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
    public void loanProductCreationWithDownPaymentConfigurationTest() {
        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // down-payment configuration
        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);

        // Loan Product creation with down-payment configuration
        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25");

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
    }

    @Test
    public void loanProductUpdateWithEnableDownPaymentConfigurationTest() {
        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);
        // Loan Product without enable down payment configuration
        GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, delinquencyBucketId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(false, getLoanProductsProductResponse.getEnableDownPayment());

        // Modify Loan Product to update enable down payment configuration
        PutLoanProductsProductIdResponse loanProductModifyResponse = updateLoanProduct(loanTransactionHelper,
                getLoanProductsProductResponse.getId());
        assertNotNull(loanProductModifyResponse);

        getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductModifyResponse.getResourceId().intValue());
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(true, getLoanProductsProductResponse.getEnableDownPayment());

    }

    @Test
    public void loanProductEnableDownPaymentConfigurationValidationTests() {
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        final LoanTransactionHelper validationErrorHelper = new LoanTransactionHelper(this.requestSpec, errorResponse);

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // down-payment configuration
        Boolean enableDownPayment = true;

        // Loan Product with enable down payment and with disbursed amount percentage as zero
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().withEnableDownPayment(enableDownPayment, "0")
                .build(null, delinquencyBucketId);

        ArrayList<HashMap> loanProductErrorData = (ArrayList<HashMap>) validationErrorHelper
                .getLoanProductError(Utils.convertToJson(loanProductMap), CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.is.less.than.min",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // Loan Product with enable down payment and with disbursed amount percentage as greater than 100
        final HashMap<String, Object> loanProductMap_1 = new LoanProductTestBuilder().withEnableDownPayment(enableDownPayment, "101")
                .build(null, delinquencyBucketId);

        loanProductErrorData = (ArrayList<HashMap>) validationErrorHelper.getLoanProductError(Utils.convertToJson(loanProductMap_1),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.is.greater.than.max",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // Loan Product with enable down payment and with disbursed amount percentage precision greater than 6
        final HashMap<String, Object> loanProductMap_2 = new LoanProductTestBuilder()
                .withEnableDownPayment(enableDownPayment, "12.55555555").build(null, delinquencyBucketId);

        loanProductErrorData = (ArrayList<HashMap>) validationErrorHelper.getLoanProductError(Utils.convertToJson(loanProductMap_2),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.scale.is.greater.than.6",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // Loan Product with disable down payment and with disbursed amount percentage
        final HashMap<String, Object> loanProductMap_3 = new LoanProductTestBuilder().withEnableDownPayment(false, "12.5").build(null,
                delinquencyBucketId);

        loanProductErrorData = (ArrayList<HashMap>) validationErrorHelper.getLoanProductError(Utils.convertToJson(loanProductMap_3),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.supported.only.for.enable.down.payment.true",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // Loan Product with enable down payment and without disbursed amount percentage
        final HashMap<String, Object> loanProductMap_4 = new LoanProductTestBuilder().withEnableDownPayment(enableDownPayment, null)
                .build(null, delinquencyBucketId);

        loanProductErrorData = (ArrayList<HashMap>) validationErrorHelper.getLoanProductError(Utils.convertToJson(loanProductMap_4),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.required.for.enable.down.payment.true",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    @Test
    public void loanApplicationCreationWithLoanProductWithEnableDownPaymentConfiguration() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // down-payment configuration
        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        // Loan Product creation with down-payment configuration
        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25");

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));

        final Integer loanId = createLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        // Retrieve Loan with loanId

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));

    }

    @Test
    public void loanApplicationWithLoanProductWithEnableDownPaymentConfigurationDoesNotChangeWithUpdateProductConfiguration() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // down-payment configuration
        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(12.5);

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        // Loan Product creation with down-payment configuration
        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "12.5");

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));

        final Integer loanId = createLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        // Retrieve Loan with loanId

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));

        // Modify Loan Product to update enable down payment configuration
        PutLoanProductsProductIdResponse loanProductModifyResponse = updateLoanProduct(loanTransactionHelper,
                getLoanProductsProductResponse.getId());
        assertNotNull(loanProductModifyResponse);

        // verify Loan product configuration change
        GetLoanProductsProductIdResponse getLoanProductsProductResponse_1 = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse_1);
        assertEquals(enableDownPayment, getLoanProductsProductResponse_1.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse_1.getDisbursedAmountPercentageForDownPayment().compareTo(BigDecimal.valueOf(25.0)));

        // make repayment for loan
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan does not change
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));

    }

    private PutLoanProductsProductIdResponse updateLoanProduct(LoanTransactionHelper loanTransactionHelper, Long id) {
        // down-payment configuration
        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25.0);
        final PutLoanProductsProductIdRequest requestModifyLoan = new PutLoanProductsProductIdRequest().enableDownPayment(enableDownPayment)
                .disbursedAmountPercentageForDownPayment(disbursedAmountPercentageForDownPayment).locale("en");
        return loanTransactionHelper.updateLoanProduct(id, requestModifyLoan);
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanProductWithDownPaymentConfiguration(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId, Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder()
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment).build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanProductId;
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
