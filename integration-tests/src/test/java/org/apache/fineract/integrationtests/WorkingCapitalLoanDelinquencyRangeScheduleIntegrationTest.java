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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.DelinquencyBucketResponse;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PutDelinquencyBucketResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyRangeScheduleHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class WorkingCapitalLoanDelinquencyRangeScheduleIntegrationTest {

    @Test
    public void testDelinquencyBucketWithMinPaymentValue() {
        // given - Create delinquency ranges
        final List<Long> rangeIds = createDelinquencyRanges();

        // when - Create a WC delinquency bucket with minimumPayment percentage
        final BigDecimal initialMinPayment = new BigDecimal("3");
        final PostDelinquencyBucketResponse createResponse = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(rangeIds, 30, 0, initialMinPayment, 1);

        // then - Verify creation was successful
        assertNotNull(createResponse);
        assertNotNull(createResponse.getResourceId());
        log.info("Created WC delinquency bucket with id: {}", createResponse.getResourceId());

        // Retrieve the bucket and verify minimumPaymentValue is persisted
        final DelinquencyBucketResponse bucket = WorkingCapitalLoanDelinquencyRangeScheduleHelper.getBucket(createResponse.getResourceId());
        assertNotNull(bucket);
        assertNotNull(bucket.getBucketType(), "bucketType should not be null");
        assertEquals("WORKING_CAPITAL", bucket.getBucketType().getId(), "Bucket type should be WORKING_CAPITAL");
        assertNotNull(bucket.getMinimumPaymentPeriodAndRule(), "minimumPaymentPeriodAndRule should not be null");
        assertEquals(30, bucket.getMinimumPaymentPeriodAndRule().getFrequency(), "Frequency should be 30");
        assertEquals("DAYS", bucket.getMinimumPaymentPeriodAndRule().getFrequencyType().getId(), "Frequency type should be DAYS");
        assertEquals(0, new BigDecimal("3").compareTo(bucket.getMinimumPaymentPeriodAndRule().getMinimumPayment()),
                "Minimum payment should be 3");
        assertEquals("PERCENTAGE", bucket.getMinimumPaymentPeriodAndRule().getMinimumPaymentType().getId(),
                "Minimum payment type should be PERCENTAGE");

        // when - Update the bucket with a different minimumPayment
        final BigDecimal updatedMinPayment = new BigDecimal("5");
        final PutDelinquencyBucketResponse updateResponse = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .updateWorkingCapitalLoanDelinquencyBucket(createResponse.getResourceId(), rangeIds, 30, 0, updatedMinPayment, 1);

        // then - Verify update was successful
        assertNotNull(updateResponse);
        log.info("Updated WC delinquency bucket with id: {}", updateResponse.getResourceId());

        // Retrieve the bucket again and verify the updated minimumPaymentValue
        final DelinquencyBucketResponse updatedBucket = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .getBucket(createResponse.getResourceId());
        assertNotNull(updatedBucket.getMinimumPaymentPeriodAndRule(), "minimumPaymentPeriodAndRule should not be null after update");
        assertEquals(0, new BigDecimal("5").compareTo(updatedBucket.getMinimumPaymentPeriodAndRule().getMinimumPayment()),
                "minimumPayment should be updated to 5");

        // Cleanup - Delete the bucket
        WorkingCapitalLoanDelinquencyRangeScheduleHelper.deleteBucket(createResponse.getResourceId());
        log.info("Deleted WC delinquency bucket with id: {}", createResponse.getResourceId());
    }

    @Test
    public void testRangeScheduleEndpointReturnsEmptyForUndisbursedLoan() {
        // given - Create a WC delinquency bucket with minimum payment configuration
        final List<Long> rangeIds = createDelinquencyRanges();
        final BigDecimal minPayment = new BigDecimal("3");
        final PostDelinquencyBucketResponse bucketResponse = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(rangeIds, 30, 0, minPayment, 1);
        assertNotNull(bucketResponse);

        // Create a WC product linked to this delinquency bucket
        final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withDelinquencyBucketId(bucketResponse.getResourceId()).build()).getResourceId();
        assertNotNull(productId);

        // Create a client and submit a WC loan
        final Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(10000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(11000)) //
                .buildSubmitJson());
        assertNotNull(loanId);
        log.info("Created WC loan with id: {}", loanId);

        // when - Retrieve the range schedule for this undisbursed loan
        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> schedule = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .getDelinquencyRangeSchedule(loanId);

        // then - Should return an empty list (no schedule generated before disbursement + COB)
        assertNotNull(schedule);
        assertTrue(schedule.isEmpty(), "Range schedule should be empty for an undisbursed loan");
    }

    @Test
    public void testRangeScheduleExpectedAmountIncludesDiscount() {
        final BigDecimal principal = BigDecimal.valueOf(9000);
        final BigDecimal discount = BigDecimal.valueOf(1000);
        final BigDecimal minPaymentPercent = new BigDecimal("3");
        // Expected: (9000 + 1000) * 3% = 300
        final BigDecimal expectedMinPayment = new BigDecimal("300.000000");

        final String businessDate = "01 January 2026";
        BusinessDateHelper.runAt(businessDate, () -> {
            // given - Create delinquency bucket with 30-day frequency, 3% minimum payment
            final List<Long> rangeIds = createDelinquencyRanges();
            final PostDelinquencyBucketResponse bucketResponse = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                    .createWorkingCapitalLoanDelinquencyBucket(rangeIds, 30, 0, minPaymentPercent, 1);

            // Create product with discount allowed and linked to delinquency bucket
            final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
            final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
            final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
            final Long productId = productHelper
                    .createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                            .withShortName(uniqueShortName).withDiscount(discount).withDelinquencyBucketId(bucketResponse.getResourceId())
                            .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).build())
                    .getResourceId();

            // Create client and submit WC loan
            final Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
            final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(clientId) //
                    .withProductId(productId) //
                    .withPrincipal(principal) //
                    .withPeriodPaymentRate(BigDecimal.ONE) //
                    .withTotalPayment(BigDecimal.valueOf(10000)) //
                    .buildSubmitJson());

            // Approve with discount
            final LocalDate approvedDate = LocalDate.of(2026, 1, 1);
            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedDate, principal, discount));

            // Disburse
            applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(approvedDate, principal));

            // Run WC COB to generate the range schedule
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            // when - Retrieve the range schedule
            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> schedule = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                    .getDelinquencyRangeSchedule(loanId);

            // then - Schedule should have at least 1 period
            assertNotNull(schedule);
            assertFalse(schedule.isEmpty(), "Range schedule should not be empty after disbursement + COB");

            // Verify expectedAmount = (principal + discount) * 3% = (9000 + 1000) * 0.03 = 300
            final WorkingCapitalLoanDelinquencyRangeScheduleData firstPeriod = schedule.get(0);
            assertEquals(1, firstPeriod.getPeriodNumber());
            assertEquals(0, expectedMinPayment.compareTo(firstPeriod.getExpectedAmount()),
                    "Expected amount should be (principal + discount) * 3% = 300, but was " + firstPeriod.getExpectedAmount());
            log.info("Range schedule period 1: expectedAmount={}, paidAmount={}, outstandingAmount={}", firstPeriod.getExpectedAmount(),
                    firstPeriod.getPaidAmount(), firstPeriod.getOutstandingAmount());
        });
    }

    private List<Long> createDelinquencyRanges() {
        final PostDelinquencyRangeResponse range1 = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(1).maximumAgeDays(30).locale("en"));
        final PostDelinquencyRangeResponse range2 = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(31).maximumAgeDays(60).locale("en"));
        return List.of(range1.getResourceId(), range2.getResourceId());
    }
}
