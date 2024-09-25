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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.cob.data.JobBusinessStepConfigData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.BusinessStepConfigurationHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanAccountArrearsAgeingCOBBusinessStepTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    public static final String UPDATE_LOAN_ARREARS_AGING = "UPDATE_LOAN_ARREARS_AGING";

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
    public void loanArrearsAgeingCOBBusinessStepTest() {
        // Set Business Date
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            LocalDate businessDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            LocalDate operationDate = businessDate.minusDays(40);
            String loanOperationDate = Utils.dateFormatter.format(operationDate);

            // create Client
            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // create Loan Product

            // Delinquency Bucket
            final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
            final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                    delinquencyBucketId);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                    delinquencyBucketId);
            assertNotNull(getLoanProductsProductResponse);

            // Loan1 ExternalId
            String loan1ExternalIdStr = UUID.randomUUID().toString();

            // create Loan Account for Client with Loan Product type 1
            Long loanProductId = getLoanProductsProductResponse.getId();
            final Integer loanId_1 = createLoanAccount(loanOperationDate, clientId, loanProductId, loan1ExternalIdStr);

            String loan2ExternalIdStr = UUID.randomUUID().toString();
            final Integer loanId_2 = createLoanAccount(loanOperationDate, clientId, loanProductId, loan2ExternalIdStr);

            // Run Loan cob with verfying business step for Update Arrears ageing details
            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);

            // COB Step Validation
            final JobBusinessStepConfigData jobBusinessStepConfigData = BusinessStepConfigurationHelper
                    .getConfiguredBusinessStepsByJobName(requestSpec, responseSpec, BusinessConfigurationApiTest.LOAN_JOB_NAME);
            assertNotNull(jobBusinessStepConfigData);
            assertEquals(BusinessConfigurationApiTest.LOAN_JOB_NAME, jobBusinessStepConfigData.getJobName());
            assertTrue(jobBusinessStepConfigData.getBusinessSteps().size() > 0);
            assertTrue(jobBusinessStepConfigData.getBusinessSteps().stream()
                    .anyMatch(businessStep -> UPDATE_LOAN_ARREARS_AGING.equals(businessStep.getStepName())));

            // Run the Loan COB Job
            final String jobName = "Loan COB";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // verify Arrears details are updated for both the loans, by verifying loan summary fields for
            // principalOverdue,totalOverdue,overdueSinceddate

            // Retrieve Loan 1 with loanId
            GetLoansLoanIdResponse loan1Details = loanTransactionHelper.getLoanDetails((long) loanId_1);
            GetLoansLoanIdSummary loan1Summary = loan1Details.getSummary();
            assertNotNull(loan1Summary);
            assertNotNull(loan1Summary.getOverdueSinceDate());
            assertEquals(loan1Summary.getPrincipalOverdue(), 1000.00);
            assertEquals(loan1Summary.getTotalOverdue(), 1000.00);

            // Retrieve Loan 2 with loanId
            GetLoansLoanIdResponse loan2Details = loanTransactionHelper.getLoanDetails((long) loanId_2);
            GetLoansLoanIdSummary loan2Summary = loan2Details.getSummary();
            assertNotNull(loan2Summary);
            assertNotNull(loan2Summary.getOverdueSinceDate());
            assertEquals(loan2Summary.getPrincipalOverdue(), 1000.00);
            assertEquals(loan2Summary.getTotalOverdue(), 1000.00);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final String operationDate, final Integer clientID, final Long loanProductID,
            final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(operationDate).withSubmittedOnDate(operationDate).withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(operationDate, "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount(operationDate, loanId, "1000");
        return loanId;
    }
}
