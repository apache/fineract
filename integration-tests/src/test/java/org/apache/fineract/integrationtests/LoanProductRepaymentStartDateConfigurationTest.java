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
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanProductRepaymentStartDateConfigurationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ClientHelper clientHelper;
    private GlobalConfigurationHelper globalConfigurationHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @Test
    public void loanProductWithRepaymentStartDateTypeConfigurationCreateAndModifyTest() {
        // create product with repayment start date configuration, get , modify

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        final Integer repaymentStartDateType = 2;

        // create loan product with repayment start date configuration
        Integer loanProductId = createLoanProductWithRepaymentStartDateTypeConfiguration(loanTransactionHelper, delinquencyBucketId,
                repaymentStartDateType);

        GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(repaymentStartDateType, getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
        assertEquals("repaymentStartDateType.submittedOnDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());

        // modify loan product repayment start date configuration to disbursement date

        PutLoanProductsProductIdResponse loanProductModifyResponse = updateLoanProduct(loanTransactionHelper,
                getLoanProductsProductResponse.getId());
        assertNotNull(loanProductModifyResponse);

        getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(1, getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
        assertEquals("repaymentStartDateType.disbursementDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());

    }

    @Test
    public void loanProductWithNoRepaymentStartDateTypeConfigurationDefaultsToDisbursementDateTest() {
        // create loan product with no configuration for repayment start date and verify that it is disbursement date by
        // default
        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        final Integer repaymentStartDateType = null;

        // create loan product with repayment start date configuration
        Integer loanProductId = createLoanProductWithRepaymentStartDateTypeConfiguration(loanTransactionHelper, delinquencyBucketId,
                repaymentStartDateType);

        GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(1, getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
        assertEquals("repaymentStartDateType.disbursementDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());
    }

    @Test
    public void loanAccountWithLoanProductRepaymentStartDateTypeAsSubmittedOnDateScheduleTest() {
        // create loan account with product with repayment start date type configuration as submitted on date, verify
        // repayment schedule is according to submitted on date, before and after disbursements
        try {

            // Set business date
            LocalDate businessDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // set repayment start date type as submittedOn date
            final Integer repaymentStartDateType = 2;

            // Loan Product creation with repayment start date type configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
                    loanTransactionHelper, repaymentStartDateType);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(repaymentStartDateType, getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
            assertEquals("repaymentStartDateType.submittedOnDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());

            // create loan account with submitted date as business date (03 March 2023) and expected disbursement date
            // as future date (07 March 2023)
            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails);

            // verify loan schedule is according to submitted on date

            assertNotNull(loanDetails.getRepaymentSchedule());
            // loan term
            assertEquals(92, loanDetails.getRepaymentSchedule().getLoanTermInDays());

            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getTotalPrincipalExpected());

            // first period [2023-03-03 to 2023-04-03]
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(333.33, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());

            // second period [2023-04-03 to 2023-05-03]
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(333.33, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod());

            // third period [2023-05-03 to 2023-06-03]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(333.34, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());

            // first disbursement on a future date (7 March 2023)

            LocalDate disbursementDate = LocalDate.of(2023, 3, 7);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            loanTransactionHelper.disburseLoanWithTransactionAmount("07 March 2023", loanId, "500");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify loan schedule is according to submitted on date after first disbursement
            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());
            // loan term
            assertEquals(92, loanDetails.getRepaymentSchedule().getLoanTermInDays());
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(500.0, loanDetails.getRepaymentSchedule().getTotalPrincipalExpected());
            assertEquals(500.0, loanDetails.getRepaymentSchedule().getTotalPrincipalDisbursed());

            // first period [2023-03-03 to 2023-04-03]
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(166.67, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());

            // second period [2023-04-03 to 2023-05-03]
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(166.67, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod());

            // third period [2023-05-03 to 2023-06-03]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(166.66, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());

            // second disbursement next month (7 April 2023)

            disbursementDate = LocalDate.of(2023, 4, 7);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            loanTransactionHelper.disburseLoanWithTransactionAmount("07 April 2023", loanId, "500");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify loan schedule is according to submitted on date after second disbursement

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());
            // loan term
            assertEquals(92, loanDetails.getRepaymentSchedule().getLoanTermInDays());
            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getTotalPrincipalExpected());
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getTotalPrincipalDisbursed());

            // first period [2023-03-03 to 2023-04-03]
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(166.67, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());

            // second period [2023-04-03 to 2023-05-03]
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(333.33, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());

            // third period [2023-05-03 to 2023-06-03]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(500.00, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod());

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    @Test
    public void loanAccountWithLoanProductRepaymentStartDateTypeAsDisbursementDateScheduleTest() {
        // create loan account with loan product with repayment start date type configuration as disbursement date ,
        // verify repayment schedule is as per disbursement date before and after disbursements

        try {

            // Set business date
            LocalDate businessDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // set repayment start date type as default, disbursement date
            final Integer repaymentStartDateType = 1;

            // Loan Product creation with repayment date type configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
                    loanTransactionHelper, repaymentStartDateType);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(repaymentStartDateType, getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
            assertEquals("repaymentStartDateType.disbursementDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());

            // create loan account with submitted date as business date (03 March 2023) and expected disbursement date
            // (07 March 2023)
            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails);

            // verify loan schedule is according to disbursement date

            assertNotNull(loanDetails.getRepaymentSchedule());

            // loan term
            assertEquals(92, loanDetails.getRepaymentSchedule().getLoanTermInDays());

            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getTotalPrincipalExpected());

            // first period [2023-03-07 to 2023-04-07]
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(333.33, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());

            // second period [2023-04-07 to 2023-05-07]
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(333.33, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod());

            // third period [2023-05-07 to 2023-06-07]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(333.34, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());

            // first disbursement (7 March 2023)

            LocalDate disbursementDate = LocalDate.of(2023, 3, 7);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            loanTransactionHelper.disburseLoanWithTransactionAmount("07 March 2023", loanId, "500");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify loan schedule is according to disbursement date
            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // loan term
            assertEquals(92, loanDetails.getRepaymentSchedule().getLoanTermInDays());
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(500.0, loanDetails.getRepaymentSchedule().getTotalPrincipalExpected());
            assertEquals(500.0, loanDetails.getRepaymentSchedule().getTotalPrincipalDisbursed());

            // first period [2023-03-07 to 2023-04-07]
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(166.67, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());

            // second period [2023-04-07 to 2023-05-07]
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(166.67, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod());

            // third period [2023-05-07 to 2023-06-07]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(166.66, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());

            // second disbursement next month (7 April 2023)

            disbursementDate = LocalDate.of(2023, 4, 7);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            loanTransactionHelper.disburseLoanWithTransactionAmount("07 April 2023", loanId, "500");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify loan schedule is according to disbursement after second disbursement

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // loan term
            assertEquals(92, loanDetails.getRepaymentSchedule().getLoanTermInDays());
            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getTotalPrincipalExpected());
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getTotalPrincipalDisbursed());

            // first period [2023-03-07 to 2023-04-07]
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(333.33, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod());

            // second period [2023-04-07 to 2023-05-07]
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(333.33, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());

            // third period [2023-05-07 to 2023-06-07]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(333.34, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod());

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    private PutLoanProductsProductIdResponse updateLoanProduct(LoanTransactionHelper loanTransactionHelper, Long id) {
        // repayment start date configuration
        final Integer repaymentStartDateType = 1;
        final PutLoanProductsProductIdRequest requestModifyLoan = new PutLoanProductsProductIdRequest()
                .repaymentStartDateType(repaymentStartDateType).locale("en");
        return loanTransactionHelper.updateLoanProduct(id, requestModifyLoan);
    }

    private Integer createLoanProductWithRepaymentStartDateTypeConfiguration(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId, final Integer repaymentStartDateType) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().withRepaymentStartDateType(repaymentStartDateType)
                .build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanProductId;

    }

    private Integer createLoanAccountMultipleRepaymentsDisbursement(final Integer clientID, final Long loanProductID,
            final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("3")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("3").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("07 March 2023").withSubmittedOnDate("03 March 2023").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("03 March 2023", "1000", loanId, null);
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
            LoanTransactionHelper loanTransactionHelper, final Integer repaymentStartDateType) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("3").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withRepaymentStartDateType(repaymentStartDateType).build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

}
