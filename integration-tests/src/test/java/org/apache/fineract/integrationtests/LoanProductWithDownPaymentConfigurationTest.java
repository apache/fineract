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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
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
    private AccountHelper accountHelper;
    private JournalEntryHelper journalEntryHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
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
        Boolean enableAutoRepaymentForDownPayment = false;
        // Loan Product creation with down-payment configuration
        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());
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
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().withEnableDownPayment(enableDownPayment, "0", false)
                .build(null, delinquencyBucketId);

        ArrayList<HashMap<String, Object>> loanProductErrorData = validationErrorHelper
                .getLoanProductError(Utils.convertToJson(loanProductMap), CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.is.less.than.min",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // Loan Product with enable down payment and with disbursed amount percentage as greater than 100
        final HashMap<String, Object> loanProductMap_1 = new LoanProductTestBuilder().withEnableDownPayment(enableDownPayment, "101", false)
                .build(null, delinquencyBucketId);

        loanProductErrorData = validationErrorHelper.getLoanProductError(Utils.convertToJson(loanProductMap_1),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.is.greater.than.max",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // Loan Product with enable down payment and with disbursed amount percentage precision greater than 6
        final HashMap<String, Object> loanProductMap_2 = new LoanProductTestBuilder()
                .withEnableDownPayment(enableDownPayment, "12.55555555", false).build(null, delinquencyBucketId);

        loanProductErrorData = validationErrorHelper.getLoanProductError(Utils.convertToJson(loanProductMap_2),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.scale.is.greater.than.6",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // Loan Product with disable down payment and with disbursed amount percentage
        final HashMap<String, Object> loanProductMap_3 = new LoanProductTestBuilder().withEnableDownPayment(false, "12.5", false)
                .build(null, delinquencyBucketId);

        loanProductErrorData = validationErrorHelper.getLoanProductError(Utils.convertToJson(loanProductMap_3),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.supported.only.for.enable.down.payment.true",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // Loan Product with enable down payment and without disbursed amount percentage
        final HashMap<String, Object> loanProductMap_4 = new LoanProductTestBuilder().withEnableDownPayment(enableDownPayment, null, false)
                .build(null, delinquencyBucketId);

        loanProductErrorData = validationErrorHelper.getLoanProductError(Utils.convertToJson(loanProductMap_4),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.required.for.enable.down.payment.true",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // Loan Product with disable down payment and enable auto repayment for down payment
        final HashMap<String, Object> loanProductMap_5 = new LoanProductTestBuilder().withEnableDownPayment(false, null, true).build(null,
                delinquencyBucketId);

        loanProductErrorData = validationErrorHelper.getLoanProductError(Utils.convertToJson(loanProductMap_5),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.enableAutoRepaymentForDownPayment.supported.only.for.enable.down.payment.true",
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
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        // Loan Product creation with down-payment configuration
        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        // Retrieve Loan with loanId

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

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
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        // Loan Product creation with down-payment configuration
        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "12.5", enableAutoRepaymentForDownPayment);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        // Retrieve Loan with loanId

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        // Modify Loan Product to update enable down payment configuration
        PutLoanProductsProductIdResponse loanProductModifyResponse = updateLoanProduct(loanTransactionHelper,
                getLoanProductsProductResponse.getId());
        assertNotNull(loanProductModifyResponse);

        // verify Loan product configuration change
        GetLoanProductsProductIdResponse getLoanProductsProductResponse_1 = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse_1);
        assertEquals(enableDownPayment, getLoanProductsProductResponse_1.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse_1.getDisbursedAmountPercentageForDownPayment().compareTo(BigDecimal.valueOf(25.0)));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse_1.getEnableAutoRepaymentForDownPayment());

        // make repayment for loan
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan does not change
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

    }

    @Test
    public void loanApplicationWithLoanProductWithEnableDownPaymentAndEnableAutoRepaymentForDownPaymentTest() {
        try {

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            // Accounts oof periodic accrual
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Delinquency Bucket
            final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
            final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                    delinquencyBucketId);

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithDownPaymentConfigurationAndAccrualAccounting(
                    loanTransactionHelper, delinquencyBucketId, enableDownPayment, "25", enableAutoRepaymentForDownPayment, assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 March 2023", loanId, "1000");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            // verify down-payment transaction created
            checkDownPaymentTransaction(disbursementDate, 250.0f, 0.0f, 0.0f, 0.0f, loanId);

            // verify journal entries for down-payment
            this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "03 March 2023",
                    new JournalEntry(250, JournalEntry.TransactionType.CREDIT));
            this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "03 March 2023",
                    new JournalEntry(250, JournalEntry.TransactionType.DEBIT));

            // verify installment details
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(0).getDueDate());
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getPeriods().get(0).getPrincipalLoanBalanceOutstanding());
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getDownPaymentPeriod());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 2), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(750.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(2).getDownPaymentPeriod());

            // second disbursement

            disbursementDate = LocalDate.of(2023, 3, 5);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);
            loanTransactionHelper.disburseLoanWithTransactionAmount("05 March 2023", loanId, "200");
            checkDownPaymentTransaction(disbursementDate, 50.0f, 0.0f, 0.0f, 0.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            // verify installment details
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(0).getDueDate());
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getPeriods().get(0).getPrincipalLoanBalanceOutstanding());
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getDownPaymentPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(200.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPrincipalLoanBalanceOutstanding());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(2).getDownPaymentPeriod());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(50.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(3).getDownPaymentPeriod());
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 2), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(900.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getDownPaymentPeriod());

            // verify journal entries for down-payment
            this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "05 March 2023",
                    new JournalEntry(50, JournalEntry.TransactionType.CREDIT));
            this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "05 March 2023",
                    new JournalEntry(50, JournalEntry.TransactionType.DEBIT));

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }

    }

    @Test
    public void loanApplicationWithLoanProductWithEnableDownPaymentAndDisableAutoRepaymentForDownPaymentVerifyNoDownPaymentCreatedTest() {
        try {

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            // Accounts oof periodic accrual
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Delinquency Bucket
            final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
            final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                    delinquencyBucketId);

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithDownPaymentConfigurationAndAccrualAccounting(
                    loanTransactionHelper, delinquencyBucketId, enableDownPayment, "25", enableAutoRepaymentForDownPayment, assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 March 2023", loanId, "1000");

            // verify no down-payment transaction created
            checkNoDownPaymentTransaction(loanId);

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }

    }

    @Test
    public void loanProductAndLoanAccountCreationWithEnableDownPaymentAndDisableRepaymentScheduleExtensionConfigurationTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // down-payment configuration
        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;
        Boolean disableScheduleExtensionForDownPayment = true;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        // Loan Product creation with down-payment configuration
        GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursementsWithDisableRepaymentConfiguration(
                loanTransactionHelper, enableDownPayment, "25", enableAutoRepaymentForDownPayment, disableScheduleExtensionForDownPayment);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());
        assertEquals(disableScheduleExtensionForDownPayment, getLoanProductsProductResponse.getDisableScheduleExtensionForDownPayment());

        final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                loanExternalIdStr);

        // Retrieve Loan with loanId

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());
        assertEquals(disableScheduleExtensionForDownPayment, loanDetails.getDisableScheduleExtensionForDownPayment());
    }

    @Test
    public void loanProductCreationWithEnableDownPaymentAndDisableRepaymentScheduleExtensionConfigurationValidationTest() {
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        final LoanTransactionHelper validationErrorHelper = new LoanTransactionHelper(this.requestSpec, errorResponse);

        // down-payment configuration
        Boolean enableDownPayment = true;
        Boolean enableAutoRepaymentForDownPayment = false;
        Boolean disableScheduleExtensionForDownPayment = true;

        // Loan Product with no multi disbursement settings and enable down payment and with disable Schedule Extension
        // For DownPayment
        String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth().withRepaymentAfterEvery("1")
                .withNumberOfRepayments("3").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withEnableDownPayment(enableDownPayment, "25", enableAutoRepaymentForDownPayment)
                .withDisableScheduleExtensionForDownPayment(disableScheduleExtensionForDownPayment).build(null);

        ArrayList<HashMap<String, Object>> loanProductErrorData = validationErrorHelper.getLoanProductError(loanProductJSON,
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals(
                "validation.msg.loanproduct.disableScheduleExtensionForDownPayment.supported.only.for.multi.disburse.loan.with.enable.down.payment.true",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // Loan Product with multi disbursement settings and disable down payment and with disable Schedule Extension
        // For DownPayment
        enableDownPayment = false;
        loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth().withRepaymentAfterEvery("1")
                .withNumberOfRepayments("3").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, null, enableAutoRepaymentForDownPayment)
                .withDisableScheduleExtensionForDownPayment(disableScheduleExtensionForDownPayment).build(null);

        loanProductErrorData = validationErrorHelper.getLoanProductError(loanProductJSON, CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals(
                "validation.msg.loanproduct.disableScheduleExtensionForDownPayment.supported.only.for.multi.disburse.loan.with.enable.down.payment.true",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    private void checkNoDownPaymentTransaction(final Integer loanID) {
        ArrayList<HashMap> transactions = (ArrayList<HashMap>) loanTransactionHelper.getLoanTransactions(this.requestSpec,
                this.responseSpec, loanID);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isDownPaymentTransaction = (Boolean) transactionType.get("downPayment");

            if (isDownPaymentTransaction) {
                isTransactionFound = true;
                break;
            }
        }
        assertFalse(isTransactionFound, "Down Payment entries are posted");
    }

    private void checkDownPaymentTransaction(final LocalDate transactionDate, final Float principalPortion, final Float interestPortion,
            final Float feePortion, final Float penaltyPortion, final Integer loanID) {
        ArrayList<HashMap> transactions = (ArrayList<HashMap>) loanTransactionHelper.getLoanTransactions(this.requestSpec,
                this.responseSpec, loanID);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isDownPaymentTransaction = (Boolean) transactionType.get("downPayment");

            if (isDownPaymentTransaction) {
                ArrayList<Integer> downPaymentDateAsArray = (ArrayList<Integer>) transactions.get(i).get("date");
                LocalDate downPaymentEntryDate = LocalDate.of(downPaymentDateAsArray.get(0), downPaymentDateAsArray.get(1),
                        downPaymentDateAsArray.get(2));

                if (DateUtils.isEqual(transactionDate, downPaymentEntryDate)) {
                    isTransactionFound = true;
                    assertEquals(principalPortion, Float.valueOf(String.valueOf(transactions.get(i).get("principalPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(interestPortion, Float.valueOf(String.valueOf(transactions.get(i).get("interestPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(feePortion, Float.valueOf(String.valueOf(transactions.get(i).get("feeChargesPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(penaltyPortion, Float.valueOf(String.valueOf(transactions.get(i).get("penaltyChargesPortion"))),
                            "Mismatch in transaction amounts");
                    break;
                }
            }
        }
        assertTrue(isTransactionFound, "No Down Payment entries are posted");
    }

    private Integer createLoanAccountMultipleRepaymentsDisbursement(final Integer clientID, final Long loanProductID,
            final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("1").withRepaymentEveryAfter("30").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 March 2023")
                .withSubmittedOnDate("03 March 2023").withLoanType("individual").withExternalId(externalId)
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("03 March 2023", "1000", loanId, null);
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductWithDownPaymentConfigurationAndAccrualAccounting(
            LoanTransactionHelper loanTransactionHelper, Integer delinquencyBucketId, Boolean enableDownPayment,
            String disbursedAmountPercentageForDownPayment, boolean enableAutoRepaymentForDownPayment, final Account... accounts) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(accounts).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30")
                .withDaysInYear("365").withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
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
            final Integer delinquencyBucketId, Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment,
            Boolean enableAutoRepaymentForDownPayment) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder()
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanProductId;
    }

    private Integer createApproveAndDisburseLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

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

    private GetLoanProductsProductIdResponse createLoanProductWithEnableDownPaymentAndMultipleDisbursementsWithDisableRepaymentConfiguration(
            LoanTransactionHelper loanTransactionHelper, Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment,
            boolean enableAutoRepaymentForDownPayment, boolean disableScheduleExtensionForDownPayment) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("3").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .withDisableScheduleExtensionForDownPayment(disableScheduleExtensionForDownPayment).build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

}
