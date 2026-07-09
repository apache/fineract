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
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

public class LoanAccrualTransactionOnChargeSubmittedDateTest extends FeignLoanTestBase {

    private static final String STRATEGY = "mifos-standard-strategy";
    private static final String CHARGE_ACCRUAL_DATE_SUBMITTED = "submitted-date";
    private static final String CHARGE_ACCRUAL_DATE_DUE = "due-date";

    @Test
    public void loanAccrualTransactionOnChargeSubmittedTest_Accrual_Accounting_Api() {
        try {
            enableSubmittedDateChargeAccrual(LocalDate.of(2023, 3, 3));
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(singleRepaymentAccrualProduct());
            final Long loanId = createSingleRepaymentLoan(clientId, loanProductId, loanExternalIdStr);

            addPenaltyCharge(loanId, "10 March 2023", 10.0);
            addFeeCharge(loanId, "14 March 2023", 10.0);

            runPeriodicAccrualAccounting("03 March 2023");
            checkAccrualTransaction(LocalDate.of(2023, 3, 3), 0.0, 10.0, 10.0, loanId);

            updateBusinessDate("04 March 2023");
            makeLoanRepayment(loanExternalIdStr, repaymentRequest("4 March 2023", 100.0));
            addFeeCharge(loanId, "21 March 2023", 10.0);

            runPeriodicAccrualAccounting("04 March 2023");
            checkAccrualTransaction(LocalDate.of(2023, 3, 4), 0.0, 10.0, 0.0, loanId);
        } finally {
            resetChargeAccrualConfig();
        }
    }

    @Test
    public void loanAccrualTransactionOnChargeSubmittedTest_Add_Periodic_Accrual_Transactions_Job() {
        try {
            enableSubmittedDateChargeAccrual(LocalDate.of(2023, 3, 3));
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(singleRepaymentAccrualProduct());
            final Long loanId = createSingleRepaymentLoan(clientId, loanProductId, loanExternalIdStr);

            addPenaltyCharge(loanId, "10 March 2023", 10.0);
            addFeeCharge(loanId, "14 March 2023", 10.0);

            final String jobName = "Add Periodic Accrual Transactions";
            schedulerHelper.executeAndAwaitJob(jobName);
            checkAccrualTransaction(LocalDate.of(2023, 3, 3), 0.0, 10.0, 10.0, loanId);

            updateBusinessDate("04 March 2023");
            makeLoanRepayment(loanExternalIdStr, repaymentRequest("4 March 2023", 100.0));
            addFeeCharge(loanId, "21 March 2023", 10.0);

            schedulerHelper.executeAndAwaitJob(jobName);
            checkAccrualTransaction(LocalDate.of(2023, 3, 4), 0.0, 10.0, 0.0, loanId);
        } finally {
            resetChargeAccrualConfig();
        }
    }

    @Test
    public void loanAccrualTransactionOnChargeSubmittedTest_Loan_COB_AddPeriodicAccrualEntriesBusinessStep() {
        try {
            LocalDate currentDate = LocalDate.of(2023, 3, 3);
            enableSubmittedDateChargeAccrual(currentDate);
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(singleRepaymentAccrualProduct());
            final Long loanId = createSingleRepaymentLoan(clientId, loanProductId, loanExternalIdStr);

            addPenaltyCharge(loanId, "10 March 2023", 10.0);
            addFeeCharge(loanId, "14 March 2023", 10.0);

            updateBusinessDate(Utils.dateFormatter.format(currentDate.plusDays(1)));
            final String jobName = "Loan COB";
            schedulerHelper.executeAndAwaitJob(jobName);
            checkAccrualTransaction(LocalDate.of(2023, 3, 3), 0.0, 10.0, 10.0, loanId);

            LocalDate futureDate = LocalDate.of(2023, 3, 4);
            updateBusinessDate("04 March 2023");
            makeLoanRepayment(loanExternalIdStr, repaymentRequest("4 March 2023", 100.0));
            addFeeCharge(loanId, "21 March 2023", 10.0);

            updateBusinessDate(Utils.dateFormatter.format(futureDate.plusDays(1)));
            schedulerHelper.executeAndAwaitJob(jobName);
            checkAccrualTransaction(futureDate, 0.0, 10.0, 0.0, loanId);
        } finally {
            resetChargeAccrualConfig();
        }
    }

