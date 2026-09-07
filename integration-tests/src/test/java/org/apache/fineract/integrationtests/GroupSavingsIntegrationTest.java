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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DepositAccountOnHoldTransactionData;
import org.apache.fineract.client.models.GetGroupsGroupIdAccountsSavingAccounts;
import org.apache.fineract.client.models.GuarantorData;
import org.apache.fineract.client.models.GuarantorsRequest;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.SavingsAccountChargeData;
import org.apache.fineract.client.models.SavingsAccountSummaryData;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGsimHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGuarantorHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Group Savings Integration Test for checking Savings Application.
 */
public class GroupSavingsIntegrationTest extends FeignSavingsTestBase {

    private static final String DEPOSIT_AMOUNT = "2000";
    private static final String WITHDRAW_AMOUNT = "1000";
    private static final String WITHDRAW_AMOUNT_ADJUSTED = "500";
    private static final String MINIMUM_OPENING_BALANCE = "1000.0";
    private static final String PRINCIPAL = "5000";
    private static final String GUARANTEE_AMOUNT = "500";
    private static final String LIFECYCLE_PRINCIPAL = "2000";
    private static final String LIFECYCLE_HALF_PRINCIPAL_GUARANTEE = "1000";
    private static final String HOLD_AMOUNT = "300";

    private static final String SUBMITTED_ON_DATE = "08 January 2013";
    private static final String SUBMITTED_ON_DATE_PLUS_ONE = "09 January 2013";
    private static final String SUBMITTED_ON_DATE_MINUS_ONE = "07 January 2013";
    private static final String TRANSACTION_DATE = "01 March 2013";
    private static final String TRANSACTION_DATE_PLUS_ONE = "02 March 2013";
    private static final String PERIOD_CHARGE_FEE_ON_MONTH_DAY = "15 January";
    private static final String PERIOD_CHARGE_DUE_DATE = "10 January 2013";
    private static final String PERIOD_CHARGE_AMOUNT = "100";
    private static final String REASON_FOR_BLOCK = "unUsualActivity";

    /** {@code GuarantorType}: a guarantor is either an existing client or, since FINERACT-2476, a group. */
    private static final int GUARANTOR_TYPE_CLIENT = 1;
    private static final int GUARANTOR_TYPE_GROUP = 4;

    private static final Long INVALID_GROUP_ID = 9999999L;

    /** The invisible default the RestAssured {@code LoanApplicationTestBuilder} put on every application. */
    private static final BigDecimal MAX_OUTSTANDING_LOAN_BALANCE = new BigDecimal("36000");

    private static FeignGroupHelper groupHelper;
    private static FeignGsimHelper gsimHelper;
    private static FeignLoanHelper loanHelper;
    private static FeignGuarantorHelper guarantorHelper;
    private static FeignTransactionHelper transactionHelper;

