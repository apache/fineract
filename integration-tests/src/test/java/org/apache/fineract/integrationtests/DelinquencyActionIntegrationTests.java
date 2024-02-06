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

import static java.lang.Boolean.TRUE;
import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.PAUSE;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.RESUME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetDelinquencyActionsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencyPausePeriod;
import org.apache.fineract.client.models.GetLoansLoanIdLoanInstallmentLevelDelinquency;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansDelinquencyActionResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
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
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("14 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

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
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("14 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // Validate Loan Delinquency Pause Period on Loan
            validateLoanDelinquencyPausePeriods(loanId, pausePeriods("10 January 2023", "15 January 2023", true));

            // Create a Resume for the Loan for the current business date, it is still expected to be in pause
            loanTransactionHelper.createLoanDelinquencyAction(loanId, RESUME, "14 January 2023");

            // Validate Loan Delinquency Pause Period on Loan
            validateLoanDelinquencyPausePeriods(loanId, pausePeriods("10 January 2023", "14 January 2023", true));

            // Update business date to 15 January 2023
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("15 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

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
            PostLoansDelinquencyActionResponse response = loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE,
                    "27 January 2023", "15 February 2023");

            List<GetDelinquencyActionsResponse> loanDelinquencyActions = loanTransactionHelper.getLoanDelinquencyActions(loanId);
            Assertions.assertNotNull(loanDelinquencyActions);
            Assertions.assertEquals(1, loanDelinquencyActions.size());
            Assertions.assertEquals("PAUSE", loanDelinquencyActions.get(0).getAction());
            Assertions.assertEquals(LocalDate.parse("27 January 2023", dateTimeFormatter), loanDelinquencyActions.get(0).getStartDate());
            Assertions.assertEquals(LocalDate.parse("15 February 2023", dateTimeFormatter), loanDelinquencyActions.get(0).getEndDate());

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
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("05 November 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // Create Delinquency Pause for the Loan
            PostLoansDelinquencyActionResponse response = loanTransactionHelper.createLoanDelinquencyAction(loanId, PAUSE,
                    "16 November 2023", "25 November 2023");

            // run cob for business date 26 November
            final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("26 November 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));
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

    @AllArgsConstructor
    public static class InstallmentDelinquencyData {

        Integer minAgeDays;
        Integer maxAgeDays;
        BigDecimal delinquentAmount;
    }

}
