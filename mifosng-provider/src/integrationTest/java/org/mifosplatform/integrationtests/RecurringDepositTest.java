package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import net.sf.ehcache.transaction.xa.EhcacheXAException;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.accounting.Account;
import org.mifosplatform.integrationtests.common.accounting.AccountHelper;
import org.mifosplatform.integrationtests.common.fixeddeposit.FixedDepositAccountHelper;
import org.mifosplatform.integrationtests.common.fixeddeposit.FixedDepositAccountStatusChecker;
import org.mifosplatform.integrationtests.common.fixeddeposit.FixedDepositProductHelper;
import org.mifosplatform.integrationtests.common.recurringdeposit.RecurringDepositAccountHelper;
import org.mifosplatform.integrationtests.common.recurringdeposit.RecurringDepositAccountStatusChecker;
import org.mifosplatform.integrationtests.common.recurringdeposit.RecurringDepositProductHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsAccountHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsProductHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsStatusChecker;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "rawtypes", "unchecked", "static-access" })
public class RecurringDepositTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private RecurringDepositProductHelper recurringDepositProductHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private AccountHelper accountHelper;
    private RecurringDepositAccountHelper recurringDepositAccountHelper;

    public static final String WHOLE_TERM = "1";
    private static final String TILL_PREMATURE_WITHDRAWAL = "2";
    private static final String DAILY = "1";
    private static final String MONTHLY = "4";
    private static final String QUARTERLY = "5";
    private static final String BI_ANNUALLY = "6";
    private static final String ANNUALLY = "7";
    private static final String INTEREST_CALCULATION_USING_DAILY_BALANCE = "1";
    private static final String INTEREST_CALCULATION_USING_AVERAGE_DAILY_BALANCE = "2";
    private static final String DAYS_360 = "360";
    private static final String DAYS_365 = "365";

    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String CLOSURE_TYPE_WITHDRAW_DEPOSIT = "100";
    public static final String CLOSURE_TYPE_TRANSFER_TO_SAVINGS = "200";
    public static final String CLOSURE_TYPE_REINVEST = "300";
    public static final Integer DAILY_COMPOUNDING_INTERVAL = 0;
    public static final Integer MONTHLY_INTERVAL = 1;
    public static final Integer QUARTERLY_INTERVAL = 3;
    public static final Integer BIANNULLY_INTERVAL = 6;
    public static final Integer ANNUL_INTERVAL = 12;

    public static final Float DEPOSIT_AMOUNT = 2000.0f;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("X-Mifos-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testRecurringDepositAccountWithPrematureClosureTypeWithdrawal() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        Integer depositTransactionId = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(depositTransactionId);

        recurringDepositAccountId = this.recurringDepositAccountHelper.calculateInterestForRecurringDeposit(recurringDepositAccountId);
        Assert.assertNotNull(recurringDepositAccountId);

        Integer transactionIdForPostInterest = this.recurringDepositAccountHelper
                .postInterestForRecurringDeposit(recurringDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        HashMap recurringDepositPrematureData = this.recurringDepositAccountHelper.calculatePrematureAmountForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.recurringDepositAccountHelper.prematureCloseForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(this.requestSpec,
                this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositAccountIsPrematureClosed(recurringDepositAccountStatusHashMap);

    }

    @Test
    public void testRecurringDepositAccountWithPrematureClosureTypeTransferToSavings() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientId, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        Integer depositTransactionId = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(depositTransactionId);

        recurringDepositAccountId = this.recurringDepositAccountHelper.calculateInterestForRecurringDeposit(recurringDepositAccountId);
        Assert.assertNotNull(recurringDepositAccountId);

        Integer transactionIdForPostInterest = this.recurringDepositAccountHelper
                .postInterestForRecurringDeposit(recurringDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        HashMap savingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);

        Float balanceBefore = (Float) savingsSummaryBefore.get("accountBalance");

        HashMap recurringDepositPrematureData = this.recurringDepositAccountHelper.calculatePrematureAmountForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.recurringDepositAccountHelper.prematureCloseForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_TRANSFER_TO_SAVINGS, savingsId,
                CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(this.requestSpec,
                this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositAccountIsPrematureClosed(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        Float prematurityAmount = (Float) recurringDepositData.get("maturityAmount");

        HashMap savingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balanceAfter = (Float) savingsSummaryAfter.get("accountBalance");
        Float expectedSavingsBalance = balanceBefore + prematurityAmount;

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");

        Assert.assertEquals("Verifying Savings Account Balance after Premature Closure", decimalFormat.format(expectedSavingsBalance),
                decimalFormat.format(balanceAfter));

    }

    @Test
    public void testRecurringDepositAccountWithPrematureClosureTypeReinvest() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        RecurringDepositAccountHelper recurringDepositAccountHelperValidationError = new RecurringDepositAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        ArrayList<HashMap> allRecurringDepositProductsData = this.recurringDepositProductHelper.retrieveAllRecurringDepositProducts(
                this.requestSpec, this.responseSpec);
        HashMap recurringDepositProductData = this.recurringDepositProductHelper.retrieveRecurringDepositProductById(this.requestSpec,
                this.responseSpec, recurringDepositProductId.toString());

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        Integer depositTransactionId = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(depositTransactionId);

        recurringDepositAccountId = this.recurringDepositAccountHelper.calculateInterestForRecurringDeposit(recurringDepositAccountId);
        Assert.assertNotNull(recurringDepositAccountId);

        Integer transactionIdForPostInterest = this.recurringDepositAccountHelper
                .postInterestForRecurringDeposit(recurringDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        HashMap recurringDepositPrematureData = this.recurringDepositAccountHelper.calculatePrematureAmountForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE);

        ArrayList<HashMap> errorResponse = (ArrayList<HashMap>) recurringDepositAccountHelperValidationError
                .prematureCloseForRecurringDeposit(recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_REINVEST, null,
                        CommonConstants.RESPONSE_ERROR);

        assertEquals("validation.msg.recurringdepositaccount.onAccountClosureId.reinvest.not.allowed",
                errorResponse.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    @Test
    public void testRecurringDepositAccountUpdation() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        todaysDate.add(Calendar.DATE, -1);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateRecurringDepositAccount(clientId.toString(),
                recurringDepositProductId.toString(), recurringDepositAccountId.toString(), VALID_FROM, VALID_TO, WHOLE_TERM,
                SUBMITTED_ON_DATE);
        Assert.assertTrue(modificationsHashMap.containsKey("submittedOnDate"));

    }

    @Test
    public void testRecurringDepositAccountUndoApproval() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.undoApproval(recurringDepositAccountId);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);
    }

    @Test
    public void testRecurringDepositAccountRejectedAndClosed() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String REJECTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.rejectApplication(recurringDepositAccountId,
                REJECTED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsRejected(recurringDepositAccountStatusHashMap);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositAccountIsClosed(recurringDepositAccountStatusHashMap);
    }

    @Test
    public void testRecurringDepositAccountWithdrawnByClientAndClosed() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String WITHDRAWN_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.withdrawApplication(recurringDepositAccountId,
                WITHDRAWN_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsWithdrawn(recurringDepositAccountStatusHashMap);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositAccountIsClosed(recurringDepositAccountStatusHashMap);
    }

    @Test
    public void testRecurringDepositAccountIsDeleted() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountId = (Integer) this.recurringDepositAccountHelper.deleteRecurringDepositApplication(
                recurringDepositAccountId, "resourceId");
        Assert.assertNotNull(recurringDepositAccountId);
    }

    @Test
    public void testUpdateAndUndoTransactionForRecurringDepositAccount() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String DEPOSIT_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositSummaryBefore = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float balanceBefore = (Float) recurringDepositSummaryBefore.get("accountBalance");

        Integer transactionIdForDeposit = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, DEPOSIT_DATE);
        Assert.assertNotNull(transactionIdForDeposit);

        Float expectedBalanceAfter = balanceBefore + DEPOSIT_AMOUNT;
        HashMap recurringDepositSummaryAfter = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float balanceAfter = (Float) recurringDepositSummaryAfter.get("accountBalance");

        Assert.assertEquals("Verifying account balance after deposit", expectedBalanceAfter, balanceAfter);

        Float updatedTransactionAmount = DEPOSIT_AMOUNT - 1000.0f;

        Integer updateTransactionId = this.recurringDepositAccountHelper.updateTransactionForRecurringDeposit(recurringDepositAccountId,
                transactionIdForDeposit, DEPOSIT_DATE, updatedTransactionAmount);
        Assert.assertNotNull(updateTransactionId);

        expectedBalanceAfter = DEPOSIT_AMOUNT - updatedTransactionAmount;
        recurringDepositSummaryAfter = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        balanceAfter = (Float) recurringDepositSummaryAfter.get("accountBalance");

        Assert.assertEquals("Verifying account balance after updating Transaction", expectedBalanceAfter, balanceAfter);

        Integer undoTransactionId = this.recurringDepositAccountHelper.undoTransactionForRecurringDeposit(recurringDepositAccountId,
                updateTransactionId, DEPOSIT_DATE, 0.0f);
        Assert.assertNotNull(undoTransactionId);

        expectedBalanceAfter = expectedBalanceAfter - updatedTransactionAmount;
        recurringDepositSummaryAfter = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        balanceAfter = (Float) recurringDepositSummaryAfter.get("accountBalance");

        Assert.assertEquals("Verifying account balance after Undo Transaction", expectedBalanceAfter, balanceAfter);

    }

    @Test
    public void testMaturityAmountForMonthlyCompoundingAndMonthlyPosting_With_365_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DATE, -(currentDate - 1));
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        Float depositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Float maturityAmount = (Float) recurringDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("accountBalance");

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.recurringDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositAmount,
                depositPeriod, interestPerDay, MONTHLY_INTERVAL, MONTHLY_INTERVAL);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Recurring Deposit Account", principal, maturityAmount);
    }

    @Test
    public void testMaturityAmountForMonthlyCompoundingAndMonthlyPosting_With_360_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DATE, -(currentDate - 1));
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_360, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        Float depositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Float maturityAmount = (Float) recurringDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("accountBalance");

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.recurringDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositAmount,
                depositPeriod, interestPerDay, MONTHLY_INTERVAL, MONTHLY_INTERVAL);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Recurring Deposit Account", principal, maturityAmount);
    }

    @Test
    public void testPostInterestForRecurringDeposit() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Integer depositTransactionId = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(depositTransactionId);

        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("totalDeposits");

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");

        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestToBePosted = new Float(decimalFormat.format(interestPerDay * principal * daysInMonth));
        principal += interestToBePosted;

        Float expectedBalanceAfter = new Float(decimalFormat.format(principal));
        System.out.println(expectedBalanceAfter);

        Integer transactionIdForPostInterest = this.recurringDepositAccountHelper
                .postInterestForRecurringDeposit(recurringDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        HashMap recurringDepositAccountSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float interestAmountPosted = new Float(decimalFormat.format(recurringDepositAccountSummary.get("totalInterestPosted")));
        Float principalAfter = new Float(decimalFormat.format(recurringDepositAccountSummary.get("accountBalance")));

        Assert.assertEquals("Verifying Amount of Interest Posted to Recurring Deposit Account", interestToBePosted, interestAmountPosted);
        Assert.assertEquals("Verifying Principal Amount after Interest Posting", expectedBalanceAfter, principalAfter);

    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestForWholeTerm_With_365_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        Float depositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) recurringDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Integer depositTransactionId = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(depositTransactionId);

        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("totalDeposits");

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(calendar.getTime()));
        Integer daysInMonth = calendar.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth + depositAmount;
        calendar.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(calendar.getTime()));

        EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(calendar.getTime());
        Integer transactionIdForDeposit = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(transactionIdForDeposit);

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap recurringDepositPrematureData = this.recurringDepositAccountHelper.calculatePrematureAmountForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.recurringDepositAccountHelper.prematureCloseForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(this.requestSpec,
                this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositAccountIsPrematureClosed(recurringDepositAccountStatusHashMap);

        recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");

        principal = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(recurringDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", principal, maturityAmount);

    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestForWholeTerm_With_360_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_360, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        Float depositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) recurringDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Integer depositTransactionId = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(depositTransactionId);

        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("totalDeposits");

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(calendar.getTime()));
        Integer daysInMonth = calendar.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth + depositAmount;
        calendar.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(calendar.getTime()));

        EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(calendar.getTime());
        Integer transactionIdForDeposit = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(transactionIdForDeposit);

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap recurringDepositPrematureData = this.recurringDepositAccountHelper.calculatePrematureAmountForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.recurringDepositAccountHelper.prematureCloseForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(this.requestSpec,
                this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositAccountIsPrematureClosed(recurringDepositAccountStatusHashMap);

        recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");

        principal = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(recurringDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", principal, maturityAmount);

    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestTillPrematureWithdrawal_With_365_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, TILL_PREMATURE_WITHDRAWAL, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        Float depositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) recurringDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Calendar activationDate = Calendar.getInstance();
        activationDate.add(Calendar.MONTH, -1);
        DateTime start = new DateTime(activationDate.getTime());

        Calendar prematureClosureDate = Calendar.getInstance();
        DateTime end = new DateTime(prematureClosureDate.getTime());

        Integer depositedPeriod = Months.monthsBetween(start, end).getMonths();

        Integer depositTransactionId = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(depositTransactionId);

        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("totalDeposits");

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositedPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(calendar.getTime()));
        Integer daysInMonth = calendar.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth + depositAmount;
        calendar.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(calendar.getTime()));

        EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(calendar.getTime());
        Integer transactionIdForDeposit = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(transactionIdForDeposit);

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap recurringDepositPrematureData = this.recurringDepositAccountHelper.calculatePrematureAmountForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.recurringDepositAccountHelper.prematureCloseForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(this.requestSpec,
                this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositAccountIsPrematureClosed(recurringDepositAccountStatusHashMap);

        recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");

        principal = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(recurringDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", principal, maturityAmount);

    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestTillPrematureWithdrawal_With_360_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, TILL_PREMATURE_WITHDRAWAL, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_360, TILL_PREMATURE_WITHDRAWAL, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        Float depositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) recurringDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Calendar activationDate = Calendar.getInstance();
        activationDate.add(Calendar.MONTH, -1);
        DateTime start = new DateTime(activationDate.getTime());

        Calendar prematureClosureDate = Calendar.getInstance();
        DateTime end = new DateTime(prematureClosureDate.getTime());

        Integer depositedPeriod = Months.monthsBetween(start, end).getMonths();

        Integer transactionIdForDeposit = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(transactionIdForDeposit);

        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("totalDeposits");

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositedPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(calendar.getTime()));
        Integer daysInMonth = calendar.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth + depositAmount;
        calendar.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(calendar.getTime()));

        EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(calendar.getTime());
        Integer newTransactionIdForDeposit = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                DEPOSIT_AMOUNT, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(newTransactionIdForDeposit);

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap recurringDepositPrematureData = this.recurringDepositAccountHelper.calculatePrematureAmountForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.recurringDepositAccountHelper.prematureCloseForRecurringDeposit(
                recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(this.requestSpec,
                this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositAccountIsPrematureClosed(recurringDepositAccountStatusHashMap);

        recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");

        principal = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(recurringDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", principal, maturityAmount);

    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndMonthlyPosting_With_365_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DATE, -(currentDate - 1));
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_365, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, MONTHLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("accountBalance");
        Float recurringDepositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Float maturityAmount = (Float) recurringDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.recurringDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, recurringDepositAmount,
                depositPeriod, interestPerDay, DAILY_COMPOUNDING_INTERVAL, MONTHLY_INTERVAL);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Recurring Deposit Account", principal, maturityAmount);

    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndMonthlyPosting_With_360_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DATE, -(currentDate - 1));
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_360, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, MONTHLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("accountBalance");
        Float recurringDepositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Float maturityAmount = (Float) recurringDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.recurringDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, recurringDepositAmount,
                depositPeriod, interestPerDay, DAILY_COMPOUNDING_INTERVAL, MONTHLY_INTERVAL);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Recurring Deposit Account", principal, maturityAmount);

    }

    @Test
    public void testRecurringDepositWithBi_AnnualCompoundingAndPosting_365_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_365, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, BI_ANNUALLY, BI_ANNUALLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("accountBalance");
        Float recurringDepositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.recurringDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, recurringDepositAmount,
                depositPeriod, interestPerDay, BIANNULLY_INTERVAL, BIANNULLY_INTERVAL);

        recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(recurringDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testRecurringDepositWithBi_AnnualCompoundingAndPosting_360_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_360, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, BI_ANNUALLY, BI_ANNUALLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("accountBalance");
        Float recurringDepositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.recurringDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, recurringDepositAmount,
                depositPeriod, interestPerDay, BIANNULLY_INTERVAL, BIANNULLY_INTERVAL);

        recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(recurringDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndAnnuallyPosting_With_365_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();

        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_365, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, ANNUALLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);

        Float principal = (Float) recurringDepositSummary.get("accountBalance");
        Float recurringDepositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.recurringDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, recurringDepositAmount,
                depositPeriod, interestPerDay, DAILY_COMPOUNDING_INTERVAL, ANNUL_INTERVAL);

        recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(recurringDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndAnnuallyPosting_With_360_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();

        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_360, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, ANNUALLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);

        Float principal = (Float) recurringDepositSummary.get("accountBalance");
        Float recurringDepositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.recurringDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, recurringDepositAmount,
                depositPeriod, interestPerDay, DAILY_COMPOUNDING_INTERVAL, ANNUL_INTERVAL);

        recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(recurringDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testRecurringDepositQuarterlyCompoundingAndQuarterlyPosting_365_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_365, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, QUARTERLY, QUARTERLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("accountBalance");
        Float recurringDepositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.recurringDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, recurringDepositAmount,
                depositPeriod, interestPerDay, QUARTERLY_INTERVAL, QUARTERLY_INTERVAL);

        recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(recurringDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testRecurringDepositQuarterlyCompoundingAndQuarterlyPosting_360_Days() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateInterestCalculationConfigForRecurringDeposit(
                clientId.toString(), recurringDepositProductId.toString(), recurringDepositAccountId.toString(), SUBMITTED_ON_DATE,
                VALID_FROM, VALID_TO, DAYS_360, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, QUARTERLY, QUARTERLY,
                EXPECTED_FIRST_DEPOSIT_ON_DATE);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        HashMap recurringDepositSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float principal = (Float) recurringDepositSummary.get("accountBalance");
        Float recurringDepositAmount = (Float) recurringDepositAccountData.get("recurringDepositAmount");
        Integer depositPeriod = (Integer) recurringDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) recurringDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.recurringDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, recurringDepositProductId);

        Float interestRate = this.recurringDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.recurringDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, recurringDepositAmount,
                depositPeriod, interestPerDay, QUARTERLY_INTERVAL, QUARTERLY_INTERVAL);

        recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(recurringDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    private Integer createRecurringDepositProduct(final String validFrom, final String validTo, Account... accounts) {
        System.out.println("------------------------------CREATING NEW RECURRING DEPOSIT PRODUCT ---------------------------------------");
        RecurringDepositProductHelper recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        final String recurringDepositProductJSON = recurringDepositProductHelper //
                .build(validFrom, validTo);
        return RecurringDepositProductHelper.createRecurringDepositProduct(recurringDepositProductJSON, requestSpec, responseSpec);
    }

    private Integer applyForRecurringDepositApplication(final String clientID, final String productID, final String validFrom,
            final String validTo, final String submittedOnDate, final String penalInterestType, final String expectedFirstDepositOnDate) {
        System.out.println("--------------------------------APPLYING FOR RECURRING DEPOSIT ACCOUNT --------------------------------");
        final String recurringDepositApplicationJSON = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec)
                .withSubmittedOnDate(submittedOnDate).withExpectedFirstDepositOnDate(expectedFirstDepositOnDate)
                .build(clientID, productID, validFrom, validTo, penalInterestType);
        return this.recurringDepositAccountHelper.applyRecurringDepositApplication(recurringDepositApplicationJSON, this.requestSpec,
                this.responseSpec);
    }

    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final String savingsProductJSON = savingsProductHelper //
                .withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsMonthly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }
}