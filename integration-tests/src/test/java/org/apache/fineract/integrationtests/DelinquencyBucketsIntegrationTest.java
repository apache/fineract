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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.DeleteDelinquencyBucketResponse;
import org.apache.fineract.client.models.DeleteDelinquencyRangeResponse;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetDelinquencyRangesResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PutDelinquencyBucketResponse;
import org.apache.fineract.client.models.PutDelinquencyRangeResponse;
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
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class DelinquencyBucketsIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();

        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testCreateDelinquencyRanges() {
        // given
        final String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);

        // when
        final PostDelinquencyRangeResponse delinquencyRangeResponse01 = DelinquencyRangesHelper.createDelinquencyRange(requestSpec,
                responseSpec, jsonRange);
        final ArrayList<GetDelinquencyRangesResponse> ranges = DelinquencyRangesHelper.getDelinquencyRanges(requestSpec, responseSpec);

        // then
        assertNotNull(delinquencyRangeResponse01);
        assertNotNull(ranges);
        assertEquals(1, ranges.get(0).getMinimumAgeDays(), "Expected Min Age Days to 1");
        assertEquals(3, ranges.get(0).getMaximumAgeDays(), "Expected Max Age Days to 3");
    }

    @Test
    public void testUpdateDelinquencyRanges() {
        // given
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        final PostDelinquencyRangeResponse delinquencyRangeResponse01 = DelinquencyRangesHelper.createDelinquencyRange(requestSpec,
                responseSpec, jsonRange);
        jsonRange = DelinquencyRangesHelper.getAsJSON(1, 7);
        assertNotNull(delinquencyRangeResponse01);

        // when
        final PutDelinquencyRangeResponse delinquencyRangeResponse02 = DelinquencyRangesHelper.updateDelinquencyRange(requestSpec,
                responseSpec, delinquencyRangeResponse01.getResourceId(), jsonRange);
        final GetDelinquencyRangesResponse range = DelinquencyRangesHelper.getDelinquencyRange(requestSpec, responseSpec,
                delinquencyRangeResponse01.getResourceId());
        final DeleteDelinquencyRangeResponse deleteDelinquencyRangeResponse = DelinquencyRangesHelper.deleteDelinquencyRange(requestSpec,
                responseSpec, delinquencyRangeResponse01.getResourceId());

        // then
        assertNotNull(delinquencyRangeResponse02);
        assertNotNull(deleteDelinquencyRangeResponse);
        assertNotNull(range);
        assertNotEquals(3, range.getMaximumAgeDays());
        assertEquals(1, range.getMinimumAgeDays());
        assertEquals(7, range.getMaximumAgeDays());
    }

    @Test
    public void testDelinquencyBuckets() {
        // given
        ArrayList<Integer> rangeIds = new ArrayList<>();
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(4, 30);
        // Create
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        PostDelinquencyBucketResponse delinquencyBucketResponse = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec,
                responseSpec, jsonBucket);
        // Update
        jsonRange = DelinquencyRangesHelper.getAsJSON(31, 60);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        PutDelinquencyBucketResponse updateDelinquencyBucketResponse = DelinquencyBucketsHelper.updateDelinquencyBucket(requestSpec,
                responseSpec, delinquencyBucketResponse.getResourceId(), jsonBucket);
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        // Read
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketResponse.getResourceId());

        // when
        final ArrayList<GetDelinquencyBucketsResponse> bucketList = DelinquencyBucketsHelper.getDelinquencyBuckets(requestSpec,
                responseSpec);

        // then
        assertNotNull(bucketList);
        assertNotNull(delinquencyBucket);
        assertEquals(2, delinquencyBucket.getRanges().size());
        assertNotNull(delinquencyBucketResponse);
        assertNotNull(updateDelinquencyBucketResponse);
    }

    @Test
    public void testDelinquencyBucketDelete() {
        // given
        ArrayList<Integer> rangeIds = new ArrayList<>();
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(4, 30);
        // Create
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        PostDelinquencyBucketResponse delinquencyBucketResponse = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec,
                responseSpec, jsonBucket);
        // Delete
        DeleteDelinquencyBucketResponse deleteDelinquencyBucketResponse = DelinquencyBucketsHelper.deleteDelinquencyBucket(requestSpec,
                responseSpec, delinquencyBucketResponse.getResourceId());

        // when
        final ArrayList<GetDelinquencyBucketsResponse> bucketList = DelinquencyBucketsHelper.getDelinquencyBuckets(requestSpec,
                responseSpec);

        // then
        assertNotNull(bucketList);
        assertNotNull(delinquencyBucketResponse);
        assertNotNull(deleteDelinquencyBucketResponse);
    }

    @Test
    public void testDelinquencyBucketsRangeAgeOverlaped() {
        // Given
        ArrayList<Integer> rangeIds = new ArrayList<>();
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(3, 30);
        // Create
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        final ResponseSpecification response403Spec = new ResponseSpecBuilder().expectStatusCode(403).build();

        // When
        DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, response403Spec, jsonBucket);
    }

    @Test
    public void testDelinquencyBucketsNameDuplication() {
        // Given
        ArrayList<Integer> rangeIds = new ArrayList<>();
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(4, 30);
        // Create
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        final ResponseSpecification response403Spec = new ResponseSpecBuilder().expectStatusCode(403).build();

        // When
        DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, jsonBucket);

        // Then
        DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, response403Spec, jsonBucket);
    }

    @Test
    public void testLoanClassificationJob() {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

        LocalDate businessDate = Utils.getLocalDateOfTenant();
        businessDate = businessDate.minusDays(57);
        log.info("Current date {}", businessDate);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

        // Given
        final LoanTransactionHelper loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);

        ArrayList<Integer> rangeIds = new ArrayList<>();
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(4, 60);
        // Create
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        final GetDelinquencyRangesResponse range = DelinquencyRangesHelper.getDelinquencyRange(requestSpec, responseSpec,
                delinquencyRangeResponse.getResourceId());
        final String classificationExpected = range.getClassification();
        log.info("Expected Delinquency Range classification {}", classificationExpected);

        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        PostDelinquencyBucketResponse delinquencyBucketResponse = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec,
                responseSpec, jsonBucket);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketResponse.getResourceId());

        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucket.getId());
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());

        final String operationDate = Utils.dateFormatter.format(businessDate);
        final String principalAmount = "10000";

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principalAmount).withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withExpectedDisbursementDate(operationDate) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate(operationDate) //
                .build(clientId.toString(), loanProductId.toString(), null);
        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(operationDate, principalAmount, loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount(operationDate, loanId, principalAmount);

        // When
        // Run first time the Job
        final String jobName = "Loan Delinquency Classification";
        schedulerJobHelper.executeAndAwaitJob(jobName);

        // Get loan details expecting to have not a delinquency classification
        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        final GetDelinquencyRangesResponse firstTestCase = getLoansLoanIdResponse.getDelinquencyRange();
        log.info("Loan Delinquency Range is null {}", (firstTestCase == null));
        GetLoansLoanIdRepaymentSchedule getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
        if (getLoanRepaymentSchedule != null) {
            log.info("Loan with {} periods", getLoanRepaymentSchedule.getPeriods().size());
            for (GetLoansLoanIdRepaymentPeriod period : getLoanRepaymentSchedule.getPeriods()) {
                log.info("Period number {} for due date {}", period.getPeriod(), period.getDueDate());
            }
        }

        // Move the Business date to get older the loan and to have an overdue loan
        businessDate = businessDate.plusDays(40);
        log.info("Current date {}", businessDate);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
        // Run Second time the Job
        schedulerJobHelper.executeAndAwaitJob(jobName);

        // Get loan details expecting to have a delinquency classification
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        final GetDelinquencyRangesResponse secondTestCase = getLoansLoanIdResponse.getDelinquencyRange();
        log.info("Loan Delinquency Range is {}", secondTestCase.getClassification());

        // Then
        assertNotNull(delinquencyBucketResponse);
        assertNotNull(getLoanProductsProductResponse);
        assertNull(firstTestCase);
        assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());
        assertNotNull(secondTestCase);
        assertEquals(secondTestCase.getClassification(), classificationExpected);

        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
    }

}
