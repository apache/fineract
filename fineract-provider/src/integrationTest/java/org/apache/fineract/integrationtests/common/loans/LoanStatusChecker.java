/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.loans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

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

    public static void verifyLoanAccountIsNotActive(final HashMap loanStatusHashMap) {
        assertFalse(getStatus(loanStatusHashMap, "active"));
    }

    public static void verifyLoanAccountIsOverPaid(final HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "overpaid"));
    }

    public static HashMap<String, Object> getStatusOfLoan(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer loanID) {
        final String url = "/mifosng-provider/api/v1/loans/" + loanID + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "status");
    }

    private static boolean getStatus(final HashMap loanStatusMap, final String nameOfLoanStatusString) {
        return (Boolean) loanStatusMap.get(nameOfLoanStatusString);
    }

}
