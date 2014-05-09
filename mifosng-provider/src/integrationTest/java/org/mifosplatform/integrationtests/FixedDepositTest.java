package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
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
import org.mifosplatform.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.savings.SavingsAccountHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsProductHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsStatusChecker;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "unchecked", "rawtypes", "static-access" })
public class FixedDepositTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private FixedDepositProductHelper fixedDepositProductHelper;
    private FixedDepositAccountHelper fixedDepositAccountHelper;
    private AccountHelper accountHelper;
    private SavingsAccountHelper savingsAccountHelper;

    public static final String WHOLE_TERM = "1";
    public static final String TILL_PREMATURE_WITHDRAWAL = "2";
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

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("X-Mifos-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeWithdrawal() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        fixedDepositAccountId = this.fixedDepositAccountHelper.calculateInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(fixedDepositAccountId);

        Integer transactionIdForPostInterest = this.fixedDepositAccountHelper.postInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

    }

    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeTransferToSavings() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        fixedDepositAccountId = this.fixedDepositAccountHelper.calculateInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(fixedDepositAccountId);

        Integer transactionIdForPostInterest = this.fixedDepositAccountHelper.postInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        HashMap savingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);

        Float balanceBefore = (Float) savingsSummaryBefore.get("accountBalance");

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_TRANSFER_TO_SAVINGS, savingsId, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float prematurityAmount = (Float) fixedDepositData.get("maturityAmount");

        HashMap savingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balanceAfter = (Float) savingsSummaryAfter.get("accountBalance");
        Float expectedSavingsBalance = balanceBefore + prematurityAmount;

        Assert.assertEquals("Verifying Savings Account Balance after Premature Closure", expectedSavingsBalance, balanceAfter);

    }

    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeReinvest() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        FixedDepositAccountHelper fixedDepositAccountHelperValidationError = new FixedDepositAccountHelper(this.requestSpec,
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

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        fixedDepositAccountId = this.fixedDepositAccountHelper.calculateInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(fixedDepositAccountId);

        Integer transactionIdForPostInterest = this.fixedDepositAccountHelper.postInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        ArrayList<HashMap> errorResponse = (ArrayList<HashMap>) fixedDepositAccountHelperValidationError.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_REINVEST, null, CommonConstants.RESPONSE_ERROR);

        assertEquals("validation.msg.fixeddepositaccount.onAccountClosureId.reinvest.not.allowed",
                errorResponse.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    @Test
    public void testFixedDepositAccountUpdation() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        ArrayList<HashMap> allFixedDepositProductsData = this.fixedDepositProductHelper.retrieveAllFixedDepositProducts(this.requestSpec,
                this.responseSpec);
        HashMap fixedDepositProductData = this.fixedDepositProductHelper.retrieveFixedDepositProductById(this.requestSpec,
                this.responseSpec, fixedDepositProductId.toString());

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        todaysDate.add(Calendar.DATE, -1);
        SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateFixedDepositAccount(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), VALID_FROM, VALID_TO, WHOLE_TERM, SUBMITTED_ON_DATE);
        Assert.assertTrue(modificationsHashMap.containsKey("submittedOnDate"));

    }

    @Test
    public void testFixedDepositAccountUndoApproval() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.undoApproval(fixedDepositAccountId);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);
    }

    @Test
    public void testFixedDepositAccountRejectedAndClosed() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.rejectApplication(fixedDepositAccountId, REJECTED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsRejected(fixedDepositAccountStatusHashMap);
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsClosed(fixedDepositAccountStatusHashMap);
    }

    @Test
    public void testFixedDepositAccountWithdrawnByClientAndClosed() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.withdrawApplication(fixedDepositAccountId, WITHDRAWN_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsWithdrawn(fixedDepositAccountStatusHashMap);
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsClosed(fixedDepositAccountStatusHashMap);
    }

    @Test
    public void testFixedDepositAccountIsDeleted() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountId = (Integer) this.fixedDepositAccountHelper.deleteFixedDepositApplication(fixedDepositAccountId, "resourceId");
        Assert.assertNotNull(fixedDepositAccountId);
    }

    @Test
    public void testMaturityAmountForMonthlyCompoundingAndMonthlyPosting_With_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Float maturityAmount = (Float) fixedDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);
        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, MONTHLY_INTERVAL, MONTHLY_INTERVAL);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Fixed Deposit Account", principal, maturityAmount);
    }

    @Test
    public void testMaturityAmountForMonthlyCompoundingAndMonthlyPosting_With_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Float maturityAmount = (Float) fixedDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);
        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, MONTHLY_INTERVAL, MONTHLY_INTERVAL);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Fixed Deposit Account", principal, maturityAmount);
    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestForWholeTerm_With_365() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) fixedDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth;
        todaysDate.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(todaysDate.getTime()));

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");

        principal = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", principal, maturityAmount);

    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestForWholeTerm_With_360() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) fixedDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth;
        todaysDate.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(todaysDate.getTime()));

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");

        principal = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", principal, maturityAmount);

    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestTillPrematureWithdrawal_With_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, TILL_PREMATURE_WITHDRAWAL);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) fixedDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Calendar activationDate = Calendar.getInstance();
        activationDate.add(Calendar.MONTH, -1);
        DateTime startDate = new DateTime(activationDate.getTime());

        Calendar prematureClosureDate = Calendar.getInstance();
        DateTime endDate = new DateTime(prematureClosureDate.getTime());

        Integer depositedPeriod = Months.monthsBetween(startDate, endDate).getMonths();

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositedPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth;
        todaysDate.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(todaysDate.getTime()));

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestTillPrematureWithdrawal_With_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, TILL_PREMATURE_WITHDRAWAL);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                TILL_PREMATURE_WITHDRAWAL, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) fixedDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Calendar activationDate = Calendar.getInstance();
        activationDate.add(Calendar.MONTH, -1);
        DateTime startDate = new DateTime(activationDate.getTime());

        Calendar prematureClosureDate = Calendar.getInstance();
        DateTime endDate = new DateTime(prematureClosureDate.getTime());

        Integer depositedPeriod = Months.monthsBetween(startDate, endDate).getMonths();

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositedPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth;
        todaysDate.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(todaysDate.getTime()));

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndMonthlyPosting_With_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_365,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, MONTHLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Float maturityAmount = (Float) fixedDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, DAILY_COMPOUNDING_INTERVAL, MONTHLY_INTERVAL);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Fixed Deposit Account", principal, maturityAmount);

    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndMonthlyPosting_With_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, MONTHLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Float maturityAmount = (Float) fixedDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, DAILY_COMPOUNDING_INTERVAL, MONTHLY_INTERVAL);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Fixed Deposit Account", principal, maturityAmount);

    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndAnnuallyPosting_With_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_365,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, ANNUALLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, DAILY_COMPOUNDING_INTERVAL, ANNUL_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testMaturityAmountDailyCompoundingAndAnnuallyPostingWith_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, ANNUALLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, DAILY_COMPOUNDING_INTERVAL, ANNUL_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testFixedDepositWithBi_AnnualCompoundingAndPosting_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_365,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, BI_ANNUALLY, BI_ANNUALLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, BIANNULLY_INTERVAL, BIANNULLY_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testFixedDepositWithBi_AnnualCompoundingAndPosting_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, BI_ANNUALLY, BI_ANNUALLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, BIANNULLY_INTERVAL, BIANNULLY_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testFixedDepositWithQuarterlyCompoundingAndQuarterlyPosting_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_365,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, QUARTERLY, QUARTERLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, QUARTERLY_INTERVAL, QUARTERLY_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);
    }

    @Test
    public void testFixedDepositWithQuarterlyCompoundingAndQuarterlyPosting_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, QUARTERLY, QUARTERLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, QUARTERLY_INTERVAL, QUARTERLY_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);
    }

    private Integer createFixedDepositProduct(final String validFrom, final String validTo) {
        System.out.println("------------------------------CREATING NEW FIXED DEPOSIT PRODUCT ---------------------------------------");
        FixedDepositProductHelper fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        final String fixedDepositProductJSON = fixedDepositProductHelper //
                .build(validFrom, validTo);
        return FixedDepositProductHelper.createFixedDepositProduct(fixedDepositProductJSON, requestSpec, responseSpec);
    }

    private Integer applyForFixedDepositApplication(final String clientID, final String productID, final String validFrom,
            final String validTo, final String submittedOnDate, final String penalInterestType) {
        System.out.println("--------------------------------APPLYING FOR FIXED DEPOSIT ACCOUNT --------------------------------");
        final String fixedDepositApplicationJSON = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec) //
                .withSubmittedOnDate(submittedOnDate).build(clientID, productID, validFrom, validTo, penalInterestType);
        return this.fixedDepositAccountHelper
                .applyFixedDepositApplication(fixedDepositApplicationJSON, this.requestSpec, this.responseSpec);
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