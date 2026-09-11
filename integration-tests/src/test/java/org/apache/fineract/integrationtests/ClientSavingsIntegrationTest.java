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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountChargeData;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.client.models.SavingsAccountSummaryData;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxComponentHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxGroupHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Client Savings Integration Test for checking Savings Application.
 */
@Order(2)
public class ClientSavingsIntegrationTest extends FeignSavingsTestBase {

    private static final String DEPOSIT_AMOUNT = "2000";
    private static final String WITHDRAW_AMOUNT = "1000";
    private static final String WITHDRAW_AMOUNT_ADJUSTED = "500";
    private static final String MINIMUM_OPENING_BALANCE = "1000.0";

    private static final String SUBMITTED_ON_DATE = "08 January 2013";
    private static final String SUBMITTED_ON_DATE_PLUS_ONE = "09 January 2013";
    private static final String SUBMITTED_ON_DATE_MINUS_ONE = "07 January 2013";
    private static final String TRANSACTION_DATE = "01 March 2013";
    private static final String TRANSACTION_DATE_PLUS_ONE = "02 March 2013";

    /** The month-day and due date the legacy period-charge body always sent. */
    private static final String PERIOD_CHARGE_FEE_ON_MONTH_DAY = "15 January";
    private static final String PERIOD_CHARGE_DUE_DATE = "10 January 2013";
    private static final String PERIOD_CHARGE_AMOUNT = "100";
    private static final String REASON_FOR_BLOCK = "unUsualActivity";
    private static final String POST_INTEREST_FOR_SAVINGS_JOB = "Post Interest For Savings";

