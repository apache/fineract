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
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetDelinquencyRangesResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class DelinquencyAndChargebackIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private static final String principalAmount = "1200.00";
    private static final Double doubleZERO = Double.valueOf("0.00");

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();

        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testLoanClassificationStepAsPartOfCOB() {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        LocalDate businessDate = todaysDate.minusDays(57);
        log.info("Current Business date {}", businessDate);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

        final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);
        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                delinquencyBucket.getId());
        assertNotNull(getLoanProductsProductResponse);

        log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());
        assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

        // Older date to have more than one overdue installment
        LocalDate transactionDate = businessDate;
        String operationDate = Utils.dateFormatter.format(transactionDate);
        log.info("Operation date {}", transactionDate);

        // Create Loan Account
        final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                getLoanProductsProductResponse.getId().toString(), operationDate);

        // Run first time the Loan COB Job
        final String jobName = "Loan COB";
        schedulerJobHelper.executeAndAwaitJob(jobName);

        // Get loan details expecting to have not a delinquency classification
        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, "0.00", principalAmount, 0, doubleZERO);

        // Move the Business date to get older the loan and to have an overdue loan
        businessDate = businessDate.plusDays(43);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
        log.info("Current Business date {}", businessDate);

        String amountVal = "100.00";
        Float transactionAmount = Float.valueOf(amountVal);
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate,
                transactionAmount, loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();
        loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId.intValue(), 0);
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, "0.00", "1100.00", 0, doubleZERO);

        final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId.intValue(), amountVal,
                0, responseSpec);
        loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId.intValue(), 1);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        // Past Due Days in Zero because the Charge back transaction exists and It was done with the current date
        validateLoanAccount(getLoansLoanIdResponse, amountVal, principalAmount, 0, Double.valueOf("100.00"));

        // Move the Business date to get older the loan and to have an overdue loan
        businessDate = businessDate.plusDays(9);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
        log.info("Current Business date {}", businessDate);
        // Run Second time the Job
        schedulerJobHelper.executeAndAwaitJob(jobName);

        // Get loan details expecting to have a delinquency classification
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);

        // The value is lower due the 3 grace days
        validateLoanAccount(getLoansLoanIdResponse, "100.00", principalAmount, 9, Double.valueOf("100.00"));

        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final LoanTransactionHelper loanTransactionHelper, final String clientId, final String loanProductId,
            final String operationDate) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principalAmount).withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("0") //
                .withExpectedDisbursementDate(operationDate) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate(operationDate) //
                .build(clientId, loanProductId, null);
        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(operationDate, principalAmount, loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount(operationDate, loanId, principalAmount);
        return loanId;
    }

    private GetDelinquencyRangesResponse validateLoanAccount(GetLoansLoanIdResponse getLoansLoanIdResponse, final String adjustments,
            final String outstanding, Integer pastDueDays, Double delinquentAmount) {
        assertNotNull(getLoansLoanIdResponse);
        final GetDelinquencyRangesResponse delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();

        log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));
        if (delinquencyRange != null) {
            log.info("Loan Delinquency Range is {}", delinquencyRange.getClassification());
        }
        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
        loanTransactionHelper.evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, Double.valueOf(adjustments));
        DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, pastDueDays, delinquentAmount);

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf(outstanding));

        return delinquencyRange;
    }

}
