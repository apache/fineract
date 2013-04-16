package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Client Loan Integration Test for checking Loan Application Repayment
 * Schedule.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClientLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void checkClientLoanCreateAndDisburseFlow() {
        loanTransactionHelper = new LoanTransactionHelper(requestSpec,responseSpec);

        Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientID);
        Integer loanProductID = createLoanProduct();
        Integer loanID = applyForLoanApplication(clientID, loanProductID);
        ArrayList<HashMap> loanSchedule = loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

    }

    private Integer createLoanProduct() {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        String loanProductJSON = new LoanProductTestBuilder().withPrincipal("12,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withinterestRatePerPeriod("1").build();
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("12,000.00").withLoanTermFrequency("4")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("4").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2")
                .withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("20 September 2011")
                .withSubmittedOnDate("20 September 2011").build(clientID.toString(), loanProductID.toString());
        return loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private void verifyLoanRepaymentSchedule(final ArrayList<HashMap> loanSchedule) {
        System.out.println("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        assertEquals("Checking for Due Date for 1st Month", new ArrayList<Integer>(Arrays.asList(2011, 10, 20)),
                loanSchedule.get(1).get("dueDate"));
        assertEquals("Checking for Principal Due for 1st Month", new Float("2911.49"), loanSchedule.get(1).get("principalOriginalDue"));
        assertEquals("Checking for Interest Due for 1st Month", new Float("240.00"), loanSchedule.get(1).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 2nd Month", new ArrayList<Integer>(Arrays.asList(2011, 11, 20)),
                loanSchedule.get(2).get("dueDate"));
        assertEquals("Checking for Principal Due for 2nd Month", new Float("2969.72"), loanSchedule.get(2).get("principalDue"));
        assertEquals("Checking for Interest Due for 2nd Month", new Float("181.77"), loanSchedule.get(2).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 3rd Month", new ArrayList<Integer>(Arrays.asList(2011, 12, 20)),
                loanSchedule.get(3).get("dueDate"));
        assertEquals("Checking for Principal Due for 3rd Month", new Float("3029.11"), loanSchedule.get(3).get("principalDue"));
        assertEquals("Checking for Interest Due for 3rd Month", new Float("122.38"), loanSchedule.get(3).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 4th Month", new ArrayList<Integer>(Arrays.asList(2012, 1, 20)),
                loanSchedule.get(4).get("dueDate"));
        assertEquals("Checking for Principal Due for 4th Month", new Float("3089.68"), loanSchedule.get(4).get("principalDue"));
        assertEquals("Checking for Interest Due for 4th Month", new Float("61.79"), loanSchedule.get(4).get("interestOriginalDue"));
    }
}