    private final FeignTaxComponentHelper taxComponentHelper = new FeignTaxComponentHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final FeignTaxGroupHelper taxGroupHelper = new FeignTaxGroupHelper(FineractFeignClientHelper.getFineractFeignClient());

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }

    @Test
    public void testSavingsAccount() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false);
        Long savingsId = submitAndUpdateApplication(clientId, savingsProductId);

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
        savingsHelper.calculateInterest(savingsId);
        assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

        savingsHelper.postInterest(savingsId);
        assertNotEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));
    }

    @Test
    public void testSavingsLastTransactionAndRunningBalanceUpdate() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false);
        Long savingsId = submitUpdateApproveActivate(clientId, savingsProductId);

        SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
        savingsHelper.calculateInterest(savingsId);
        assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

        savingsHelper.postInterest(savingsId);
        assertNotNull(savingsHelper.getSavingsSummary(savingsId).getInterestPostedTillDate());
    }

    @Test
    public void testSavingsBackedDatedTransactionsNotAllowed() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false);
        Long savingsId = submitUpdateApproveActivate(clientId, savingsProductId);

        SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
        savingsHelper.calculateInterest(savingsId);
        assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

        savingsHelper.postInterest(savingsId);
        LocalDate interestPostedTillDate = savingsHelper.getSavingsSummary(savingsId).getInterestPostedTillDate();
        assertNotNull(interestPostedTillDate);

        globalConfigurationHelper.updateGlobalConfiguration(
                GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING,
                new PutGlobalConfigurationsRequest().enabled(false));

        CallFailedRuntimeException error = savingsTransactionHelper.depositExpectingError(savingsId, "3000",
                Utils.dateFormatter.format(interestPostedTillDate.minusDays(1)));

        globalConfigurationHelper.updateGlobalConfiguration(
                GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING,
                new PutGlobalConfigurationsRequest().enabled(true));

        SavingsTestValidators.verifyFirstErrorCode("error.msg.savings.transaction.is.not.allowed", error);
    }

    @Test
    public void testSavingsAccountWithMinBalanceForInterestCalculation() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, "5000", null, false, false);
        Long savingsId = submitUpdateApproveActivate(clientId, savingsProductId);

        SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
        savingsHelper.calculateInterest(savingsId);
        assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

        savingsHelper.postInterest(savingsId);
        SavingsAccountSummaryData summary = savingsHelper.getSavingsSummary(savingsId);

        // the balance is under the threshold, so posting stamps the till-date and moves nothing else
        summary.setInterestPostedTillDate(null);
        assertEquals(summaryBefore, summary);
        assertNull(summary.getTotalInterestEarned());
    }

    @Test
    public void testSavingsAccount_CLOSE_APPLICATION() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, "1000.0", true, false);
        Long savingsId = submitApproveActivate(clientId, savingsProductId);

        String closedOnDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.close.results.in.balance.not.zero",
                savingsHelper.closeSavingsExpectingError(savingsId, closedOnDate, false));

        closeSavings(savingsId, closedOnDate, true);
        SavingsTestValidators.verifySavingsIsClosed(savingsHelper.getSavingsStatus(savingsId));
    }

    @Test
    public void testSavingsAccount_WITH_ENFORCE_MIN_BALANCE() {
        String openingBalance = "1600";
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(openingBalance, null, "1500.0", true, false);

        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

        Long activationChargeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsActivationFeeCharge()).getResourceId();
        addPeriodCharge(savingsId, activationChargeId, true);

        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        BigDecimal balance = new BigDecimal(openingBalance).subtract(new BigDecimal(PERIOD_CHARGE_AMOUNT));
        SavingsTestValidators.verifyAmount(balance, savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying opening Balance");

        String transactionDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        String withdrawAmount = "800";
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsTransactionHelper.withdrawExpectingError(savingsId, withdrawAmount, transactionDate));

        balance = balance.add(new BigDecimal(DEPOSIT_AMOUNT));
        verifyTransactionAmountAndRunningBalance(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, transactionDate).getResourceId(),
                new BigDecimal(DEPOSIT_AMOUNT), balance, "Deposit");

        balance = balance.subtract(new BigDecimal(withdrawAmount));
        verifyTransactionAmountAndRunningBalance(savingsId, withdraw(savingsId, withdrawAmount, transactionDate).getResourceId(),
                new BigDecimal(withdrawAmount), balance, "Withdrawal");
    }

    @Test
    public void testSavingsAccount_DELETE_APPLICATION() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false);

        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.delete.not.in.submittedandpendingapproval.state",
                savingsHelper.deleteSavingsApplicationExpectingError(savingsId));

        savingsHelper.undoApproval(savingsId);
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

        deleteSavingsApplication(savingsId);

        SavingsTestValidators.verifyFirstErrorCode("error.msg.saving.account.id.invalid",
                savingsHelper.getSavingsDetailsExpectingError(savingsId));
    }

    @Test
    public void testSavingsAccount_REJECT_APPLICATION() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false);

        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.reject.not.in.submittedandpendingapproval.state",
                savingsHelper.rejectSavingsExpectingError(savingsId, SUBMITTED_ON_DATE_PLUS_ONE));

        savingsHelper.undoApproval(savingsId);
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.reject.cannot.be.a.future.date", savingsHelper
                .rejectSavingsExpectingError(savingsId, Utils.dateFormatter.format(Utils.getLocalDateOfTenant().plusYears(1))));

        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.reject.cannot.be.before.submittal.date",
                savingsHelper.rejectSavingsExpectingError(savingsId, SUBMITTED_ON_DATE_MINUS_ONE));

        savingsHelper.rejectSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsRejected(savingsHelper.getSavingsStatus(savingsId));
    }

    @Test
    public void testSavingsAccount_WITHDRAW_APPLICATION() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false);

        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

        savingsHelper.withdrawnByApplicant(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsWithdrawn(savingsHelper.getSavingsStatus(savingsId));
    }

    @Test
    public void testSavingsAccountTransactions() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false);

        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.account.is.not.active",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "100", TRANSACTION_DATE));
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.account.is.not.active",
                savingsTransactionHelper.depositExpectingError(savingsId, "100", TRANSACTION_DATE));

        activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        BigDecimal balance = new BigDecimal(MINIMUM_OPENING_BALANCE);
        SavingsTestValidators.verifyAmount(balance, savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying opening Balance");

        balance = balance.add(new BigDecimal(DEPOSIT_AMOUNT));
        verifyTransactionAmountAndRunningBalance(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, TRANSACTION_DATE).getResourceId(),
                new BigDecimal(DEPOSIT_AMOUNT), balance, "Deposit");

        Long withdrawTransactionId = withdraw(savingsId, WITHDRAW_AMOUNT, TRANSACTION_DATE).getResourceId();
        balance = balance.subtract(new BigDecimal(WITHDRAW_AMOUNT));
        verifyTransactionAmountAndRunningBalance(savingsId, withdrawTransactionId, new BigDecimal(WITHDRAW_AMOUNT), balance, "Withdrawal");

        Long adjustedTransactionId = savingsTransactionHelper
                .modifyTransaction(savingsId, withdrawTransactionId, WITHDRAW_AMOUNT_ADJUSTED, TRANSACTION_DATE).getResourceId();
        balance = balance.add(new BigDecimal(WITHDRAW_AMOUNT)).subtract(new BigDecimal(WITHDRAW_AMOUNT_ADJUSTED));
        verifyTransactionAmountAndRunningBalance(savingsId, adjustedTransactionId, new BigDecimal(WITHDRAW_AMOUNT_ADJUSTED), balance,
                "adjusted");
        SavingsTestValidators.verifyAmount(balance, savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying Adjusted Balance");
        assertTrue(Boolean.TRUE.equals(savingsTransactionHelper.getTransaction(savingsId, withdrawTransactionId).getReversed()),
                "The replaced withdrawal should be reversed");

        savingsTransactionHelper.undoTransaction(savingsId, adjustedTransactionId);
        assertTrue(Boolean.TRUE.equals(savingsTransactionHelper.getTransaction(savingsId, adjustedTransactionId).getReversed()),
                "The undone withdrawal should be reversed");
        balance = balance.add(new BigDecimal(WITHDRAW_AMOUNT_ADJUSTED));
        SavingsTestValidators.verifyAmount(balance, savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying Balance After Undo Transaction");

        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "5000", TRANSACTION_DATE));

        String futureDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant().plusYears(1));
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.in.the.future",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "5000", futureDate));
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.in.the.future",
                savingsTransactionHelper.depositExpectingError(savingsId, "5000", futureDate));

        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.before.activation.date",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "5000", SUBMITTED_ON_DATE_MINUS_ONE));
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.before.activation.date",
                savingsTransactionHelper.depositExpectingError(savingsId, "5000", SUBMITTED_ON_DATE_MINUS_ONE));
    }

    @Test
    public void testSavingsAccountCharges() {
        String submittedOnDate = "28 September 2022";
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        Long savingsId = null;
        try {
            updateBusinessDate(LocalDate.of(2022, 9, 28));

            Long clientId = createClient();
            Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false);
            savingsId = submitSavingsApplication(clientId, savingsProductId, submittedOnDate).getSavingsId();
            SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

            Long withdrawalChargeId = savingsChargeHelper.createWithdrawalFeeCharge().getResourceId();
            addPeriodCharge(savingsId, withdrawalChargeId, false);
            List<SavingsAccountChargeData> charges = savingsHelper.getSavingsAccountCharges(savingsId);
            assertEquals(1, charges.size());

            Long savingsChargeId = charges.get(0).getId();
            savingsChargeHelper.updateCharge(savingsId, savingsChargeId, "50");
            SavingsTestValidators.verifyAmount(new BigDecimal("50"), chargeById(savingsId, savingsChargeId).getAmount(),
                    "Verifying updated charge amount");

            assertEquals(savingsChargeId, savingsChargeHelper.deleteCharge(savingsId, savingsChargeId).getResourceId());
            assertTrue(isEmpty(savingsHelper.getSavingsAccountCharges(savingsId)), "The deleted charge should be gone");

            approveSavings(savingsId, submittedOnDate);
            SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
            activateSavings(savingsId, submittedOnDate);
            SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

            Long annualChargeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsAnnualFeeCharge()).getResourceId();
            assertTrue(isEmpty(savingsHelper.getSavingsAccountCharges(savingsId)), "The account should carry no charges yet");

            savingsChargeHelper.addChargeWithDueDateAndFeeOnMonthDay(savingsId, annualChargeId, "10 January 2023", PERIOD_CHARGE_AMOUNT,
                    PERIOD_CHARGE_FEE_ON_MONTH_DAY);
            charges = savingsHelper.getSavingsAccountCharges(savingsId);
            assertEquals(1, charges.size());

            SavingsAccountChargeData annualCharge = charges.get(0);
            Long annualSavingsChargeId = annualCharge.getId();

            updateBusinessDate(LocalDate.of(2023, 1, 16));
            SavingsTestValidators.verifyFirstErrorCode(
                    "validation.msg.savingsaccountcharge.inactivation.of.charge.not.allowed.when.charge.is.due",
                    savingsChargeHelper.inactivateChargeExpectingError(savingsId, annualSavingsChargeId));

            BigDecimal chargeAmount = annualCharge.getAmount();
            LocalDate chargeDueDate = annualCharge.getDueDate();

            LocalDate today = LocalDate.of(2024, 1, 17);
            int noChargeDues = today.getYear() - chargeDueDate.getYear();
            updateBusinessDate(today);

            for (int dueYearDiff = 0; dueYearDiff <= noChargeDues; dueYearDiff++) {
                savingsChargeHelper.payCharge(savingsId, annualSavingsChargeId, chargeAmount.toPlainString(),
                        Utils.dateFormatter.format(chargeDueDate.plusYears(dueYearDiff)));
                SavingsTestValidators.verifyAmount(chargeAmount.multiply(BigDecimal.valueOf(dueYearDiff + 1L)),
                        chargeById(savingsId, annualSavingsChargeId).getAmountPaid(), "Verifying paid annual fee");
            }

            assertEquals(annualSavingsChargeId, savingsChargeHelper.inactivateCharge(savingsId, annualSavingsChargeId).getResourceId(),
                    "Inactivated Savings Charges Id");

            Long monthlyFeeChargeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsMonthlyFeeCharge()).getResourceId();
            addPeriodCharge(savingsId, monthlyFeeChargeId, true);
            charges = savingsHelper.getSavingsAccountCharges(savingsId);
            assertEquals(2, charges.size());

            SavingsAccountChargeData monthlyCharge = charges.get(1);
            Long monthlySavingsChargeId = monthlyCharge.getId();
            SavingsTestValidators.verifyFirstErrorCode(
                    "validation.msg.savingsaccountcharge.inactivation.of.charge.not.allowed.when.charge.is.due",
                    savingsChargeHelper.inactivateChargeExpectingError(savingsId, monthlySavingsChargeId));

            savingsChargeHelper.waiveCharge(savingsId, monthlySavingsChargeId);
            SavingsTestValidators.verifyAmount(monthlyCharge.getAmount(), chargeById(savingsId, monthlySavingsChargeId).getAmountWaived(),
                    "Verifying waived monthly fee");

            savingsChargeHelper.waiveCharge(savingsId, monthlySavingsChargeId);
            SavingsTestValidators.verifyAmount(monthlyCharge.getAmount().add(monthlyCharge.getAmount()),
                    chargeById(savingsId, monthlySavingsChargeId).getAmountWaived(), "Verifying twice waived monthly fee");

            Long weeklyFeeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsWeeklyFeeCharge()).getResourceId();
            addPeriodCharge(savingsId, weeklyFeeId, true);
            charges = savingsHelper.getSavingsAccountCharges(savingsId);
            assertEquals(3, charges.size());

            SavingsAccountChargeData weeklyCharge = charges.get(2);
            Long weeklySavingsChargeId = weeklyCharge.getId();
            BigDecimal weeklyChargeAmount = weeklyCharge.getAmount();
            LocalDate weeklyChargeDueDate = weeklyCharge.getDueDate();
            SavingsTestValidators.verifyFirstErrorCode(
                    "validation.msg.savingsaccountcharge.inactivation.of.charge.not.allowed.when.charge.is.due",
                    savingsChargeHelper.inactivateChargeExpectingError(savingsId, weeklySavingsChargeId));

            // the scheduler job deducts the fee, so the account is funded well past what the charge needs
            deposit(savingsId, "100000", "17 January 2024");

            savingsChargeHelper.payCharge(savingsId, weeklySavingsChargeId, weeklyChargeAmount.toPlainString(),
                    Utils.dateFormatter.format(weeklyChargeDueDate));
            SavingsAccountChargeData paidCharge = chargeById(savingsId, weeklySavingsChargeId);
            SavingsTestValidators.verifyAmount(weeklyChargeAmount, paidCharge.getAmountPaid(), "Verifying paid weekly fee");
            assertEquals(weeklyChargeDueDate.plusWeeks(paidCharge.getFeeInterval()), paidCharge.getDueDate(),
                    "A paid weekly fee falls due again one interval later");
        } finally {
            updateBusinessDate(LocalDate.of(2024, 11, 11));
            if (savingsId != null) {
                closeSavings(savingsId, "11 November 2024", true);
            }
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    /**
     * Test case for overdraft account functionality. Open account with zero balance, perform transactions then post
     * interest and verify posted interest
     */
    @Test
    public void testSavingsAccountWithOverdraft() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct("0.0", null, null, false, true);
        Long savingsId = submitAndUpdateApplication(clientId, savingsProductId);

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        LocalDate firstDayOfPreviousMonth = Utils.getLocalDateOfTenant().minusMonths(1).withDayOfMonth(1);
        String activationDate = Utils.dateFormatter.format(firstDayOfPreviousMonth);
        String lastDayOfPreviousMonth = Utils.dateFormatter
                .format(firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth()));

        activateSavings(savingsId, activationDate);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
        savingsHelper.calculateInterest(savingsId);
        assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

        // no deposit precedes it, so the withdrawal takes the account into overdraft
        BigDecimal balance = new BigDecimal(WITHDRAW_AMOUNT).negate();
        verifyTransactionAmountAndRunningBalance(savingsId, withdraw(savingsId, WITHDRAW_AMOUNT, activationDate).getResourceId(),
                new BigDecimal(WITHDRAW_AMOUNT), balance, "Withdrawal");

        balance = balance.add(new BigDecimal(DEPOSIT_AMOUNT));
        verifyTransactionAmountAndRunningBalance(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, lastDayOfPreviousMonth).getResourceId(),
                new BigDecimal(DEPOSIT_AMOUNT), balance, "Deposit");

        savingsHelper.postInterest(savingsId);
        SavingsAccountData account = savingsHelper.getSavingsDetails(savingsId);
        // the deposit lands on the last day of the month and the balance is negative before it, so one day accrues
        assertEquals(expectedInterest(account, balance, 1), roundToThreeDecimals(account.getSummary().getTotalInterestPosted()),
                "Verifying interest posted");

        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.close.results.in.balance.not.zero",
                savingsHelper.closeSavingsExpectingError(savingsId, Utils.dateFormatter.format(Utils.getLocalDateOfTenant()), false));
    }

    @Test
    public void testSavingsAccountPostInterestOnLastDayWithOverdraft() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        try {
            updateBusinessDate(Utils.getLocalDateOfTenant());

            Long clientId = createClient();
            Long savingsProductId = createSavingsProduct("0.0", null, null, false, true);
            Long savingsId = submitAndUpdateApplication(clientId, savingsProductId);

            SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
            approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
            SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

            LocalDate previousMonth = Utils.getLocalDateOfTenant().minusMonths(1);
            activateSavings(savingsId, Utils.dateFormatter.format(previousMonth));
            SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

            SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
            savingsHelper.calculateInterest(savingsId);
            assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

            String lastDayOfPreviousMonth = Utils.dateFormatter.format(previousMonth.withDayOfMonth(previousMonth.lengthOfMonth()));
            LocalDate transactionDate = Utils.getLocalDateOfTenant().withDayOfMonth(2);
            updateBusinessDate(transactionDate);

            BigDecimal balance = new BigDecimal(DEPOSIT_AMOUNT);
            verifyTransactionAmountAndRunningBalance(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, lastDayOfPreviousMonth).getResourceId(),
                    new BigDecimal(DEPOSIT_AMOUNT), balance, "Deposit");

            savingsHelper.postInterest(savingsId);
            SavingsAccountData account = savingsHelper.getSavingsDetails(savingsId);
            BigDecimal expected = expectedInterest(account, balance, 1);
            assertEquals(expected, roundToThreeDecimals(account.getSummary().getTotalInterestPosted()), "Verifying interest posted");

            savingsTransactionHelper.postInterestAsOn(savingsId, Utils.dateFormatter.format(transactionDate));

            transactionDate = transactionDate.withDayOfMonth(3);
            updateBusinessDate(transactionDate);
            savingsTransactionHelper.postInterestAsOn(savingsId, Utils.dateFormatter.format(transactionDate));

            // posting as on a date the account has already posted past is refused
            transactionDate = transactionDate.withDayOfMonth(1);
            updateBusinessDate(transactionDate);
            savingsTransactionHelper.postInterestAsOnExpectingError(savingsId, Utils.dateFormatter.format(transactionDate));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testSavingsAccountPostInterestOnLastDayWithdrawalWithOverdraft() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        try {
            updateBusinessDate(Utils.getLocalDateOfTenant());

            Long clientId = createClient();
            Long savingsProductId = createSavingsProduct("0.0", null, null, false, true);
            Long savingsId = submitAndUpdateApplication(clientId, savingsProductId);

            SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
            approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
            SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

            LocalDate previousMonth = Utils.getLocalDateOfTenant().minusMonths(1);
            String activationDate = Utils.dateFormatter.format(previousMonth);
            activateSavings(savingsId, activationDate);
            SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

            SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
            savingsHelper.calculateInterest(savingsId);
            assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

            String lastDayOfPreviousMonth = Utils.dateFormatter.format(previousMonth.withDayOfMonth(previousMonth.lengthOfMonth()));
            LocalDate transactionDate = Utils.getLocalDateOfTenant().withDayOfMonth(2);
            updateBusinessDate(transactionDate);

            BigDecimal balance = new BigDecimal(WITHDRAW_AMOUNT).negate();
            verifyTransactionAmountAndRunningBalance(savingsId, withdraw(savingsId, WITHDRAW_AMOUNT, activationDate).getResourceId(),
                    new BigDecimal(WITHDRAW_AMOUNT), balance, "Withdrawal");

            balance = balance.add(new BigDecimal(DEPOSIT_AMOUNT));
            verifyTransactionAmountAndRunningBalance(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, lastDayOfPreviousMonth).getResourceId(),
                    new BigDecimal(DEPOSIT_AMOUNT), balance, "Deposit");

            savingsHelper.postInterest(savingsId);
            SavingsAccountData account = savingsHelper.getSavingsDetails(savingsId);
            BigDecimal expected = expectedInterest(account, balance, 1);
            assertEquals(expected, roundToThreeDecimals(account.getSummary().getTotalInterestPosted()), "Verifying interest posted");

            savingsTransactionHelper.postInterestAsOn(savingsId, Utils.dateFormatter.format(transactionDate));

            transactionDate = transactionDate.withDayOfMonth(3);
            updateBusinessDate(transactionDate);
            savingsTransactionHelper.postInterestAsOn(savingsId, Utils.dateFormatter.format(transactionDate));

            transactionDate = transactionDate.withDayOfMonth(1);
            updateBusinessDate(transactionDate);
            savingsTransactionHelper.postInterestAsOnExpectingError(savingsId, Utils.dateFormatter.format(transactionDate));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testSavingsAccountPostInterestWithOverdraft() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        try {
            updateBusinessDate(Utils.getLocalDateOfTenant());

            Long clientId = createClient();
            Long savingsProductId = createSavingsProduct("0.0", null, null, false, true);
            Long savingsId = submitAndUpdateApplication(clientId, savingsProductId);

            SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
            approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
            SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

            LocalDate firstDayOfPreviousMonth = Utils.getLocalDateOfTenant().minusMonths(1).withDayOfMonth(1);
            String activationDate = Utils.dateFormatter.format(firstDayOfPreviousMonth);
            String lastDayOfPreviousMonth = Utils.dateFormatter
                    .format(firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth()));

            LocalDate postedDate = Utils.getLocalDateOfTenant().withDayOfMonth(2);
            updateBusinessDate(postedDate);

            activateSavings(savingsId, activationDate);
            SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

            SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
            savingsHelper.calculateInterest(savingsId);
            assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

            BigDecimal balance = new BigDecimal(WITHDRAW_AMOUNT).negate();
            verifyTransactionAmountAndRunningBalance(savingsId, withdraw(savingsId, WITHDRAW_AMOUNT, activationDate).getResourceId(),
                    new BigDecimal(WITHDRAW_AMOUNT), balance, "Withdrawal");

            balance = balance.add(new BigDecimal(DEPOSIT_AMOUNT));
            verifyTransactionAmountAndRunningBalance(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, lastDayOfPreviousMonth).getResourceId(),
                    new BigDecimal(DEPOSIT_AMOUNT), balance, "Deposit");

            savingsHelper.postInterest(savingsId);
            SavingsAccountData account = savingsHelper.getSavingsDetails(savingsId, "transactions");
            assertEquals(expectedInterest(account, balance, 1), roundToThreeDecimals(account.getSummary().getTotalInterestPosted()),
                    "Verifying interest posted");

            savingsTransactionHelper.postInterestAsOn(savingsId, Utils.dateFormatter.format(postedDate));

            LocalDate interestPostingDate = account.getTransactions().get(0).getDate();
            String closedOnDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant().plusDays(1));

            if (Utils.getLocalDateOfTenant().equals(interestPostingDate)) {
                savingsHelper.closeSavingsValidatingPostedInterest(savingsId, closedOnDate, true);
            } else {
                SavingsTestValidators.verifyFirstErrorCode("error.msg.postInterest.notDone",
                        savingsHelper.closeSavingsValidatingPostedInterestExpectingError(savingsId, closedOnDate, true));
            }
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testPostInterestAsOnSavingsAccountWithOverdraft() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct("0.0", null, null, false, true);
        Long savingsId = submitAndUpdateApplication(clientId, savingsProductId);

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        LocalDate firstDayOfPreviousMonth = Utils.getLocalDateOfTenant().minusMonths(1).withDayOfMonth(1);
        String activationDate = Utils.dateFormatter.format(firstDayOfPreviousMonth);
        String lastDayOfPreviousMonth = Utils.dateFormatter
                .format(firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth()));
        String firstDayOfThisMonth = Utils.dateFormatter.format(Utils.getLocalDateOfTenant().withDayOfMonth(1));

        activateSavings(savingsId, activationDate);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
        savingsHelper.calculateInterest(savingsId);
        assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

        BigDecimal balance = new BigDecimal(WITHDRAW_AMOUNT).negate();
        verifyTransactionAmountAndRunningBalance(savingsId, withdraw(savingsId, WITHDRAW_AMOUNT, activationDate).getResourceId(),
                new BigDecimal(WITHDRAW_AMOUNT), balance, "Withdrawal");

        balance = balance.add(new BigDecimal(DEPOSIT_AMOUNT));
        verifyTransactionAmountAndRunningBalance(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, lastDayOfPreviousMonth).getResourceId(),
                new BigDecimal(DEPOSIT_AMOUNT), balance, "Deposit");

        savingsTransactionHelper.postInterestAsOn(savingsId, firstDayOfThisMonth);
        SavingsAccountData account = savingsHelper.getSavingsDetails(savingsId);
        BigDecimal expected = expectedInterest(account, balance, 1);
        assertEquals(expected, roundToThreeDecimals(account.getSummary().getTotalInterestPosted()), "Verifying interest posted");

        savingsTransactionHelper.postInterestAsOn(savingsId, Utils.dateFormatter.format(Utils.getLocalDateOfTenant()));
    }

    @Test
    public void testSavingsAccount_WITH_WITHHOLD_TAX() {
        Long clientId = createClient();
        Long taxGroupId = createTaxGroup("10");
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false, taxGroupId, false);
        Long savingsId = submitUpdateApproveActivate(clientId, savingsProductId);

        SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
        savingsHelper.calculateInterest(savingsId);
        assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

        savingsHelper.postInterest(savingsId);
        SavingsAccountSummaryData summary = savingsHelper.getSavingsSummary(savingsId);
        assertNotEquals(summaryBefore, summary);
        assertNotNull(summary.getTotalWithholdTax());

        BigDecimal expected = summary.getTotalDeposits().add(summary.getTotalInterestPosted()).subtract(summary.getTotalWithholdTax());
        verifyBalanceWithinOne(expected, summary.getAccountBalance());
    }

    @Test
    public void testSavingsAccount_WITH_WITHHOLD_TAX_DISABLE_AT_ACCOUNT_LEVEL() {
        Long clientId = createClient();
        Long taxGroupId = createTaxGroup("10");
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false, taxGroupId, false);
        Long savingsId = submitUpdateApproveActivate(clientId, savingsProductId);

        SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
        savingsHelper.calculateInterest(savingsId);
        assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

        assertNotNull(savingsHelper.updateWithHoldTaxStatus(savingsId, false).getChanges().getWithHoldTax());

        savingsHelper.postInterest(savingsId);
        SavingsAccountSummaryData summary = savingsHelper.getSavingsSummary(savingsId);
        assertNotEquals(summaryBefore, summary);
        assertNull(summary.getTotalWithholdTax());

        verifyBalanceWithinOne(summary.getTotalDeposits().add(summary.getTotalInterestPosted()), summary.getAccountBalance());
    }

    @Test
    public void testSavingsAccount_DormancyTracking() {
        Long clientId = createClient();
        Long taxGroupId = createTaxGroup("10");
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false, taxGroupId, true);
        Long noActivityFeeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsNoActivityFeeCharge()).getResourceId();

        List<Long> savingsList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Long savingsId = submitAndUpdateApplication(clientId, savingsProductId);
            addPeriodCharge(savingsId, noActivityFeeId, false);
            SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
            approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
            SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
            activateSavings(savingsId, TRANSACTION_DATE);
            SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
            savingsList.add(savingsId);
        }

        Long pendingSavingsId = submitAndUpdateApplication(clientId, savingsProductId);
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(pendingSavingsId));
        savingsList.add(pendingSavingsId);

        LocalDate transactionDate = Utils.getLocalDateOfTenant();
        for (int i = 0; i < 4; i++) {
            deposit(savingsList.get(i), DEPOSIT_AMOUNT, Utils.dateFormatter.format(transactionDate));
            transactionDate = transactionDate.minusDays(30);
        }

        schedulerHelper.executeAndAwaitJob("Update Savings Dormant Accounts");

        // within the inactivity window, so untouched
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsList.get(0)));
        SavingsTestValidators.verifySavingsSubStatusIsNone(savingsHelper.getSavingsSubStatus(savingsList.get(0)));
        SavingsTestValidators.verifyAmount(new BigDecimal("3000"), savingsHelper.getSavingsSummary(savingsList.get(0)).getAccountBalance(),
                "Verifying account Balance");

        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsList.get(1)));
        SavingsTestValidators.verifySavingsSubStatusIsInactive(savingsHelper.getSavingsSubStatus(savingsList.get(1)));
        SavingsTestValidators.verifyAmount(new BigDecimal("2900"), savingsHelper.getSavingsSummary(savingsList.get(1)).getAccountBalance(),
                "Verifying account Balance");

        // a transaction on an inactive account brings it back
        deposit(savingsList.get(1), DEPOSIT_AMOUNT, Utils.dateFormatter.format(Utils.getLocalDateOfTenant()));
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsList.get(1)));
        SavingsTestValidators.verifySavingsSubStatusIsNone(savingsHelper.getSavingsSubStatus(savingsList.get(1)));

        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsList.get(2)));
        SavingsTestValidators.verifySavingsSubStatusIsDormant(savingsHelper.getSavingsSubStatus(savingsList.get(2)));
        SavingsTestValidators.verifyAmount(new BigDecimal("2900"), savingsHelper.getSavingsSummary(savingsList.get(2)).getAccountBalance(),
                "Verifying account Balance");

        deposit(savingsList.get(2), DEPOSIT_AMOUNT, Utils.dateFormatter.format(Utils.getLocalDateOfTenant()));
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsList.get(2)));
        SavingsTestValidators.verifySavingsSubStatusIsNone(savingsHelper.getSavingsSubStatus(savingsList.get(2)));

        // escheat, because its only transaction is older than the escheat window
        SavingsTestValidators.verifySavingsIsClosed(savingsHelper.getSavingsStatus(savingsList.get(3)));
        SavingsTestValidators.verifySavingsSubStatusIsEscheat(savingsHelper.getSavingsSubStatus(savingsList.get(3)));
        SavingsTestValidators.verifyAmount(new BigDecimal("2900"), savingsHelper.getSavingsSummary(savingsList.get(3)).getAccountBalance(),
                "Verifying account Balance");

        // escheat, because it never had a transaction after activation
        SavingsTestValidators.verifySavingsIsClosed(savingsHelper.getSavingsStatus(savingsList.get(4)));
        SavingsTestValidators.verifySavingsSubStatusIsEscheat(savingsHelper.getSavingsSubStatus(savingsList.get(4)));
        SavingsTestValidators.verifyAmount(new BigDecimal("900"), savingsHelper.getSavingsSummary(savingsList.get(4)).getAccountBalance(),
                "Verifying account Balance");

        // the job leaves accounts that were never activated alone
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsList.get(5)));
        SavingsTestValidators.verifySavingsSubStatusIsNone(savingsHelper.getSavingsSubStatus(savingsList.get(5)));
    }

    @Test
    public void testSavingsAccountBlockStatus() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false, false);
        Long savingsId = submitApproveActivate(clientId, savingsProductId);

        BigDecimal balance = new BigDecimal(MINIMUM_OPENING_BALANCE);

        savingsHelper.blockSavings(savingsId, REASON_FOR_BLOCK);
        SavingsTestValidators.verifySavingsSubStatusIsBlocked(savingsHelper.getSavingsSubStatus(savingsId));
        SavingsTestValidators.verifyFirstErrorCode("error.msg.saving.account.blocked.transaction.not.allowed",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "100", TRANSACTION_DATE));
        SavingsTestValidators.verifyFirstErrorCode("error.msg.saving.account.blocked.transaction.not.allowed",
                savingsTransactionHelper.depositExpectingError(savingsId, "100", TRANSACTION_DATE));

        savingsHelper.unblockSavings(savingsId);
        SavingsTestValidators.verifySavingsSubStatusIsNone(savingsHelper.getSavingsSubStatus(savingsId));
        balance = balance.add(new BigDecimal(DEPOSIT_AMOUNT));
        verifyTransactionAmount(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, TRANSACTION_DATE).getResourceId(),
                new BigDecimal(DEPOSIT_AMOUNT), "Deposit");

        savingsHelper.blockDebit(savingsId, REASON_FOR_BLOCK);
        SavingsTestValidators.verifySavingsSubStatusIsDebitBlocked(savingsHelper.getSavingsSubStatus(savingsId));
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savings.account.debit.transaction.not.allowed",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "100", TRANSACTION_DATE));
        balance = balance.add(new BigDecimal(DEPOSIT_AMOUNT));
        verifyTransactionAmount(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, TRANSACTION_DATE).getResourceId(),
                new BigDecimal(DEPOSIT_AMOUNT), "Deposit");

        savingsHelper.unblockDebit(savingsId);
        SavingsTestValidators.verifySavingsSubStatusIsNone(savingsHelper.getSavingsSubStatus(savingsId));
        balance = balance.subtract(new BigDecimal(WITHDRAW_AMOUNT));
        verifyTransactionAmount(savingsId, withdraw(savingsId, WITHDRAW_AMOUNT, TRANSACTION_DATE).getResourceId(),
                new BigDecimal(WITHDRAW_AMOUNT), "Withdrawal");

        savingsHelper.blockCredit(savingsId, REASON_FOR_BLOCK);
        SavingsTestValidators.verifySavingsSubStatusIsCreditBlocked(savingsHelper.getSavingsSubStatus(savingsId));
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savings.account.credit.transaction.not.allowed",
                savingsTransactionHelper.depositExpectingError(savingsId, "100", TRANSACTION_DATE));
        balance = balance.subtract(new BigDecimal(WITHDRAW_AMOUNT));
        verifyTransactionAmount(savingsId, withdraw(savingsId, WITHDRAW_AMOUNT, TRANSACTION_DATE).getResourceId(),
                new BigDecimal(WITHDRAW_AMOUNT), "Withdrawal");

        savingsHelper.unblockCredit(savingsId);
        SavingsTestValidators.verifySavingsSubStatusIsNone(savingsHelper.getSavingsSubStatus(savingsId));
        balance = balance.add(new BigDecimal(DEPOSIT_AMOUNT));
        verifyTransactionAmount(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, TRANSACTION_DATE).getResourceId(),
                new BigDecimal(DEPOSIT_AMOUNT), "Deposit");

        Long holdTransactionId = savingsTransactionHelper
                .holdAmount(savingsId, balance.subtract(new BigDecimal("100")).toPlainString(), TRANSACTION_DATE, REASON_FOR_BLOCK, false)
                .getResourceId();
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "300", TRANSACTION_DATE_PLUS_ONE));

        savingsTransactionHelper.releaseAmount(savingsId, holdTransactionId);
        verifyTransactionAmount(savingsId,
                withdraw(savingsId, "300", Utils.dateFormatter.format(Utils.getLocalDateOfTenant())).getResourceId(), new BigDecimal("300"),
                "Withdrawal");
    }

    @Test
    public void testSavingsAccountLienAllowedAtProductLevelWithEnforceBalance() {
        Long clientId = createClient();
        Long savingsProductId = createLienSavingsProduct(MINIMUM_OPENING_BALANCE, null, true, false, true);
        Long savingsId = submitApproveActivate(clientId, savingsProductId);

        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.insufficient.balance",
                savingsTransactionHelper.holdAmountExpectingError(savingsId, "2000", TRANSACTION_DATE, REASON_FOR_BLOCK, false));

        savingsTransactionHelper.holdAmount(savingsId, "2000", TRANSACTION_DATE, REASON_FOR_BLOCK, true);
        SavingsTestValidators.verifyAmount(new BigDecimal("-1000"), savingsHelper.getSavingsSummary(savingsId).getAvailableBalance(),
                "Verifying available Balance is -1000");

        deposit(savingsId, "1200", TRANSACTION_DATE);
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "200", today));

        assertNotNull(withdraw(savingsId, "100", today).getResourceId());
    }

    @Test
    public void testSavingsAccountLienAllowedAtProductLevelWithOverDraftLimit() {
        Long clientId = createClient();
        Long savingsProductId = createLienSavingsProduct(MINIMUM_OPENING_BALANCE, null, false, true, true);
        Long savingsId = submitApproveActivate(clientId, savingsProductId);

        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.insufficient.balance",
                savingsTransactionHelper.holdAmountExpectingError(savingsId, "2000", TRANSACTION_DATE, REASON_FOR_BLOCK, false));

        savingsTransactionHelper.holdAmount(savingsId, "2000", TRANSACTION_DATE, REASON_FOR_BLOCK, true);
        SavingsTestValidators.verifyAmount(new BigDecimal("-1000"), savingsHelper.getSavingsSummary(savingsId).getAvailableBalance(),
                "Verifying available Balance is -1000");

        deposit(savingsId, "1200", TRANSACTION_DATE);
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "300", today));

        assertNotNull(withdraw(savingsId, "200", today).getResourceId());
    }

    @Test
    public void testSavingsAccountLienAllowedAtProductLevelWithNoConfig() {
        Long clientId = createClient();
        Long savingsProductId = createLienSavingsProduct(MINIMUM_OPENING_BALANCE, null, false, false, true);
        Long savingsId = submitApproveActivate(clientId, savingsProductId);

        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.insufficient.balance",
                savingsTransactionHelper.holdAmountExpectingError(savingsId, "2000", TRANSACTION_DATE, REASON_FOR_BLOCK, false));

        savingsTransactionHelper.holdAmount(savingsId, "2000", TRANSACTION_DATE, REASON_FOR_BLOCK, true);
        SavingsTestValidators.verifyAmount(new BigDecimal("-1000"), savingsHelper.getSavingsSummary(savingsId).getAvailableBalance(),
                "Verifying available Balance is -1000");

        deposit(savingsId, "1100", TRANSACTION_DATE);
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "200", today));

        assertNotNull(withdraw(savingsId, "100", today).getResourceId());
    }

    @Test
    public void testSavingsAccountWithoutLienAllowed() {
        Long clientId = createClient();
        Long savingsProductId = createLienSavingsProduct(MINIMUM_OPENING_BALANCE, null, false, true, false);
        Long savingsId = submitApproveActivate(clientId, savingsProductId);

        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.lien.is.not.allowed.in.product.level",
                savingsTransactionHelper.holdAmountExpectingError(savingsId, "2000", TRANSACTION_DATE, REASON_FOR_BLOCK, true));

        // 1500 is what the overdraft limit alone allows to be held
        savingsTransactionHelper.holdAmount(savingsId, "1500", TRANSACTION_DATE, REASON_FOR_BLOCK, false);
        SavingsTestValidators.verifyAmount(new BigDecimal("-500"), savingsHelper.getSavingsSummary(savingsId).getAvailableBalance(),
                "Verifying available Balance is -500");

        deposit(savingsId, "2000", TRANSACTION_DATE);
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "1600", today));

        assertNotNull(withdraw(savingsId, "1500", today).getResourceId());
    }

    @Test
    public void testSavingsAccountLienAllowedAtProductLevelWithOverDraftLimitGreaterThanLienLimit() {
        assertNotNull(savingsProductHelper.createSavingsProductExpectingError(
                lienSavingsProductRequest(MINIMUM_OPENING_BALANCE, null, false, true, "2000.0", true, "1000.0")));
    }

    /**
     * incorrect savings account balance when charge transaction is reversed during an overdraft recalculate Daily
     * Balances
     */
    @Test
    public void testAccountBalanceAfterTransactionReversal() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct("0", null, "500", false, true);

        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();
        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        Long depositTransactionId = deposit(savingsId, "500", TRANSACTION_DATE).getResourceId();

        Long chargeId = savingsChargeHelper
                .createCharge(SavingsRequestBuilders.savingsSpecifiedDueDateCharge(300.0, SavingsTestData.CURRENCY_CODE)).getResourceId();
        Long savingsChargeId = savingsChargeHelper.addChargeWithDueDate(savingsId, chargeId, TRANSACTION_DATE, "300").getResourceId();
        savingsChargeHelper.payCharge(savingsId, savingsChargeId, "300", TRANSACTION_DATE);

        savingsTransactionHelper.undoTransaction(savingsId, depositTransactionId);
        assertTrue(Boolean.TRUE.equals(savingsTransactionHelper.getTransaction(savingsId, depositTransactionId).getReversed()),
                "The undone deposit should be reversed");

        SavingsTestValidators.verifyAmount(new BigDecimal("-300"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying opening Balance is -300");
    }

    @Test
    public void testSavingsAccountWithdrawalChargesOnPaymentTypes() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct("10000", null, null, false, false);
        Long savingsId = submitApproveActivate(clientId, savingsProductId);

        Long paymentTypeIdOne = createPaymentType();
        Long chargeIdOne = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsWithdrawalFeeCharge(10.0, paymentTypeIdOne))
                .getResourceId();
        savingsChargeHelper.addChargeWithFeeOnMonthDay(savingsId, chargeIdOne, "10", PERIOD_CHARGE_FEE_ON_MONTH_DAY);

        savingsTransactionHelper.withdraw(savingsId, "1000", TRANSACTION_DATE, paymentTypeIdOne);
        // 10,000 opening - 1,000 withdrawn - 10 charge
        SavingsTestValidators.verifyAmount(new BigDecimal("8990"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying Balance after withdrawal charge ");

        Long paymentTypeIdTwo = createPaymentType();
        Long chargeIdTwo = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsWithdrawalFeeCharge(20.0, paymentTypeIdTwo))
                .getResourceId();
        savingsChargeHelper.addChargeWithFeeOnMonthDay(savingsId, chargeIdTwo, "20", PERIOD_CHARGE_FEE_ON_MONTH_DAY);

        savingsTransactionHelper.withdraw(savingsId, "2000", TRANSACTION_DATE, paymentTypeIdTwo);
        // 8,990 - 2,000 withdrawn - 20 charge
        SavingsTestValidators.verifyAmount(new BigDecimal("6970"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying Balance after withdrawal charge two ");
    }

    /**
     * Test Transaction reversal feature, here a new reversal transaction is posted when a savings transaction is
     * reversed
     */
    @Test
    public void testAccountBalanceAfterSavingsTransactionReversalPosting() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct("0", null, "0", false, true);

        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();
        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        Long depositTransactionId = deposit(savingsId, "500", TRANSACTION_DATE).getResourceId();
        savingsTransactionHelper.reverseTransaction(savingsId, depositTransactionId);

        assertTrue(Boolean.TRUE.equals(savingsTransactionHelper.getTransaction(savingsId, depositTransactionId).getReversed()),
                "The reversed deposit should be marked reversed");
        assertTrue(Boolean.TRUE.equals(savingsTransactionHelper.getTransactions(savingsId).get(0).getIsReversal()),
                "A reversal transaction should have been posted");

        SavingsTestValidators.verifyAmount(BigDecimal.ZERO, savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying balance is back to zero");
    }

    @Test
    public void testReversalWhenIsBulkIsTrue() {
        Long savingsId = createSavingsAccountWithWithdrawalFee();
        Long withdrawalTransactionId = withdraw(savingsId, "500", TRANSACTION_DATE).getResourceId();
        SavingsTestValidators.verifyAmount(new BigDecimal("400.0"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying account balance is 400");

        // a bulk reversal takes the charge the withdrawal paid with it
        savingsTransactionHelper.reverseTransaction(savingsId, withdrawalTransactionId, true);
        SavingsTestValidators.verifyAmount(new BigDecimal("1000.0"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying account balance is 1000");
    }

    @Test
    public void testReversalWhenIsBulkIsFalse() {
        Long savingsId = createSavingsAccountWithWithdrawalFee();
        Long withdrawalTransactionId = withdraw(savingsId, "500", TRANSACTION_DATE).getResourceId();
        SavingsTestValidators.verifyAmount(new BigDecimal("400.0"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying account balance is 400");

        // a non-bulk reversal leaves the charge the withdrawal paid in place
        savingsTransactionHelper.reverseTransaction(savingsId, withdrawalTransactionId, false);
        SavingsTestValidators.verifyAmount(new BigDecimal("900.0"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying account balance is 900");
    }

    @Test
    public void testAccountBalanceAndTransactionRunningBalanceWithConfigOn() {
        configurationForBackdatedTransaction();

        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct("0", null, "0", false, true);
        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();
        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        String startDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant().minusDays(5));

        withdraw(savingsId, "500", startDate);
        SavingsTestValidators.verifyAmount(new BigDecimal("-500.0"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying account balance is -500");

        withdraw(savingsId, "500", startDate);
        SavingsTestValidators.verifyAmount(new BigDecimal("-1000.0"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying account balance is -1000");

        List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
        SavingsTestValidators.verifyAmount(new BigDecimal("-1000.0"), transactions.get(transactions.size() - 2).getRunningBalance(),
                "Equality check for Balance");
    }

    @Test
    public void testSavingsAccountChargesBackDate() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct("0", null, null, false, false);
        Long savingsId = submitUpdateApproveActivate(clientId, savingsProductId);

        Long chargeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsSpecifiedDueDateCharge()).getResourceId();
        assertTrue(isEmpty(savingsHelper.getSavingsAccountCharges(savingsId)), "The account should carry no charges yet");

        deposit(savingsId, "100", "05 March 2013");
        deposit(savingsId, "100", "07 March 2013");

        Long savingsChargeId = savingsChargeHelper.addChargeWithDueDate(savingsId, chargeId, "07 March 2013", "200").getResourceId();

        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsChargeHelper.payChargeExpectingError(savingsId, savingsChargeId, "200", "06 March 2013"));

        assertNotNull(savingsChargeHelper.payCharge(savingsId, savingsChargeId, "200", "07 March 2013").getResourceId());
    }

    @Test
    public void testAnnualChargePaymentAfterDueDate() {
        String submittedOnDate = "01 January 2023";
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        Long savingsId = null;
        try {
            updateBusinessDate(LocalDate.of(2023, 1, 1));

            Long clientId = createClient();
            Long savingsProductId = createSavingsProduct("1000", null, null, false, false);

            savingsId = submitSavingsApplication(clientId, savingsProductId, submittedOnDate).getSavingsId();
            SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
            approveSavings(savingsId, submittedOnDate);
            SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
            activateSavings(savingsId, submittedOnDate);
            SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

            Long chargeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsAnnualFeeCharge()).getResourceId();
            savingsChargeHelper.addChargeWithDueDateAndFeeOnMonthDay(savingsId, chargeId, "15 February 2023", PERIOD_CHARGE_AMOUNT,
                    "15 February");

            List<SavingsAccountChargeData> charges = savingsHelper.getSavingsAccountCharges(savingsId);
            assertEquals(1, charges.size());

            Long annualSavingsChargeId = charges.get(0).getId();
            BigDecimal chargeAmount = charges.get(0).getAmount();

            LocalDate paymentDate = LocalDate.of(2023, 3, 1);
            updateBusinessDate(paymentDate);

            assertNotNull(savingsChargeHelper
                    .payCharge(savingsId, annualSavingsChargeId, chargeAmount.toPlainString(), Utils.dateFormatter.format(paymentDate))
                    .getResourceId());
            SavingsTestValidators.verifyAmount(chargeAmount, chargeById(savingsId, annualSavingsChargeId).getAmountPaid(),
                    "Verifying paid annual fee");
        } finally {
            if (savingsId != null) {
                updateBusinessDate(LocalDate.of(2024, 11, 11));
                closeSavings(savingsId, "11 November 2024", true);
            }
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testRunningBalanceAfterWithdrawalWithBackdateConfigurationOn() {
        configurationForBackdatedTransaction();
        LocalDate transactionDate = Utils.getLocalDateOfTenant().minusDays(5);
        String startDate = Utils.dateFormatter.format(transactionDate);
        String secondTrx = Utils.dateFormatter.format(transactionDate.plusDays(1));

        Long clientId = createClient(startDate);
        Long savingsId = createSavingsAccountDailyPostingOverdraft(clientId, startDate);

        deposit(savingsId, "100", startDate);
        schedulerHelper.executeAndAwaitJob(POST_INTEREST_FOR_SAVINGS_JOB);
        withdraw(savingsId, "200", secondTrx);

        SavingsTestValidators.verifyAmount(new BigDecimal("-100.0822"), savingsHelper.getSavingsSummary(savingsId).getAvailableBalance(),
                "Equality check for Balance");
    }

    @Test
    public void testRunningBalanceAfterDepositWithBackdateConfigurationOn() {
        configurationForBackdatedTransaction();
        LocalDate transactionDate = Utils.getLocalDateOfTenant().minusDays(5);
        String startDate = Utils.dateFormatter.format(transactionDate);
        String secondTrx = Utils.dateFormatter.format(transactionDate.plusDays(1));

        Long clientId = createClient(startDate);
        Long savingsId = createSavingsAccountDailyPostingOverdraft(clientId, startDate);

        withdraw(savingsId, "100", startDate);
        schedulerHelper.executeAndAwaitJob(POST_INTEREST_FOR_SAVINGS_JOB);
        deposit(savingsId, "200", secondTrx);

        SavingsTestValidators.verifyAmount(new BigDecimal("100.0822"), savingsHelper.getSavingsSummary(savingsId).getAvailableBalance(),
                "Equality check for Balance");
    }

    @Test
    public void testRunningBalanceAfterWithdrawalReversalWithBackdateConfigurationOn() {
        configurationForBackdatedTransaction();
        LocalDate transactionDate = Utils.getLocalDateOfTenant().minusDays(5);
        String startDate = Utils.dateFormatter.format(transactionDate);
        String secondTrx = Utils.dateFormatter.format(transactionDate.plusDays(1));

        Long clientId = createClient(startDate);
        Long savingsId = createSavingsAccountDailyPostingOverdraft(clientId, startDate);

        deposit(savingsId, "100", startDate);
        schedulerHelper.executeAndAwaitJob(POST_INTEREST_FOR_SAVINGS_JOB);
        Long withdrawalToReverse = withdraw(savingsId, "200", secondTrx).getResourceId();
        savingsTransactionHelper.reverseTransaction(savingsId, withdrawalToReverse);

        SavingsTestValidators.verifyAmount(new BigDecimal("100.137"), savingsHelper.getSavingsSummary(savingsId).getAvailableBalance(),
                "Equality check for Balance");
    }

    @Test
    public void testRunningBalanceAfterDepositReversalWithBackdateConfigurationOn() {
        configurationForBackdatedTransaction();
        LocalDate transactionDate = Utils.getLocalDateOfTenant().minusDays(5);
        String startDate = Utils.dateFormatter.format(transactionDate);
        String secondTrx = Utils.dateFormatter.format(transactionDate.plusDays(1));

        Long clientId = createClient(startDate);
        Long savingsId = createSavingsAccountDailyPostingOverdraft(clientId, startDate);

        withdraw(savingsId, "100", startDate);
        schedulerHelper.executeAndAwaitJob(POST_INTEREST_FOR_SAVINGS_JOB);
        Long depositToReverse = deposit(savingsId, "200", secondTrx).getResourceId();
        savingsTransactionHelper.reverseTransaction(savingsId, depositToReverse);

        SavingsTestValidators.verifyAmount(new BigDecimal("-100.137"), savingsHelper.getSavingsSummary(savingsId).getAvailableBalance(),
                "Equality check for Balance");
    }

    @Test
    public void testToPerformTransactionBeforePivotDate() {
        configurationForBackdatedTransaction();
        String startDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant().minusDays(10));

        Long clientId = createClient(startDate);
        Long savingsId = createSavingsAccountDailyPostingOverdraft(clientId, startDate);

        deposit(savingsId, "200", startDate);
        schedulerHelper.executeAndAwaitJob(POST_INTEREST_FOR_SAVINGS_JOB);

        SavingsTestValidators.verifyFirstErrorCode("error.msg.savings.transaction.is.not.allowed",
                savingsTransactionHelper.depositExpectingError(savingsId, "300", startDate));
    }

    @Test
    public void testReversalEntriesAfterSystemReversingTransactionWithReversalConfigOn() {
        assertTrue(hasReversalTransaction(reverseByBackdatedWithdrawal(true)),
                "A reversal transaction should be posted while the configuration is on");
    }

    @Test
    public void testReversalEntriesAfterSystemReversingTransactionWithReversalConfigOff() {
        assertFalse(hasReversalTransaction(reverseByBackdatedWithdrawal(false)),
                "No reversal transaction should be posted while the configuration is off");
    }

    @Test
    public void testSavingsAccountDepositAfterHoldAmount() {
        Long clientId = createClient();
        Long savingsProductId = createLienSavingsProduct("0", null, false, true, false);
        Long savingsId = submitApproveActivate(clientId, savingsProductId);

        savingsTransactionHelper.holdAmount(savingsId, "100", TRANSACTION_DATE, REASON_FOR_BLOCK, false);
        assertNotNull(deposit(savingsId, "200", TRANSACTION_DATE).getResourceId());

        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsTransactionHelper.withdrawExpectingError(savingsId, "200", TRANSACTION_DATE));
    }

    @Test
    public void testSavingsAccountWithdrawalWithoutPriorTransactionsWithoutOverdraft() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct("0.0", null, null, false, false);
        Long savingsId = submitAndUpdateApplication(clientId, savingsProductId);

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        LocalDate firstDayOfPreviousMonth = Utils.getLocalDateOfTenant().minusMonths(1).withDayOfMonth(1);
        activateSavings(savingsId, Utils.dateFormatter.format(firstDayOfPreviousMonth));
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        String lastDayOfPreviousMonth = Utils.dateFormatter
                .format(firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth()));
        SavingsTestValidators.verifyFirstErrorCode("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsTransactionHelper.withdrawExpectingError(savingsId, WITHDRAW_AMOUNT, lastDayOfPreviousMonth));
    }

    @Test
    public void testWithdrawalWithPriorTransactionsWithOverdraft_AMT_GT_Balance() {
        verifyWithdrawalAgainstOpeningBalance("500.00", true);
    }

    @Test
    public void testWithdrawalWithPriorTransactionsWithOverdraft_AMT_LT_Balance() {
        verifyWithdrawalAgainstOpeningBalance("2000", false);
    }

    @Test
    public void testWithdrawalWithPriorTransactionsWithOverdraft_AMT_EQ_Balance() {
        verifyWithdrawalAgainstOpeningBalance("1000", false);
    }

    private Long submitAndUpdateApplication(Long clientId, Long savingsProductId) {
        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();
        assertNotNull(savingsHelper.updateSavingsApplication(savingsId, clientId, savingsProductId, SUBMITTED_ON_DATE_PLUS_ONE).getChanges()
                .getSubmittedOnDate());
        return savingsId;
    }

    private Long submitUpdateApproveActivate(Long clientId, Long savingsProductId) {
        return approveAndActivate(submitAndUpdateApplication(clientId, savingsProductId));
    }

    private Long submitApproveActivate(Long clientId, Long savingsProductId) {
        return approveAndActivate(submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId());
    }

    private Long approveAndActivate(Long savingsId) {
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private Long createSavingsAccountWithWithdrawalFee() {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, "0", false, false);
        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();

        Long withdrawalChargeId = savingsChargeHelper.createWithdrawalFeeCharge().getResourceId();
        addPeriodCharge(savingsId, withdrawalChargeId, false);

        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private Long createSavingsAccountDailyPostingOverdraft(Long clientId, String startDate) {
        Long savingsProductId = savingsProductHelper.createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct()//
                .withdrawalFeeForTransfers(true)//
                .withHoldTax(false)//
                .interestPostingPeriodType(SavingsTestData.InterestPostingPeriodType.DAILY)//
                .allowOverdraft(true)//
                .overdraftLimit(new BigDecimal("10000.0"))//
                .nominalAnnualInterestRateOverdraft(new BigDecimal("10"))).getResourceId();

        Long savingsId = submitSavingsApplication(clientId, savingsProductId, startDate).getSavingsId();
        approveSavings(savingsId, startDate);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        activateSavings(savingsId, startDate);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    /** Two withdrawals either side of an interest posting; the second makes the system reverse and repost. */
    private Long reverseByBackdatedWithdrawal(boolean postReversalTransactions) {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_POST_REVERSAL_TXNS_FOR_REVERSE_TRANSACTIONS,
                new PutGlobalConfigurationsRequest().enabled(postReversalTransactions));

        LocalDate transactionDate = Utils.getLocalDateOfTenant().minusDays(5);
        String startDate = Utils.dateFormatter.format(transactionDate);
        String nextTransaction = Utils.dateFormatter.format(transactionDate.plusDays(2));

        Long clientId = createClient(startDate);
        Long savingsId = createSavingsAccountDailyPostingOverdraft(clientId, startDate);
        withdraw(savingsId, "100", startDate);
        schedulerHelper.executeAndAwaitJob(POST_INTEREST_FOR_SAVINGS_JOB);
        withdraw(savingsId, "100", nextTransaction);
        return savingsId;
    }

    private boolean hasReversalTransaction(Long savingsId) {
        return savingsTransactionHelper.getTransactions(savingsId).stream()
                .anyMatch(transaction -> Boolean.TRUE.equals(transaction.getIsReversal()));
    }

    /** Activates on the first of the previous month, then withdraws against the opening balance alone. */
    private void verifyWithdrawalAgainstOpeningBalance(String openingBalance, boolean allowOverdraft) {
        Long clientId = createClient();
        Long savingsProductId = createSavingsProduct(openingBalance, null, null, false, allowOverdraft);
        Long savingsId = submitAndUpdateApplication(clientId, savingsProductId);

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        String activationDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant().minusMonths(1).withDayOfMonth(1));
        activateSavings(savingsId, activationDate);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        BigDecimal balance = new BigDecimal(openingBalance).subtract(new BigDecimal(WITHDRAW_AMOUNT));
        verifyTransactionAmountAndRunningBalance(savingsId, withdraw(savingsId, WITHDRAW_AMOUNT, activationDate).getResourceId(),
                new BigDecimal(WITHDRAW_AMOUNT), balance, "Withdrawal");
    }

    /** The four knobs the legacy product builder exposed; overdraft, when on, is capped at 2000. */
    private Long createSavingsProduct(String minOpeningBalance, String minBalanceForInterestCalculation, String minRequiredBalance,
            boolean enforceMinRequiredBalance, boolean allowOverdraft) {
        return createSavingsProduct(minOpeningBalance, minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance,
                allowOverdraft, null, false);
    }

    private Long createSavingsProduct(String minOpeningBalance, String minBalanceForInterestCalculation, String minRequiredBalance,
            boolean enforceMinRequiredBalance, boolean allowOverdraft, Long taxGroupId, boolean withDormancy) {
        PostSavingsProductsRequest request = SavingsRequestBuilders.defaultSavingsProduct()//
                .withdrawalFeeForTransfers(true)//
                .minRequiredOpeningBalance(amount(minOpeningBalance))//
                .minBalanceForInterestCalculation(amount(minBalanceForInterestCalculation))//
                .minRequiredBalance(amount(minRequiredBalance))//
                .enforceMinRequiredBalance(enforceMinRequiredBalance)//
                .withHoldTax(taxGroupId != null)//
                .taxGroupId(taxGroupId);
        if (allowOverdraft) {
            request.allowOverdraft(true).overdraftLimit(new BigDecimal("2000.0"));
        }
        if (withDormancy) {
            request.isDormancyTrackingActive(true).daysToInactive(30L).daysToDormancy(60L).daysToEscheat(90L);
        }
        return savingsProductHelper.createSavingsProduct(request).getResourceId();
    }

    /** Each lien knob brings its own limit, which is why they are not parameters of the plain product. */
    private PostSavingsProductsRequest lienSavingsProductRequest(String minOpeningBalance, String minBalanceForInterestCalculation,
            boolean enforceMinRequiredBalance, boolean allowOverdraft, String overdraftLimit, boolean lienAllowed,
            String maxAllowedLienLimit) {
        PostSavingsProductsRequest request = SavingsRequestBuilders.defaultSavingsProduct()//
                .withdrawalFeeForTransfers(true)//
                .withHoldTax(false)//
                .minRequiredOpeningBalance(amount(minOpeningBalance))//
                .minBalanceForInterestCalculation(amount(minBalanceForInterestCalculation));
        if (lienAllowed) {
            request.lienAllowed(true).maxAllowedLienLimit(new BigDecimal(maxAllowedLienLimit));
        }
        if (enforceMinRequiredBalance) {
            request.minRequiredBalance(new BigDecimal("100.0")).enforceMinRequiredBalance(true);
        }
        if (allowOverdraft) {
            request.allowOverdraft(true).overdraftLimit(new BigDecimal(overdraftLimit));
        }
        return request;
    }

    private Long createLienSavingsProduct(String minOpeningBalance, String minBalanceForInterestCalculation,
            boolean enforceMinRequiredBalance, boolean allowOverdraft, boolean lienAllowed) {
        return savingsProductHelper.createSavingsProduct(lienSavingsProductRequest(minOpeningBalance, minBalanceForInterestCalculation,
                enforceMinRequiredBalance, allowOverdraft, "500.0", lienAllowed, "2000.0")).getResourceId();
    }

    private Long createTaxGroup(String percentage) {
        Long taxComponentId = taxComponentHelper.createTaxComponent(new PostTaxesComponentsRequest()//
                .name(Utils.uniqueRandomStringGenerator("Tax_component_Name_", 5))//
                .percentage(Float.parseFloat(percentage))//
                .startDate("01 January 2013")//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE)).getResourceId();

        return taxGroupHelper.createTaxGroup(new PostTaxesGroupRequest()//
                .name(Utils.uniqueRandomStringGenerator("Tax_group_Name_", 5))//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE)//
                .taxComponents(Set.of(new PostTaxesGroupTaxComponents().taxComponentId(taxComponentId).startDate("01 January 2013"))))
                .getResourceId();
    }

    private Long createPaymentType() {
        return paymentTypeHelper.createPaymentType(new PaymentTypeCreateRequest()//
                .name(Utils.uniqueRandomStringGenerator("P_T", 5))//
                .description(Utils.uniqueRandomStringGenerator("PT_Desc", 15))//
                .isCashPayment(false)//
                .position(1L)).getResourceId();
    }

    private void configurationForBackdatedTransaction() {
        globalConfigurationHelper.updateGlobalConfiguration(
                GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING,
                new PutGlobalConfigurationsRequest().enabled(false));
        globalConfigurationHelper.updateGlobalConfiguration(
                GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING_DATE_FOR_DAYS,
                new PutGlobalConfigurationsRequest().enabled(true).value(5L));
    }

    private void updateBusinessDate(LocalDate date) {
        businessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), date.toString());
    }

    /** The body the legacy period-charge helper sent: the recurring day always, the due date only when asked for. */
    private void addPeriodCharge(Long savingsId, Long chargeId, boolean withDueDate) {
        if (withDueDate) {
            savingsChargeHelper.addChargeWithDueDateAndFeeOnMonthDay(savingsId, chargeId, PERIOD_CHARGE_DUE_DATE, PERIOD_CHARGE_AMOUNT,
                    PERIOD_CHARGE_FEE_ON_MONTH_DAY);
        } else {
            savingsChargeHelper.addChargeWithFeeOnMonthDay(savingsId, chargeId, PERIOD_CHARGE_AMOUNT, PERIOD_CHARGE_FEE_ON_MONTH_DAY);
        }
    }

    private SavingsAccountChargeData chargeById(Long savingsId, Long savingsChargeId) {
        return savingsHelper.getSavingsAccountCharges(savingsId).stream().filter(charge -> savingsChargeId.equals(charge.getId()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Savings charge " + savingsChargeId + " is not on the account"));
    }

    /** The account only carries a charges list when it has charges, so an empty one comes back as null. */
    private static boolean isEmpty(List<SavingsAccountChargeData> charges) {
        return charges == null || charges.isEmpty();
    }

    private void verifyTransactionAmountAndRunningBalance(Long savingsId, Long transactionId, BigDecimal expectedAmount,
            BigDecimal expectedRunningBalance, String label) {
        SavingsAccountTransactionData transaction = savingsTransactionHelper.getTransaction(savingsId, transactionId);
        SavingsTestValidators.verifyAmount(expectedAmount, transaction.getAmount(), "Verifying " + label + " Amount");
        SavingsTestValidators.verifyAmount(expectedRunningBalance, transaction.getRunningBalance(), "Verifying Balance after " + label);
    }

    private void verifyTransactionAmount(Long savingsId, Long transactionId, BigDecimal expectedAmount, String label) {
        SavingsTestValidators.verifyAmount(expectedAmount, savingsTransactionHelper.getTransaction(savingsId, transactionId).getAmount(),
                "Verifying " + label + " Amount");
    }

    /** Interest and tax are rounded independently, so the derived balance is only expected to be within a unit. */
    private static void verifyBalanceWithinOne(BigDecimal expected, BigDecimal actual) {
        assertNotNull(actual, "Account balance is missing");
        assertTrue(expected.subtract(actual).abs().compareTo(BigDecimal.ONE) <= 0,
                "Expected a balance within 1 of " + expected + " but was " + actual);
    }

    /** One day of interest at the product's own rate, rounded the way the account reports it. */
    private static BigDecimal expectedInterest(SavingsAccountData account, BigDecimal balance, int days) {
        BigDecimal ratePerDay = account.getNominalAnnualInterestRate().divide(BigDecimal.valueOf(100), MathContext.DECIMAL64)
                .divide(BigDecimal.valueOf(account.getInterestCalculationDaysInYearType().getId()), MathContext.DECIMAL64);
        return roundToThreeDecimals(ratePerDay.multiply(balance).multiply(BigDecimal.valueOf(days)));
    }

    private static BigDecimal roundToThreeDecimals(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal amount(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
