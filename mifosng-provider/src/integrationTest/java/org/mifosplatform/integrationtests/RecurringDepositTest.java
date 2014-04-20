package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.accounting.Account;
import org.mifosplatform.integrationtests.common.accounting.AccountHelper;
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

    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String CLOSURE_TYPE_WITHDRAW_DEPOSIT = "100";
    public static final String CLOSURE_TYPE_TRANSFER_TO_SAVINGS = "200";
    public static final String CLOSURE_TYPE_REINVEST = "300";

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testRecurringDepositAccountWithPrematureClosureTypeWithdrawal() {
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE);
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
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE);
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
        decimalFormat.applyPattern(".00");
        // decimalFormat.format(expectedSavingsBalance);
        // decimalFormat.format(balanceAfter);
        
        Assert.assertEquals("Verifying Savings Account Balance after Premature Closure", decimalFormat.format(expectedSavingsBalance), decimalFormat.format(balanceAfter));

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
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE);
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        ArrayList<HashMap> allRecurringDepositProductsData = this.recurringDepositProductHelper.retrieveAllRecurringDepositProducts(
                this.requestSpec, this.responseSpec);
        HashMap recurringDepositProductData = this.recurringDepositProductHelper.retrieveRecurringDepositProductById(this.requestSpec,
                this.responseSpec, recurringDepositProductId.toString());

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap modificationsHashMap = this.recurringDepositAccountHelper.updateRecurringDepositAccount(clientId.toString(),
                recurringDepositProductId.toString(), recurringDepositAccountId.toString(), VALID_FROM, VALID_TO);
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        ArrayList<HashMap> allRecurringDepositProductsData = this.recurringDepositProductHelper.retrieveAllRecurringDepositProducts(
                this.requestSpec, this.responseSpec);
        HashMap recurringDepositProductData = this.recurringDepositProductHelper.retrieveRecurringDepositProductById(this.requestSpec,
                this.responseSpec, recurringDepositProductId.toString());

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE);
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        ArrayList<HashMap> allRecurringDepositProductsData = this.recurringDepositProductHelper.retrieveAllRecurringDepositProducts(
                this.requestSpec, this.responseSpec);
        HashMap recurringDepositProductData = this.recurringDepositProductHelper.retrieveRecurringDepositProductById(this.requestSpec,
                this.responseSpec, recurringDepositProductId.toString());

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE);
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        ArrayList<HashMap> allRecurringDepositProductsData = this.recurringDepositProductHelper.retrieveAllRecurringDepositProducts(
                this.requestSpec, this.responseSpec);
        HashMap recurringDepositProductData = this.recurringDepositProductHelper.retrieveRecurringDepositProductById(this.requestSpec,
                this.responseSpec, recurringDepositProductId.toString());

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE);
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        ArrayList<HashMap> allRicurringDepositProductsData = this.recurringDepositProductHelper.retrieveAllRecurringDepositProducts(
                this.requestSpec, this.responseSpec);
        HashMap recurringDepositProductData = this.recurringDepositProductHelper.retrieveRecurringDepositProductById(this.requestSpec,
                this.responseSpec, recurringDepositProductId.toString());

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountId = (Integer) this.recurringDepositAccountHelper.deleteRecurringDepositApplication(
                recurringDepositAccountId, "resourceId");
        Assert.assertNotNull(recurringDepositAccountId);
    }

    @Test
    public void testDepositForRecurringDepositAccount() {
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String DEPOSIT_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE);
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
                DEPOSIT_DATE);
        Assert.assertNotNull(transactionIdForDeposit);

        HashMap recurringDepositSummaryAfter = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float balanceAfter = (Float) recurringDepositSummaryAfter.get("accountBalance");

    }

    private Integer createRecurringDepositProduct(final String validFrom, final String validTo, Account... accounts) {
        System.out.println("------------------------------CREATING NEW RECURRING DEPOSIT PRODUCT ---------------------------------------");
        RecurringDepositProductHelper recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        final String recurringDepositProductJSON = recurringDepositProductHelper //
                // .withAccountingRuleAsCashBased(accounts)
                .build(validFrom, validTo);
        return RecurringDepositProductHelper.createRecurringDepositProduct(recurringDepositProductJSON, requestSpec, responseSpec);
    }

    private Integer applyForRecurringDepositApplication(final String clientID, final String productID, final String validFrom,
            final String validTo, final String submittedOnDate) {
        System.out.println("--------------------------------APPLYING FOR RECURRING DEPOSIT ACCOUNT --------------------------------");
        final String recurringDepositApplicationJSON = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec) //
                .withSubmittedOnDate(submittedOnDate).build(clientID, productID, validFrom, validTo);
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