    @Test
    public void loanAccrualTransactionOnChargeSubmittedTest_Add_Accrual_Transactions_Job() {
        try {
            enableSubmittedDateChargeAccrual(LocalDate.of(2023, 3, 3));
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(singleRepaymentAccrualProduct());
            final Long loanId = createSingleRepaymentLoan(clientId, loanProductId, loanExternalIdStr);

            addPenaltyCharge(loanId, "10 March 2023", 10.0);
            addFeeCharge(loanId, "14 March 2023", 10.0);

            final String jobName = "Add Accrual Transactions";
            schedulerHelper.executeAndAwaitJob(jobName);
            checkAccrualTransaction(LocalDate.of(2023, 3, 3), 0.0, 10.0, 10.0, loanId);

            updateBusinessDate("04 March 2023");
            makeLoanRepayment(loanExternalIdStr, repaymentRequest("4 March 2023", 100.0));
            addFeeCharge(loanId, "21 March 2023", 10.0);

            schedulerHelper.executeAndAwaitJob(jobName);
            checkAccrualTransaction(LocalDate.of(2023, 3, 4), 0.0, 10.0, 0.0, loanId);
        } finally {
            resetChargeAccrualConfig();
        }
    }

    @Test
    public void loanAccrualTransactionOnChargeSubmitted_With_Multiple_Repayments_Test_Add_Periodic_Accrual_Transactions_Job() {
        try {
            enableSubmittedDateChargeAccrual(LocalDate.of(2023, 3, 3));
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(multipleRepaymentsAccrualProduct());
            final Long loanId = createMultipleRepaymentsLoan(clientId, loanProductId, loanExternalIdStr, LoanTestData.InterestType.FLAT);

            // Due for future date in one of the schedule
            addPenaltyCharge(loanId, "10 March 2023", 10.0);
            // Due for future date in different of the schedule
            addPenaltyCharge(loanId, "17 March 2023", 10.0);

            final String jobName = "Add Periodic Accrual Transactions";
            schedulerHelper.executeAndAwaitJob(jobName);

            // verify multiple accrual transactions are created on charge created date according to repayment schedule
            // to which charge due date falls
            checkAccrualTransactionsForMultipleRepaymentSchedulesChargeDueDate(LocalDate.of(2023, 3, 3), loanId);
        } finally {
            resetChargeAccrualConfig();
        }
    }

    @Test
    public void loanAccrualTransactionOnChargeSubmitted_multiple_disbursement_reversal_test_Loan_COB() {
        try {
            LocalDate currentDate = LocalDate.of(2023, 3, 3);
            enableSubmittedDateChargeAccrual(currentDate);
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(multipleDisbursementsAccrualProduct());
            final Long loanId = createMultipleRepaymentsLoan(clientId, loanProductId, loanExternalIdStr,
                    LoanTestData.InterestType.DECLINING_BALANCE, false);

            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(1000.0, "03 March 2023"));

            addPenaltyCharge(loanId, "09 March 2023", 10.0);

            updateBusinessDate(Utils.dateFormatter.format(currentDate.plusDays(1)));
            final String jobName = "Loan COB";
            schedulerHelper.executeAndAwaitJob(jobName);
            checkAccrualTransaction(currentDate, 0.0, 0.0, 10.0, loanId);

            LocalDate futureDate = LocalDate.of(2023, 3, 4);
            updateBusinessDate("04 March 2023");
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(300.0, "04 March 2023"));

