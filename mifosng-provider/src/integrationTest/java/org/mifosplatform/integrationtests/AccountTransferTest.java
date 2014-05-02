package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.charges.ChargesHelper;
import org.mifosplatform.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanStatusChecker;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;
import org.mifosplatform.integrationtests.common.savings.AccountTransferHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsAccountHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsProductHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsStatusChecker;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * JUnit Test Cases for Account Transfer for.
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class AccountTransferTest {

    public static final String MINIMUM_OPENING_BALANCE = "30000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String ACCOUNT_TRANSFER_AMOUNT = "15000.0";
    public static final String ACCOUNT_TRANSFER_AMOUNT_ADJUST = "3000.0";
    public static final String FROM_LOAN_ACCOUNT_TYPE = "1";
    public static final String FROM_SAVINGS_ACCOUNT_TYPE = "2";
    public static final String TO_LOAN_ACCOUNT_TYPE = "1";
    public static final String TO_SAVINGS_ACCOUNT_TYPE = "2";

    public static final String LOAN_APPROVAL_DATE = "01 March 2013";
    public static final String LOAN_APPROVAL_DATE_PLUS_ONE = "02 March 2013";
    public static final String LOAN_DISBURSAL_DATE = "01 March 2013";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsAccountHelper savingsAccountHelper;
    private AccountTransferHelper accountTransferHelper;
    private LoanTransactionHelper loanTransactionHelper;

    Float TRANSFER_AMOUNT = new Float(ACCOUNT_TRANSFER_AMOUNT);
    Float TRANSFER_AMOUNT_ADJUST = new Float(ACCOUNT_TRANSFER_AMOUNT_ADJUST);

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testFromSavingsToSavingsAccountTransfer() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.accountTransferHelper = new AccountTransferHelper(this.requestSpec, this.responseSpec);

        // Creating Savings Account to which fund to be Transferred
        final Integer toClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(toClientID);

        final Integer toSavingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(toSavingsProductID);

        final Integer toSavingsID = this.savingsAccountHelper.applyForSavingsApplication(toClientID, toSavingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(toSavingsProductID);

        HashMap toSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, toSavingsID);
        SavingsStatusChecker.verifySavingsIsPending(toSavingsStatusHashMap);

        toSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(toSavingsID);
        SavingsStatusChecker.verifySavingsIsApproved(toSavingsStatusHashMap);

        toSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(toSavingsID);
        SavingsStatusChecker.verifySavingsIsActive(toSavingsStatusHashMap);

        final HashMap toSavingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(toSavingsID);

        // Creating Savings Account from which the Fund has to be Transferred
        final Integer fromClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(fromClientID);

        final Integer fromSavingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(fromSavingsProductID);

        final Integer fromSavingsID = this.savingsAccountHelper.applyForSavingsApplication(fromClientID, fromSavingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(fromSavingsID);

        HashMap fromSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, fromSavingsID);
        SavingsStatusChecker.verifySavingsIsPending(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsApproved(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsActive(fromSavingsStatusHashMap);

        final HashMap fromSavingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);

        Float fromSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);
        Float toSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);

        this.accountTransferHelper.accountTransfer(fromClientID, fromSavingsID, toClientID, toSavingsID, FROM_SAVINGS_ACCOUNT_TYPE,
                TO_SAVINGS_ACCOUNT_TYPE, ACCOUNT_TRANSFER_AMOUNT);

        fromSavingsBalance -= new Float(ACCOUNT_TRANSFER_AMOUNT);
        toSavingsBalance += new Float(ACCOUNT_TRANSFER_AMOUNT);

        HashMap fromSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);
        assertEquals("Verifying From Savings Account Balance after Account Transfer", fromSavingsBalance,
                fromSavingsSummaryAfter.get("accountBalance"));

        HashMap toSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(toSavingsID);
        assertEquals("Verifying To Savings Account Balance after Account Transfer", toSavingsBalance,
                toSavingsSummaryAfter.get("accountBalance"));
    }

    @Test
    public void testFromSavingsToLoanAccountTransfer() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountTransferHelper = new AccountTransferHelper(this.requestSpec, this.responseSpec);

        // Creating Loan Account to which Fund to be Transferred.
        final Integer toClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(toClientID);

        final Integer toLoanProductID = createLoanProduct();
        Assert.assertNotNull(toLoanProductID);

        final Integer toLoanID = applyForLoanApplication(toClientID, toLoanProductID);
        Assert.assertNotNull(toLoanID);

        HashMap toLoanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, toLoanID);
        LoanStatusChecker.verifyLoanIsPending(toLoanStatusHashMap);

        toLoanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_APPROVAL_DATE, toLoanID);
        LoanStatusChecker.verifyLoanIsApproved(toLoanStatusHashMap);

        toLoanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSAL_DATE, toLoanID);
        LoanStatusChecker.verifyLoanIsActive(toLoanStatusHashMap);

        // Creating Savings Account from which Fund to be Transferred
        final Integer fromClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(fromClientID);

        final Integer fromSavingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(fromSavingsProductID);

        final Integer fromSavingsID = this.savingsAccountHelper.applyForSavingsApplication(fromClientID, fromSavingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(fromSavingsID);

        HashMap fromSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, fromSavingsID);
        SavingsStatusChecker.verifySavingsIsPending(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsApproved(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsActive(fromSavingsStatusHashMap);

        final HashMap fromSavingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);

        Float fromSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);

        this.accountTransferHelper.accountTransfer(fromClientID, fromSavingsID, toClientID, toLoanID, FROM_SAVINGS_ACCOUNT_TYPE,
                TO_LOAN_ACCOUNT_TYPE, ACCOUNT_TRANSFER_AMOUNT_ADJUST);

        fromSavingsBalance -= TRANSFER_AMOUNT_ADJUST;

        HashMap fromSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);
        assertEquals("Verifying From Savings Account Balance after Account Transfer", fromSavingsBalance,
                fromSavingsSummaryAfter.get("accountBalance"));

        HashMap toLoanSummaryAfter = this.loanTransactionHelper.getLoanSummary(requestSpec, responseSpec, toLoanID);
        assertEquals("Verifying To Loan Repayment Amount after Account Transfer", TRANSFER_AMOUNT_ADJUST, toLoanSummaryAfter.get("totalRepayment"));
    }

    @Test
    public void testFromLoanToSavingsAccountTransfer() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountTransferHelper = new AccountTransferHelper(this.requestSpec, this.responseSpec);

        // Creating Savings Account to which Fund to be Transferred
        final Integer toClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(toClientID);

        final Integer toSavingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(toSavingsProductID);

        final Integer toSavingsID = this.savingsAccountHelper.applyForSavingsApplication(toClientID, toSavingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(toSavingsID);

        HashMap toSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, toSavingsID);
        SavingsStatusChecker.verifySavingsIsPending(toSavingsStatusHashMap);

        toSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(toSavingsID);
        SavingsStatusChecker.verifySavingsIsApproved(toSavingsStatusHashMap);

        toSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(toSavingsID);
        SavingsStatusChecker.verifySavingsIsActive(toSavingsStatusHashMap);

        // Creating Loan Account to or from which Fund to be Transferred.
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer loanProductID = createLoanProduct();
        Assert.assertNotNull(loanProductID);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_APPROVAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // Creating Savings Account from which Fund to be Transferred
        final Integer fromClientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(fromClientId);

        final Integer fromSavingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(fromSavingsProductID);

        final Integer fromSavingsID = this.savingsAccountHelper.applyForSavingsApplication(fromClientId, fromSavingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(fromSavingsID);

        HashMap fromSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, fromSavingsID);
        SavingsStatusChecker.verifySavingsIsPending(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsApproved(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsActive(fromSavingsStatusHashMap);

        final HashMap toSavingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(toSavingsID);

        Float fromSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);

        this.accountTransferHelper.accountTransfer(fromClientId, fromSavingsID, clientID, loanID, FROM_SAVINGS_ACCOUNT_TYPE,
                TO_LOAN_ACCOUNT_TYPE, ACCOUNT_TRANSFER_AMOUNT);

        fromSavingsBalance -= TRANSFER_AMOUNT;

        HashMap fromSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);

        // Verifying fromSavings Account Balance after Account Transfer
        assertEquals("Verifying From Savings Account Balance after Account Transfer", fromSavingsBalance,
                fromSavingsSummaryAfter.get("accountBalance"));

        Float toSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);

        this.accountTransferHelper.accountTransfer(clientID, loanID, toClientID, toSavingsID, FROM_LOAN_ACCOUNT_TYPE,
                TO_SAVINGS_ACCOUNT_TYPE, ACCOUNT_TRANSFER_AMOUNT_ADJUST);

        toSavingsBalance += TRANSFER_AMOUNT_ADJUST;

        HashMap toSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(toSavingsID);

        // Verifying toSavings Account Balance after Account Transfer
        assertEquals("Verifying From Savings Account Balance after Account Transfer", toSavingsBalance,
                toSavingsSummaryAfter.get("accountBalance"));
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

    private Integer createLoanProduct() {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("8,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("8,000.00") //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("10 January 2013") //
                .withSubmittedOnDate("10 January 2013") //
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}