    @BeforeAll
    public static void setupGroupHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        groupHelper = new FeignGroupHelper(client);
        gsimHelper = new FeignGsimHelper(client);
        loanHelper = new FeignLoanHelper(client);
        guarantorHelper = new FeignGuarantorHelper(client);
        transactionHelper = new FeignTransactionHelper(client);
    }

    @Test
    public void testSavingsAccount() {
        Long groupId = createGroupWithClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);

        Long savingsId = submitGroupApplication(groupId, savingsProductId);
        assertNotNull(savingsHelper.updateGroupSavingsApplication(savingsId, groupId, savingsProductId, SUBMITTED_ON_DATE_PLUS_ONE)
                .getChanges().getSubmittedOnDate());

        approveAndActivate(savingsId);

        SavingsAccountSummaryData summaryBefore = savingsHelper.getSavingsSummary(savingsId);
        savingsHelper.calculateInterest(savingsId);
        assertEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));

        savingsHelper.postInterest(savingsId);
        assertNotEquals(summaryBefore, savingsHelper.getSavingsSummary(savingsId));
    }

    @Test
    public void testSavingsAccount_CLOSE_APPLICATION() {
        Long groupId = createGroupWithClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, "1000.0", true);
        Long savingsId = approveAndActivate(submitGroupApplication(groupId, savingsProductId));

        String closedOnDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.close.results.in.balance.not.zero",
                savingsHelper.closeSavingsExpectingError(savingsId, closedOnDate, false));

        closeSavings(savingsId, closedOnDate, true);
        SavingsTestValidators.verifySavingsIsClosed(savingsHelper.getSavingsStatus(savingsId));
    }

    @Test
    public void testGsimSavingsAccount_WithTwoClients_ChildCountTwo() {
        Long parentClientId = createClient();
        Long childClientId = createClient();
        Long groupId = groupHelper.createActiveGroup().getResourceId();
        groupHelper.associateClient(groupId, parentClientId);
        groupHelper.associateClient(groupId, childClientId);

        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);

        assertNotNull(gsimHelper
                .submitApplication(
                        List.of(SavingsRequestBuilders.gsimClient(parentClientId, groupId, savingsProductId, SUBMITTED_ON_DATE, true),
                                SavingsRequestBuilders.gsimClient(childClientId, groupId, savingsProductId, SUBMITTED_ON_DATE, false)))
                .getGsimId());

        assertEquals(2, gsimHelper.childAccountCount(groupId));
    }

    @Test
    public void testSavingsAccount_DELETE_APPLICATION() {
        Long groupId = createGroupWithClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = submitGroupApplication(groupId, savingsProductId);

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
    public void testGsimSavingsAccount_REJECT_APPLICATION() {
        Long clientId = createClient();
        Long groupId = createGroupWith(clientId);
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);

        Long gsimId = gsimHelper.submitApplication(clientId, groupId, savingsProductId, SUBMITTED_ON_DATE);

        gsimHelper.approve(gsimId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.reject.not.in.submittedandpendingapproval.state",
                gsimHelper.rejectExpectingError(gsimId, SUBMITTED_ON_DATE_PLUS_ONE));

        gsimHelper.undoApproval(gsimId);

        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.reject.cannot.be.a.future.date",
                gsimHelper.rejectExpectingError(gsimId, Utils.dateFormatter.format(Utils.getLocalDateOfTenant().plusYears(1))));
        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.reject.cannot.be.before.submittal.date",
                gsimHelper.rejectExpectingError(gsimId, SUBMITTED_ON_DATE_MINUS_ONE));

        assertNotNull(gsimHelper.reject(gsimId, SUBMITTED_ON_DATE_PLUS_ONE));
    }

    @Test
    public void testGsimSavingsAccount_DEPOSIT_APPLICATION() {
        Long clientId = createClient();
        Long groupId = createGroupWith(clientId);
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = approveAndActivate(submitGroupApplication(groupId, savingsProductId));

        Long paymentTypeId = createPaymentType(true);
        assertNotNull(gsimHelper
                .deposit(savingsId, List.of(SavingsRequestBuilders.gsimSavings(savingsId, paymentTypeId, "2500", "10 March 2013")))
                .getResourceId());
    }

    @Test
    public void testGsimSavingsAccount_CLOSE_APPLICATION() {
        Long clientId = createClient();
        Long groupId = createGroupWith(clientId);
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, "1000.0", true);

        Long gsimId = gsimHelper.submitApplication(clientId, groupId, savingsProductId, SUBMITTED_ON_DATE);
        gsimHelper.approve(gsimId, SUBMITTED_ON_DATE_PLUS_ONE);
        gsimHelper.activate(gsimId, TRANSACTION_DATE);

        String closedOnDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        SavingsTestValidators.verifyFirstErrorCode("validation.msg.savingsaccount.close.results.in.balance.not.zero",
                gsimHelper.closeExpectingError(gsimId, closedOnDate, false));

        assertNotNull(gsimHelper.close(gsimId, closedOnDate, true));
    }

    @Test
    public void testGsimSavingsAccount_UPDATE_APPLICATION() {
        Long clientId = createClient();
        Long groupId = createGroupWith(clientId);
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, "1000.0", true);

        Long gsimId = gsimHelper.submitApplication(clientId, groupId, savingsProductId, SUBMITTED_ON_DATE);
        assertNotNull(gsimHelper.updateApplication(gsimId, clientId, groupId, savingsProductId).getSavingsId());
    }

    @Test
    public void getGsimAccount() {
        Long clientId = createClient();
        Long groupId = createGroupWith(clientId);
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, "1000.0", true);

        gsimHelper.submitApplication(clientId, groupId, savingsProductId, SUBMITTED_ON_DATE);
        assertNotNull(gsimHelper.retrieveGsimAccounts(groupId).get(0).getGsimId());
    }

    @Test
    public void testSavingsAccount_REJECT_APPLICATION() {
        Long groupId = createGroupWithClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = submitGroupApplication(groupId, savingsProductId);

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
        Long groupId = createGroupWithClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = submitGroupApplication(groupId, savingsProductId);

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        savingsHelper.withdrawnByApplicant(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsWithdrawn(savingsHelper.getSavingsStatus(savingsId));
    }

    @Test
    public void testSavingsAccountTransactions() {
        Long groupId = createGroupWithClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = submitGroupApplication(groupId, savingsProductId);

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
        Long groupId = createGroupWithClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = submitGroupApplication(groupId, savingsProductId);
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

        approveAndActivate(savingsId);

        Long annualChargeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsAnnualFeeCharge()).getResourceId();
        assertTrue(isEmpty(savingsHelper.getSavingsAccountCharges(savingsId)), "The account should carry no charges yet");

        addPeriodCharge(savingsId, annualChargeId, true);
        charges = savingsHelper.getSavingsAccountCharges(savingsId);
        assertEquals(1, charges.size());

        SavingsAccountChargeData annualCharge = charges.get(0);
        savingsChargeHelper.payCharge(savingsId, annualCharge.getId(), annualCharge.getAmount().toPlainString(),
                Utils.dateFormatter.format(annualCharge.getDueDate()));
        SavingsTestValidators.verifyAmount(annualCharge.getAmount(), chargeById(savingsId, annualCharge.getId()).getAmountPaid(),
                "Verifying paid annual fee");

        Long monthlyFeeChargeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsMonthlyFeeCharge()).getResourceId();
        addPeriodCharge(savingsId, monthlyFeeChargeId, true);
        charges = savingsHelper.getSavingsAccountCharges(savingsId);
        assertEquals(2, charges.size());

        SavingsAccountChargeData monthlyCharge = charges.get(1);
        savingsChargeHelper.waiveCharge(savingsId, monthlyCharge.getId());
        SavingsTestValidators.verifyAmount(monthlyCharge.getAmount(), chargeById(savingsId, monthlyCharge.getId()).getAmountWaived(),
                "Verifying waived monthly fee");

        savingsChargeHelper.waiveCharge(savingsId, monthlyCharge.getId());
        SavingsTestValidators.verifyAmount(monthlyCharge.getAmount().add(monthlyCharge.getAmount()),
                chargeById(savingsId, monthlyCharge.getId()).getAmountWaived(), "Verifying twice waived monthly fee");

        Long weeklyFeeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsWeeklyFeeCharge()).getResourceId();
        addPeriodCharge(savingsId, weeklyFeeId, true);
        charges = savingsHelper.getSavingsAccountCharges(savingsId);
        assertEquals(3, charges.size());

        SavingsAccountChargeData weeklyCharge = charges.get(2);
        // the scheduler job deducts the fee, so the account is funded well past what the charge needs
        deposit(savingsId, "100000", TRANSACTION_DATE);

        savingsChargeHelper.payCharge(savingsId, weeklyCharge.getId(), weeklyCharge.getAmount().toPlainString(),
                Utils.dateFormatter.format(weeklyCharge.getDueDate()));
        SavingsAccountChargeData paidCharge = chargeById(savingsId, weeklyCharge.getId());
        SavingsTestValidators.verifyAmount(weeklyCharge.getAmount(), paidCharge.getAmountPaid(), "Verifying paid weekly fee");
        assertEquals(weeklyCharge.getDueDate().plusWeeks(paidCharge.getFeeInterval()), paidCharge.getDueDate(),
                "A paid weekly fee falls due again one interval later");

        closeSavings(savingsId, Utils.dateFormatter.format(Utils.getLocalDateOfTenant()), true);
    }

    @Test
    public void testOnHoldTransactionsApiForGroupSavingsAccount() {
        Long clientId = createClient();
        Long groupId = groupHelper.createActiveGroup().getResourceId();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = approveAndActivate(submitGroupApplication(groupId, savingsProductId));

        Long loanId = createGuaranteeLoan(clientId, PRINCIPAL, 4, LoanTestData.RepaymentFrequencyType.WEEKS);
        assertNotNull(guarantorHelper.createGuarantor(loanId, groupGuarantor(groupId, savingsId, GUARANTEE_AMOUNT)));

        // approval, not disbursal, is what places the guarantor hold
        loanHelper.approveLoan(loanId, approveLoanRequest(TRANSACTION_DATE));

        List<DepositAccountOnHoldTransactionData> onHoldTransactions = savingsHelper.getOnHoldTransactions(savingsId);
        assertNotNull(onHoldTransactions);
        assertTrue(!onHoldTransactions.isEmpty(), "Should have at least one on-hold transaction");

        DepositAccountOnHoldTransactionData hold = onHoldTransactions.stream()
                .filter(transaction -> transaction.getSavingsClientName() != null && !transaction.getSavingsClientName().isBlank())
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Should find at least one on-hold transaction with savingsClientName populated (group name)"));

        assertNotNull(hold.getAmount(), "Transaction amount should not be null");
        assertNotNull(hold.getSavingsAccountNo(), "savingsAccountNo should not be null");
        assertEquals(savingsId, hold.getSavingsId(), "savingsId should match");
        assertNotNull(hold.getTransactionDate(), "transactionDate should not be null");
    }

    @Test
    public void testGroupGuarantorWithInvalidGroupId() {
        Long clientId = createClient();
        Long loanId = createGuaranteeLoan(clientId, PRINCIPAL, 4, LoanTestData.RepaymentFrequencyType.WEEKS);

        assertNotNull(guarantorHelper.createGuarantorExpectingError(loanId, groupGuarantor(INVALID_GROUP_ID, 1L, GUARANTEE_AMOUNT)),
                "Should return error for invalid group ID");
    }

    @Test
    public void testDuplicateGroupGuarantor() {
        Long clientId = createClient();
        Long groupId = groupHelper.createActiveGroup().getResourceId();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = approveAndActivate(submitGroupApplication(groupId, savingsProductId));

        Long loanId = createGuaranteeLoan(clientId, PRINCIPAL, 4, LoanTestData.RepaymentFrequencyType.WEEKS);
        GuarantorsRequest guarantor = groupGuarantor(groupId, savingsId, GUARANTEE_AMOUNT);

        assertNotNull(guarantorHelper.createGuarantor(loanId, guarantor), "First guarantor creation should succeed");

        CallFailedRuntimeException error = guarantorHelper.createGuarantorExpectingError(loanId, guarantor);
        assertTrue(FeignErrors.errorGlobalisationCode(error).contains("already.exist"),
                "Error message should indicate duplicate guarantor");
    }

    @Test
    public void testGroupGuarantorLoanLifecycle() {
        Long clientId = createClient();
        Long groupId = groupHelper.createActiveGroup().getResourceId();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = approveAndActivate(submitGroupApplication(groupId, savingsProductId));
        deposit(savingsId, DEPOSIT_AMOUNT, TRANSACTION_DATE);

        Long loanId = createGuaranteeLoan(clientId, LIFECYCLE_PRINCIPAL, 1, LoanTestData.RepaymentFrequencyType.WEEKS);
        assertNotNull(guarantorHelper.createGuarantor(loanId, groupGuarantor(groupId, savingsId, LIFECYCLE_HALF_PRINCIPAL_GUARANTEE)));

        List<GuarantorData> guarantors = guarantorHelper.getAllGuarantors(loanId);
        assertEquals(1, guarantors.size(), "Should have 1 group guarantor");
        assertEquals(GUARANTOR_TYPE_GROUP, guarantors.get(0).getGuarantorType().getId().intValue(), "Guarantor type should be GROUP (4)");

        assertNotNull(loanHelper.approveLoan(loanId, approveLoanRequest(TRANSACTION_DATE)),
                "Loan approval should succeed with group guarantor");
        loanHelper.disburseLoan(loanId, disburseLoanRequest(TRANSACTION_DATE, LIFECYCLE_PRINCIPAL));
        transactionHelper.makeRepayment(TRANSACTION_DATE, Float.parseFloat(LIFECYCLE_PRINCIPAL), loanId.intValue());
    }

    @Test
    public void testMixedClientAndGroupGuarantors() {
        Long borrowerClientId = createClient();
        Long guarantorClientId = createClient();
        Long guarantorGroupId = groupHelper.createActiveGroup().getResourceId();

        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long clientSavingsId = approveAndActivate(
                submitSavingsApplication(guarantorClientId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId());
        Long groupSavingsId = approveAndActivate(submitGroupApplication(guarantorGroupId, savingsProductId));

        Long loanId = createGuaranteeLoan(borrowerClientId, PRINCIPAL, 4, LoanTestData.RepaymentFrequencyType.WEEKS);

        assertNotNull(guarantorHelper.createGuarantor(loanId, clientGuarantor(guarantorClientId, clientSavingsId, "250")),
                "Client guarantor creation should succeed");
        assertNotNull(guarantorHelper.createGuarantor(loanId, groupGuarantor(guarantorGroupId, groupSavingsId, "250")),
                "Group guarantor creation should succeed");

        List<GuarantorData> guarantors = guarantorHelper.getAllGuarantors(loanId);
        assertEquals(2, guarantors.size(), "Should have 2 guarantors (1 client, 1 group)");
        assertTrue(guarantors.stream().anyMatch(guarantor -> guarantor.getGuarantorType().getId() == GUARANTOR_TYPE_CLIENT),
                "Should have client guarantor");
        assertTrue(guarantors.stream().anyMatch(guarantor -> guarantor.getGuarantorType().getId() == GUARANTOR_TYPE_GROUP),
                "Should have group guarantor");

        loanHelper.approveLoan(loanId, approveLoanRequest(TRANSACTION_DATE));
    }

    @Test
    public void testGroupAccountAvailableBalance() {
        Long groupId = createGroupWithClient();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = approveAndActivate(submitGroupApplication(groupId, savingsProductId));

        assertNotNull(deposit(savingsId, DEPOSIT_AMOUNT, TRANSACTION_DATE).getResourceId());

        BigDecimal expectedBalance = new BigDecimal(MINIMUM_OPENING_BALANCE).add(new BigDecimal(DEPOSIT_AMOUNT));
        SavingsTestValidators.verifyAmount(expectedBalance, savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying Deposit Balance");

        GetGroupsGroupIdAccountsSavingAccounts account = groupSavingsAccount(groupId, savingsId);
        BigDecimal onHoldFunds = orZero(account.getOnHoldFunds());
        BigDecimal savingsAmountOnHold = orZero(account.getSavingsAmountOnHold());

        SavingsTestValidators.verifyAmount(expectedBalance, account.getAccountBalance(),
                "accountBalance should equal minimum opening balance plus deposited amount");
        assertEquals(0, BigDecimal.ZERO.compareTo(onHoldFunds), "onHoldFunds should be 0 when no holds are placed");
        assertEquals(0, BigDecimal.ZERO.compareTo(savingsAmountOnHold), "savingsAmountOnHold should be 0 when no holds are placed");
        assertEquals(0,
                account.getAccountBalance().subtract(onHoldFunds).subtract(savingsAmountOnHold).compareTo(account.getAvailableBalance()),
                "availableBalance should equal accountBalance - onHoldFunds - savingsAmountOnHold");
        assertEquals(0, account.getAccountBalance().compareTo(account.getAvailableBalance()),
                "availableBalance should equal accountBalance when there are no holds");
    }

    @Test
    public void testGroupAccountWithHold() {
        Long groupId = groupHelper.createActiveGroup().getResourceId();
        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long savingsId = approveAndActivate(submitGroupApplication(groupId, savingsProductId));

        assertNotNull(deposit(savingsId, DEPOSIT_AMOUNT, TRANSACTION_DATE).getResourceId());
        assertNotNull(
                savingsTransactionHelper.holdAmount(savingsId, HOLD_AMOUNT, TRANSACTION_DATE, REASON_FOR_BLOCK, false).getResourceId());

        GetGroupsGroupIdAccountsSavingAccounts account = groupSavingsAccount(groupId, savingsId);
        BigDecimal onHoldFunds = orZero(account.getOnHoldFunds());
        BigDecimal savingsAmountOnHold = orZero(account.getSavingsAmountOnHold());

        assertEquals(0, new BigDecimal(HOLD_AMOUNT).compareTo(savingsAmountOnHold), "savingsAmountOnHold should equal the hold amount");
        assertEquals(0,
                account.getAccountBalance().subtract(onHoldFunds).subtract(savingsAmountOnHold).compareTo(account.getAvailableBalance()),
                "availableBalance should equal accountBalance - onHoldFunds - savingsAmountOnHold");
    }

    @Test
    public void testGroupAccountAsGuarantorWithGuaranteeHolds() {
        Long borrowerClientId = createClient();
        Long groupId = groupHelper.createActiveGroup().getResourceId();
        groupHelper.associateClient(groupId, borrowerClientId);

        Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, null, null, false);
        Long guarantorSavingsId = approveAndActivate(submitGroupApplication(groupId, savingsProductId));
        assertNotNull(deposit(guarantorSavingsId, "10000", TRANSACTION_DATE).getResourceId());

        Long loanId = createGuaranteeLoan(borrowerClientId, "10000", 12, LoanTestData.RepaymentFrequencyType.MONTHS,
                TRANSACTION_DATE_PLUS_ONE);

        String guaranteeAmount = "5000";
        assertNotNull(guarantorHelper.createGuarantor(loanId, clientGuarantor(borrowerClientId, guarantorSavingsId, guaranteeAmount)),
                "Guarantor with group savings account created successfully");

        // the hold is placed on approval, not on disbursement
        loanHelper.approveLoan(loanId, approveLoanRequest(TRANSACTION_DATE));

        SavingsTestValidators.verifyAmount(new BigDecimal(guaranteeAmount),
                orZero(savingsHelper.getSavingsDetails(guarantorSavingsId).getOnHoldFunds()),
                "Group account should have automatic guarantor hold equal to guarantee amount after loan approval");
    }

    private Long createGroupWithClient() {
        return createGroupWith(createClient());
    }

    private Long createGroupWith(Long clientId) {
        Long groupId = groupHelper.createActiveGroup().getResourceId();
        groupHelper.associateClient(groupId, clientId);
        return groupId;
    }

    private Long submitGroupApplication(Long groupId, Long savingsProductId) {
        return savingsHelper.submitGroupApplication(groupId, savingsProductId, SUBMITTED_ON_DATE).getSavingsId();
    }

    private Long approveAndActivate(Long savingsId) {
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        approveSavings(savingsId, SUBMITTED_ON_DATE_PLUS_ONE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private Long createSavingsProduct(String minOpeningBalance, String minBalanceForInterestCalculation, String minRequiredBalance,
            boolean enforceMinRequiredBalance) {
        PostSavingsProductsRequest request = SavingsRequestBuilders.defaultSavingsProduct()//
                .withdrawalFeeForTransfers(true)//
                .withHoldTax(false)//
                .minRequiredOpeningBalance(amount(minOpeningBalance))//
                .minBalanceForInterestCalculation(amount(minBalanceForInterestCalculation))//
                .minRequiredBalance(amount(minRequiredBalance))//
                .enforceMinRequiredBalance(enforceMinRequiredBalance);
        return savingsProductHelper.createSavingsProduct(request).getResourceId();
    }

    private Long createPaymentType(boolean isCashPayment) {
        return paymentTypeHelper.createPaymentType(new PaymentTypeCreateRequest()//
                .name(Utils.uniqueRandomStringGenerator("P_T", 5))//
                .description(Utils.uniqueRandomStringGenerator("PT_Desc", 15))//
                .isCashPayment(isCashPayment)//
                .position(1L)).getResourceId();
    }

    /**
     * A loan whose product holds guarantee funds but requires none of it, which is what lets a group account stand as
     * guarantor: the holds are still placed, and no minimum percentage is checked against a group's null client id.
     */
    private Long createGuaranteeLoan(Long clientId, String principal, int repayments, int repaymentFrequencyType) {
        return createGuaranteeLoan(clientId, principal, repayments, repaymentFrequencyType, TRANSACTION_DATE);
    }

    private Long createGuaranteeLoan(Long clientId, String principal, int repayments, int repaymentFrequencyType,
            String expectedDisbursementDate) {
        Long loanProductId = loanHelper.createLoanProduct(guaranteeLoanProduct(principal, repayments, repaymentFrequencyType))
                .getResourceId();
        return loanHelper.applyForLoan(new PostLoansRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .loanType("individual")//
                .principal(new BigDecimal(principal))//
                .loanTermFrequency(repayments)//
                .loanTermFrequencyType(repaymentFrequencyType)//
                .numberOfRepayments(repayments)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(repaymentFrequencyType)//
                .interestRatePerPeriod(new BigDecimal("2"))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)//
                .maxOutstandingLoanBalance(MAX_OUTSTANDING_LOAN_BALANCE)//
                .submittedOnDate(TRANSACTION_DATE)//
                .expectedDisbursementDate(expectedDisbursementDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE)).getLoanId();
    }

    private PostLoanProductsRequest guaranteeLoanProduct(String principal, int repayments, int repaymentFrequencyType) {
        return new PostLoanProductsRequest()//
                .name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("Group guarantor loan product")//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .principal(Double.valueOf(principal))//
                .numberOfRepayments(repayments)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(Long.valueOf(repaymentFrequencyType))//
                .interestRatePerPeriod(2.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .daysInMonthType(LoanTestData.DaysInMonthType.ACTUAL)//
                .daysInYearType(LoanTestData.DaysInYearType.ACTUAL)//
                .isInterestRecalculationEnabled(false)//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.toString())//
                .accountingRule(SavingsTestData.AccountingRule.NONE)//
                .holdGuaranteeFunds(true)//
                .mandatoryGuarantee(BigDecimal.ZERO)//
                .minimumGuaranteeFromOwnFunds(BigDecimal.ZERO)//
                .minimumGuaranteeFromGuarantor(BigDecimal.ZERO)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
    }

    private GuarantorsRequest groupGuarantor(Long groupId, Long savingsId, String guaranteeAmount) {
        return existingGuarantor(GUARANTOR_TYPE_GROUP, groupId, savingsId, guaranteeAmount);
    }

    private GuarantorsRequest clientGuarantor(Long clientId, Long savingsId, String guaranteeAmount) {
        return existingGuarantor(GUARANTOR_TYPE_CLIENT, clientId, savingsId, guaranteeAmount);
    }

    /** An existing client or group pledges one of its savings accounts; the entity id says which of the two it is. */
    private GuarantorsRequest existingGuarantor(int guarantorTypeId, Long entityId, Long savingsId, String guaranteeAmount) {
        return new GuarantorsRequest()//
                .guarantorTypeId(guarantorTypeId)//
                .entityId(entityId)//
                .savingsId(savingsId)//
                .amount(new BigDecimal(guaranteeAmount))//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
    }

    private PostLoansLoanIdRequest approveLoanRequest(String approvedOnDate) {
        return new PostLoansLoanIdRequest()//
                .approvedOnDate(approvedOnDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
    }

    private PostLoansLoanIdRequest disburseLoanRequest(String actualDisbursementDate, String amount) {
        return new PostLoansLoanIdRequest()//
                .actualDisbursementDate(actualDisbursementDate)//
                .transactionAmount(new BigDecimal(amount))//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
    }

    private GetGroupsGroupIdAccountsSavingAccounts groupSavingsAccount(Long groupId, Long savingsId) {
        return groupHelper.retrieveGroupAccounts(groupId).getSavingsAccounts().stream().filter(account -> savingsId.equals(account.getId()))
                .findFirst().orElseThrow(() -> new AssertionError("Savings account should be in the response"));
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

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal amount(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
