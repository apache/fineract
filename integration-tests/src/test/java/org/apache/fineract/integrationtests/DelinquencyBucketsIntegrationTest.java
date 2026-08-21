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

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.PAUSE;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.RESUME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.DeleteDelinquencyBucketResponse;
import org.apache.fineract.client.models.DeleteDelinquencyRangeResponse;
import org.apache.fineract.client.models.DelinquencyBucketRequest;
import org.apache.fineract.client.models.DelinquencyBucketResponse;
import org.apache.fineract.client.models.DelinquencyRangeData;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.DelinquencyRangeResponse;
import org.apache.fineract.client.models.GetDelinquencyTagHistoryResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencySummary;
import org.apache.fineract.client.models.GetLoansLoanIdLoanInstallmentLevelDelinquency;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.JobBusinessStepConfigData;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansDelinquencyActionResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutDelinquencyBucketResponse;
import org.apache.fineract.client.models.PutDelinquencyRangeResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.LoanProductCommandsApi;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.AmortizationType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInMonthType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInYearType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestCalculationPeriodType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestRateFrequencyType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class DelinquencyBucketsIntegrationTest extends FeignLoanTestBase {

    private static final String CLIENT_ACTIVATION_DATE = "01 January 2012";
    private static final Double PRINCIPAL_AMOUNT = 10000.0;
    private static final Integer ACCOUNTING_RULE_NONE = 1;

    private final FeignDelinquencyHelper delinquencyHelper = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignBusinessStepHelper businessStepHelper = new FeignBusinessStepHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testCreateDelinquencyRanges() {
        // given
        final PostDelinquencyRangeResponse delinquencyRangeResponse01 = delinquencyHelper.createRange(new DelinquencyRangeRequest()
                .minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        final List<DelinquencyRangeResponse> ranges = delinquencyHelper.getRanges();

        // then
        assertNotNull(delinquencyRangeResponse01);
        assertNotNull(ranges);
        assertFalse(ranges.isEmpty());
        DelinquencyRangeResponse range = ranges.stream().filter(r -> r.getId().equals(delinquencyRangeResponse01.getResourceId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Range with id " + delinquencyRangeResponse01.getResourceId() + " not found"));
        assertEquals(1, range.getMinimumAgeDays(), "Expected Min Age Days to 1");
        assertEquals(3, range.getMaximumAgeDays(), "Expected Max Age Days to 3");
    }

    @Test
    public void testUpdateDelinquencyRanges() {
        // given
        final PostDelinquencyRangeResponse delinquencyRangeResponse01 = delinquencyHelper.createRange(new DelinquencyRangeRequest()
                .minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        // when
        final PutDelinquencyRangeResponse delinquencyRangeResponse02 = delinquencyHelper
                .updateRange(delinquencyRangeResponse01.getResourceId(), new DelinquencyRangeRequest().minimumAgeDays(1).maximumAgeDays(7)
                        .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        final DelinquencyRangeResponse range = delinquencyHelper.getRange(delinquencyRangeResponse01.getResourceId());
        final DeleteDelinquencyRangeResponse deleteDelinquencyRangeResponse = delinquencyHelper
                .deleteRange(delinquencyRangeResponse01.getResourceId());

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
        ArrayList<Long> rangeIds = new ArrayList<>();
        PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest()
                .minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(30)
                .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
        // Update
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        PutDelinquencyBucketResponse updateDelinquencyBucketResponse = delinquencyHelper.updateBucket(
                delinquencyBucketResponse.getResourceId(),
                new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
        delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(31).maximumAgeDays(60)
                .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        // Read
        final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

        // when
        final List<DelinquencyBucketResponse> bucketList = delinquencyHelper.getBuckets();

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
        ArrayList<Long> rangeIds = new ArrayList<>();
        final PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest()
                .minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
        // Delete
        DeleteDelinquencyBucketResponse deleteDelinquencyBucketResponse = delinquencyHelper
                .deleteBucket(delinquencyBucketResponse.getResourceId());

        // when
        final List<DelinquencyBucketResponse> bucketList = delinquencyHelper.getBuckets();

        // then
        assertNotNull(bucketList);
        assertNotNull(delinquencyBucketResponse);
        assertNotNull(deleteDelinquencyBucketResponse);
    }

    @Test
    public void testDelinquencyBucketsRangeAgeOverlaped() {
        // Given
        ArrayList<Long> rangeIds = new ArrayList<>();
        PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest()
                .minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(3).maximumAgeDays(30)
                .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        // When
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> {
            delinquencyHelper.createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
        });
        assertEquals(403, exception.getStatus());

    }

    @Test
    public void testDelinquencyBucketsNameDuplication() {
        // Given
        ArrayList<Long> rangeIds = new ArrayList<>();
        PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest()
                .minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        // When
        String bucketName = Utils.randomStringGenerator("DLQ_B_", 10);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        delinquencyHelper.createBucket(new DelinquencyBucketRequest().name(bucketName).ranges(rangeIds));

        // When
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> {
            delinquencyHelper.createBucket(new DelinquencyBucketRequest().name(bucketName).ranges(rangeIds));
        });
        assertEquals(403, exception.getStatus());
    }

    @Test
    public void testLoanProductCreationWithAndWithoutDelinquencyBucket() {
        // Given
        ArrayList<Long> rangeIds = new ArrayList<>();
        // First Range
        PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest()
                .minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        DelinquencyRangeResponse range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());

        // Second Range
        delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(60)
                .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());
        final String classificationExpected = range.getClassification();
        log.info("Expected Delinquency Range classification after Disbursement {}", classificationExpected);

        PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
        assertNotNull(delinquencyBucketResponse);
        final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

        // Loan product creation without Delinquency bucket
        GetLoanProductsProductIdResponse getLoanProductResponse = createLoanProduct(null, null);
        assertNotNull(getLoanProductResponse);
        assertNull(getLoanProductResponse.getDelinquencyBucket().getId());

        // Loan product creation with Delinquency bucket
        getLoanProductResponse = createLoanProduct(delinquencyBucket.getId(), null);
        assertNotNull(getLoanProductResponse);
        log.info("Loan Product Bucket Name: {}", getLoanProductResponse.getDelinquencyBucket().getName());
        assertEquals(getLoanProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

        // Update Loan product to remove the Delinquency bucket
        final Long loanProductId = getLoanProductResponse.getId();
        clearDelinquencyBucket(loanProductId);
        getLoanProductResponse = retrieveLoanProduct(loanProductId);
        assertNotNull(getLoanProductResponse);
        assertNull(getLoanProductResponse.getDelinquencyBucket().getId());
    }

    @Test
    public void testLoanClassificationRealtime() {
        try {
            // Given
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final LocalDate bussinesLocalDate = Utils.getDateAsLocalDate("01 March 2012");
            log.info("Current date {}", bussinesLocalDate);
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(bussinesLocalDate));
            final BusinessDateResponse businessDateResponse = businessDateHelper.getBusinessDate(BusinessDateType.BUSINESS_DATE.name());

            ArrayList<Long> rangeIds = new ArrayList<>();
            // First Range
            PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                    .createRange(new DelinquencyRangeRequest().minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE)
                            .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            DelinquencyRangeResponse range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());

            // Second Range
            delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(60)
                    .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());
            final String classificationExpected = range.getClassification();
            log.info("Expected Delinquency Range classification after Disbursement {}", classificationExpected);

            PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                    .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
            assertNotNull(delinquencyBucketResponse);
            final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

            // Client and Loan account creation
            final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), null);
            assertNotNull(getLoanProductsProductResponse);
            log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());
            assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

            // Older date to have more than one overdue installment
            final LocalDate transactionDate = bussinesLocalDate.minusDays(50);
            String operationDate = Utils.dateFormatter.format(transactionDate);

            // Create Loan Account
            final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), operationDate, null);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getDelinquencyRange());
            log.info("Loan Delinquency Range after Disbursement {}", getLoansLoanIdResponse.getDelinquencyRange().getClassification());
            // First Loan Delinquency Classification after Disbursement command
            assertEquals(getLoansLoanIdResponse.getDelinquencyRange().getClassification(), classificationExpected);

            // Apply a partial repayment
            operationDate = Utils.dateFormatter.format(bussinesLocalDate);
            makeLoanRepayment(loanId, "repayment", operationDate, 100.0);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            log.info("Loan Delinquency Range after Repayment {}", getLoansLoanIdResponse.getDelinquencyRange());
            assertNotNull(getLoansLoanIdResponse.getDelinquencyRange());
            // First Loan Delinquency Classification remains after Repayment because the installment is not fully paid
            assertEquals(getLoansLoanIdResponse.getDelinquencyRange().getClassification(), classificationExpected);

            // Apply a repayment to get a full paid installment
            makeLoanRepayment(loanId, "repayment", operationDate, 1000.0);
            getLoansLoanIdResponse = getLoanDetails(loanId);
            log.info("Loan Delinquency Range after Repayment {}", getLoansLoanIdResponse.getDelinquencyRange());
            assertNotNull(getLoansLoanIdResponse);
            // The Loan Delinquency Classification after Repayment command must be null
            assertNull(getLoansLoanIdResponse.getDelinquencyRange());
            // Get the Delinquency Tags
            List<GetDelinquencyTagHistoryResponse> getDelinquencyTagsHistory = getLoanDelinquencyTags(loanId);
            assertNotNull(getDelinquencyTagsHistory);
            log.info("Delinquency Tag History items {}", getDelinquencyTagsHistory.size());
            assertEquals(1, getDelinquencyTagsHistory.size());
            assertNotNull(getDelinquencyTagsHistory.get(0).getLiftedOnDate());
            assertEquals(getDelinquencyTagsHistory.get(0).getAddedOnDate(), businessDateResponse.getDate());
            assertEquals(getDelinquencyTagsHistory.get(0).getLiftedOnDate(), businessDateResponse.getDate());
            assertEquals(getDelinquencyTagsHistory.get(0).getDelinquencyRange().getClassification(), classificationExpected);
            log.info("Delinquency Tag Item with Lifted On {}", getDelinquencyTagsHistory.get(0).getLiftedOnDate());
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanClassificationRealtimeWithCharges() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final LocalDate bussinesLocalDate = Utils.getDateAsLocalDate("01 April 2012");
            log.info("Current date {}", bussinesLocalDate);
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(bussinesLocalDate));

            // Given
            ArrayList<Long> rangeIds = new ArrayList<>();
            // First Range
            PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                    .createRange(new DelinquencyRangeRequest().minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE)
                            .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            DelinquencyRangeResponse range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());

            // Second Range
            delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(60)
                    .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());
            final String classificationExpected = range.getClassification();
            log.info("Expected Delinquency Range classification after Disbursement {}", classificationExpected);

            PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                    .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
            assertNotNull(delinquencyBucketResponse);
            final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

            // Client and Loan account creation
            final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), null);
            assertNotNull(getLoanProductsProductResponse);
            log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());
            assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

            // Older date to have more than one overdue installment
            LocalDate transactionDate = bussinesLocalDate.minusMonths(2).minusDays(5);
            String operationDate = Utils.dateFormatter.format(transactionDate);

            // Create Loan Account
            final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), operationDate, null);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            log.info("Loan Delinquency Range after Disbursement {}", getLoansLoanIdResponse.getDelinquencyRange().getClassification());
            assertNotNull(getLoansLoanIdResponse);
            // First Loan Delinquency Classification after Disbursement command
            assertEquals(getLoansLoanIdResponse.getDelinquencyRange().getClassification(), classificationExpected);

            // Apply a repayment to get a full paid installment
            operationDate = Utils.dateFormatter.format(bussinesLocalDate);
            makeLoanRepayment(loanId, "repayment", operationDate, 2049.99);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            // The Loan Delinquency Classification after Repayment command must be null
            log.info("Loan Delinquency Range after Repayment {}", getLoansLoanIdResponse.getDelinquencyRange());
            assertNull(getLoansLoanIdResponse.getDelinquencyRange());

            transactionDate = bussinesLocalDate.minusDays(18);
            operationDate = Utils.dateFormatter.format(transactionDate);

            // Create and apply Charge for Specific Due Date
            final Long chargeId = createLoanSpecifiedDueDateCharge(30.0);
            assertNotNull(chargeId);
            final PostLoansLoanIdChargesResponse loanChargeResponse = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeId).amount(12.0).dueDate(operationDate).dateFormat(DATETIME_PATTERN).locale(LoanTestData.LOCALE));
            assertNotNull(loanChargeResponse.getResourceId());

            getLoansLoanIdResponse = getLoanDetails(loanId);

            log.info("Loan Delinquency Range after add Loan Charge {}", getLoansLoanIdResponse.getDelinquencyRange());
            assertNotNull(getLoansLoanIdResponse.getDelinquencyRange());
            // Evaluate a Delinquency Tag set after add charge to the Loan
            assertEquals(getLoansLoanIdResponse.getDelinquencyRange().getClassification(), classificationExpected);
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanClassificationRealtimeOlderLoan() {

        // Given
        ArrayList<Long> rangeIds = new ArrayList<>();
        // First Range
        PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                .createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(30).locale(LoanTestData.LOCALE)
                        .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        DelinquencyRangeResponse range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());
        final String classificationExpected02 = range.getClassification();
        log.info("Expected Delinquency Range classification after first repayment {}", classificationExpected02);

        // Second Range
        delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(31).maximumAgeDays(60)
                .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());
        final String classificationExpected01 = range.getClassification();
        log.info("Expected Delinquency Range classification after Disbursement {}", classificationExpected01);

        // Third Range
        delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(61).maximumAgeDays(90)
                .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());

        PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
        assertNotNull(delinquencyBucketResponse);
        final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

        // Client and Loan account creation
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), null);
        assertNotNull(getLoanProductsProductResponse);
        log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());
        assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate.minusDays(85);
        String operationDate = Utils.dateFormatter.format(transactionDate);

        // Create Loan Account
        final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), operationDate, null);

        GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        log.info("Loan Delinquency Range after Disbursement in null? {}", (getLoansLoanIdResponse.getDelinquencyRange() == null));
        assertNotNull(getLoansLoanIdResponse.getDelinquencyRange());
        log.info("Loan Delinquency Range after Disbursement {}", getLoansLoanIdResponse.getDelinquencyRange());
        // First Loan Delinquency Classification after Disbursement command
        assertEquals(getLoansLoanIdResponse.getDelinquencyRange().getClassification(), classificationExpected01);

        // Apply a repayment to get a first full paid installment
        transactionDate = todaysDate.minusDays(1);
        operationDate = Utils.dateFormatter.format(transactionDate);
        PostLoansLoanIdTransactionsResponse loansLoanIdTransactions = makeLoanRepayment(loanId, "repayment", operationDate, 1050.0);
        assertNotNull(loansLoanIdTransactions);
        log.info("Loan repayment transaction id {}", loansLoanIdTransactions.getResourceId());
        getLoansLoanIdResponse = getLoanDetails(loanId);
        log.info("Loan Delinquency Range after first Repayment {}", getLoansLoanIdResponse.getDelinquencyRange());
        assertNotNull(getLoansLoanIdResponse.getDelinquencyRange());
        // First Loan Delinquency Classification remains after Repayment because the installment is not fully paid
        assertEquals(getLoansLoanIdResponse.getDelinquencyRange().getClassification(), classificationExpected02);

        List<GetDelinquencyTagHistoryResponse> getDelinquencyTagsHistory = getLoanDelinquencyTags(loanId);
        assertNotNull(getDelinquencyTagsHistory);
        log.info("Delinquency Tag History items {}", getDelinquencyTagsHistory.size());
        log.info("Delinquency Tag Item with Lifted On {}", getDelinquencyTagsHistory.get(0).getLiftedOnDate());
        assertEquals(getDelinquencyTagsHistory.get(0).getAddedOnDate(), Utils.getLocalDateOfTenant());
        assertEquals(getDelinquencyTagsHistory.get(0).getLiftedOnDate(), Utils.getLocalDateOfTenant());
        assertEquals(getDelinquencyTagsHistory.get(0).getDelinquencyRange().getClassification(), classificationExpected01);
        log.info("Loan Id {} with Loan status {}", getLoansLoanIdResponse.getId(), getLoansLoanIdResponse.getStatus().getCode());

        // Apply a repayment to get a second full paid installment
        loansLoanIdTransactions = makeLoanRepayment(loanId, "repayment", operationDate, 1020.0);
        assertNotNull(loansLoanIdTransactions);
        log.info("Loan repayment transaction id {}", loansLoanIdTransactions.getResourceId());
        getLoansLoanIdResponse = getLoanDetails(loanId);
        log.info("Loan Delinquency Range after second Repayment {}", getLoansLoanIdResponse.getDelinquencyRange());
        assertNotNull(getLoansLoanIdResponse);
        // The Loan Delinquency Classification after Repayment command must be null
        assertNull(getLoansLoanIdResponse.getDelinquencyRange());

        getDelinquencyTagsHistory = getLoanDelinquencyTags(loanId);
        assertNotNull(getDelinquencyTagsHistory);
        log.info("Delinquency Tag History items {}", getDelinquencyTagsHistory.size());
        log.info("Delinquency Tag Item with Lifted On {}", getDelinquencyTagsHistory.get(1).getLiftedOnDate());
        assertEquals(getDelinquencyTagsHistory.get(1).getAddedOnDate(), Utils.getLocalDateOfTenant());
        assertEquals(getDelinquencyTagsHistory.get(1).getLiftedOnDate(), Utils.getLocalDateOfTenant());
        assertEquals(getDelinquencyTagsHistory.get(1).getDelinquencyRange().getClassification(), classificationExpected02);
        log.info("Loan Id {} with final Loan status {}", getLoansLoanIdResponse.getId(), getLoansLoanIdResponse.getStatus().getCode());
    }

    @Test
    public void testLoanClassificationRealtimeWithReversedRepayment() {
        // Given
        ArrayList<Long> rangeIds = new ArrayList<>();
        // First Range
        PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                .createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(30).locale(LoanTestData.LOCALE)
                        .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        DelinquencyRangeResponse range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());
        final String classificationExpected = range.getClassification();
        log.info("Expected Delinquency Range classification after first repayment {}", classificationExpected);

        PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
        assertNotNull(delinquencyBucketResponse);
        final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

        // Client and Loan account creation
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), null);
        assertNotNull(getLoanProductsProductResponse);
        log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());
        assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        log.info("Local date of Tenant: {}", todaysDate);

        // Older date to have more than one overdue installment
        final LocalDate transactionDate = todaysDate.minusDays(50);
        String operationDate = Utils.dateFormatter.format(transactionDate);

        // Create Loan Account
        final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), operationDate, null);

        GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);

        log.info("Loan Delinquency Range after Disbursement in null? {}", (getLoansLoanIdResponse.getDelinquencyRange() == null));
        assertNotNull(getLoansLoanIdResponse);
        assertNotNull(getLoansLoanIdResponse.getDelinquencyRange());
        log.info("Loan Delinquency Range after Disbursement {}", getLoansLoanIdResponse.getDelinquencyRange());
        // First Loan Delinquency Classification after Disbursement command
        assertEquals(getLoansLoanIdResponse.getDelinquencyRange().getClassification(), classificationExpected);

        // Apply a repayment to get a full paid installment
        operationDate = Utils.dateFormatter.format(todaysDate);
        PostLoansLoanIdTransactionsResponse loansLoanIdTransactions = makeLoanRepayment(loanId, "repayment", operationDate, 1050.0);
        assertNotNull(loansLoanIdTransactions);
        log.info("Loan repayment transaction id {}", loansLoanIdTransactions.getResourceId());
        getLoansLoanIdResponse = getLoanDetails(loanId);
        log.info("Loan Delinquency Range after Repayment {}", getLoansLoanIdResponse.getDelinquencyRange());
        // Loan Delinquency Classification removed after Repayment because the installment is fully paid
        assertNull(getLoansLoanIdResponse.getDelinquencyRange());

        List<GetDelinquencyTagHistoryResponse> getDelinquencyTagsHistory = getLoanDelinquencyTags(loanId);
        assertNotNull(getDelinquencyTagsHistory);
        log.info("Delinquency Tag History items {}", getDelinquencyTagsHistory.size());
        log.info("Delinquency Tag Item with Lifted On {}", getDelinquencyTagsHistory.get(0).getLiftedOnDate());
        assertEquals(getDelinquencyTagsHistory.get(0).getAddedOnDate(), Utils.getLocalDateOfTenant());
        assertEquals(getDelinquencyTagsHistory.get(0).getLiftedOnDate(), Utils.getLocalDateOfTenant());
        assertEquals(getDelinquencyTagsHistory.get(0).getDelinquencyRange().getClassification(), classificationExpected);
        log.info("Loan Id {} with Loan status {}", getLoansLoanIdResponse.getId(), getLoansLoanIdResponse.getStatus().getCode());

        // Reverse the Previous Loan Repayment
        PostLoansLoanIdTransactionsResponse loansLoanIdReverseTransactions = reverseLoanTransaction(loanId,
                loansLoanIdTransactions.getResourceId(), operationDate);
        assertNotNull(loansLoanIdReverseTransactions);
        log.info("Loan repayment reverse transaction id {}", loansLoanIdReverseTransactions.getResourceId());
        getLoansLoanIdResponse = getLoanDetails(loanId);
        log.info("Loan Delinquency Range after Reverse Repayment {}", getLoansLoanIdResponse.getDelinquencyRange());
        // Loan Delinquency Classification goes back after Repayment because the installment is not paid
        assertEquals(getLoansLoanIdResponse.getDelinquencyRange().getClassification(), classificationExpected);

        getDelinquencyTagsHistory = getLoanDelinquencyTags(loanId);
        assertNotNull(getDelinquencyTagsHistory);
        log.info("Delinquency Tag History items {}", getDelinquencyTagsHistory.size());
        log.info("Delinquency Tag Item with Lifted On {}", getDelinquencyTagsHistory.get(1).getLiftedOnDate());
        assertEquals(getDelinquencyTagsHistory.get(1).getAddedOnDate(), Utils.getLocalDateOfTenant());
        // Second record is open with liftedOn in null
        assertNull(getDelinquencyTagsHistory.get(1).getLiftedOnDate());
        assertEquals(getDelinquencyTagsHistory.get(1).getDelinquencyRange().getClassification(), classificationExpected);
        log.info("Loan Id {} with final Loan status {}", getLoansLoanIdResponse.getId(), getLoansLoanIdResponse.getStatus().getCode());
    }

    @Test
    public void testLoanClassificationJob() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            LocalDate businessDate = Utils.getLocalDateOfTenant();
            businessDate = businessDate.minusDays(37);
            log.info("Current date {}", businessDate);
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(businessDate));

            ArrayList<Long> rangeIds = new ArrayList<>();
            // First Range
            PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                    .createRange(new DelinquencyRangeRequest().minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE)
                            .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            DelinquencyRangeResponse range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());

            // Second Range
            delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(60)
                    .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());
            final String classificationExpected = range.getClassification();
            log.info("Expected Delinquency Range classification {}", classificationExpected);

            PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                    .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
            final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

            // Client and Loan account creation
            final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), null);
            assertNotNull(getLoanProductsProductResponse);
            log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());
            assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            // Older date to have more than one overdue installment
            final LocalDate transactionDate = todaysDate.minusDays(57);
            String operationDate = Utils.dateFormatter.format(transactionDate);

            // Create Loan Account
            final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), operationDate, null);

            // Run first time the Job
            final String jobName = "Loan Delinquency Classification";
            schedulerHelper.executeAndAwaitJob(jobName);

            // Get loan details expecting to have not a delinquency classification
            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            final DelinquencyRangeData firstTestCase = getLoansLoanIdResponse.getDelinquencyRange();
            log.info("Loan Delinquency Range is null {}", (firstTestCase == null));

            // Move the Business date to get older the loan and to have an overdue loan
            businessDate = businessDate.plusMonths(1);
            log.info("Current date {}", businessDate);
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(businessDate));
            // Run Second time the Job
            schedulerHelper.executeAndAwaitJob(jobName);

            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = getLoanDetails(loanId);

            final DelinquencyRangeData secondTestCase = getLoansLoanIdResponse.getDelinquencyRange();
            assertNotNull(secondTestCase);
            log.info("Loan Delinquency Range is {}", secondTestCase.getClassification());

            // Then
            assertNotNull(delinquencyBucketResponse);
            assertNotNull(getLoanProductsProductResponse);
            assertNull(firstTestCase);
            assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());
            assertEquals(secondTestCase.getClassification(), classificationExpected);

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanClassificationStepAsPartOfCOB() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            LocalDate bussinesLocalDate = Utils.getDateAsLocalDate("01 April 2012");
            log.info("Current date {}", bussinesLocalDate);
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(bussinesLocalDate));

            // Given
            ArrayList<Long> rangeIds = new ArrayList<>();
            // First Range
            PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                    .createRange(new DelinquencyRangeRequest().minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE)
                            .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            DelinquencyRangeResponse range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());
            final String classificationExpected = range.getClassification();
            // Second Range
            delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(60)
                    .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());

            PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                    .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
            final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

            // Client and Loan account creation
            final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), null);
            assertNotNull(getLoanProductsProductResponse);
            log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());
            assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

            // Older date to have more than one overdue installment
            final LocalDate transactionDate = bussinesLocalDate.minusDays(31);
            String operationDate = Utils.dateFormatter.format(transactionDate);

            // Create Loan Account
            final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), operationDate, null);

            // COB Step Validation
            final JobBusinessStepConfigData jobBusinessStepConfigData = businessStepHelper
                    .getConfiguredBusinessStepsByJobName(BusinessConfigurationApiTest.LOAN_JOB_NAME);
            assertNotNull(jobBusinessStepConfigData);
            assertEquals(BusinessConfigurationApiTest.LOAN_JOB_NAME, jobBusinessStepConfigData.getJobName());
            assertTrue(jobBusinessStepConfigData.getBusinessSteps().size() > 0);
            assertTrue(jobBusinessStepConfigData.getBusinessSteps().stream().anyMatch(
                    businessStep -> BusinessConfigurationApiTest.LOAN_DELINQUENCY_CLASSIFICATION.equals(businessStep.getStepName())));

            // Run the Loan inline COB Job
            executeInlineCOB(loanId);

            // Get loan details expecting to have not a delinquency classification
            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            final DelinquencyRangeData firstTestCase = getLoansLoanIdResponse.getDelinquencyRange();
            log.info("Loan Delinquency Range is null {}", (firstTestCase == null));

            // Move the Business date to get older the loan and to have an overdue loan
            bussinesLocalDate = bussinesLocalDate.plusDays(3);

            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(bussinesLocalDate));
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);

            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = getLoanDetails(loanId);
            final DelinquencyRangeData secondTestCase = getLoansLoanIdResponse.getDelinquencyRange();
            assertNotNull(secondTestCase);
            log.info("Loan Delinquency Range is {}", secondTestCase.getClassification());

            // Then
            assertNotNull(delinquencyBucketResponse);
            assertNotNull(getLoanProductsProductResponse);
            assertNull(firstTestCase);
            assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());
            assertEquals(secondTestCase.getClassification(), classificationExpected);
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanClassificationToValidateNegatives() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            LocalDate bussinesLocalDate = Utils.getDateAsLocalDate("01 January 2012");
            log.info("Current date {}", bussinesLocalDate);
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(bussinesLocalDate));

            // Given
            ArrayList<Long> rangeIds = new ArrayList<>();
            // First Range
            PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                    .createRange(new DelinquencyRangeRequest().minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE)
                            .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            DelinquencyRangeResponse range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());

            // Second Range
            delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(60)
                    .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());

            PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                    .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
            final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

            // Client and Loan account creation
            final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), 3);
            assertNotNull(getLoanProductsProductResponse);
            log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());
            assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

            // Older date to have more than one overdue installment
            final LocalDate transactionDate = bussinesLocalDate;
            String operationDate = Utils.dateFormatter.format(transactionDate);

            // Create Loan Account
            final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), operationDate, null);

            // Get loan details expecting to have a delinquency classification
            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            final DelinquencyRangeData firstTestCase = getLoansLoanIdResponse.getDelinquencyRange();
            log.info("Loan Delinquency Range is null {}", (firstTestCase == null));

            bussinesLocalDate = Utils.getDateAsLocalDate("31 January 2012");

            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(bussinesLocalDate));
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);

            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = getLoanDetails(loanId);

            GetLoansLoanIdDelinquencySummary getLoansLoanIdCollectionData = getLoansLoanIdResponse.getDelinquent();
            assertNotNull(getLoansLoanIdCollectionData);
            assertEquals(0, getLoansLoanIdCollectionData.getDelinquentDays());
            assertEquals(0, getLoansLoanIdCollectionData.getPastDueDays());

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanClassificationUsingAgeingArrears() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            LocalDate bussinesLocalDate = Utils.getDateAsLocalDate("01 January 2012");
            log.info("Current date {}", bussinesLocalDate);
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(bussinesLocalDate));

            // Given
            ArrayList<Long> rangeIds = new ArrayList<>();
            // First Range
            PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                    .createRange(new DelinquencyRangeRequest().minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE)
                            .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            DelinquencyRangeResponse range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());

            // Second Range
            delinquencyRangeResponse = delinquencyHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(60)
                    .locale(LoanTestData.LOCALE).classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            range = delinquencyHelper.getRange(delinquencyRangeResponse.getResourceId());

            PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                    .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
            final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

            // Client and Loan account creation
            final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), 3);
            assertNotNull(getLoanProductsProductResponse);
            log.info("Loan Product Arrears: {}", getLoanProductsProductResponse.getInArrearsTolerance());
            assertEquals(3, getLoanProductsProductResponse.getInArrearsTolerance());

            // Older date to have more than one overdue installment
            final LocalDate transactionDate = bussinesLocalDate;
            String operationDate = Utils.dateFormatter.format(transactionDate);

            // Create Loan Account
            final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), operationDate, 3);

            // Get loan details expecting to have a delinquency classification
            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            final DelinquencyRangeData firstTestCase = getLoansLoanIdResponse.getDelinquencyRange();
            log.info("Loan Delinquency Range is null {}", (firstTestCase == null));
            log.info("Loan Account Arrears {}", getLoansLoanIdResponse.getInArrearsTolerance());
            assertEquals(3, getLoansLoanIdResponse.getInArrearsTolerance());

            // Update the Loan Product
            updateLoanProductInArrearsTolerance(getLoanProductsProductResponse.getId(), 0);
            GetLoanProductsProductIdResponse loanProductsProductIdResponseUpd = retrieveLoanProduct(getLoanProductsProductResponse.getId());
            assertNotNull(loanProductsProductIdResponseUpd);
            log.info("Loan Product Arrears: {}", loanProductsProductIdResponseUpd.getInArrearsTolerance());
            assertEquals(0, loanProductsProductIdResponseUpd.getInArrearsTolerance());

            bussinesLocalDate = Utils.getDateAsLocalDate("31 January 2012");

            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(bussinesLocalDate));
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);

            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = getLoanDetails(loanId);

            GetLoansLoanIdDelinquencySummary getLoansLoanIdCollectionData = getLoansLoanIdResponse.getDelinquent();
            assertNotNull(getLoansLoanIdCollectionData);
            assertEquals(0, getLoansLoanIdCollectionData.getDelinquentDays());
            assertEquals(0, getLoansLoanIdCollectionData.getPastDueDays());

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testDelinquencyWithPauseLettingPauseExpire() {
        runAt("01 January 2012", () -> {
            Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

            final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucketId, 3);
            assertNotNull(getLoanProductsProductResponse);
            log.info("Loan Product Arrears: {}", getLoanProductsProductResponse.getInArrearsTolerance());
            assertEquals(3, getLoanProductsProductResponse.getInArrearsTolerance());

            final String operationDate = "01 January 2012";

            final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), operationDate, 3);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            final DelinquencyRangeData firstTestCase = getLoansLoanIdResponse.getDelinquencyRange();
            log.info("Loan Delinquency Range is null {}", (firstTestCase == null));
            log.info("Loan Account Arrears {}", getLoansLoanIdResponse.getInArrearsTolerance());
            assertEquals(3, getLoansLoanIdResponse.getInArrearsTolerance());

            updateBusinessDate("06 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 5);

            createLoanDelinquencyAction(loanId, PAUSE, "06 February 2012", "10 February 2012");

            updateBusinessDate("09 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 5);

            updateBusinessDate("12 March 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 2049.99, 36);
        });
    }

    @Test
    public void testDelinquencyWithPauseResumeBeforePauseExpires() {
        runAt("01 January 2012", () -> {
            Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

            final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucketId, 3);
            assertNotNull(getLoanProductsProductResponse);
            log.info("Loan Product Arrears: {}", getLoanProductsProductResponse.getInArrearsTolerance());
            assertEquals(3, getLoanProductsProductResponse.getInArrearsTolerance());

            final String operationDate = "01 January 2012";

            final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), operationDate, 3);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            final DelinquencyRangeData firstTestCase = getLoansLoanIdResponse.getDelinquencyRange();
            log.info("Loan Delinquency Range is null {}", (firstTestCase == null));
            log.info("Loan Account Arrears {}", getLoansLoanIdResponse.getInArrearsTolerance());
            assertEquals(3, getLoansLoanIdResponse.getInArrearsTolerance());

            updateBusinessDate("06 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 5);

            createLoanDelinquencyAction(loanId, PAUSE, "06 February 2012", "10 March 2012");

            updateBusinessDate("09 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 5);

            updateBusinessDate("10 February 2012");
            createLoanDelinquencyAction(loanId, RESUME, "10 February 2012");

            updateBusinessDate("12 March 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 2049.99, 36);
        });
    }

    @Test
    public void testDelinquencyWithMultiplePausePeriods() {
        runAt("01 January 2012", () -> {

            Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

            final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucketId, 3);
            assertNotNull(getLoanProductsProductResponse);
            log.info("Loan Product Arrears: {}", getLoanProductsProductResponse.getInArrearsTolerance());
            assertEquals(3, getLoanProductsProductResponse.getInArrearsTolerance());

            final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), "01 January 2012", 3);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            final DelinquencyRangeData firstTestCase = getLoansLoanIdResponse.getDelinquencyRange();
            log.info("Loan Delinquency Range is null {}", (firstTestCase == null));
            log.info("Loan Account Arrears {}", getLoansLoanIdResponse.getInArrearsTolerance());
            assertEquals(3, getLoansLoanIdResponse.getInArrearsTolerance());

            // delinquent days: 5
            updateBusinessDate("06 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 5);

            // Add delinquency pause on 06 February 2012
            createLoanDelinquencyAction(loanId, PAUSE, "06 February 2012", "10 March 2012");
            updateBusinessDate("09 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 5);

            // Add delinquency resume on 10 February 2012
            updateBusinessDate("10 February 2012");
            createLoanDelinquencyAction(loanId, RESUME, "10 February 2012");

            updateBusinessDate("13 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 8);

            // Add new pause on 13 February 2012
            createLoanDelinquencyAction(loanId, PAUSE, "13 February 2012", "18 February 2012");

            updateBusinessDate("23 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 13);

            // Add new pause on 23 February 2012
            createLoanDelinquencyAction(loanId, PAUSE, "23 February 2012", "28 February 2012");
            updateBusinessDate("25 February 2012");
            createLoanDelinquencyAction(loanId, RESUME, "25 February 2012");
            updateBusinessDate("12 March 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 2049.99, 29);
        });
    }

    @Test
    public void testDelinquencyWithMultiplePausePeriodsWithInstallmentLevelDelinquency() {
        runAt("01 January 2012", () -> {
            Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

            final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithInstallmentLevelDelinquency(
                    delinquencyBucketId, 3);
            assertNotNull(getLoanProductsProductResponse);
            log.info("Loan Product Arrears: {}", getLoanProductsProductResponse.getInArrearsTolerance());
            assertEquals(3, getLoanProductsProductResponse.getInArrearsTolerance());

            final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), "01 January 2012", 3);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            final DelinquencyRangeData firstTestCase = getLoansLoanIdResponse.getDelinquencyRange();
            log.info("Loan Delinquency Range is null {}", (firstTestCase == null));
            log.info("Loan Account Arrears {}", getLoansLoanIdResponse.getInArrearsTolerance());
            assertEquals(3, getLoansLoanIdResponse.getInArrearsTolerance());

            // delinquent days: 5
            updateBusinessDate("06 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 5);

            createLoanDelinquencyAction(loanId, PAUSE, "06 February 2012", "10 March 2012");

            updateBusinessDate("09 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 5);

            updateBusinessDate("10 February 2012");
            createLoanDelinquencyAction(loanId, RESUME, "10 February 2012");

            updateBusinessDate("13 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 8);

            createLoanDelinquencyAction(loanId, PAUSE, "13 February 2012", "18 February 2012");

            updateBusinessDate("23 February 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);
            verifyDelinquency(loanId, "01 February 2012", 1033.33, 13);

            createLoanDelinquencyAction(loanId, PAUSE, "23 February 2012", "28 February 2012");

            updateBusinessDate("25 February 2012");
            createLoanDelinquencyAction(loanId, RESUME, "25 February 2012");

            updateBusinessDate("14 March 2012");
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            GetLoansLoanIdDelinquencySummary delinquent = getLoansLoanIdResponse.getDelinquent();

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(Utils.getDoubleValue(delinquent.getDelinquentAmount())).as("Total delinquent amount").isEqualTo(2049.99);
            softly.assertThat(delinquent.getDelinquentDate()).as("Delinquent date").isEqualTo(LocalDate.of(2012, 2, 1));
            softly.assertThat(delinquent.getDelinquentDays()).as("Delinquent days").isEqualTo(31);

            // Installment-level delinquency is aggregated by range
            // Both installments (31 days and 13 days) fall into Range 2 (4-60 days)
            // So we expect 1 aggregated entry with total amount 2049.99
            softly.assertThat(delinquent.getInstallmentLevelDelinquency()).as("Installment level delinquency size").hasSize(1);

            if (delinquent.getInstallmentLevelDelinquency().size() >= 1) {
                GetLoansLoanIdLoanInstallmentLevelDelinquency rangeDelinquency = delinquent.getInstallmentLevelDelinquency().get(0);
                // This is the aggregated amount for all installments in Range 2 (4-60 days)
                softly.assertThat(rangeDelinquency.getDelinquentAmount().stripTrailingZeros())
                        .as("Range 2 (4-60 days) aggregated delinquent amount").isEqualByComparingTo(BigDecimal.valueOf(2049.99));
                softly.assertThat(rangeDelinquency.getMinimumAgeDays()).as("Range minimum days").isEqualTo(4);
                softly.assertThat(rangeDelinquency.getMaximumAgeDays()).as("Range maximum days").isEqualTo(60);
            }

            softly.assertAll();
        });
    }

    @Test
    public void testLoanClassificationOnlyForActiveLoan() {

        // Given
        ArrayList<Long> rangeIds = new ArrayList<>();
        // First Range
        PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                .createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(30).locale(LoanTestData.LOCALE)
                        .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
        assertNotNull(delinquencyBucketResponse);
        final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

        // Client and Loan account creation
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), null);
        assertNotNull(getLoanProductsProductResponse);

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate.minusDays(37);
        String operationDate = Utils.dateFormatter.format(transactionDate);

        // Create Loan Application
        final Long loanId = createLoanApplication(clientId, getLoanProductsProductResponse.getId(), operationDate, null);

        // Evaluate default delinquent values in No Active Loan
        GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        assertNotNull(getLoansLoanIdResponse.getDelinquent());
        assertEquals(0, getLoansLoanIdResponse.getDelinquent().getDelinquentDays());
        assertEquals(0.0, Utils.getDoubleValue(getLoansLoanIdResponse.getDelinquent().getDelinquentAmount()));

        // Loan Disbursement
        disburseLoanWithAmount(loanId, operationDate, PRINCIPAL_AMOUNT);
        // Evaluate default delinquent values in No Active Loan
        getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        assertNotNull(getLoansLoanIdResponse.getDelinquent());
        assertNotEquals(0, getLoansLoanIdResponse.getDelinquent().getDelinquentDays());
        assertNotEquals(0, Utils.getDoubleValue(getLoansLoanIdResponse.getDelinquent().getDelinquentAmount()));
    }

    @Test
    public void testLoanClassificationOnlyForActiveLoanWithCOB() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            final String operationDate = "01 January 2012";

            LocalDate bussinesLocalDate = Utils.getDateAsLocalDate(operationDate);
            log.info("Current date {}", bussinesLocalDate);
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(bussinesLocalDate));

            // Given
            ArrayList<Long> rangeIds = new ArrayList<>();
            // First Range
            PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                    .createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(30).locale(LoanTestData.LOCALE)
                            .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                    .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
            assertNotNull(delinquencyBucketResponse);
            final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

            // Client creation
            final Long clientId = createClient(operationDate);
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), null);
            assertNotNull(getLoanProductsProductResponse);

            // Create Loan Application
            final Long loanId = createLoanApplication(clientId, getLoanProductsProductResponse.getId(), operationDate, null);

            // run cob for business date 01 January 2012
            bussinesLocalDate = Utils.getDateAsLocalDate(operationDate);
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), dateTimeFormatter.format(bussinesLocalDate));
            // Run the Loan inline COB Job
            executeInlineCOB(loanId);

            // Loan delinquency data
            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            GetLoansLoanIdDelinquencySummary delinquent = getLoansLoanIdResponse.getDelinquent();
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(delinquent);
            assertEquals(0, delinquent.getDelinquentDays());
            assertEquals(0.0, Utils.getDoubleValue(delinquent.getDelinquentAmount()));

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanDelinquencyDataWithAmountPerPortions() {
        // Given
        ArrayList<Long> rangeIds = new ArrayList<>();
        // First Range
        PostDelinquencyRangeResponse delinquencyRangeResponse = delinquencyHelper
                .createRange(new DelinquencyRangeRequest().minimumAgeDays(4).maximumAgeDays(30).locale(LoanTestData.LOCALE)
                        .classification(Utils.randomStringGenerator("DLQ_R_", 10)));
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        PostDelinquencyBucketResponse delinquencyBucketResponse = delinquencyHelper
                .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));
        assertNotNull(delinquencyBucketResponse);
        final DelinquencyBucketResponse delinquencyBucket = delinquencyHelper.getBucket(delinquencyBucketResponse.getResourceId());

        // Client and Loan account creation
        final Long clientId = createClient();

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucket.getId(), null);
        assertNotNull(getLoanProductsProductResponse);
        log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());
        assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        log.info("Local date of Tenant: {}", todaysDate);

        // Older date to have more than one overdue installment
        final LocalDate transactionDate = todaysDate.minusDays(50);
        final String operationDate = Utils.dateFormatter.format(transactionDate);

        final Double amount = 2000.0;
        PostLoansRequest applicationRequest = applyLoanRequest(clientId, getLoanProductsProductResponse.getId(), operationDate, amount, 4);

        applicationRequest = applicationRequest.numberOfRepayments(5).loanTermFrequency(5)
                .loanTermFrequencyType(RepaymentFrequencyType.MONTHS).interestRatePerPeriod(BigDecimal.valueOf(12.3))
                .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY).repaymentEvery(1)
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS);

        final Long loanId = applyForLoan(applicationRequest);

        approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(amount)).dateFormat(DATETIME_PATTERN)
                .approvedOnDate(operationDate).locale(LoanTestData.LOCALE));

        disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(operationDate).dateFormat(DATETIME_PATTERN)
                .transactionAmount(BigDecimal.valueOf(amount)).locale(LoanTestData.LOCALE));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        log.info("Loan Delinquency Range after Disbursement {}", loanDetails.getDelinquencyRange().getClassification());
        assertNotNull(loanDetails.getDelinquent());
        log.info("Loan Delinquency Data {} {}", loanDetails.getDelinquent().getDelinquentPrincipal(),
                loanDetails.getDelinquent().getDelinquentInterest());
        assertNotNull(loanDetails.getDelinquent().getDelinquentPrincipal());
        assertEquals(new BigDecimal("312.95"), loanDetails.getDelinquent().getDelinquentPrincipal().stripTrailingZeros());
        assertNotNull(loanDetails.getDelinquent().getDelinquentInterest());
        assertEquals(new BigDecimal("246"), loanDetails.getDelinquent().getDelinquentInterest().stripTrailingZeros());

        // Apply a partial repayment to move only the interest
        PostLoansLoanIdTransactionsResponse loansLoanIdTransactions = makeLoanRepayment(loanId, "repayment", operationDate, 120.0);
        assertNotNull(loansLoanIdTransactions);
        log.info("Loan repayment transaction id {}", loansLoanIdTransactions.getResourceId());

        loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails.getDelinquent());
        assertNotNull(loanDetails.getDelinquencyRange().getClassification());
        assertEquals(new BigDecimal("312.95"), loanDetails.getDelinquent().getDelinquentPrincipal().stripTrailingZeros());
        assertEquals(new BigDecimal("126"), loanDetails.getDelinquent().getDelinquentInterest().stripTrailingZeros());

        // Apply a repayment to cover interest and part of the principal
        loansLoanIdTransactions = makeLoanRepayment(loanId, "repayment", operationDate, 330.72);
        assertNotNull(loansLoanIdTransactions);
        log.info("Loan repayment transaction id {}", loansLoanIdTransactions.getResourceId());

        loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails.getDelinquent());
        assertNotNull(loanDetails.getDelinquencyRange().getClassification());
        assertEquals(new BigDecimal("108.23"), loanDetails.getDelinquent().getDelinquentPrincipal().stripTrailingZeros());
        assertEquals(BigDecimal.ZERO, loanDetails.getDelinquent().getDelinquentInterest().stripTrailingZeros());

        // Apply a repayment to cover the remain principal
        loansLoanIdTransactions = makeLoanRepayment(loanId, "repayment", operationDate, 108.23);
        assertNotNull(loansLoanIdTransactions);
        log.info("Loan repayment transaction id {}", loansLoanIdTransactions.getResourceId());
        // Loan without Delinquency Classification
        loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails.getDelinquent());
        assertNull(loanDetails.getDelinquencyRange());
        assertEquals(BigDecimal.ZERO, loanDetails.getDelinquent().getDelinquentPrincipal().stripTrailingZeros());
        assertEquals(BigDecimal.ZERO, loanDetails.getDelinquent().getDelinquentInterest().stripTrailingZeros());

        // Undo the last repayment transaction we must to have pending the principal
        PostLoansLoanIdTransactionsResponse reverseRepayment = reverseLoanTransaction(loanId, loansLoanIdTransactions.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate(operationDate)
                        .transactionAmount(0.0).locale(LoanTestData.LOCALE));
        assertNotNull(reverseRepayment);
        loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails.getDelinquent());
        assertNotNull(loanDetails.getDelinquencyRange().getClassification());
        assertEquals(new BigDecimal("108.23"), loanDetails.getDelinquent().getDelinquentPrincipal().stripTrailingZeros());
        assertEquals(BigDecimal.ZERO, loanDetails.getDelinquent().getDelinquentInterest().stripTrailingZeros());
    }

    private void verifyDelinquency(Long loanId, String date, Double amount, int delinquentDays) {
        GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
        GetLoansLoanIdDelinquencySummary delinquent = getLoansLoanIdResponse.getDelinquent();
        assertEquals(amount, Utils.getDoubleValue(delinquent.getDelinquentAmount()));
        assertEquals(LocalDate.parse(date, dateTimeFormatter), delinquent.getDelinquentDate());
        assertEquals(delinquentDays, delinquent.getDelinquentDays());
    }

    private PostLoansDelinquencyActionResponse createLoanDelinquencyAction(Long loanId, DelinquencyAction action, String startDate,
            String endDate) {
        return loanHelper.createLoanDelinquencyAction(loanId, action.name(), startDate, endDate);
    }

    private PostLoansDelinquencyActionResponse createLoanDelinquencyAction(Long loanId, DelinquencyAction action, String startDate) {
        return loanHelper.createLoanDelinquencyAction(loanId, action.name(), startDate, null);
    }

    /**
     * Mirrors the legacy {@code LoanProductTestBuilder} defaults with 30-days months / 360-days years, as used by the
     * original RestAssured version of this test class.
     */
    private GetLoanProductsProductIdResponse createLoanProduct(final Long delinquencyBucketId, final Integer inArrearsTolerance) {
        final PostLoanProductsRequest request = baseDelinquencyLoanProductRequest(delinquencyBucketId, inArrearsTolerance)
                .daysInMonthType(DaysInMonthType.DAYS_30).daysInYearType(DaysInYearType.DAYS_360);
        return retrieveLoanProduct(createLoanProduct(request));
    }

    private GetLoanProductsProductIdResponse createLoanProductWithInstallmentLevelDelinquency(final Long delinquencyBucketId,
            final Integer inArrearsTolerance) {
        final PostLoanProductsRequest request = baseDelinquencyLoanProductRequest(delinquencyBucketId, inArrearsTolerance)
                .enableInstallmentLevelDelinquency(true);
        return retrieveLoanProduct(createLoanProduct(request));
    }

    private PostLoanProductsRequest baseDelinquencyLoanProductRequest(final Long delinquencyBucketId, final Integer inArrearsTolerance) {
        return new PostLoanProductsRequest().name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .currencyCode("USD")//
                .locale(LoanTestData.LOCALE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .principal(PRINCIPAL_AMOUNT)//
                .minPrincipal(1000.0)//
                .maxPrincipal(10000000.0)//
                .numberOfRepayments(5)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(2.0)//
                .interestRateFrequencyType(InterestRateFrequencyType.MONTHS)//
                .amortizationType(AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(InterestType.FLAT)//
                .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .inArrearsTolerance(inArrearsTolerance)//
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY)//
                .accountingRule(ACCOUNTING_RULE_NONE)//
                .isEqualAmortization(false)//
                .overdueDaysForNPA(5)//
                .daysInMonthType(DaysInMonthType.ACTUAL)//
                .daysInYearType(DaysInYearType.ACTUAL)//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.name())//
                .isInterestRecalculationEnabled(false)//
                .delinquencyBucketId(delinquencyBucketId);
    }

    private PutLoanProductsProductIdResponse updateLoanProductInArrearsTolerance(final Long productId, final Integer inArrearsTolerance) {
        return updateLoanProduct(productId, new PutLoanProductsProductIdRequest().inArrearsTolerance(inArrearsTolerance));
    }

    /**
     * Mirrors the legacy {@code LoanApplicationTestBuilder} application of the original test class: 10k principal over
     * 12 monthly repayments at 2% declining-balance interest with equal-principal amortization.
     */
    private Long createLoanApplication(final Long clientId, final Long loanProductId, final String operationDate,
            final Integer inArrearsTolerance) {
        final PostLoansRequest applicationRequest = new PostLoansRequest().clientId(clientId).productId(loanProductId)
                .principal(BigDecimal.valueOf(PRINCIPAL_AMOUNT)).loanTermFrequency(12).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                .numberOfRepayments(12).repaymentEvery(1).repaymentFrequencyType(RepaymentFrequencyType.MONTHS)
                .interestRatePerPeriod(BigDecimal.valueOf(2)).interestType(InterestType.DECLINING_BALANCE)
                .amortizationType(AmortizationType.EQUAL_PRINCIPAL)
                .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY).expectedDisbursementDate(operationDate).submittedOnDate(operationDate)
                .loanType("individual").dateFormat(DATETIME_PATTERN).locale(LoanTestData.LOCALE)
                .inArrearsTolerance(inArrearsTolerance == null ? null : BigDecimal.valueOf(inArrearsTolerance));
        final Long loanId = applyForLoan(applicationRequest);
        approveLoan(loanId, approveLoanRequest(PRINCIPAL_AMOUNT, operationDate));
        return loanId;
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String operationDate,
            final Integer inArrearsTolerance) {
        final Long loanId = createLoanApplication(clientId, loanProductId, operationDate, inArrearsTolerance);
        disburseLoanWithAmount(loanId, operationDate, PRINCIPAL_AMOUNT);
        return loanId;
    }

    /**
     * Detaches the delinquency bucket from a loan product. Needs a body carrying an explicit JSON null, which the model
     * on this module's test classpath cannot produce - see {@link LoanProductCommandsApi}.
     */
    private void clearDelinquencyBucket(final Long loanProductId) {
        ok(() -> FineractFeignClientHelper.getFineractFeignClient().create(LoanProductCommandsApi.class)
                .clearDelinquencyBucket(loanProductId, new LoanProductCommandsApi.ClearDelinquencyBucketRequest()));
    }
}
