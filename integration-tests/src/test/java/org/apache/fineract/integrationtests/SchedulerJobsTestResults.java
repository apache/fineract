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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetHolidaysResponse;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.GetStandingInstructionHistoryPageItemsResponse;
import org.apache.fineract.client.models.GetStandingInstructionsStandingInstructionIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostFixedDepositAccountsRequest;
import org.apache.fineract.client.models.PostFixedDepositProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutJobsJobIDRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFixedDepositHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFixedDepositProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsChargeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.HolidayHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.StandingInstructionsHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@Order(1)
@TestMethodOrder(MethodName.class)
public class SchedulerJobsTestResults extends FeignLoanTestBase {

    private static final String FROM_ACCOUNT_TYPE_SAVINGS = "2";
    private static final String TO_ACCOUNT_TYPE_SAVINGS = "2";
    private static final String DATE_OF_JOINING = "01 January 2011";
    private static final String TRANSACTION_DATE = "01 March 2013";
    public static final String LOAN_APPROVAL_DATE = "01 March 2013";
    public static final String LOAN_APPROVAL_DATE_PLUS_ONE = "02 March 2013";
    public static final String LOAN_DISBURSAL_DATE = "01 March 2013";
    private static final String MINIMUM_OPENING_BALANCE = "1000";
    private static final BigDecimal SP_BALANCE = new BigDecimal(MINIMUM_OPENING_BALANCE);
    private static final String SAVINGS_SUBMITTED_DATE = "08 January 2013";
    private static final String SAVINGS_APPROVED_DATE = "09 January 2013";
    private static final String SAVINGS_ACTIVATED_DATE = "01 March 2013";
    private static final String LOAN_LOCALE = "en_GB";
    private static final String LOAN_MAX_OUTSTANDING_BALANCE = "36000";
    private static final double CHARGE_AMOUNT = 100;
    private static final String FEE_ON_MONTH_DAY = "04 March";
    private static final String FEE_FREQUENCY_MONTHS = "2";
    private static final String FEE_INTERVAL = "2";

    private TimeZone systemTimeZone;
    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();
    private FeignSavingsHelper savingsHelper;
    private FeignSavingsProductHelper savingsProductHelper;
    private FeignSavingsChargeHelper savingsChargeHelper;
    private FeignCollateralHelper collateralHelper;

