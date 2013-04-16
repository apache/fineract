package org.mifosplatform.integrationtests.common.loans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.mifosplatform.integrationtests.common.Utils;

@SuppressWarnings("rawtypes")
public class LoanStatusChecker {

    public static void verifyLoanIsApproved(final HashMap loanStatusHashMap) {
        assertFalse(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    public static void verifyLoanIsWaitingForDisbursal(final HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "waitingForDisbursal"));
    }

    public static void verifyLoanIsPending(final HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    public static void verifyLoanIsActive(final HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "active"));
    }

    public static void verifyLoanAccountIsClosed(final HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "closed"));
    }

    public  static void verifyLoanAccountIsNotActive(final HashMap loanStatusHashMap) {
        assertFalse(getStatus(loanStatusHashMap, "active"));
    }

    public static HashMap getStatusOfLoan(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer loanID) {
        String url = "/mifosng-provider/api/v1/loans/" + loanID + "?tenantIdentifier=default";
        return Utils.performServerGet(requestSpec, responseSpec, url, "status");
    }

    private static boolean getStatus(final HashMap loanStatusMap, final String nameOfLoanStatusString) {
        return (Boolean) loanStatusMap.get(nameOfLoanStatusString);
    }

}
