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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.PAUSE;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.RESUME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.client.models.GetDelinquencyActionsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencyPausePeriod;
import org.apache.fineract.client.models.GetLoansLoanIdLoanInstallmentLevelDelinquency;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansDelinquencyActionResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class DelinquencyActionIntegrationTests extends BaseLoanIntegrationTest {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);

    @Test
    public void testCreateAndReadPauseDelinquencyAction() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0, 2);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Create Delinquency Pause for the Loan
            PostLoansDelinquencyActionResponse response = loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE,
                    "10 January 2023", "15 January 2023");

            List<GetDelinquencyActionsResponse> loanDelinquencyActions = loanTransactionHelper.getLoanDelinquencyActions(loanId);
            Assertions.assertNotNull(loanDelinquencyActions);
            Assertions.assertEquals(1, loanDelinquencyActions.size());
            Assertions.assertEquals("PAUSE", loanDelinquencyActions.get(0).getAction());
            Assertions.assertEquals(LocalDate.parse("10 January 2023", dateTimeFormatter), loanDelinquencyActions.get(0).getStartDate());
            Assertions.assertEquals(LocalDate.parse("15 January 2023", dateTimeFormatter), loanDelinquencyActions.get(0).getEndDate());
        });
    }

    @Test
    public void testCreateAndReadPauseDelinquencyActionUsingExternalId() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Create external ID
            String externalId = UUID.randomUUID().toString();

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0, 2, req -> req.externalId(externalId));

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Create Delinquency Pause for the Loan
            PostLoansDelinquencyActionResponse response = loanTransactionHelper.createLoanDelinquencyAction(externalId, PAUSE,
                    "10 January 2023", "15 January 2023");

            List<GetDelinquencyActionsResponse> loanDelinquencyActions = loanTransactionHelper.getLoanDelinquencyActions(externalId);
            Assertions.assertNotNull(loanDelinquencyActions);
            Assertions.assertEquals(1, loanDelinquencyActions.size());
            Assertions.assertEquals("PAUSE", loanDelinquencyActions.get(0).getAction());
            Assertions.assertEquals(LocalDate.parse("10 January 2023", dateTimeFormatter), loanDelinquencyActions.get(0).getStartDate());
            Assertions.assertEquals(LocalDate.parse("15 January 2023", dateTimeFormatter), loanDelinquencyActions.get(0).getEndDate());
        });
    }

    @Test
    public void testCreatePauseAndResumeDelinquencyAction() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0, 2);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Create Delinquency Pause for the Loan
            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "10 January 2023", "15 January 2023");

            // Update business date
            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("14 January 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            // Create 2nd Delinquency Resume for the Loan
            loanTransactionHelper.createLoanDelinquencyAction(loanId, RESUME, "14 January 2023");

            List<GetDelinquencyActionsResponse> loanDelinquencyActions = loanTransactionHelper.getLoanDelinquencyActions(loanId);
            Assertions.assertNotNull(loanDelinquencyActions);
            Assertions.assertEquals(2, loanDelinquencyActions.size());

            Assertions.assertEquals("PAUSE", loanDelinquencyActions.get(0).getAction());
            Assertions.assertEquals(LocalDate.parse("10 January 2023", dateTimeFormatter), loanDelinquencyActions.get(0).getStartDate());
            Assertions.assertEquals(LocalDate.parse("15 January 2023", dateTimeFormatter), loanDelinquencyActions.get(0).getEndDate());

            Assertions.assertEquals("RESUME", loanDelinquencyActions.get(1).getAction());
            Assertions.assertEquals(LocalDate.parse("14 January 2023", dateTimeFormatter), loanDelinquencyActions.get(1).getStartDate());
        });
    }

    @Test
    public void testCreatePauseAndResumeDelinquencyActionWithStatusFlag() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0, 2);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Create Delinquency Pause for the Loan
            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "10 January 2023", "15 January 2023");

            // Update business date
            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("14 January 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            // Validate Loan Delinquency Pause Period on Loan
            validateLoanDelinquencyPausePeriods(loanId, pausePeriods("10 January 2023", "15 January 2023", true));

            // Create a Resume for the Loan for the current business date, it is still expected to be in pause
            loanTransactionHelper.createLoanDelinquencyAction(loanId, RESUME, "14 January 2023");

            // Validate Loan Delinquency Pause Period on Loan
            validateLoanDelinquencyPausePeriods(loanId, pausePeriods("10 January 2023", "14 January 2023", true));

            // Update business date to 15 January 2023
            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("15 January 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            // Validate Loan Delinquency Pause Period on Loan
            validateLoanDelinquencyPausePeriods(loanId, pausePeriods("10 January 2023", "14 January 2023", false));

            // Create a new pause action for the future
            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "20 January 2023", "25 January 2023");

            // Validate Loan Delinquency Pause Period on Loan
            validateLoanDelinquencyPausePeriods(loanId, //
                    pausePeriods("10 January 2023", "14 January 2023", false), //
                    pausePeriods("20 January 2023", "25 January 2023", false) //
            );
        });
    }

    @Test
    public void testValidationErrorIsThrownWhenCreatingPauseActionWithBackdatedStartDateBeforeDisbursement() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0, 2);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Create Delinquency Pause for the Loan before disbursement date
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "05 December 2022", "15 January 2023"));
            assertTrue(exception.getMessage().contains("Start date of pause period must be after first disbursal date"));
        });
    }

    @Test
    public void testCreateAndVerifyBackdatedPauseDelinquencyAction() {
        runAt("30 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "25 December 2022", 1500.0, 3,
                    req -> req.submittedOnDate("25 December 2022"));

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "25 December 2022");

            // Create Delinquency Pause for the Loan in the past
            PostLoansDelinquencyActionResponse response = loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE,
                    "28 January 2023", "15 February 2023");

            List<GetDelinquencyActionsResponse> loanDelinquencyActions = loanTransactionHelper.getLoanDelinquencyActions(loanId);
            Assertions.assertNotNull(loanDelinquencyActions);
            Assertions.assertEquals(1, loanDelinquencyActions.size());
            Assertions.assertEquals("PAUSE", loanDelinquencyActions.get(0).getAction());
            Assertions.assertEquals(LocalDate.parse("28 January 2023", dateTimeFormatter), loanDelinquencyActions.get(0).getStartDate());
            Assertions.assertEquals(LocalDate.parse("15 February 2023", dateTimeFormatter), loanDelinquencyActions.get(0).getEndDate());

            // Validate Active Delinquency Pause Period on Loan
            validateLoanDelinquencyPausePeriods(loanId, pausePeriods("28 January 2023", "15 February 2023", true));
        });
    }

    @Test
    public void testVerifyLoanDelinquencyRecalculationForBackdatedPauseDelinquencyAction() {
        runAt("30 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPaymentAndDelinquencyBucket(true, true, true, 3);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "25 December 2022", 1500.0, 3,
                    req -> req.submittedOnDate("25 December 2022"));

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "25 December 2022");

            // Loan delinquency data before backdated pause
            verifyLoanDelinquencyData(loanId, 6, new InstallmentDelinquencyData(4, 10, BigDecimal.valueOf(250.0)));

            // Create Delinquency Pause for the Loan in the past
            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "27 January 2023", "15 February 2023");

            List<GetDelinquencyActionsResponse> loanDelinquencyActions = loanTransactionHelper.getLoanDelinquencyActions(loanId);
            Assertions.assertNotNull(loanDelinquencyActions);
            Assertions.assertEquals(1, loanDelinquencyActions.size());
            Assertions.assertEquals("PAUSE", loanDelinquencyActions.getFirst().getAction());
            Assertions.assertEquals(LocalDate.parse("27 January 2023", dateTimeFormatter),
                    loanDelinquencyActions.getFirst().getStartDate());
            Assertions.assertEquals(LocalDate.parse("15 February 2023", dateTimeFormatter), loanDelinquencyActions.getFirst().getEndDate());

            // Loan delinquency data calculation after backdated pause
            verifyLoanDelinquencyData(loanId, 3, new InstallmentDelinquencyData(1, 3, BigDecimal.valueOf(250.0)));

            // Validate Active Delinquency Pause Period on Loan
            validateLoanDelinquencyPausePeriods(loanId, pausePeriods("27 January 2023", "15 February 2023", true));
        });
    }

    @Test
    public void testValidationErrorIsThrownWhenCreatingActionThatOverlaps() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0, 2);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Create Delinquency Pause for the Loan
            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "01 January 2023", "15 January 2023");

            // Create overlapping Delinquency Pause for the Loan
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "01 January 2023", "15 January 2023"));
            assertTrue(exception.getMessage().contains("Delinquency pause period cannot overlap with another pause period"));
        });
    }

    @Test
    public void testLoanAndInstallmentDelinquencyCalculationForCOBAfterPausePeriodEndTest() {
        runAt("01 November 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPaymentAndDelinquencyBucket(true, true, true, 0);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 November 2023", 1000.0, 3, req -> {
                req.submittedOnDate("01 November 2023");
                req.setLoanTermFrequency(45);
                req.setRepaymentEvery(15);
                req.setGraceOnArrearsAgeing(0);
            });

            // Partial Loan amount Disbursement
            disburseLoan(loanId, BigDecimal.valueOf(100.00), "01 November 2023");

            // Update business date
            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("05 November 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            // Create Delinquency Pause for the Loan
            PostLoansDelinquencyActionResponse response = loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE,
                    "16 November 2023", "25 November 2023");

            // run cob for business date 26 November
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("26 November 2023").dateFormat(DATETIME_PATTERN).locale("en"));
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));

            // Loan delinquency data
            verifyLoanDelinquencyData(loanId, 1, new InstallmentDelinquencyData(1, 3, BigDecimal.valueOf(25.0)));

            // Validate Delinquency Pause Period on Loan
            validateLoanDelinquencyPausePeriods(loanId, pausePeriods("16 November 2023", "25 November 2023", false));
        });
    }

    private void validateLoanDelinquencyPausePeriods(Long loanId, GetLoansLoanIdDelinquencyPausePeriod... pausePeriods) {
        GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        Assertions.assertNotNull(loan.getDelinquent());
        if (pausePeriods.length > 0) {
            Assertions.assertEquals(Arrays.asList(pausePeriods), loan.getDelinquent().getDelinquencyPausePeriods());
        } else {
            Assertions.assertNull(loan.getDelinquent().getDelinquencyPausePeriods());
        }
    }

    private GetLoansLoanIdDelinquencyPausePeriod pausePeriods(String startDate, String endDate, boolean active) {
        GetLoansLoanIdDelinquencyPausePeriod pausePeriod = new GetLoansLoanIdDelinquencyPausePeriod();
        pausePeriod.setActive(active);
        pausePeriod.setPausePeriodStart(LocalDate.parse(startDate, dateTimeFormatter));
        pausePeriod.setPausePeriodEnd(LocalDate.parse(endDate, dateTimeFormatter));
        return pausePeriod;
    }

    private void verifyLoanDelinquencyData(Long loanId, Integer loanLevelDelinquentDays,
            InstallmentDelinquencyData... expectedInstallmentLevelInstallmentDelinquencyData) {
        GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        Assertions.assertNotNull(loan.getDelinquent());
        List<GetLoansLoanIdLoanInstallmentLevelDelinquency> installmentLevelDelinquency = loan.getDelinquent()
                .getInstallmentLevelDelinquency();

        assertThat(loan.getDelinquent().getDelinquentDays()).isEqualTo(loanLevelDelinquentDays);

        assertThat(installmentLevelDelinquency.get(0).getMaximumAgeDays())
                .isEqualTo(expectedInstallmentLevelInstallmentDelinquencyData[0].maxAgeDays);
        assertThat(installmentLevelDelinquency.get(0).getMinimumAgeDays())
                .isEqualTo(expectedInstallmentLevelInstallmentDelinquencyData[0].minAgeDays);
        assertThat(installmentLevelDelinquency.get(0).getDelinquentAmount())
                .isEqualByComparingTo(expectedInstallmentLevelInstallmentDelinquencyData[0].delinquentAmount);

    }

    private Long createLoanProductWith25PctDownPayment(boolean autoDownPaymentEnabled, boolean multiDisburseEnabled) {
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(multiDisburseEnabled);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE);
        product.setEnableAutoRepaymentForDownPayment(autoDownPaymentEnabled);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());

        Long loanProductId = loanProductResponse.getResourceId();

        assertEquals(TRUE, getLoanProductsProductIdResponse.getEnableDownPayment());
        assertNotNull(getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment());
        assertEquals(0, getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment().compareTo(DOWN_PAYMENT_PERCENTAGE));
        assertEquals(autoDownPaymentEnabled, getLoanProductsProductIdResponse.getEnableAutoRepaymentForDownPayment());
        assertEquals(multiDisburseEnabled, getLoanProductsProductIdResponse.getMultiDisburseLoan());
        return loanProductId;
    }

    private Long createLoanProductWith25PctDownPaymentAndDelinquencyBucket(boolean autoDownPaymentEnabled, boolean multiDisburseEnabled,
            boolean installmentLevelDelinquencyEnabled, Integer graceOnArrearsAging) {
        // Create DelinquencyBuckets
        Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                Pair.of(1, 3), //
                Pair.of(4, 10), //
                Pair.of(11, 60), //
                Pair.of(61, null)//
        ));
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setDelinquencyBucketId(delinquencyBucketId.longValue());
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.setEnableDownPayment(true);
        product.setGraceOnArrearsAgeing(graceOnArrearsAging);

        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE);
        product.setEnableAutoRepaymentForDownPayment(autoDownPaymentEnabled);
        product.setEnableInstallmentLevelDelinquency(installmentLevelDelinquencyEnabled);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());

        Long loanProductId = loanProductResponse.getResourceId();

        assertEquals(TRUE, getLoanProductsProductIdResponse.getEnableDownPayment());
        assertNotNull(getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment());
        assertEquals(0, getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment().compareTo(DOWN_PAYMENT_PERCENTAGE));
        assertEquals(autoDownPaymentEnabled, getLoanProductsProductIdResponse.getEnableAutoRepaymentForDownPayment());
        assertEquals(multiDisburseEnabled, getLoanProductsProductIdResponse.getMultiDisburseLoan());
        return loanProductId;

    }

    private Long createLoanProductWithDelinquencyBucketNoDownPayment(boolean multiDisburseEnabled,
            boolean installmentLevelDelinquencyEnabled, Integer graceOnArrearsAging) {
        Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                Pair.of(1, 3), //
                Pair.of(4, 10), //
                Pair.of(11, 60), //
                Pair.of(61, null)//
        ));
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setDelinquencyBucketId(delinquencyBucketId.longValue());
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.setEnableDownPayment(false);
        product.setGraceOnArrearsAgeing(graceOnArrearsAging);
        product.setEnableInstallmentLevelDelinquency(installmentLevelDelinquencyEnabled);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        return loanProductResponse.getResourceId();
    }

    @Test
    public void testDelinquentDaysAndDateAfterPastDelinquencyPause() {
        final Long[] loanIdHolder = new Long[1];

        runAt("01 January 2022", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWith25PctDownPaymentAndDelinquencyBucket(true, true, false, 0);
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2022", 1000.0, 2, req -> {
                req.setLoanTermFrequency(30);
                req.setRepaymentEvery(15);
                req.setGraceOnArrearsAgeing(0);
            });
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2022");
            loanIdHolder[0] = loanId;

            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "20 January 2022", "30 January 2022");
        });

        runAt("02 February 2022", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            assertNotNull(loanDetails.getDelinquent(), "Delinquent data should not be null");

            Integer pastDueDays = loanDetails.getDelinquent().getPastDueDays();
            assertNotNull(pastDueDays, "Past due days should not be null");
            assertEquals(17, pastDueDays, "Past due days should be 17 (16 Jan due date to 02 Feb business date)");

            Integer delinquentDays = loanDetails.getDelinquent().getDelinquentDays();
            assertNotNull(delinquentDays, "Delinquent days should not be null");
            assertEquals(7, delinquentDays, "Delinquent days should be 7 (17 past due days - 10 paused days = 7)");

            LocalDate delinquentDate = loanDetails.getDelinquent().getDelinquentDate();
            assertNotNull(delinquentDate, "Delinquent date should not be null");
            assertEquals(LocalDate.parse("16 January 2022", dateTimeFormatter), delinquentDate,
                    "Delinquent date should be 16 Jan 2022 (first installment due date, NOT adjusted for pause)");

            List<GetLoansLoanIdDelinquencyPausePeriod> pausePeriods = loanDetails.getDelinquent().getDelinquencyPausePeriods();
            assertNotNull(pausePeriods);
            assertEquals(1, pausePeriods.size());
            assertEquals(LocalDate.parse("20 January 2022", dateTimeFormatter), pausePeriods.get(0).getPausePeriodStart());
            assertEquals(LocalDate.parse("30 January 2022", dateTimeFormatter), pausePeriods.get(0).getPausePeriodEnd());
            assertEquals(FALSE, pausePeriods.get(0).getActive());
        });
    }

    @Test
    public void testInstallmentLevelDelinquencyWithMultipleOverdueInstallments() {
        final Long[] loanIdHolder = new Long[1];

        runAt("01 January 2022", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWith25PctDownPaymentAndDelinquencyBucket(true, true, true, 0);
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2022", 1000.0, 3, req -> {
                req.setLoanTermFrequency(45);
                req.setRepaymentEvery(15);
                req.setGraceOnArrearsAgeing(0);
            });
            disburseLoan(loanId, BigDecimal.valueOf(100.00), "01 January 2022");
            loanIdHolder[0] = loanId;

            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("05 January 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "20 January 2022", "30 January 2022");
        });

        runAt("02 March 2022", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            assertNotNull(loanDetails.getDelinquent(), "Loan delinquent data should not be null");

            Integer loanLevelPastDueDays = loanDetails.getDelinquent().getPastDueDays();
            assertEquals(45, loanLevelPastDueDays, "Loan level past due days should be 45 (16 Jan to 02 Mar)");

            Integer loanLevelDelinquentDays = loanDetails.getDelinquent().getDelinquentDays();
            assertEquals(35, loanLevelDelinquentDays, "Loan level delinquent days should be 35 (45 past due days - 10 paused days = 35)");

            LocalDate loanLevelDelinquentDate = loanDetails.getDelinquent().getDelinquentDate();
            assertEquals(LocalDate.parse("16 January 2022", dateTimeFormatter), loanLevelDelinquentDate,
                    "Loan level delinquent date should be 16 Jan 2022 (first installment due date)");

            Map<String, BigDecimal> expectedTotals = calculateExpectedBucketTotals(loanDetails,
                    LocalDate.parse("02 March 2022", dateTimeFormatter));
            assertTrue(expectedTotals.containsKey("11-60"), "Expected 11-60 bucket to contain delinquent installments");
            assertInstallmentDelinquencyBuckets(loanDetails, LocalDate.parse("02 March 2022", dateTimeFormatter), expectedTotals);
        });
    }

    @Test
    public void testInstallmentDelinquencyWithSinglePauseAffectingMultipleInstallments() {
        final Long[] loanIdHolder = new Long[1];

        runAt("10 January 2022", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWith25PctDownPaymentAndDelinquencyBucket(true, true, true, 0);
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "10 January 2022", 1000.0, 3, req -> {
                req.setLoanTermFrequency(30);
                req.setRepaymentEvery(10);
                req.setGraceOnArrearsAgeing(0);
            });
            disburseLoan(loanId, BigDecimal.valueOf(100.00), "10 January 2022");
            loanIdHolder[0] = loanId;

            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("14 January 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "15 January 2022", "25 January 2022");
        });

        runAt("05 February 2022", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertNotNull(loanDetails.getDelinquent(), "Loan delinquent data should not be null");

            List<GetLoansLoanIdLoanInstallmentLevelDelinquency> delinquencies = loanDetails.getDelinquent()
                    .getInstallmentLevelDelinquency();
            assertNotNull(delinquencies, "Installment level delinquency should not be null");

            Map<String, BigDecimal> actualTotals = new HashMap<>();
            for (GetLoansLoanIdLoanInstallmentLevelDelinquency delinquency : delinquencies) {
                String bucketKey = formatBucketKey(delinquency.getMinimumAgeDays(), delinquency.getMaximumAgeDays());
                actualTotals.merge(bucketKey, delinquency.getDelinquentAmount(), BigDecimal::add);
            }

            assertEquals(2, actualTotals.size(), "Should have 2 delinquency buckets");
            assertTrue(actualTotals.containsKey("4-10"), "Should have 4-10 bucket");
            assertTrue(actualTotals.containsKey("11-60"), "Should have 11-60 bucket");
            assertEquals(0, BigDecimal.valueOf(25.0).compareTo(actualTotals.get("4-10")), "4-10 bucket should have 25.0");
            assertEquals(0, BigDecimal.valueOf(25.0).compareTo(actualTotals.get("11-60")), "11-60 bucket should have 25.0");
        });
    }

    @Test
    public void testInstallmentDelinquencyWithMultiplePausesAffectingSameInstallment() {
        final Long[] loanIdHolder = new Long[1];

        runAt("01 January 2022", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWith25PctDownPaymentAndDelinquencyBucket(true, true, true, 0);
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2022", 1000.0, 1, req -> {
                req.setLoanTermFrequency(30);
                req.setRepaymentEvery(30);
                req.setGraceOnArrearsAgeing(0);
            });
            disburseLoan(loanId, BigDecimal.valueOf(100.00), "01 January 2022");
            loanIdHolder[0] = loanId;
        });

        runAt("04 February 2022", () -> {
            Long loanId = loanIdHolder[0];
            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "04 February 2022", "09 February 2022");
        });

        runAt("15 February 2022", () -> {
            Long loanId = loanIdHolder[0];
            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "15 February 2022", "20 February 2022");
        });

        runAt("01 March 2022", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertNotNull(loanDetails.getDelinquent(), "Loan delinquent data should not be null");

            LocalDate businessDate = LocalDate.parse("01 March 2022", dateTimeFormatter);
            LocalDate installmentDueDate = loanDetails.getDelinquent().getDelinquentDate();

            Integer loanLevelPastDueDays = loanDetails.getDelinquent().getPastDueDays();
            long expectedPastDueDays = ChronoUnit.DAYS.between(installmentDueDate, businessDate);
            assertEquals((int) expectedPastDueDays, loanLevelPastDueDays,
                    "Loan level past due days should match the business date minus the first installment due date");

            Integer loanLevelDelinquentDays = loanDetails.getDelinquent().getDelinquentDays();
            long expectedDelinquentDays = Math.max(expectedPastDueDays - 10, 0);
            assertEquals((int) expectedDelinquentDays, loanLevelDelinquentDays,
                    "Loan level delinquent days should subtract both five-day pause periods from the past due days");

            LocalDate loanLevelDelinquentDate = loanDetails.getDelinquent().getDelinquentDate();
            assertEquals(installmentDueDate, loanLevelDelinquentDate, "Loan level delinquent date should equal the installment due date");

            List<GetLoansLoanIdLoanInstallmentLevelDelinquency> delinquencies = loanDetails.getDelinquent()
                    .getInstallmentLevelDelinquency();
            assertNotNull(delinquencies, "Installment level delinquency should not be null");

            Map<String, BigDecimal> actualTotals = new HashMap<>();
            for (GetLoansLoanIdLoanInstallmentLevelDelinquency delinquency : delinquencies) {
                String bucketKey = formatBucketKey(delinquency.getMinimumAgeDays(), delinquency.getMaximumAgeDays());
                actualTotals.merge(bucketKey, delinquency.getDelinquentAmount(), BigDecimal::add);
            }

            assertEquals(1, actualTotals.size(), "Should have 1 delinquency bucket");
            assertTrue(actualTotals.containsKey("11-60"), "Should have 11-60 bucket");
            assertEquals(0, BigDecimal.valueOf(75.0).compareTo(actualTotals.get("11-60")), "11-60 bucket should have 75.0");
        });
    }

    @Test
    public void testInstallmentDelinquencyWithPauseBetweenSequentialInstallments() {
        final Long[] loanIdHolder = new Long[1];

        runAt("01 January 2022", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWith25PctDownPaymentAndDelinquencyBucket(true, true, true, 0);
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2022", 1000.0, 2, req -> {
                req.setLoanTermFrequency(20);
                req.setRepaymentEvery(10);
                req.setGraceOnArrearsAgeing(0);
            });
            disburseLoan(loanId, BigDecimal.valueOf(100.00), "01 January 2022");
            loanIdHolder[0] = loanId;

            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("02 January 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "03 January 2022", "10 January 2022");
        });

        runAt("12 January 2022", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertNotNull(loanDetails.getDelinquent(), "Loan delinquent data should not be null");

            Map<String, BigDecimal> expectedTotals = calculateExpectedBucketTotals(loanDetails,
                    LocalDate.parse("12 January 2022", dateTimeFormatter));
            assertInstallmentDelinquencyBuckets(loanDetails, LocalDate.parse("12 January 2022", dateTimeFormatter), expectedTotals);
        });
    }

    @Test
    public void testInstallmentDelinquencyWithFourInstallmentsAndPausePeriod() {
        final Long[] loanIdHolder = new Long[1];

        runAt("01 January 2022", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWith25PctDownPaymentAndDelinquencyBucket(true, true, true, 0);
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2022", 1000.0, 4, req -> {
                req.setLoanTermFrequency(60);
                req.setRepaymentEvery(15);
                req.setGraceOnArrearsAgeing(0);
            });
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2022");
            loanIdHolder[0] = loanId;

            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("01 January 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "02 January 2022", "20 January 2022");
        });

        runAt("01 March 2022", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertNotNull(loanDetails.getDelinquent(), "Loan delinquent data should not be null");

            Map<String, BigDecimal> expectedTotals = calculateExpectedBucketTotals(loanDetails,
                    LocalDate.parse("01 March 2022", dateTimeFormatter));
            assertInstallmentDelinquencyBuckets(loanDetails, LocalDate.parse("01 March 2022", dateTimeFormatter), expectedTotals);
        });
    }

    @Test
    public void testPauseUsesBusinessDateNotCOBDate() {
        final Long[] loanIdHolder = new Long[1];

        runAt("28 May 2025", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithDelinquencyBucketNoDownPayment(true, true, 3);
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "28 May 2025", 1000.0, 7, req -> {
                req.setLoanTermFrequency(210);
                req.setRepaymentEvery(30);
                req.setGraceOnArrearsAgeing(3);
            });
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "28 May 2025");
            loanIdHolder[0] = loanId;
        });

        runAt("15 June 2025", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE, "17 June 2025", "19 August 2025");
        });

        runAt("01 July 2025", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
        });

        runAt("01 August 2025", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
        });

        runAt("01 September 2025", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
        });

        runAt("01 October 2025", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
        });

        runAt("31 October 2025", () -> {
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            Long loanId = loanIdHolder[0];
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertNotNull(loanDetails.getDelinquent(), "Loan delinquent data should not be null");

            Integer loanLevelPastDueDays = loanDetails.getDelinquent().getPastDueDays();
            assertEquals(126, loanLevelPastDueDays,
                    "Loan level past due days should be 126 (June 27 to Oct 31) - First installment due June 27 (30 days after May 28)");

            Integer loanLevelDelinquentDays = loanDetails.getDelinquent().getDelinquentDays();
            assertEquals(70, loanLevelDelinquentDays,
                    "Loan level delinquent days should be 70 (125 overdue days from June 28 to Oct 31, minus 52 paused days from June 28 to Aug 19, minus 3 grace)");

            LocalDate loanLevelDelinquentDate = loanDetails.getDelinquent().getDelinquentDate();
            assertEquals(LocalDate.parse("30 June 2025", dateTimeFormatter), loanLevelDelinquentDate,
                    "Loan level delinquent date should be June 30, 2025 (first installment due June 27 + 3 days grace)");

            Map<String, BigDecimal> expectedTotals = calculateExpectedBucketTotals(loanDetails,
                    LocalDate.parse("31 October 2025", dateTimeFormatter));
            assertInstallmentDelinquencyBuckets(loanDetails, LocalDate.parse("31 October 2025", dateTimeFormatter), expectedTotals);
        });
    }

    @AllArgsConstructor
    public static class InstallmentDelinquencyData {

        Integer minAgeDays;
        Integer maxAgeDays;
        BigDecimal delinquentAmount;
    }

    private void assertInstallmentDelinquencyBuckets(GetLoansLoanIdResponse loanDetails, LocalDate businessDate,
            Map<String, BigDecimal> expectedBucketTotals) {
        SoftAssertions softly = new SoftAssertions();

        List<GetLoansLoanIdLoanInstallmentLevelDelinquency> delinquencies = loanDetails.getDelinquent().getInstallmentLevelDelinquency();
        softly.assertThat(delinquencies).as("Installment level delinquency should not be null").isNotNull();

        Map<String, BigDecimal> calculatedTotals = calculateExpectedBucketTotals(loanDetails, businessDate);
        Map<String, BigDecimal> actualTotals = new HashMap<>();
        for (GetLoansLoanIdLoanInstallmentLevelDelinquency delinquency : delinquencies) {
            String bucketKey = formatBucketKey(delinquency.getMinimumAgeDays(), delinquency.getMaximumAgeDays());
            actualTotals.merge(bucketKey, delinquency.getDelinquentAmount(), BigDecimal::add);
        }

        softly.assertThat(actualTotals.keySet()).as("Unexpected delinquency bucket set").isEqualTo(calculatedTotals.keySet());

        calculatedTotals.forEach((bucket, expectedAmount) -> {
            BigDecimal actualAmount = actualTotals.get(bucket);
            softly.assertThat(actualAmount).as("Missing delinquency bucket " + bucket).isNotNull();
            softly.assertThat(actualAmount.setScale(2, RoundingMode.HALF_DOWN)).as("Unexpected delinquent amount for bucket " + bucket)
                    .isEqualByComparingTo(expectedAmount.setScale(2, RoundingMode.HALF_DOWN));
        });

        if (expectedBucketTotals != null) {
            expectedBucketTotals.forEach((bucket, amount) -> {
                BigDecimal calculated = calculatedTotals.get(bucket);
                softly.assertThat(calculated).as("Expected bucket " + bucket + " not present in calculated totals").isNotNull();
                softly.assertThat(calculated.setScale(2, RoundingMode.HALF_DOWN))
                        .as("Calculated delinquent amount did not match expectation for bucket " + bucket)
                        .isEqualByComparingTo(amount.setScale(2, RoundingMode.HALF_DOWN));
            });
        }

        BigDecimal loanLevelAmount = loanDetails.getDelinquent().getDelinquentAmount();
        if (loanLevelAmount != null) {
            BigDecimal actualSum = actualTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            softly.assertThat(actualSum.setScale(2, RoundingMode.HALF_DOWN))
                    .as("Installment bucket totals should sum to the loan level delinquent amount")
                    .isEqualByComparingTo(loanLevelAmount.setScale(2, RoundingMode.HALF_DOWN));
        }

        softly.assertAll();
    }

    private Map<String, BigDecimal> calculateExpectedBucketTotals(GetLoansLoanIdResponse loanDetails, LocalDate businessDate) {
        Map<String, BigDecimal> totals = new HashMap<>();
        List<GetLoansLoanIdDelinquencyPausePeriod> pauses = loanDetails.getDelinquent().getDelinquencyPausePeriods();

        for (GetLoansLoanIdRepaymentPeriod period : loanDetails.getRepaymentSchedule().getPeriods()) {
            if (Boolean.TRUE.equals(period.getDownPaymentPeriod())) {
                continue;
            }
            LocalDate dueDate = period.getDueDate();
            if (dueDate == null || !dueDate.isBefore(businessDate)) {
                continue;
            }
            BigDecimal outstanding = period.getTotalOutstandingForPeriod();
            if (outstanding == null || outstanding.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            long pastDueDays = ChronoUnit.DAYS.between(dueDate, businessDate);
            if (pastDueDays <= 0) {
                continue;
            }

            long pausedDays = 0L;
            if (pauses != null) {
                for (GetLoansLoanIdDelinquencyPausePeriod pause : pauses) {
                    LocalDate pauseStart = pause.getPausePeriodStart();
                    LocalDate pauseEnd = pause.getPausePeriodEnd() != null ? pause.getPausePeriodEnd() : businessDate;
                    if (pauseStart == null || !pauseEnd.isAfter(pauseStart)) {
                        continue;
                    }
                    LocalDate overlapStart = pauseStart.isAfter(dueDate) ? pauseStart : dueDate;
                    LocalDate overlapEnd = pauseEnd.isBefore(businessDate) ? pauseEnd : businessDate;
                    if (overlapEnd.isAfter(overlapStart)) {
                        pausedDays += ChronoUnit.DAYS.between(overlapStart, overlapEnd);
                    }
                }
            }

            long delinquentDays = pastDueDays - pausedDays;
            if (delinquentDays <= 0) {
                continue;
            }

            String bucket = formatBucketKeyForDays(delinquentDays);
            totals.merge(bucket, outstanding, BigDecimal::add);
        }
        return totals;
    }

    private String formatBucketKey(Integer minAgeDays, Integer maxAgeDays) {
        if (minAgeDays == null) {
            return "0";
        }
        if (maxAgeDays == null) {
            return minAgeDays + "+";
        }
        return minAgeDays + "-" + maxAgeDays;
    }

    private String formatBucketKeyForDays(long delinquentDays) {
        if (delinquentDays >= 1 && delinquentDays <= 3) {
            return "1-3";
        } else if (delinquentDays >= 4 && delinquentDays <= 10) {
            return "4-10";
        } else if (delinquentDays >= 11 && delinquentDays <= 60) {
            return "11-60";
        } else if (delinquentDays >= 61) {
            return "61+";
        }
        return "0";
    }

}
