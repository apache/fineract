/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.fixeddeposit;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Assert;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class FixedDepositAccountStatusChecker {

    private static final String FIXED_DEPOSIT_ACCOUNT_URL = "/mifosng-provider/api/v1/fixeddepositaccounts";

    public static void verifyFixedDepositIsApproved(final HashMap fixedDepositStatusHashMap) {
        System.out.println("-------------------- VERIFYING FIXED DEPOSIT APPLICATION IS APPROVED --------------------");
        assertTrue("Error in Approving Fixed deposit application", getStatus(fixedDepositStatusHashMap, "approved"));
        System.out.println(fixedDepositStatusHashMap);
    }

    public static void verifyFixedDepositIsPending(final HashMap fixedDepositStatusHashMap) {
        System.out.println("-------------------- VERIFYING FIXED DEPOSIT APPLICATION IS PENDING --------------------");
        assertTrue("FIXED DEPOSIT ACCOUNT IS NOT IN PENDING STATE", getStatus(fixedDepositStatusHashMap, "submittedAndPendingApproval"));
        System.out.println(fixedDepositStatusHashMap);
    }

    public static void verifyFixedDepositIsActive(final HashMap fixedDepositStatusHashMap) {
        System.out.println("----------------- VERIFYING FIXED DEPOSIT APPLICATION IS ACTIVE -----------------");
        assertTrue("ERROR IN ACTIVATING THE FIXED DEPOSIT APPLICATION", getStatus(fixedDepositStatusHashMap, "active"));
        System.out.println(fixedDepositStatusHashMap);
    }

    public static void verifyFixedDepositIsRejected(final HashMap fixedDepositStatusHashMap) {
        System.out.println("-------------- VERIFYING FIXED DEPOSIT APPLICATION IS REJECTED ----------------");
        assertTrue("ERROR IN REJECTING THE FIXED DEPOSIT APPLICATION", getStatus(fixedDepositStatusHashMap, "rejected"));
        System.out.println(fixedDepositStatusHashMap);
    }

    public static void verifyFixedDepositIsWithdrawn(final HashMap fixedDepositStatusHashMap) {
        System.out.println("---------------- VERIFYING FIXED DEPOSIT APPLICATION IS WITHDRAWN ----------------");
        assertTrue("ERROR IN WITHDRAW  THE FIXED DEPOSIT APPLICATION", getStatus(fixedDepositStatusHashMap, "withdrawnByApplicant"));
        System.out.println(fixedDepositStatusHashMap);
    }

    public static void verifyFixedDepositAccountIsClosed(final HashMap fixedDepositStatusHashMap) {
        System.out.println("--------------------- VERIFYING FIXED DEPOSIT APPLICATION IS CLOSED ---------------------");
        assertTrue("ERROR IN CLOSING THE FIXED DEPOSIT APPLICATION", getStatus(fixedDepositStatusHashMap, "closed"));
        System.out.println(fixedDepositStatusHashMap);
    }

    public static void verifyFixedDepositAccountIsNotActive(final HashMap fixedDepositStatusHashMap) {
        System.out.println("------------------ VERIFYING FIXED DEPOSIT APPLICATION IS INACTIVE --------------------");
        Assert.assertFalse(getStatus(fixedDepositStatusHashMap, "active"));
        System.out.println(fixedDepositStatusHashMap);
    }

    public static HashMap getStatusOfFixedDepositAccount(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String fixedDepositAccountID) {
        final String GET_STATUS_OF_FIXED_DEPOSIT_ACCOUNT_URL = FIXED_DEPOSIT_ACCOUNT_URL + "/" + fixedDepositAccountID + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, GET_STATUS_OF_FIXED_DEPOSIT_ACCOUNT_URL, "status");
    }

    public static void verifyFixedDepositAccountIsPrematureClosed(HashMap fixedDepositStatusHashMap) {
        System.out.println("--------------------- VERIFYING FIXED DEPOSIT APPLICATION IS CLOSED ---------------------");
        assertTrue("ERROR IN PREMATURELY CLOSING THE FIXED DEPOSIT ACCOUNT", getStatus(fixedDepositStatusHashMap, "prematureClosed"));
        System.out.println(fixedDepositStatusHashMap);
    }

    private static boolean getStatus(final HashMap fixedDepositStatusMap, final String fixedDepositStatusString) {
        return (Boolean) fixedDepositStatusMap.get(fixedDepositStatusString);
    }
}