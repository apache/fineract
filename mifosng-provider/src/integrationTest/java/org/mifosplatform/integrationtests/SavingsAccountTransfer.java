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
import org.mifosplatform.integrationtests.common.savings.SavingsAccountHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsAccountTransferTestBuilder;
import org.mifosplatform.integrationtests.common.savings.SavingsAccountTransferTestBuilder;
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
@SuppressWarnings({ "rawtypes", "unused", "static-access" })
public class SavingsAccountTransfer {

    public static final String MINIMUM_OPENING_BALANCE = "10000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String TO_LOAN_ACCOUNT_TYPE = "1";
    public static final String TO_SAVINGS_ACCOUNT_TYPE = "2";

    private final String LOAN_APPROVAL_DATE = "01 March 2013";
    private final String LOAN_DISBURSAL_DATE = "01 March 2013";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsAccountHelper savingsAccountHelper;
    private SavingsAccountTransferTestBuilder savingsAccountTransferTestBuilder;
    private LoanTransactionHelper loanTransactionHelper;

    Float TRANSFER_AMOUNT = new Float(savingsAccountHelper.ACCOUNT_TRANSFER_AMOUNT);

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testFundTransferToSavingsAccount() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        // Creating Loan Account to which Fund to be Transferred.
        final Integer toClientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(toClientId);

        final Integer toLoanProductID = createLoanProduct();
        Assert.assertNotNull(toLoanProductID);

        final Integer toLoanID = applyForLoanApplication(toClientId, toLoanProductID);
        Assert.assertNotNull(toLoanID);

        HashMap toLoanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, toLoanID);
        LoanStatusChecker.verifyLoanIsPending(toLoanStatusHashMap);

        toLoanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_APPROVAL_DATE, toLoanID);
        LoanStatusChecker.verifyLoanIsApproved(toLoanStatusHashMap);

        toLoanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSAL_DATE, toLoanID);
        LoanStatusChecker.verifyLoanIsActive(toLoanStatusHashMap);

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

        this.savingsAccountHelper.toSavingsAccountTransfer(fromClientID, fromSavingsID, toClientID, toSavingsID, TO_SAVINGS_ACCOUNT_TYPE);

        fromSavingsBalance -= new Float(savingsAccountHelper.ACCOUNT_TRANSFER_AMOUNT);
        toSavingsBalance += new Float(savingsAccountHelper.ACCOUNT_TRANSFER_AMOUNT);

        HashMap fromSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);
        assertEquals("Verifying From Savings Account Balance after Account Transfer", fromSavingsBalance,
                fromSavingsSummaryAfter.get("accountBalance"));

        HashMap toSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(toSavingsID);
        assertEquals("Verifying To Savings Account Balance after Account Transfer", toSavingsBalance,
                toSavingsSummaryAfter.get("accountBalance"));

        this.savingsAccountHelper.toSavingsAccountTransfer(fromClientID, fromSavingsID, toClientId, toLoanID, TO_LOAN_ACCOUNT_TYPE);

        fromSavingsBalance -= TRANSFER_AMOUNT;

        fromSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);
        assertEquals("Verifying From Savings Account Balance after Account Transfer", fromSavingsBalance,
                fromSavingsSummaryAfter.get("accountBalance"));

        HashMap toLoanSummaryAfter = this.loanTransactionHelper.getLoanSummary(requestSpec, responseSpec, toLoanID);
        assertEquals("Verifying To Loan Repayment Amount after Account Transfer", TRANSFER_AMOUNT, toLoanSummaryAfter.get("totalRepayment"));
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
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .build();
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("15,000.00") //
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
                .build(clientID.toString(), loanProductID.toString());
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}