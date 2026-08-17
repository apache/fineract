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

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.JobBusinessStepConfigData;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(1)
public class LoanAccountArrearsAgeingCOBBusinessStepTest extends FeignLoanTestBase {

    private static final String LOAN_JOB_NAME = "LOAN_CLOSE_OF_BUSINESS";
    private static final String UPDATE_LOAN_ARREARS_AGING = "UPDATE_LOAN_ARREARS_AGING";

    private final FeignDelinquencyHelper delinquencyHelper = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignBusinessStepHelper businessStepHelper = new FeignBusinessStepHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void loanArrearsAgeingCOBBusinessStepTest() {
        // Set Business Date
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            LocalDate businessDate = Utils.getLocalDateOfTenant();
            updateBusinessDate(dateTimeFormatter.format(businessDate));

            LocalDate operationDate = businessDate.minusDays(40);
            String loanOperationDate = dateTimeFormatter.format(operationDate);

            Long clientId = createClient();
            // create Client
            // Delinquency Bucket
            Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();
            // create Loan Product
            Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 1, 0,
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)
                    .delinquencyBucketId(delinquencyBucketId));

            // create Loan Account for Client with Loan Product type 1
            Long loanId1 = createArrearsLoanAccount(clientId, loanProductId, loanOperationDate);
            Long loanId2 = createArrearsLoanAccount(clientId, loanProductId, loanOperationDate);

            // Run Loan cob with verfying business step for Update Arrears ageing details
            // COB Step Validation
            JobBusinessStepConfigData jobBusinessStepConfigData = businessStepHelper.getConfiguredBusinessStepsByJobName(LOAN_JOB_NAME);
            assertNotNull(jobBusinessStepConfigData);
            assertEquals(LOAN_JOB_NAME, jobBusinessStepConfigData.getJobName());
            assertTrue(jobBusinessStepConfigData.getBusinessSteps().size() > 0);
            assertTrue(jobBusinessStepConfigData.getBusinessSteps().stream()
                    .anyMatch(businessStep -> UPDATE_LOAN_ARREARS_AGING.equals(businessStep.getStepName())));

            // Run the Loan COB Job
            schedulerHelper.executeAndAwaitJob("Loan COB");

            // verify Arrears details are updated for both loans
            verifyArrearsSummary(loanId1);
            // verify Arrears details are updated for both the loans, by verifying loan summary fields for
            // principalOverdue,totalOverdue,overdueSinceddate
            // Retrieve Loan 1 with loanId
            // Retrieve Loan 2 with loanId
            verifyArrearsSummary(loanId2);
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private void verifyArrearsSummary(final Long loanId) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        GetLoansLoanIdSummary loanSummary = loanDetails.getSummary();
        assertNotNull(loanSummary);
        assertNotNull(loanSummary.getOverdueSinceDate());
        assertEquals(1000.00, Utils.getDoubleValue(loanSummary.getPrincipalOverdue()));
        assertEquals(1000.00, Utils.getDoubleValue(loanSummary.getTotalOverdue()));
    }

    private Long createArrearsLoanAccount(final Long clientId, final Long productId, final String operationDate) {
        PostLoansRequest request = new PostLoansRequest().clientId(clientId).productId(productId).principal(new BigDecimal("1000"))
                .loanTermFrequency(30).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS).numberOfRepayments(1)
                .repaymentEvery(30).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS).interestRatePerPeriod(BigDecimal.ZERO)
                .interestType(LoanTestData.InterestType.FLAT).amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)
                .transactionProcessingStrategyCode(
                        LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .expectedDisbursementDate(operationDate).submittedOnDate(operationDate).dateFormat(LoanTestData.DATETIME_PATTERN)
                .locale(LoanTestData.LOCALE).loanType("individual");
        Long loanId = applyForLoan(request);
        approveLoan(loanId, approveLoanRequest(1000.0, operationDate));
        disburseLoan(loanId, BigDecimal.valueOf(1000), operationDate);
        return loanId;
    }
}
