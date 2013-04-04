package org.mifosplatform.integrationtests;

//import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.LoanStatusChecker;
import org.mifosplatform.integrationtests.common.LoanTransactionHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import static org.junit.Assert.assertEquals;

/**
* Client Loan Integration Test for checking Loan Disbursement with Waive
* Interest and Write-Off.
*/
@SuppressWarnings({ "rawtypes", "unchecked" })
public class LoanWithWaiveInterestAndWriteOffIntegrationTest {

    ResponseSpecification responseSpec;
    RequestSpecification requestSpec;

    final String LP_PRINCIPAL = "12,000.00", LP_REPAYMENTS = "2", LP_REPAYMENT_PERIOD = "6", LP_INTEREST_RATE = "1",

    PRINCIPAL = "4,500.00", LOAN_TERM_FREQUENCY = "18", NUMBER_OF_REPAYMENTS = "9", REPAYMENT_PERIOD = "2",
            DISBURSEMENT_DATE = "30 October 2010", LOAN_APPLICATION_SUBMISSION_DATE = "23 September 2010",
            EXPECTED_DISBURSAL_DATE = "28 October 2010", RATE_OF_INTEREST_PER_PERIOD = "2", DATE_OF_JOINING = "04 March 2009",
            INTEREST_VALUE_AMOUNT = "40.00";

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void checkClientLoanCreateAndDisburseFlow() {
        // CREATE CLIENT
        Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);
        ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientID);

        // CREATE LOAN PRODUCT
        Integer loanProductID = createLoanProduct();
        // APPLY FOR LOAN
        Integer loanID = applyForLoanApplication(clientID, loanProductID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);

        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LoanTransactionHelper.approveLoan(requestSpec, responseSpec, "28 September 2010", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // UNDO APPROVAL
        loanStatusHashMap = LoanTransactionHelper.undoApproval(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------RE-APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LoanTransactionHelper.approveLoan(requestSpec, responseSpec, "1 October 2010", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE
        loanStatusHashMap = LoanTransactionHelper.disburseLoan(requestSpec, responseSpec, DISBURSEMENT_DATE, loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // PERFORM REPAYMENTS AND CHECK LOAN STATUS
        verifyRepaymentScheduleEntryFor(1, 4000.0F, loanID);
        LoanTransactionHelper.makeRepayment(requestSpec, responseSpec, "1 January 2011", 540.0f, loanID);
        LoanTransactionHelper.makeRepayment(requestSpec, responseSpec, "1 March 2011", 540.0f, loanID);
        LoanTransactionHelper.waiveInterest(requestSpec, responseSpec, "1 May 2011", INTEREST_VALUE_AMOUNT, loanID);
        LoanTransactionHelper.makeRepayment(requestSpec, responseSpec, "1 May 2011", 500.0f, loanID);
        LoanTransactionHelper.makeRepayment(requestSpec, responseSpec, "1 July 2011", 540.0f, loanID);
        LoanTransactionHelper.waiveInterest(requestSpec, responseSpec, "1 September 2011", INTEREST_VALUE_AMOUNT, loanID);
        LoanTransactionHelper.makeRepayment(requestSpec, responseSpec, "1 September 2011", 500.0f, loanID);
        LoanTransactionHelper.makeRepayment(requestSpec, responseSpec, "1 November 2011", 540.0f, loanID);
        LoanTransactionHelper.waiveInterest(requestSpec, responseSpec, "1 January 2012", INTEREST_VALUE_AMOUNT, loanID);
        LoanTransactionHelper.makeRepayment(requestSpec, responseSpec, "1 January 2012", 500.0f, loanID);

        verifyRepaymentScheduleEntryFor(7, 1000.0f, loanID);

        // WRITE OFF LOAN AND CHECK ACCOUNT IS CLOSED
        LoanStatusChecker.verifyLoanAccountIsClosed(LoanTransactionHelper.writeOffLoan(requestSpec, responseSpec, "1 March 2012", loanID));

    }

    private void verifyRepaymentScheduleEntryFor(final int repaymentNumber, final float expectedPrincipalOutstanding, final Integer loanID) {
        System.out.println("---------------------------GETTING LOAN REPAYMENT SCHEDULE--------------------------------");
        ArrayList<HashMap> repaymentPeriods = LoanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals("Mismatch in Principal Loan Balance Outstanding ", expectedPrincipalOutstanding, repaymentPeriods.get(repaymentNumber).get("principalLoanBalanceOutstanding"));
    }

    private Integer createLoanProduct() {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        String loanProductJSON = new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrinciplePayment().withInterestTypeAsFlat().build();
        return LoanTransactionHelper.getLoanProductId(requestSpec, responseSpec, loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(PRINCIPAL).withLoanTermFrequency(LOAN_TERM_FREQUENCY)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(NUMBER_OF_REPAYMENTS).withRepaymentEveryAfter(REPAYMENT_PERIOD)
                .withRepaymentFrequencyTypeAsMonths().withInterestRateFrequencyTypeAsMonths()
                .withInterestRatePerPeriod(RATE_OF_INTEREST_PER_PERIOD).withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualInstallments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(EXPECTED_DISBURSAL_DATE).withSubmittedOnDate(LOAN_APPLICATION_SUBMISSION_DATE)
                .build(clientID.toString(), loanProductID.toString());
        return LoanTransactionHelper.getLoanId(requestSpec, responseSpec, loanApplicationJSON);
    }
}
