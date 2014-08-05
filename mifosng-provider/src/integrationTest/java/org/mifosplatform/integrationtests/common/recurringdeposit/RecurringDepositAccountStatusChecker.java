/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.recurringdeposit;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Assert;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class RecurringDepositAccountStatusChecker {

    private static final String RECURRING_DEPOSIT_ACCOUNT_URL = "/mifosng-provider/api/v1/recurringdepositaccounts";

    public static void verifyRecurringDepositIsApproved(final HashMap recurringDepositStatusHashMap) {
        System.out.println("-------------------- VERIFYING RECURRING DEPOSIT APPLICATION IS APPROVED --------------------");
        assertTrue("Error in Approving Recurring deposit application", getStatus(recurringDepositStatusHashMap, "approved"));
        System.out.println(recurringDepositStatusHashMap);
    }

    public static void verifyRecurringDepositIsPending(final HashMap recurringDepositStatusHashMap) {
        System.out.println("-------------------- VERIFYING RECURRING DEPOSIT APPLICATION IS PENDING --------------------");
        assertTrue("RECURRING DEPOSIT ACCOUNT IS NOT IN PENDING STATE",
                getStatus(recurringDepositStatusHashMap, "submittedAndPendingApproval"));
        System.out.println(recurringDepositStatusHashMap);
    }

    public static void verifyRecurringDepositIsActive(final HashMap recurringDepositStatusHashMap) {
        System.out.println("----------------- VERIFYING RECURRING DEPOSIT APPLICATION IS ACTIVE -----------------");
        assertTrue("ERROR IN ACTIVATING THE RECURRING DEPOSIT APPLICATION", getStatus(recurringDepositStatusHashMap, "active"));
        System.out.println(recurringDepositStatusHashMap);
    }

    public static void verifyRecurringDepositIsRejected(final HashMap recurringDepositStatusHashMap) {
        System.out.println("-------------- VERIFYING RECURRING DEPOSIT APPLICATION IS REJECTED ----------------");
        assertTrue("ERROR IN REJECTING THE RECURRING DEPOSIT APPLICATION", getStatus(recurringDepositStatusHashMap, "rejected"));
        System.out.println(recurringDepositStatusHashMap);
    }

    public static void verifyRecurringDepositIsWithdrawn(final HashMap recurringDepositStatusHashMap) {
        System.out.println("---------------- VERIFYING RECURRING DEPOSIT APPLICATION IS WITHDRAWN ----------------");
        assertTrue("ERROR IN WITHDRAW  THE RECURRING DEPOSIT APPLICATION", getStatus(recurringDepositStatusHashMap, "withdrawnByApplicant"));
        System.out.println(recurringDepositStatusHashMap);
    }

    public static void verifyRecurringDepositAccountIsClosed(final HashMap recurringDepositStatusHashMap) {
        System.out.println("--------------------- VERIFYING RECURRING DEPOSIT APPLICATION IS CLOSED ---------------------");
        assertTrue("ERROR IN CLOSING THE RECURRING DEPOSIT APPLICATION", getStatus(recurringDepositStatusHashMap, "closed"));
        System.out.println(recurringDepositStatusHashMap);
    }

    public static void verifyRecurringDepositAccountIsNotActive(final HashMap recurringDepositStatusHashMap) {
        System.out.println("------------------ VERIFYING RECURRING DEPOSIT APPLICATION IS INACTIVE --------------------");
        Assert.assertFalse(getStatus(recurringDepositStatusHashMap, "active"));
        System.out.println(recurringDepositStatusHashMap);
    }

    public static HashMap getStatusOfRecurringDepositAccount(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String recurringDepositAccountID) {
        final String GET_STATUS_OF_RECURRING_DEPOSIT_ACCOUNT_URL = RECURRING_DEPOSIT_ACCOUNT_URL + "/" + recurringDepositAccountID + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, GET_STATUS_OF_RECURRING_DEPOSIT_ACCOUNT_URL, "status");
    }

    public static void verifyRecurringDepositAccountIsPrematureClosed(HashMap recurringDepositStatusHashMap) {
        System.out.println("--------------------- VERIFYING RECURRING DEPOSIT APPLICATION IS CLOSED ---------------------");
        assertTrue("ERROR IN PREMATURELY CLOSING THE RECURRING DEPOSIT ACCOUNT",
                getStatus(recurringDepositStatusHashMap, "prematureClosed"));
        System.out.println(recurringDepositStatusHashMap);
    }

    private static boolean getStatus(final HashMap recurringDepositStatusMap, final String recurringDepositStatusString) {
        return (Boolean) recurringDepositStatusMap.get(recurringDepositStatusString);
    }
}