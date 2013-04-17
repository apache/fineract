package org.mifosplatform.integrationtests;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanStatusChecker;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
* Client Loan Integration Test for checking Loan Disbursement with Waive
* Interest and Write-Off.
*/
@SuppressWarnings({ "rawtypes" })
public class LoanWithWaiveInterestAndWriteOffIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    private final String LP_PRINCIPAL = "12,000.00", LP_REPAYMENTS = "2", LP_REPAYMENT_PERIOD = "6", LP_INTEREST_RATE = "1",
                         PRINCIPAL = "4,500.00", LOAN_TERM_FREQUENCY = "18", NUMBER_OF_REPAYMENTS = "9", REPAYMENT_PERIOD = "2",
                         DISBURSEMENT_DATE = "30 October 2010", LOAN_APPLICATION_SUBMISSION_DATE = "23 September 2010",
                         EXPECTED_DISBURSAL_DATE = "28 October 2010", RATE_OF_INTEREST_PER_PERIOD = "2",
                         DATE_OF_JOINING = "04 March 2009",INTEREST_VALUE_AMOUNT = "40.00";
    private LoanTransactionHelper loanTransactionHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec,responseSpec);
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
        loanStatusHashMap = loanTransactionHelper.approveLoan("28 September 2010", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // UNDO APPROVAL
        loanStatusHashMap = loanTransactionHelper.undoApproval(loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------RE-APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = loanTransactionHelper.approveLoan("1 October 2010", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE
        loanStatusHashMap = loanTransactionHelper.disburseLoan(DISBURSEMENT_DATE, loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // PERFORM REPAYMENTS AND CHECK LOAN STATUS
        loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, 4000.0F, loanID);
        loanTransactionHelper.makeRepayment("1 January 2011", 540.0f, loanID);

        //UNDO DISBURSE LOAN
        loanStatusHashMap=loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        //DIBURSE AGAIN
        loanStatusHashMap = loanTransactionHelper.disburseLoan(DISBURSEMENT_DATE, loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        //MAKE REPAYMENTS
        final float repayment_with_interest = 540.0f;
        final float repayment_without_interest = 500.0f;

        loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, 4000.0F, loanID);
        loanTransactionHelper.makeRepayment("1 January 2011",repayment_with_interest, loanID);
        loanTransactionHelper.makeRepayment("1 March 2011", repayment_with_interest, loanID);
        loanTransactionHelper.waiveInterest("1 May 2011", INTEREST_VALUE_AMOUNT, loanID);
        loanTransactionHelper.makeRepayment("1 May 2011", repayment_without_interest, loanID);
        loanTransactionHelper.makeRepayment("1 July 2011", repayment_with_interest, loanID);
        loanTransactionHelper.waiveInterest("1 September 2011", INTEREST_VALUE_AMOUNT, loanID);
        loanTransactionHelper.makeRepayment("1 September 2011", repayment_without_interest, loanID);
        loanTransactionHelper.makeRepayment("1 November 2011", repayment_with_interest, loanID);
        loanTransactionHelper.waiveInterest("1 January 2012", INTEREST_VALUE_AMOUNT, loanID);
        loanTransactionHelper.makeRepayment("1 January 2012", repayment_without_interest, loanID);
        loanTransactionHelper.verifyRepaymentScheduleEntryFor(7, 1000.0f, loanID);

        // WRITE OFF LOAN AND CHECK ACCOUNT IS CLOSED
        LoanStatusChecker.verifyLoanAccountIsClosed(loanTransactionHelper.writeOffLoan("1 March 2012", loanID));

    }

    private Integer createLoanProduct() {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        String loanProductJSON = new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().build();

        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(PRINCIPAL).withLoanTermFrequency(LOAN_TERM_FREQUENCY)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(NUMBER_OF_REPAYMENTS).withRepaymentEveryAfter(REPAYMENT_PERIOD)
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod(RATE_OF_INTEREST_PER_PERIOD).withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualInstallments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(EXPECTED_DISBURSAL_DATE).withSubmittedOnDate(LOAN_APPLICATION_SUBMISSION_DATE)
                .build(clientID.toString(), loanProductID.toString());
        return loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}
