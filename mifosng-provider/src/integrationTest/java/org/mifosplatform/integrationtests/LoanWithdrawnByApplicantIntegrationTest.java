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

@SuppressWarnings("rawtypes")
public class LoanWithdrawnByApplicantIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
    }

    @Test
    public void loanWithdrawnByApplicant() {
        Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2012");
        Integer loanProductID = loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build());
        Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanTransactionHelper.withdrawLoanApplicationByClient("03 April 2012", loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsNotActive(loanStatusHashMap);

    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        String loanApplication = new LoanApplicationTestBuilder().withPrincipal("5000").withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate("04 April 2012")
                .withSubmittedOnDate("02 April 2012").build(clientID.toString(), loanProductID.toString());
        return loanTransactionHelper.getLoanId(loanApplication);
    }
}