    @BeforeAll
    public void beforeAll() {
        // setup COB Business Steps to prevent test failing due other integration test configurations
        new FeignBusinessStepHelper(fineractClient()).updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS",
                "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING",
                "ADD_PERIODIC_ACCRUAL_ENTRIES", "EXTERNAL_ASSET_OWNER_TRANSFER", "CHECK_DUE_INSTALLMENTS", "ACCRUAL_ACTIVITY_POSTING",
                "LOAN_INTEREST_RECALCULATION");
    }

    @BeforeEach
    public void setup() {
        this.systemTimeZone = TimeZone.getTimeZone(Utils.TENANT_TIME_ZONE);
        this.savingsHelper = new FeignSavingsHelper(fineractClient());
        this.savingsProductHelper = new FeignSavingsProductHelper(fineractClient());
        this.savingsChargeHelper = new FeignSavingsChargeHelper(fineractClient());
        this.collateralHelper = new FeignCollateralHelper(fineractClient());
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }

    @Test
    public void testApplyAnnualFeeForSavingsJobOutcome() throws InterruptedException {
        Long savingsId = null;
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            LocalDate submittedDate = LocalDate.of(2022, 9, 28);
            String submittedDateString = "28 September 2022";
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, submittedDate);

            final Long clientID = clientHelper.createClient();
            Assertions.assertNotNull(clientID);

            final Long savingsProductID = createSavingsProduct(MINIMUM_OPENING_BALANCE);
            Assertions.assertNotNull(savingsProductID);

            savingsId = savingsHelper.submitApplication(clientID, savingsProductID, submittedDateString).getSavingsId();
            Assertions.assertNotNull(savingsProductID);

            SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

            final Long annualFeeChargeId = chargesHelper.createCharge(SavingsRequestBuilders.savingsAnnualFeeCharge()).getResourceId();
            Assertions.assertNotNull(annualFeeChargeId);

            savingsChargeHelper.addChargeWithDueDateAndFeeOnMonthDay(savingsId, annualFeeChargeId, "10 January 2023", "100", "15 January");
            Assertions.assertEquals(1, savingsHelper.getSavingsCharges(savingsId).size());

            savingsHelper.approveSavings(savingsId, submittedDateString);
            SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

            savingsHelper.activateSavings(savingsId, submittedDateString);
            SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 11, 11));
            String JobName = "Apply Annual Fee For Savings";

            SchedulerJobHelper.executeAndAwaitJob(JobName);

            LocalDate nextDueDateForAnnualFee = savingsHelper.getSavingsDetails(savingsId).getAnnualFee().getDueDate();
            LocalDate expectedDueDate = LocalDate.of(2023, 1, 15);

            assertThat(nextDueDateForAnnualFee).isEqualTo(expectedDueDate);
        } finally {
            savingsHelper.closeSavings(savingsId, "11 November 2022", true);
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testInterestPostingForSavingsJobOutcome() throws InterruptedException {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long savingsProductID = createSavingsProduct(MINIMUM_OPENING_BALANCE);
        Assertions.assertNotNull(savingsProductID);

        final Long savingsId = applyApproveAndActivateSavings(clientID, savingsProductID);

        final BigDecimal balanceBefore = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

        String JobName = "Post Interest For Savings";

        SchedulerJobHelper.executeAndAwaitJob(JobName);
        final BigDecimal balanceAfter = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

        Assertions.assertNotSame(balanceBefore, balanceAfter, "Verifying the Balance after running Post Interest for Savings Job");
    }

    @Test
    public void testTransferFeeForLoansFromSavingsJobOutcome() throws InterruptedException {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long savingsProductID = createSavingsProduct(MINIMUM_OPENING_BALANCE);
        Assertions.assertNotNull(savingsProductID);

        final Long savingsId = applyApproveAndActivateSavings(clientID, savingsProductID);

        final Long loanProductID = createLoanProductWithCharge(null);
        Assertions.assertNotNull(loanProductID);

        final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, savingsId, "1 March 2013");
        Assertions.assertNotNull(loanID);

        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanID, LOAN_APPROVAL_DATE);
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        Long specifiedDueDateChargeId = chargesHelper
                .createCharge(ChargeRequestBuilders.loanSpecifiedDueDateAccountTransferFee(CHARGE_AMOUNT, true)).getResourceId();
        Assertions.assertNotNull(specifiedDueDateChargeId);

        addSpecifiedDueDateCharge(loanID, specifiedDueDateChargeId, "12 March 2013", "100");
        Assertions.assertEquals(1, loanHelper.getLoanCharges(loanID).size());

        disburseLoanWithNetDisbursalAmount(loanID, LOAN_DISBURSAL_DATE);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);
        final BigDecimal balanceBefore = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

        String JobName = "Transfer Fee For Loans From Savings";
        SchedulerJobHelper.executeAndAwaitJob(JobName);
        final BigDecimal balanceAfter = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

        final BigDecimal chargeAmount = BigDecimal.valueOf(chargesHelper.getCharge(specifiedDueDateChargeId).getAmount());

        final BigDecimal balance = balanceBefore.subtract(chargeAmount);

        assertAmount(balance, balanceAfter, "Verifying the Balance after running Transfer Fee for Loans from Savings");
    }

    @Test
    public void testApplyHolidaysToLoansJobOutcome() throws InterruptedException {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        Long holidayId = HolidayHelper.createHolidays();
        Assertions.assertNotNull(holidayId);

        final Long loanProductID = createLoanProductWithCharge(null);
        Assertions.assertNotNull(loanProductID);

        final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, null, "01 March 2013");
        Assertions.assertNotNull(loanID);

        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanID, LOAN_APPROVAL_DATE);
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        GetLoansLoanIdResponse loanDetails = disburseLoanWithNetDisbursalAmount(loanID, LOAN_DISBURSAL_DATE);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        // Updating Value for reschedule-repayments-on-holidays Global
        // Configuration
        String configName = GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS;
        globalConfigurationHelper.updateGlobalConfiguration(configName, new PutGlobalConfigurationsRequest().enabled(true));

        holidayId = HolidayHelper.activateHolidays(holidayId);
        Assertions.assertNotNull(holidayId);

        GetHolidaysResponse holidayData = HolidayHelper.getHolidayById(holidayId);
        LocalDate repaymentsRescheduledDate = holidayData.getRepaymentsRescheduledTo();
        Assertions.assertNotNull(repaymentsRescheduledDate);

        // Loan Repayment Schedule Before Apply Holidays To Loans
        final List<GetLoansLoanIdRepaymentPeriod> periodsBeforeHolidaysApply = loanDetails.getRepaymentSchedule().getPeriods();

        for (GetLoansLoanIdRepaymentPeriod period : periodsBeforeHolidaysApply) {
            final LocalDate fromDate = period.getFromDate();
            if (fromDate != null) {
                final Integer fromDateMonth = fromDate.getMonthValue();
                final Integer repaymentsRescheduledDateMonth = repaymentsRescheduledDate.getMonthValue();
                if (Objects.equals(fromDateMonth, repaymentsRescheduledDateMonth)) {
                    final Integer repaymentsRescheduledDateDay = repaymentsRescheduledDate.getDayOfMonth();
                    final Integer fromDateDay = fromDate.getDayOfMonth();
                    Assertions.assertNotEquals(repaymentsRescheduledDateDay, fromDateDay,
                            "Verifying Repayment Rescheduled Day before Running Apply Holidays to Loans Scheduler Job");
                }
            }
        }

        String jobName = "Apply Holidays To Loans";

        SchedulerJobHelper.executeAndAwaitJob(jobName);

        // Loan Repayment Schedule After Apply Holidays To Loans
        final List<GetLoansLoanIdRepaymentPeriod> periodsAfterHolidaysApply = repaymentPeriods(loanID);
        LocalDate dateToApplyHolidays = null;

        for (GetLoansLoanIdRepaymentPeriod periodBefore : periodsBeforeHolidaysApply) {
            for (GetLoansLoanIdRepaymentPeriod periodAfter : periodsAfterHolidaysApply) {
                final LocalDate fromDateBefore = periodBefore.getFromDate();
                final LocalDate fromDateAfter = periodAfter.getFromDate();

                if (fromDateBefore != null && fromDateAfter != null) {
                    final Integer fromDateMonthBefore = fromDateBefore.getMonthValue();
                    final Integer fromDateMonthAfter = fromDateAfter.getMonthValue();
                    final Integer repaymentsRescheduledDateMonth = repaymentsRescheduledDate.getMonthValue();

                    if (Objects.equals(fromDateMonthAfter, repaymentsRescheduledDateMonth)) {
                        dateToApplyHolidays = fromDateAfter;
                    } else if (Objects.equals(fromDateMonthAfter, fromDateMonthBefore)) {
                        assertEqualDay(fromDateBefore, fromDateAfter,
                                "Verifying Repayment Scheduled Days Before And After Running Apply Holidays to Loans Scheduler Job Are Equals");
                    }
                }
            }
        }

        Assertions.assertNotNull(dateToApplyHolidays);
        Assertions.assertEquals(repaymentsRescheduledDate.getDayOfMonth(), dateToApplyHolidays.getDayOfMonth(),
                "Verifying Repayment Rescheduled Day after Running Apply Holidays to Loans Scheduler Job");
    }

    private void assertEqualDay(LocalDate fromDateBefore, LocalDate fromDateAfter, String message) {
        Integer fromDateDayBefore = fromDateBefore.getDayOfMonth();
        Integer fromDateDayAfter = fromDateAfter.getDayOfMonth();
        Assertions.assertEquals(fromDateDayBefore, fromDateDayAfter, message);
    }

    @Test
    public void testApplyType1HolidaysToLoansJobOutcome() throws InterruptedException {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        Long holidayId = HolidayHelper.createTyoe1Holidays();
        Assertions.assertNotNull(holidayId);

        final Long loanProductID = createLoanProductWithCharge(null);
        Assertions.assertNotNull(loanProductID);

        final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, null, "04 January 2024");
        Assertions.assertNotNull(loanID);

        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanID, "04 January 2024");
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        GetLoansLoanIdResponse loanDetails = disburseLoanWithNetDisbursalAmount(loanID, "04 January 2024");
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        String configName = GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS;
        globalConfigurationHelper.updateGlobalConfiguration(configName, new PutGlobalConfigurationsRequest().enabled(true));

        holidayId = HolidayHelper.activateHolidays(holidayId);
        Assertions.assertNotNull(holidayId);

        GetHolidaysResponse holidayData = HolidayHelper.getHolidayById(holidayId);

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        String JobName = "Apply Holidays To Loans";

        SchedulerJobHelper.executeAndAwaitJob(JobName);

        // Loan Repayment Schedule After Apply Holidays To Loans
        List<GetLoansLoanIdRepaymentPeriod> periodsAfterRescheduleApplied = repaymentPeriods(loanID);

        LocalDate fromDate = periods.get(1).getFromDate();
        LocalDate dueDate = periods.get(1).getDueDate();
        Assertions.assertEquals(LocalDate.of(2024, 1, 4), fromDate,
                "Verifying Repayment Rescheduled Date before Running Apply Holidays to Loans Scheduler Job");
        Assertions.assertEquals(LocalDate.of(2024, 2, 4), dueDate,
                "Verifying Repayment Rescheduled Date before Running Apply Holidays to Loans Scheduler Job");

        fromDate = periods.get(2).getFromDate();
        dueDate = periods.get(2).getDueDate();
        Assertions.assertEquals(LocalDate.of(2024, 2, 4), fromDate,
                "Verifying Repayment Rescheduled Date before Running Apply Holidays to Loans Scheduler Job");
        Assertions.assertEquals(LocalDate.of(2024, 3, 4), dueDate,
                "Verifying Repayment Rescheduled Date before Running Apply Holidays to Loans Scheduler Job");

        fromDate = periods.get(3).getFromDate();
        dueDate = periods.get(3).getDueDate();
        Assertions.assertEquals(LocalDate.of(2024, 3, 4), fromDate,
                "Verifying Repayment Rescheduled Date before Running Apply Holidays to Loans Scheduler Job");
        Assertions.assertEquals(LocalDate.of(2024, 4, 4), dueDate,
                "Verifying Repayment Rescheduled Date before Running Apply Holidays to Loans Scheduler Job");

        fromDate = periods.get(4).getFromDate();
        dueDate = periods.get(4).getDueDate();
        Assertions.assertEquals(LocalDate.of(2024, 4, 4), fromDate,
                "Verifying Repayment Rescheduled Date before Running Apply Holidays to Loans Scheduler Job");
        Assertions.assertEquals(LocalDate.of(2024, 5, 4), dueDate,
                "Verifying Repayment Rescheduled Date before Running Apply Holidays to Loans Scheduler Job");

        fromDate = periodsAfterRescheduleApplied.get(1).getFromDate();
        dueDate = periodsAfterRescheduleApplied.get(1).getDueDate();
        Assertions.assertEquals(LocalDate.of(2024, 1, 4), fromDate,
                "Verifying Repayment Rescheduled Date after Running Apply Holidays to Loans Scheduler Job");
        Assertions.assertEquals(LocalDate.of(2024, 2, 4), dueDate,
                "Verifying Repayment Rescheduled Date after Running Apply Holidays to Loans Scheduler Job");

        fromDate = periodsAfterRescheduleApplied.get(2).getFromDate();
        dueDate = periodsAfterRescheduleApplied.get(2).getDueDate();
        Assertions.assertEquals(LocalDate.of(2024, 2, 4), fromDate,
                "Verifying Repayment Rescheduled Date after Running Apply Holidays to Loans Scheduler Job");
        Assertions.assertEquals(LocalDate.of(2024, 3, 4), dueDate,
                "Verifying Repayment Rescheduled Date after Running Apply Holidays to Loans Scheduler Job");

        fromDate = periodsAfterRescheduleApplied.get(3).getFromDate();
        dueDate = periodsAfterRescheduleApplied.get(3).getDueDate();
        Assertions.assertEquals(LocalDate.of(2024, 3, 4), fromDate,
                "Verifying Repayment Rescheduled Date after Running Apply Holidays to Loans Scheduler Job");
        Assertions.assertEquals(LocalDate.of(2024, 5, 4), dueDate,
                "Verifying Repayment Rescheduled Date after Running Apply Holidays to Loans Scheduler Job");

        fromDate = periodsAfterRescheduleApplied.get(4).getFromDate();
        dueDate = periodsAfterRescheduleApplied.get(4).getDueDate();
        Assertions.assertEquals(LocalDate.of(2024, 5, 4), fromDate,
                "Verifying Repayment Rescheduled Date after Running Apply Holidays to Loans Scheduler Job");
        Assertions.assertEquals(LocalDate.of(2024, 6, 4), dueDate,
                "Verifying Repayment Rescheduled Date after Running Apply Holidays to Loans Scheduler Job");

        // Remove the Holiday created
        HolidayHelper.deleteHoliday(holidayId);
    }

    @Test
    public void testApplyDueFeeChargesForSavingsJobOutcome() throws InterruptedException {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long savingsProductID = createSavingsProduct(MINIMUM_OPENING_BALANCE);
        Assertions.assertNotNull(savingsProductID);

        final Long savingsId = savingsHelper.submitApplication(clientID, savingsProductID, SAVINGS_SUBMITTED_DATE).getSavingsId();
        Assertions.assertNotNull(savingsProductID);

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

        final Long specifiedDueDateChargeId = chargesHelper.createCharge(SavingsRequestBuilders.savingsSpecifiedDueDateCharge())
                .getResourceId();
        Assertions.assertNotNull(specifiedDueDateChargeId);

        savingsChargeHelper.addChargeToSavings(savingsId, savingsChargeWithDueDate(specifiedDueDateChargeId));
        Assertions.assertEquals(1, savingsHelper.getSavingsCharges(savingsId).size());

        savingsHelper.approveSavings(savingsId, SAVINGS_APPROVED_DATE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        savingsHelper.activateSavings(savingsId, SAVINGS_ACTIVATED_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        BigDecimal balanceBefore = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

        String JobName = "Pay Due Savings Charges";

        SchedulerJobHelper.executeAndAwaitJob(JobName);
        BigDecimal balanceAfter = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

        final BigDecimal chargeAmount = BigDecimal.valueOf(chargesHelper.getCharge(specifiedDueDateChargeId).getAmount());

        final BigDecimal balance = balanceBefore.subtract(chargeAmount);

        assertAmount(balance, balanceAfter, "Verifying the Balance after running Pay due Savings Charges");
    }

    @Test
    public void testUpdateAccountingRunningBalancesJobOutcome() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();

        final Long accountID = assetAccount.getAccountID().longValue();

        final Long savingsProductID = savingsProductHelper.createSavingsProduct(SavingsRequestBuilders.withCashBasedAccounting(
                savingsProduct(SavingsTestData.InterestPostingPeriodType.QUARTERLY, MINIMUM_OPENING_BALANCE), assetAccount,
                liabilityAccount, incomeAccount, expenseAccount)).getResourceId();

        final Long clientID = clientHelper.createClient(DATE_OF_JOINING);
        final Long savingsID = applyApproveAndActivateSavings(clientID, savingsProductID);

        // Checking initial Account entries.
        journalHelper.checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE,
                LoanTestData.Journal.debit(accountID, SP_BALANCE.doubleValue()));
        journalHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE,
                LoanTestData.Journal.credit(liabilityAccount.getAccountID().longValue(), SP_BALANCE.doubleValue()));

        String JobName = "Update Accounting Running Balances";

        SchedulerJobHelper.executeAndAwaitJob(JobName);
        final Long runningBalanceAfter = fineractClient().generalLedgerAccount().retreiveAccount(accountID, true)
                .getOrganizationRunningBalance();

        final Long INT_BALANCE = Long.valueOf(MINIMUM_OPENING_BALANCE);

        Assertions.assertEquals(INT_BALANCE, runningBalanceAfter,
                "Verifying Account Running Balance after running Update Accounting Running Balances Scheduler Job");
    }

    @Test
    public void testUpdateLoanArrearsAgingJobOutcome() {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long loanProductID = createLoanProductWithCharge(null);
        Assertions.assertNotNull(loanProductID);

        final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, null, "1 March 2013");
        Assertions.assertNotNull(loanID);

        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanID, LOAN_APPROVAL_DATE);
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        disburseLoanWithNetDisbursalAmount(loanID, LOAN_DISBURSAL_DATE);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        String JobName = "Update Loan Arrears Ageing";

        SchedulerJobHelper.executeAndAwaitJob(JobName);
        GetLoansLoanIdSummary loanSummaryData = loanHelper.getLoanDetails(loanID).getSummary();

        BigDecimal totalLoanArrearsAging = loanSummaryData.getPrincipalOverdue().add(loanSummaryData.getInterestOverdue());

        assertAmount(totalLoanArrearsAging, loanSummaryData.getTotalOverdue(),
                "Verifying Arrears Aging after Running Update Loan Arrears Aging Scheduler Job");
    }

    @Test
    public void testExecuteStandingInstructionsJobOutcome() throws InterruptedException {
        StandingInstructionsHelper standingInstructionsHelper = new StandingInstructionsHelper();

        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);
        final DateTimeFormatter monthDayFormat = DateTimeFormatter.ofPattern("dd MMMM", Locale.US);

        // Create the LocalDate with the Zone used by default
        final LocalDate localDate = LocalDate.now(this.systemTimeZone.toZoneId());
        ZonedDateTime currentDate = ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, this.systemTimeZone.toZoneId());
        // When the Stanging Instruction will be applied
        final String MONTH_DAY = monthDayFormat.format(currentDate.toLocalDate());
        // Standing Instruction valid from (One week before today)
        currentDate = currentDate.minus(Duration.ofDays(7));
        final String VALID_FROM = dateFormat.format(currentDate);
        // Standing Instruction valid to (One year after)
        currentDate = currentDate.plus(1, ChronoUnit.YEARS);
        final String VALID_TO = dateFormat.format(currentDate);

        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long savingsProductID = createSavingsProduct(MINIMUM_OPENING_BALANCE);
        Assertions.assertNotNull(savingsProductID);

        final Long fromSavingsId = applyApproveAndActivateSavings(clientID, savingsProductID);

        final Long toSavingsId = applyApproveAndActivateSavings(clientID, savingsProductID);

        BigDecimal fromSavingsBalanceBefore = savingsHelper.getSavingsSummary(fromSavingsId).getAccountBalance();

        BigDecimal toSavingsBalanceBefore = savingsHelper.getSavingsSummary(toSavingsId).getAccountBalance();

        Integer standingInstructionId = standingInstructionsHelper.createStandingInstruction(clientID.toString(), fromSavingsId.toString(),
                toSavingsId.toString(), FROM_ACCOUNT_TYPE_SAVINGS, TO_ACCOUNT_TYPE_SAVINGS, VALID_FROM, VALID_TO, MONTH_DAY);
        Assertions.assertNotNull(standingInstructionId);

        String JobName = "Execute Standing Instruction";
        SchedulerJobHelper.executeAndAwaitJob(JobName);
        BigDecimal fromSavingsBalanceAfter = savingsHelper.getSavingsSummary(fromSavingsId).getAccountBalance();

        BigDecimal toSavingsBalanceAfter = savingsHelper.getSavingsSummary(toSavingsId).getAccountBalance();

        final GetStandingInstructionsStandingInstructionIdResponse standingInstructionData = standingInstructionsHelper
                .getStandingInstructionById(standingInstructionId.longValue());
        final BigDecimal instructionAmount = new BigDecimal(String.valueOf(standingInstructionData.getAmount()));
        BigDecimal expectedFromSavingsBalance = fromSavingsBalanceBefore.subtract(instructionAmount);
        BigDecimal expectedToSavingsBalance = toSavingsBalanceBefore.add(instructionAmount);

        assertAmount(expectedFromSavingsBalance, fromSavingsBalanceAfter,
                "Verifying From Savings Balance after Successful completion of Scheduler Job");
        assertAmount(expectedToSavingsBalance, toSavingsBalanceAfter,
                "Verifying To Savings Balance after Successful completion of Scheduler Job");
        Integer fromAccountType = PortfolioAccountType.SAVINGS.getValue();
        Integer transferType = AccountTransferType.ACCOUNT_TRANSFER.getValue();
        Set<GetStandingInstructionHistoryPageItemsResponse> standingInstructionHistoryData = standingInstructionsHelper
                .getStandingInstructionHistory(fromSavingsId.intValue(), fromAccountType, clientID.intValue(), transferType);
        Assertions.assertEquals(1, standingInstructionHistoryData.size(),
                "Verifying the no of standing instruction transactions logged for the client");
        GetStandingInstructionHistoryPageItemsResponse loggedTransaction = standingInstructionHistoryData.iterator().next();

        Assertions.assertEquals(standingInstructionData.getAmount(), loggedTransaction.getAmount(),
                "Verifying transferred amount and logged transaction amounts");
    }

    @Test
    public void testApplyPenaltyForOverdueLoansJobOutcome() throws InterruptedException {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        Long overdueFeeChargeId = chargesHelper.createCharge(loanOverdueFee()).getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);

        final Long loanProductID = createLoanProductWithCharge(overdueFeeChargeId.toString());
        Assertions.assertNotNull(loanProductID);

        final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, null, "1 March 2020");
        Assertions.assertNotNull(loanID);

        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanID, "01 March 2020");
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        disburseLoanWithNetDisbursalAmount(loanID, "02 March 2020");
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        String JobName = "Apply penalty to overdue loans";
        SchedulerJobHelper.executeAndAwaitJob(JobName);

        final BigDecimal chargeAmount = BigDecimal.valueOf(chargesHelper.getCharge(overdueFeeChargeId).getAmount());

        List<GetLoansLoanIdRepaymentPeriod> repaymentScheduleDataAfter = repaymentPeriods(loanID);

        assertAmount(chargeAmount, repaymentScheduleDataAfter.get(1).getPenaltyChargesDue(),
                "Verifying From Penalty Charges due fot first Repayment after Successful completion of Scheduler Job");

        loanHelper.undoDisbursement(loanID);
        verifyLoanStatus(loanID, LoanStatus.APPROVED);
        verifyLoanStatus(loanHelper.getLoanDetails(loanID), GetLoansLoanIdStatus::getWaitingForDisbursal);
    }

    @Test
    public void testApplyPenaltyForOverdueLoansJobOutcomeIfLoanChargedOff() throws InterruptedException {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        Long overdueFeeChargeId = chargesHelper.createCharge(loanOverdueFee()).getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);

        final Long loanProductID = createLoanProductNoInterest(overdueFeeChargeId.toString());
        Assertions.assertNotNull(loanProductID);

        final Long loanID = applyForLoanWithCollateralNoInterest(clientID, loanProductID, null, "01 March 2020");
        Assertions.assertNotNull(loanID);

        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanID, "01 March 2020");
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        disburseLoanWithNetDisbursalAmount(loanID, "02 March 2020");
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        transactionHelper.chargeOffLoan(loanID,
                new PostLoansLoanIdTransactionsRequest().transactionDate("03 March 2020").locale("en").dateFormat("dd MMMM yyyy"));

        String JobName = "Apply penalty to overdue loans";
        SchedulerJobHelper.executeAndAwaitJob(JobName);

        List<GetLoansLoanIdRepaymentPeriod> repaymentScheduleDataAfter = repaymentPeriods(loanID);

        assertAmount(BigDecimal.ZERO, repaymentScheduleDataAfter.get(1).getPenaltyChargesDue(),
                "Verifying From Penalty Charges due fot first Repayment after Successful completion of Scheduler Job");

    }

    @Test
    public void testLoanCOBJobOutcome() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final Long clientID = clientHelper.createClient();
            Assertions.assertNotNull(clientID);

            Long overdueFeeChargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanOverdueFeePercentageOfAmountAndInterest(1))
                    .getResourceId();
            Assertions.assertNotNull(overdueFeeChargeId);

            Long fee = chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10, false))
                    .getResourceId();
            Assertions.assertNotNull(fee);

            final Long loanProductID = createLoanProductWithCharge(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            List<Long> loanIDs = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, null, "1 March 2020");

                Assertions.assertNotNull(loanID);

                verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

                approveLoan(loanID, "01 March 2020");
                verifyLoanStatus(loanID, LoanStatus.APPROVED);

                disburseLoanWithNetDisbursalAmount(loanID, "02 March 2020");
                verifyLoanStatus(loanID, LoanStatus.ACTIVE);
                loanIDs.add(loanID);

                addSpecifiedDueDateCharge(loanID, fee, "02 March 2020", "10");
            }

            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, LocalDate.of(2020, 9, 2));
            String jobName = "Loan COB";
            SchedulerJobHelper.executeAndAwaitJob(jobName);
            for (Long loanId : loanIDs) {
                List<GetLoansLoanIdRepaymentPeriod> repaymentScheduleDataAfter = repaymentPeriods(loanId);

                assertAmount(new BigDecimal("10.00"), repaymentScheduleDataAfter.get(1).getFeeChargesDue(),
                        "Verifying From Fee Charges due for first Repayment after Successful completion of Scheduler Job");
                assertAmount(new BigDecimal("39.39"), repaymentScheduleDataAfter.get(1).getPenaltyChargesDue(),
                        "Verifying From Penalty Charges due for first Repayment after Successful completion of Scheduler Job");
                assertAmount(new BigDecimal("39.39"), repaymentScheduleDataAfter.get(2).getPenaltyChargesDue(),
                        "Verifying From Penalty Charges due for first Repayment after Successful completion of Scheduler Job");
                assertAmount(new BigDecimal("39.39"), repaymentScheduleDataAfter.get(3).getPenaltyChargesDue(),
                        "Verifying From Penalty Charges due for first Repayment after Successful completion of Scheduler Job");
                assertAmount(new BigDecimal("39.39"), repaymentScheduleDataAfter.get(4).getPenaltyChargesDue(),
                        "Verifying From Penalty Charges due for first Repayment after Successful completion of Scheduler Job");

            }
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanCOBJobOutcomeWhileAddingFeeOnDisbursementDate() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, LocalDate.of(2020, 6, 2));

            final Long clientID = clientHelper.createClient();
            Assertions.assertNotNull(clientID);

            Long fee = chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10, false))
                    .getResourceId();
            Assertions.assertNotNull(fee);

            final Long loanProductID = createLoanProductWithPeriodicAccrual(null);
            Assertions.assertNotNull(loanProductID);

            final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, null, "1 June 2020");

            Assertions.assertNotNull(loanID);

            verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

            approveLoan(loanID, "01 June 2020");
            verifyLoanStatus(loanID, LoanStatus.APPROVED);

            disburseLoanWithNetDisbursalAmount(loanID, "02 June 2020");
            verifyLoanStatus(loanID, LoanStatus.ACTIVE);

            addSpecifiedDueDateCharge(loanID, fee, "02 June 2020", "10");

            String jobName = "Loan COB";
            SchedulerJobHelper.executeAndAwaitJob(jobName);

            List<GetLoansLoanIdRepaymentPeriod> repaymentScheduleDataAfter = repaymentPeriods(loanID);

            assertAmount(new BigDecimal("10.00"), repaymentScheduleDataAfter.get(1).getFeeChargesDue(),
                    "Verifying From Fee Charges due for first Repayment after Successful completion of Scheduler Job");

            GetLoansLoanIdResponse getLoansLoanIdResponse = loanHelper.getLoanDetails(loanID, "transactions");
            // First accrual transaction
            assertTrue(getLoansLoanIdResponse.getTransactions().get(1).getType().getAccrual());
            assertEquals(10.00, Utils.getDoubleValue(getLoansLoanIdResponse.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(LocalDate.of(2020, 6, 2), getLoansLoanIdResponse.getTransactions().get(1).getDate());
            Long transactionId = getLoansLoanIdResponse.getTransactions().get(1).getId();

            final GetJournalEntriesTransactionIdResponse journalEntriesResponse = journalHelper.getJournalEntries("L" + transactionId);
            assertNotNull(journalEntriesResponse);
            final List<JournalEntryTransactionItem> journalEntries = journalEntriesResponse.getPageItems();
            assertEquals(2, journalEntries.size());
            assertEquals(10, journalEntries.get(0).getAmount());
            assertEquals(10, journalEntries.get(1).getAmount());
            assertEquals(LocalDate.of(2020, 6, 2), journalEntries.get(1).getTransactionDate());
            assertEquals(LocalDate.of(2020, 6, 2), journalEntries.get(0).getTransactionDate());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanCOBRunsOnlyOnLoansOneDayBehind() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final Long clientID = clientHelper.createClient();
            Assertions.assertNotNull(clientID);

            Long overdueFeeChargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanOverdueFeePercentageOfAmountAndInterest(1))
                    .getResourceId();
            Assertions.assertNotNull(overdueFeeChargeId);

            final Long loanProductID = createLoanProductWithCharge(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);

            final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, null, "1 July 2020");

            Assertions.assertNotNull(loanID);

            verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

            approveLoan(loanID, "01 July 2020");
            verifyLoanStatus(loanID, LoanStatus.APPROVED);

            disburseLoanWithNetDisbursalAmount(loanID, "02 July 2020");
            verifyLoanStatus(loanID, LoanStatus.ACTIVE);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, LocalDate.of(2020, 7, 2));
            String jobName = "Loan COB";

            SchedulerJobHelper.executeAndAwaitJob(jobName);
            GetLoansLoanIdResponse loan = loanHelper.getLoanDetails(loanID);
            Assertions.assertEquals(LocalDate.of(2020, 7, 2), loan.getLastClosedBusinessDate());

            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, LocalDate.of(2020, 7, 3));
            SchedulerJobHelper.executeAndAwaitJob(jobName);

            loan = loanHelper.getLoanDetails(loanID);
            Assertions.assertEquals(LocalDate.of(2020, 7, 3), loan.getLastClosedBusinessDate());

            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, LocalDate.of(2020, 7, 5));
            SchedulerJobHelper.executeAndAwaitJob(jobName);

            loan = loanHelper.getLoanDetails(loanID);
            Assertions.assertEquals(LocalDate.of(2020, 7, 3), loan.getLastClosedBusinessDate());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanCOBApplyPenaltyOnDue() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, LocalDate.of(2019, 2, 2));
            // set penalty wait period to 0
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                    new PutGlobalConfigurationsRequest().value(0L));

            final Long clientID = clientHelper.createClient();
            Assertions.assertNotNull(clientID);

            Long overdueFeeChargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanOverdueFeePercentageOfAmountAndInterest(1))
                    .getResourceId();
            Assertions.assertNotNull(overdueFeeChargeId);

            final Long loanProductID = createLoanProductWithCharge(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);

            final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, null, "1 March 2019");

            Assertions.assertNotNull(loanID);

            verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

            approveLoan(loanID, "01 March 2019");
            verifyLoanStatus(loanID, LoanStatus.APPROVED);

            disburseLoanWithNetDisbursalAmount(loanID, "02 March 2019");
            verifyLoanStatus(loanID, LoanStatus.ACTIVE);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, LocalDate.of(2019, 4, 1));
            String jobName = "Loan COB";

            SchedulerJobHelper.executeAndAwaitJob(jobName);
            List<GetLoansLoanIdRepaymentPeriod> repaymentScheduleDataAfter = repaymentPeriods(loanID);
            assertAmount(BigDecimal.ZERO, repaymentScheduleDataAfter.get(1).getPenaltyChargesDue(),
                    "Verifying From Penalty Charges due fot first Repayment after Successful completion of Scheduler Job");

            LocalDate lastBusinessDateBeforeFastForward = LocalDate.of(2019, 4, 2);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, lastBusinessDateBeforeFastForward);
            SchedulerJobHelper.executeAndAwaitJob(jobName);
            repaymentScheduleDataAfter = repaymentPeriods(loanID);
            assertAmount(new BigDecimal("39.39"), repaymentScheduleDataAfter.get(1).getPenaltyChargesDue(),
                    "Verifying From Penalty Charges due fot first Repayment after Successful completion of Scheduler Job");

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                    new PutGlobalConfigurationsRequest().value(2L));
        }
    }

    @Test
    public void testLoanCOBApplyPenaltyOnDue1DayGracePeriod() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, LocalDate.of(2020, 2, 2));
            // set penalty wait period to 0
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                    new PutGlobalConfigurationsRequest().value(0L));

            final Long clientID = clientHelper.createClient();
            Assertions.assertNotNull(clientID);

            Long overdueFeeChargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanOverdueFeePercentageOfAmountAndInterest(1))
                    .getResourceId();
            Assertions.assertNotNull(overdueFeeChargeId);

            final Long loanProductID = createLoanProductWithCharge(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            // Test penalty where there is 1 day grace period
            final Long loanID2 = applyForLoanWithCollateral(clientID, loanProductID, null, "1 April 2020");

            Assertions.assertNotNull(loanID2);

            verifyLoanStatus(loanID2, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

            approveLoan(loanID2, "01 April 2020");
            verifyLoanStatus(loanID2, LoanStatus.APPROVED);

            disburseLoanWithNetDisbursalAmount(loanID2, "02 April 2020");
            verifyLoanStatus(loanID2, LoanStatus.ACTIVE);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                    new PutGlobalConfigurationsRequest().value(1L));
            LocalDate dateToFastForward = LocalDate.of(2020, 5, 2);
            String jobName = "Loan COB";
            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, dateToFastForward);
            SchedulerJobHelper.executeAndAwaitJob(jobName);
            List<GetLoansLoanIdRepaymentPeriod> repaymentScheduleDataAfter = repaymentPeriods(loanID2);
            assertAmount(BigDecimal.ZERO, repaymentScheduleDataAfter.get(1).getPenaltyChargesDue(),
                    "Verifying From Penalty Charges due fot first Repayment after Successful completion of Scheduler Job");

            BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, LocalDate.of(2020, 5, 3));
            SchedulerJobHelper.executeAndAwaitJob(jobName);
            repaymentScheduleDataAfter = repaymentPeriods(loanID2);
            assertAmount(new BigDecimal("39.39"), repaymentScheduleDataAfter.get(1).getPenaltyChargesDue(),
                    "Verifying From Penalty Charges due fot first Repayment after Successful completion of Scheduler Job");

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                    new PutGlobalConfigurationsRequest().value(2L));
        }
    }

    @Test
    public void testAvoidUnncessaryPenaltyWhenAmountZeroForOverdueLoansJobOutcome() throws InterruptedException {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        Long overdueFeeChargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanOverdueFeePercentageOfAmountAndInterest(0.000001))
                .getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);

        final Long loanProductID = createLoanProductWithCharge(overdueFeeChargeId.toString());
        Assertions.assertNotNull(loanProductID);

        final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, null, "1 March 2013");
        Assertions.assertNotNull(loanID);

        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanID, LOAN_APPROVAL_DATE);
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        disburseLoanWithNetDisbursalAmount(loanID, LOAN_APPROVAL_DATE_PLUS_ONE);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        String JobName = "Apply penalty to overdue loans";
        int jobId = 12;

        SchedulerJobHelper.executeAndAwaitJob(JobName);

        GetJobsResponse schedulerJob = SchedulerJobHelper.getSchedulerJobById(jobId);

        Assertions.assertNotNull(schedulerJob);
        while (schedulerJob.getCurrentlyRunning()) {
            Thread.sleep(15000);
            schedulerJob = SchedulerJobHelper.getSchedulerJobById(jobId);
            Assertions.assertNotNull(schedulerJob);
        }

        List<GetLoansLoanIdRepaymentPeriod> repaymentScheduleDataAfter = repaymentPeriods(loanID);

        assertAmount(BigDecimal.ZERO, repaymentScheduleDataAfter.get(1).getPenaltyChargesDue(),
                "Verifying From Penalty Charges due fot first Repayment after Successful completion of Scheduler Job");

        Assertions.assertTrue(loanHelper.getLoanCharges(loanID).isEmpty(), "Verifying that charge isn't created when the amount is 0");

        loanHelper.undoDisbursement(loanID);
        verifyLoanStatus(loanID, LoanStatus.APPROVED);
        verifyLoanStatus(loanHelper.getLoanDetails(loanID), GetLoansLoanIdStatus::getWaitingForDisbursal);
    }

    @Test
    public void testUpdateOverdueDaysForNPA() throws InterruptedException {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long loanProductID = createLoanProductWithCharge(null);
        Assertions.assertNotNull(loanProductID);

        final Long loanID = applyForLoanWithCollateral(clientID, loanProductID, null, "1 March 2013");
        Assertions.assertNotNull(loanID);

        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanID, LOAN_APPROVAL_DATE);
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        disburseLoanWithNetDisbursalAmount(loanID, LOAN_APPROVAL_DATE_PLUS_ONE);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        final Boolean isNPABefore = loanHelper.getLoanDetails(loanID).getIsNPA();
        Assertions.assertFalse(isNPABefore);
        String JobName = "Update Non Performing Assets";
        SchedulerJobHelper.executeAndAwaitJob(JobName);
        final Boolean isNPAAfter = loanHelper.getLoanDetails(loanID).getIsNPA();
        assertTrue(isNPAAfter);
    }

    @Test
    public void testInterestTransferForSavings() throws InterruptedException {
        FeignFixedDepositHelper fixedDepositHelper = new FeignFixedDepositHelper(fineractClient());

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -2);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final int WHOLE_TERM = 1;

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        BigDecimal balance = new BigDecimal(MINIMUM_OPENING_BALANCE).add(DepositTestData.DEPOSIT_AMOUNT);
        final Long savingsProductID = createSavingsProduct(balance.toPlainString());
        Assertions.assertNotNull(savingsProductID);

        final Long savingsId = applyApproveAndActivateSavings(clientId, savingsProductID);
        Assertions.assertNotNull(savingsId);
        assertAmount(balance, savingsHelper.getSavingsSummary(savingsId).getAccountBalance(), "Verifying opening Balance");

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM,
                savingsId);
        Assertions.assertNotNull(fixedDepositAccountId);

        Assertions.assertTrue(fixedDepositHelper.getAccount(fixedDepositAccountId).getStatus().getSubmittedAndPendingApproval());

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        Assertions.assertTrue(fixedDepositHelper.getAccount(fixedDepositAccountId).getStatus().getApproved());

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        Assertions.assertTrue(fixedDepositHelper.getAccount(fixedDepositAccountId).getStatus().getActive());
        balance = new BigDecimal(MINIMUM_OPENING_BALANCE);
        assertAmount(balance, savingsHelper.getSavingsSummary(savingsId).getAccountBalance(), "Verifying Balance");

        fixedDepositHelper.postInterest(fixedDepositAccountId);

        BigDecimal interestPosted = savingsHelper.getSavingsSummary(fixedDepositAccountId).getAccountBalance()
                .subtract(DepositTestData.DEPOSIT_AMOUNT);

        String JobName = "Transfer Interest To Savings";
        SchedulerJobHelper.executeAndAwaitJob(JobName);
        assertAmount(DepositTestData.DEPOSIT_AMOUNT, savingsHelper.getSavingsSummary(fixedDepositAccountId).getAccountBalance(),
                "Verifying opening Balance");

        balance = new BigDecimal(MINIMUM_OPENING_BALANCE).add(interestPosted);
        validateNumberForEqualExcludePrecision(balance.toPlainString(),
                savingsHelper.getSavingsSummary(savingsId).getAccountBalance().toPlainString());
    }

    @Test
    public void businessDateIsCorrectForCronJob() throws InterruptedException {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("2022.09.04").dateFormat("yyyy.MM.dd").locale("en"));

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account assetFeeAndPenaltyAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            Long penalty = chargesHelper
                    .createCharge(ChargeRequestBuilders.loanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10, true)).getResourceId();

            final PostLoanProductsRequest loanProductRequest = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                    .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                    .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                    .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                    .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                    .withFeeAndPenaltyAssetAccount(assetFeeAndPenaltyAccount).buildRequest(null);
            final Long loanProductID = loanHelper.createLoanProduct(loanProductRequest).getResourceId();

            final Long clientId = clientHelper.createClient(ClientRequestBuilders.defaultClient()).getClientId();

            Long loanId = applyForLoanWithCollateral(clientId, loanProductID, null, "02 September 2022");

            approveLoan(loanId, "02 September 2022");
            loanHelper.disburseLoan("03 September 2022", loanId, "1000");

            BusinessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("2022.09.05").dateFormat("yyyy.MM.dd").locale("en"));

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            addSpecifiedDueDateCharge(loanId, penalty, penaltyCharge1AddedDate, "10");

            SchedulerJobHelper.updateSchedulerStatus(true);
            SchedulerJobHelper.updateSchedulerJob(16L, new PutJobsJobIDRequest().active(true).cronExpression("0/1 * * * * ?"));

            Thread.sleep(2000);
            GetLoansLoanIdResponse loanDetails = loanHelper.getLoanDetails(loanId, "transactions");
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(1).getDate());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            SchedulerJobHelper.updateSchedulerJob(16L, new PutJobsJobIDRequest().cronExpression("0 2 0 1/1 * ? *"));
        }
    }

    private PostSavingsProductsRequest savingsProduct(int interestPostingPeriodType, String minOpeningBalance) {
        return SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY, interestPostingPeriodType,
                SavingsTestData.InterestCalculationType.DAILY_BALANCE).minRequiredOpeningBalance(new BigDecimal(minOpeningBalance));
    }

    private Long createSavingsProduct(final String minOpeningBalance) {
        return savingsProductHelper
                .createSavingsProduct(savingsProduct(SavingsTestData.InterestPostingPeriodType.MONTHLY, minOpeningBalance)).getResourceId();
    }

    private Long applyApproveAndActivateSavings(Long clientId, Long savingsProductId) {
        final Long savingsId = savingsHelper.submitApplication(clientId, savingsProductId, SAVINGS_SUBMITTED_DATE).getSavingsId();
        Assertions.assertNotNull(savingsProductId);

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

        savingsHelper.approveSavings(savingsId, SAVINGS_APPROVED_DATE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        savingsHelper.activateSavings(savingsId, SAVINGS_ACTIVATED_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private static PostSavingsAccountsSavingsAccountIdChargesRequest savingsChargeWithDueDate(Long chargeId) {
        return new PostSavingsAccountsSavingsAccountIdChargesRequest().chargeId(chargeId).amount(100F).feeOnMonthDay("15 January")
                .locale("en").monthDayFormat("dd MMMM").dateFormat("dd MMMM yyy").dueDate("10 January 2013");
    }

    private static ChargeRequest loanOverdueFee() {
        return ChargeRequestBuilders.loanOverdueFee(CHARGE_AMOUNT).feeFrequency(FEE_FREQUENCY_MONTHS).feeOnMonthDay(FEE_ON_MONTH_DAY)
                .feeInterval(FEE_INTERVAL).monthDayFormat("dd MMM");
    }

    private Long createLoanProductWithCharge(final String chargeId) {
        return loanHelper
                .createLoanProduct(
                        new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4").withRepaymentAfterEvery("1")
                                .withRepaymentTypeAsMonth().withinterestRatePerPeriod("1").withInterestRateFrequencyTypeAsMonths()
                                .withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance().buildRequest(chargeId))
                .getResourceId();
    }

    private Long createLoanProductNoInterest(final String chargeId) {
        return loanHelper.createLoanProduct(new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withAmortizationTypeAsEqualInstallments().buildRequest(chargeId)).getResourceId();
    }

    private Long createLoanProductWithPeriodicAccrual(final String chargeId) {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account assetFeeAndPenaltyAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        return loanHelper.createLoanProduct(new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(assetFeeAndPenaltyAccount).buildRequest(chargeId)).getResourceId();
    }

    private Long applyForLoanWithCollateral(final Long clientID, final Long loanProductID, final Long savingsID, final String date) {
        return loanHelper.applyForLoan(loanApplication(clientID, loanProductID, savingsID, date).interestRatePerPeriod(new BigDecimal("2"))
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)).getLoanId();
    }

    private Long applyForLoanWithCollateralNoInterest(final Long clientID, final Long loanProductID, final Long savingsID,
            final String date) {
        return loanHelper.applyForLoan(loanApplication(clientID, loanProductID, savingsID, date).interestRatePerPeriod(BigDecimal.ZERO)
                .interestType(LoanTestData.InterestType.FLAT)).getLoanId();
    }

    /** Mirrors the payload {@code LoanApplicationTestBuilder.build} sent, with one client collateral attached. */
    private PostLoansRequest loanApplication(final Long clientID, final Long loanProductID, final Long savingsID, final String date) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);

        return new PostLoansRequest().clientId(clientID).productId(loanProductID).loanType("individual").principal(new BigDecimal("15000"))
                .loanTermFrequency(4).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS).numberOfRepayments(4)
                .repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .transactionProcessingStrategyCode(LoanApplicationTestBuilder.DEFAULT_STRATEGY).expectedDisbursementDate(date)
                .submittedOnDate(date).linkAccountId(savingsID).maxOutstandingLoanBalance(new BigDecimal(LOAN_MAX_OUTSTANDING_BALANCE))
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)))
                .charges(new ArrayList<>()).dateFormat("dd MMMM yyyy").locale(LOAN_LOCALE);
    }

    private void approveLoan(final Long loanId, final String approvalDate) {
        loanHelper.approveLoan(loanId,
                new PostLoansLoanIdRequest().approvedOnDate(approvalDate).note("Approval NOTE").dateFormat("dd MMMM yyyy").locale("en"));
    }

    /** Disburses the net disbursal amount and answers the loan as it stood just before. */
    private GetLoansLoanIdResponse disburseLoanWithNetDisbursalAmount(final Long loanId, final String disbursementDate) {
        final GetLoansLoanIdResponse loanDetails = loanHelper.getLoanDetails(loanId, "repaymentSchedule");
        loanHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDate)
                .netDisbursalAmount(loanDetails.getNetDisbursalAmount()).note("DISBURSE NOTE").dateFormat("dd MMMM yyyy").locale("en"));
        return loanDetails;
    }

    private void addSpecifiedDueDateCharge(final Long loanId, final Long chargeId, final String dueDate, final String amount) {
        loanHelper.addLoanCharge(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeId).dueDate(dueDate)
                .amount(Double.valueOf(amount)).dateFormat("dd MMMM yyyy").locale(LOAN_LOCALE));
    }

    private List<GetLoansLoanIdRepaymentPeriod> repaymentPeriods(final Long loanId) {
        return loanHelper.getLoanDetails(loanId, "repaymentSchedule").getRepaymentSchedule().getPeriods();
    }

    private Long createFixedDepositProduct(final String validFrom, final String validTo) {
        PostFixedDepositProductsRequest request = DepositRequestBuilders.withChart(DepositRequestBuilders.fixedDepositProduct(), validFrom,
                validTo, DepositTestData.periodRangeChartSlabs());
        return new FeignFixedDepositProductHelper(fineractClient()).createProduct(request).getResourceId();
    }

    private Long applyForFixedDepositApplication(final Long clientID, final Long productID, final String submittedOnDate,
            final int penalInterestType, final Long savingsId) {
        final PostFixedDepositAccountsRequest request = DepositRequestBuilders
                .fixedDepositAccount(clientID, productID, submittedOnDate, penalInterestType).linkAccountId(savingsId)
                .transferInterestToSavings(true).lockinPeriodFrequency(0)
                .lockinPeriodFrequencyType(SavingsTestData.PeriodFrequencyType.WEEKS);
        return new FeignFixedDepositHelper(fineractClient()).submitApplication(request).getSavingsId();
    }

    private static void assertAmount(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertNotNull(actual, message);
        assertEquals(0, expected.compareTo(actual), () -> message + ": expected " + expected + " but was " + actual);
    }

    private void validateNumberForEqualExcludePrecision(String val, String val2) {
        DecimalFormat twoDForm = new DecimalFormat("#", new DecimalFormatSymbols(Locale.US));
        assertEquals(0,
                Float.valueOf(twoDForm.format(Float.parseFloat(val))).compareTo(Float.valueOf(twoDForm.format(Float.parseFloat(val2)))));
    }
}
