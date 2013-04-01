package org.mifosplatform.integrationtests.testbuilder;

import junit.framework.Assert;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoanStatusChecker {

    private static boolean getStatus(HashMap loanStatusMap, String nameOfLoanStatusString){
       return (Boolean)loanStatusMap.get(nameOfLoanStatusString);
    }

    public static void verifyLoanIsApproved(HashMap loanStatusHashMap) {
        assertFalse(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    public static void verifyLoanStatusIsWaitingForDisbursal(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "waitingForDisbursal"));
    }

    public static void verifyLoanStatusIsPending(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    public static void verifyLoanStatusIsActive(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "active"));
    }

    private static void verifyLoanNeedsApproval(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    public static void verifyLoanAccountIsClosed(HashMap loanStatusHashMap){
        Assert.assertTrue(getStatus(loanStatusHashMap, "closed"));
    }
}