            // verify accrual transaction exists with same date,amount and is not reversed by regeneration of repayment
            // schedule
            checkAccrualTransaction(currentDate, 0.0, 0.0, 10.0, loanId);
        } finally {
            resetChargeAccrualConfig();
        }
    }

    @Test
    public void loanAccrualTransaction_ChargeSubmittedDate_AdjustRepaymentScheduleSnoozeFeeDueDateNewFeeAddTest() {
        try {
            enableSubmittedDateChargeAccrual(LocalDate.of(2023, 5, 19));
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(multipleRepaymentsAccrualProduct());
            final Long loanId = createLoanAccountAndDisburse(clientId, loanProductId, loanExternalIdStr);

            // set business date as date when fee charge is added/submitted
            LocalDate chargeSubmittedDate = LocalDate.of(2023, 6, 12);
            updateBusinessDate("12 June 2023");

            addFeeCharge(loanId, "18 July 2023", 10.0);

            // adjust loan schedule
            Long loanRescheduleRequestId = createRescheduleRequest(
                    LoanRequestBuilders.rescheduleRequest(loanId, "19 May 2023", "18 June 2023", "18 July 2023"));
            approveRescheduleRequest(loanRescheduleRequestId, LoanRequestBuilders.approveReschedule("19 May 2023"));

            // run inline cob for loan
            updateBusinessDate(Utils.dateFormatter.format(chargeSubmittedDate.plusDays(1)));
            executeInlineCOB(loanId);
            checkAccrualTransaction(chargeSubmittedDate, 0.0, 10.0, 0.0, loanId);

            updateBusinessDate("18 July 2023");
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    repaymentRequest("18 July 2023", 1010.0));

            LocalDate currentDate = LocalDate.of(2023, 7, 19);
            updateBusinessDate("19 July 2023");
            reverseLoanTransaction(loanId, repaymentTransaction.getResourceId(), "19 July 2023");

            addPenaltyCharge(loanId, "19 July 2023", 10.0);

            updateBusinessDate(Utils.dateFormatter.format(currentDate.plusDays(1)));
            executeInlineCOB(loanId);
            checkAccrualTransaction(currentDate, 0.0, 0.0, 10.0, loanId);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());

            List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
            assertNotNull(periods);
            verifyPeriodDates(periods);
        } finally {
            resetChargeAccrualConfig();
        }
    }

    private void enableSubmittedDateChargeAccrual(LocalDate businessDate) {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        updateBusinessDate(Utils.dateFormatter.format(businessDate));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                new PutGlobalConfigurationsRequest().stringValue(CHARGE_ACCRUAL_DATE_SUBMITTED));
    }

    private void resetChargeAccrualConfig() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                new PutGlobalConfigurationsRequest().stringValue(CHARGE_ACCRUAL_DATE_DUE));
    }

    private void addPenaltyCharge(Long loanId, String dueDate, double amount) {
        Long chargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(amount).getResourceId();
        assertNotNull(addChargesForLoan(loanId, loanCharge(chargeId, dueDate, amount)).getResourceId());
    }

    private void addFeeCharge(Long loanId, String dueDate, double amount) {
        Long chargeId = chargesHelper.createLoanSpecifiedDueDateCharge(amount).getResourceId();
        assertNotNull(addChargesForLoan(loanId, loanCharge(chargeId, dueDate, amount)).getResourceId());
    }

    private PostLoansLoanIdChargesRequest loanCharge(Long chargeId, String dueDate, double amount) {
        return new PostLoansLoanIdChargesRequest().chargeId(chargeId).dateFormat(DATETIME_PATTERN).locale("en").amount(amount)
                .dueDate(dueDate);
    }

    private PostLoansLoanIdTransactionsRequest repaymentRequest(String date, double amount) {
        return new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(date).locale("en")
                .transactionAmount(amount);
    }

    private PostLoanProductsRequest singleRepaymentAccrualProduct() {
        return create4Period1MonthLongWithoutInterestProduct(STRATEGY).numberOfRepayments(1).maxNumberOfRepayments(30);
    }

    private PostLoanProductsRequest multipleRepaymentsAccrualProduct() {
        return create4Period1MonthLongWithoutInterestProduct(STRATEGY).numberOfRepayments(1).maxNumberOfRepayments(30).repaymentEvery(1)
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS_L);
    }

    private PostLoanProductsRequest multipleDisbursementsAccrualProduct() {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().repaymentEvery(1)
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L).numberOfRepayments(1).maxNumberOfRepayments(30)
                .transactionProcessingStrategyCode(STRATEGY).loanScheduleType("CUMULATIVE").loanScheduleProcessingType(null)
                .paymentAllocation(null);
    }

    private Long createSingleRepaymentLoan(Long clientId, Long loanProductId, String externalId) {
        Long loanId = applyForLoan(loanRequest(clientId, loanProductId, externalId, 1, LoanTestData.RepaymentFrequencyType.MONTHS, 1, 1,
                LoanTestData.RepaymentFrequencyType.MONTHS, LoanTestData.InterestType.FLAT, "03 March 2023"));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "03 March 2023"));
        disburseLoanWithNetDisbursalAmount(loanId, "03 March 2023", "1000");
        return loanId;
    }

    private Long createMultipleRepaymentsLoan(Long clientId, Long loanProductId, String externalId, Integer interestType) {
        return createMultipleRepaymentsLoan(clientId, loanProductId, externalId, interestType, true);
    }

    private Long createMultipleRepaymentsLoan(Long clientId, Long loanProductId, String externalId, Integer interestType,
            boolean disburse) {
        Long loanId = applyForLoan(loanRequest(clientId, loanProductId, externalId, 30, LoanTestData.RepaymentFrequencyType.DAYS, 10, 3,
                LoanTestData.RepaymentFrequencyType.DAYS, interestType, "03 March 2023"));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "03 March 2023"));
        if (disburse) {
            disburseLoanWithNetDisbursalAmount(loanId, "03 March 2023", "1000");
        }
        return loanId;
    }

    private Long createLoanAccountAndDisburse(Long clientId, Long loanProductId, String externalId) {
        Long loanId = applyForLoan(loanRequest(clientId, loanProductId, externalId, 30, LoanTestData.RepaymentFrequencyType.DAYS, 1, 30,
                LoanTestData.RepaymentFrequencyType.DAYS, LoanTestData.InterestType.FLAT, "19 May 2023"));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "19 May 2023"));
        disburseLoanWithNetDisbursalAmount(loanId, "19 May 2023", "1000");
        return loanId;
    }

    private PostLoansRequest loanRequest(Long clientId, Long loanProductId, String externalId, int loanTermFrequency,
            Integer loanTermFrequencyType, int numberOfRepayments, int repaymentEvery, Integer repaymentFrequencyType, Integer interestType,
            String date) {
        return new PostLoansRequest().clientId(clientId).productId(loanProductId).externalId(externalId).principal(new BigDecimal("1000"))
                .loanTermFrequency(loanTermFrequency).loanTermFrequencyType(loanTermFrequencyType).numberOfRepayments(numberOfRepayments)
                .repaymentEvery(repaymentEvery).repaymentFrequencyType(repaymentFrequencyType).interestRatePerPeriod(BigDecimal.ZERO)
                .interestType(interestType)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).expectedDisbursementDate(date).submittedOnDate(date)
                .transactionProcessingStrategyCode(STRATEGY).loanType("individual").dateFormat(DATETIME_PATTERN).locale("en");
    }

    private void checkAccrualTransaction(final LocalDate transactionDate, final double interestPortion, final double feePortion,
            final double penaltyPortion, final Long loanId) {
        boolean isTransactionFound = false;
        for (GetLoansLoanIdTransactions transaction : getLoanDetails(loanId).getTransactions()) {
            if (Boolean.TRUE.equals(transaction.getType().getAccrual()) && transactionDate.equals(transaction.getDate())) {
                isTransactionFound = true;
                assertEquals(interestPortion, Utils.getDoubleValue(transaction.getInterestPortion()), "Mismatch in transaction amounts");
                assertEquals(feePortion, Utils.getDoubleValue(transaction.getFeeChargesPortion()), "Mismatch in transaction amounts");
                assertEquals(penaltyPortion, Utils.getDoubleValue(transaction.getPenaltyChargesPortion()),
                        "Mismatch in transaction amounts");
                break;
            }
        }
        assertTrue(isTransactionFound, "No Accrual entries are posted");
    }

    private void checkAccrualTransactionsForMultipleRepaymentSchedulesChargeDueDate(LocalDate transactionDate, Long loanId) {
        boolean isTransactionFound = false;
        for (GetLoansLoanIdTransactions transaction : getLoanDetails(loanId).getTransactions()) {
            if (Boolean.TRUE.equals(transaction.getType().getAccrual()) && transactionDate.equals(transaction.getDate())) {
                isTransactionFound = true;
                assertEquals(0.0, Utils.getDoubleValue(transaction.getInterestPortion()), "Mismatch in transaction amounts");
                assertEquals(0.0, Utils.getDoubleValue(transaction.getFeeChargesPortion()), "Mismatch in transaction amounts");
                assertEquals(10.0, Utils.getDoubleValue(transaction.getPenaltyChargesPortion()), "Mismatch in transaction amounts");
            }
        }
        assertTrue(isTransactionFound, "No Accrual entries are posted");
    }

    private void verifyPeriodDates(List<GetLoansLoanIdRepaymentPeriod> periods) {
        assertEquals(3, periods.size());
        assertEquals(LocalDate.of(2023, 5, 19), periods.get(1).getFromDate());
        assertEquals(LocalDate.of(2023, 7, 18), periods.get(1).getDueDate());
        assertEquals(LocalDate.of(2023, 7, 18), periods.get(2).getFromDate());
        assertEquals(LocalDate.of(2023, 7, 19), periods.get(2).getDueDate());
    }
}
