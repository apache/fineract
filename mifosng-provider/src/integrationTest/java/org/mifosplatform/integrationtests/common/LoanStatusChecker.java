package org.mifosplatform.integrationtests.common;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import junit.framework.Assert;

import java.util.HashMap;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoanStatusChecker {

    private static boolean getStatus(HashMap loanStatusMap, String nameOfLoanStatusString){
       return (Boolean)loanStatusMap.get(nameOfLoanStatusString);
    }

    public static void verifyLoanIsApproved(HashMap loanStatusHashMap) {
        assertFalse(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    public static void verifyLoanIsWaitingForDisbursal(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "waitingForDisbursal"));
    }

    public static void verifyLoanIsPending(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    public static void verifyLoanIsActive(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "active"));
    }

    public static void verifyLoanNeedsApproval(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    public static void verifyLoanAccountIsClosed(HashMap loanStatusHashMap){
        Assert.assertTrue(getStatus(loanStatusHashMap, "closed"));
    }

    public static HashMap getStatusOfLoan(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer loanID){
         String url = "/mifosng-provider/api/v1/loans/" + loanID + "?tenantIdentifier=default";
        return Utils.performServerGet(requestSpec, responseSpec, url, "status");
    }
}
