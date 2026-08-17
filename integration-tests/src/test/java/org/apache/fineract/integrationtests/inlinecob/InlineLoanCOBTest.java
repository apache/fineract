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
package org.apache.fineract.integrationtests.inlinecob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.LongStream;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.DelinquencyBucketRequest;
import org.apache.fineract.client.models.DelinquencyBucketResponse;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.GetDelinquencyTagHistoryResponse;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class InlineLoanCOBTest extends FeignLoanTestBase {

    private static final String CUMULATIVE_STRATEGY = "mifos-standard-strategy";
    private static final Long REPAYMENT_BATCH_REQUEST_ID = 4730L;
    private static final int INLINE_COB_MAX_LOAN_IDS = 1000;

    @Test
    public void testInlineCOB() {
        try {
            enableBusinessDateAtMarch2();
            Long loanId = createOverdueLoan();

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            executeInlineCOB(loanId);
            verifyLastClosedBusinessDate(loanId, "02 March 2020");

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "03 March 2020");
            executeInlineCOB(loanId);
            verifyLastClosedBusinessDate(loanId, "03 March 2020");

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "10 March 2020");
            executeInlineCOB(loanId);
            verifyLastClosedBusinessDate(loanId, "10 March 2020");
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testInlineCOBCatchUpLoans() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            FeignDelinquencyHelper delinquencyHelper = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient());

            List<Long> rangeIds = new ArrayList<>();
            // First Range
            PostDelinquencyRangeResponse firstRange = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(1)
                    .maximumAgeDays(3).locale("en").classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(firstRange.getResourceId());
            // Second Range
            PostDelinquencyRangeResponse secondRange = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(4)
                    .maximumAgeDays(60).locale("en").classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(secondRange.getResourceId());

            PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                    .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
            assertNotNull(delinquencyBucketResponse);
            DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

            Long loanId = createDelinquentLoan(delinquencyBucket.getId());

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            executeInlineCOB(loanId);
            assertTrue(getLoanDelinquencyTags(loanId).isEmpty());
            verifyLastClosedBusinessDate(loanId, "02 March 2020");

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "04 April 2020");
            executeInlineCOB(loanId);
            List<GetDelinquencyTagHistoryResponse> loanDelinquencyTags = getLoanDelinquencyTags(loanId);
            verifyLastClosedBusinessDate(loanId, "04 April 2020");
            assertEquals(1, loanDelinquencyTags.size());
            assertEquals("2020-04-03", loanDelinquencyTags.get(0).getAddedOnDate().toString());

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "10 April 2020");
            executeInlineCOB(loanId);
            loanDelinquencyTags = getLoanDelinquencyTags(loanId);
            verifyLastClosedBusinessDate(loanId, "10 April 2020");
            assertEquals(2, loanDelinquencyTags.size());
            assertEquals("2020-04-03", loanDelinquencyTags.get(1).getAddedOnDate().toString());
            assertEquals("2020-04-06", loanDelinquencyTags.get(0).getAddedOnDate().toString());
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testInlineCOBOnRepaymentWithSoftLockedLoan() {
        try {
            enableBusinessDateAtMarch2();
            Long loanId = createOverdueLoan();

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            executeInlineCOB(loanId);
            verifyLastClosedBusinessDate(loanId, "02 March 2020");

            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "10 March 2020");

            makeRepaymentAsNonByPassUser(loanId, "10 March 2020", 10.0);

            verifyLastClosedBusinessDate(loanId, "09 March 2020");
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testInlineCOBCatchUpOnRepaymentWithNotLockedLoan() {
        try {
            enableBusinessDateAtMarch2();
            Long loanId = createOverdueLoan();

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            executeInlineCOB(loanId);
            verifyLastClosedBusinessDate(loanId, "02 March 2020");

            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "10 March 2020");

            makeRepaymentAsNonByPassUser(loanId, "10 March 2020", 10.0);

            verifyLastClosedBusinessDate(loanId, "09 March 2020");
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testInlineCOBOnBatchAPIWithOldRelativeUrls() {
        try {
            enableBusinessDateAtMarch2();
            Long loanId = createOverdueLoan();

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            executeInlineCOB(loanId);
            verifyLastClosedBusinessDate(loanId, "02 March 2020");

            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "10 March 2020");

            AtomicReference<List<BatchResponse>> responseRef = new AtomicReference<>();
            runAsNonByPass(() -> responseRef
                    .set(batchRequest().repayLoanLegacyRelativeUrl(REPAYMENT_BATCH_REQUEST_ID, loanId, "10", "10 March 2020")
                            .executeWithoutEnclosingTransaction()));
            assertEquals(HttpStatus.SC_OK, responseRef.get().get(0).getStatusCode().intValue(), "Verify Status Code 200 for Repayment");

            verifyLastClosedBusinessDate(loanId, "09 March 2020");
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testInlineCOBOnBatchAPI() {
        try {
            enableBusinessDateAtMarch2();
            Long loanId = createOverdueLoan();

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            executeInlineCOB(loanId);
            verifyLastClosedBusinessDate(loanId, "02 March 2020");

            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "10 March 2020");

            AtomicReference<List<BatchResponse>> responseRef = new AtomicReference<>();
            runAsNonByPass(() -> responseRef.set(batchRequest().repayLoan(REPAYMENT_BATCH_REQUEST_ID, loanId, "10", "10 March 2020")
                    .executeWithoutEnclosingTransaction()));
            assertEquals(HttpStatus.SC_OK, responseRef.get().get(0).getStatusCode().intValue(), "Verify Status Code 200 for Repayment");

            verifyLastClosedBusinessDate(loanId, "09 March 2020");
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testInlineCOBRequestBodyItemLimitValidation() {
        List<Long> loanIds = LongStream.rangeClosed(1, INLINE_COB_MAX_LOAN_IDS + 1).boxed().toList();
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> executeInlineCOB(loanIds));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Size of the loan IDs list cannot be over " + INLINE_COB_MAX_LOAN_IDS),
                "Expected the item-limit validation message but got: " + exception.getMessage());
    }

    private void enableBusinessDateAtMarch2() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "02 March 2020");
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                new PutGlobalConfigurationsRequest().value(0L));
    }

    private void disableBusinessDate() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    private void makeRepaymentAsNonByPassUser(Long loanId, String date, double amount) {
        FeignTransactionHelper nonByPassTransactionHelper = new FeignTransactionHelper(
                FeignUserHelper.getSimpleUserWithoutBypassPermissionClient());
        nonByPassTransactionHelper.makeLoanRepayment(loanId, "repayment", date, amount);
    }

    private Long createOverdueLoan() {
        Long clientId = createClient();
        ChargeRequest overdueFeeCharge = ChargeRequestBuilders.loanOverdueFee(1.0)
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue());
        Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeeCharge).getResourceId();
        PostLoanProductsRequest product = create4Period1MonthLongWithoutInterestProduct(CUMULATIVE_STRATEGY).principal(15000.0)
                .charges(List.of(new LoanProductChargeData().id(overdueFeeChargeId)));
        return applyApproveDisburse(clientId, createLoanProduct(product));
    }

    private Long createDelinquentLoan(Long delinquencyBucketId) {
        Long clientId = createClient();
        PostLoanProductsRequest product = create4Period1MonthLongWithoutInterestProduct(CUMULATIVE_STRATEGY).principal(15000.0)
                .delinquencyBucketId(delinquencyBucketId);
        return applyApproveDisburse(clientId, createLoanProduct(product));
    }

    private Long applyApproveDisburse(Long clientId, Long loanProductId) {
        PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 March 2020", 15000.0, 4)//
                .repaymentEvery(1)//
                .loanTermFrequency(4)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS);
        Long loanId = applyForLoan(applicationRequest);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        approveLoan(loanId, approveLoanRequest(15000.0, "01 March 2020"));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        disburseLoan(loanId, BigDecimal.valueOf(15000.0), "02 March 2020");
        verifyLoanStatus(loanId, LoanStatus.ACTIVE);
        return loanId;
    }
}
